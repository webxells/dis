/**
 * Copyright (C) 2020-2026 webXells GmbH
 *
 * This work is licensed under the Creative Commons
 * Attribution-NonCommercial-NoDerivatives 4.0 International Public License.
 *
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    https://creativecommons.org/licenses/by-nc-nd/4.0/
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an AS IS BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 **/
package com.webxells.dis.base.trigger.group;

import com.webxells.dis.api.Logger;
import com.webxells.dis.api.RuntimeEnvironment;
import com.webxells.dis.api.config.TriggerConfig;
import com.webxells.dis.api.error.InvalidApi;
import com.webxells.dis.api.trigger.Trigger;
import com.webxells.dis.base.trigger.group.GroupingTriggerConfiguration.ErrorStrategy;
import com.webxells.dis.base.trigger.group.GroupingTriggerConfiguration.UpdateEvent;
import com.webxells.dis.boot.RawThreadEnvironment;
import com.webxells.dis.boot.ServiceManager;
import com.webxells.dis.logging.LoggerProxyFactory;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static com.webxells.dis.base.trigger.group.GroupingTriggerConfiguration.UpdateEvent.END;
import static com.webxells.dis.base.trigger.group.GroupingTriggerConfiguration.UpdateEvent.ERROR;
import static com.webxells.dis.base.trigger.group.GroupingTriggerConfiguration.UpdateEvent.START;

class GroupingTriggerDirector {
    private enum DirectorDirective {
        FAIL, STOP, GO, SHUT_DOWN
    }

    private static final Logger LOGGER = LoggerProxyFactory.logger(GroupingTriggerDirector.class);
    private static final int DEFAULT_TICK_SLEEP = 500;

    private final List<Child> orderedTriggers = Collections.synchronizedList(new ArrayList<>());
    private final String name;
    private final AtomicBoolean triggeredStarter = new AtomicBoolean();
    private final AtomicReference<DirectorDirective> directorDirective = new AtomicReference<>(DirectorDirective.GO);

    private TriggerConfig starterConfig;
    private Trigger<?> starter;
    private RuntimeEnvironment runtimeEnvironment = RawThreadEnvironment.instance();
    private RuntimeEnvironment.Run directorThread;
    private int maxParallel = 1;
    private Iterator<Child> childIterator;
    private long pauseBetweenRuns = DEFAULT_TICK_SLEEP;
    private UpdateSender updateSender;
    private List<UpdateEvent> eventsToUpdate = List.of(UpdateEvent.END);
    private ErrorStrategy errorStrategy = ErrorStrategy.STOP;

    public GroupingTriggerDirector(final String name) {
        this.name = name;
    }

    public void register(GroupingTrigger trigger, final Runnable action) {
        LOGGER.d("%s got a new child: %s", name, trigger);
        orderedTriggers.add(new Child(trigger, action));
        start();
    }

    public void validate() throws InvalidApi {
        if (null == starterConfig) {
            throw new InvalidApi("starter is required. In case of doubt, just use JustStart");
        }
        if (1 > maxParallel) {
            throw new InvalidApi("maxParallel should be a positive integer");
        }
    }

    public void abort() {
        directorDirective.set(DirectorDirective.STOP);
    }

    private synchronized void shutDown() {
        if (DirectorDirective.SHUT_DOWN == directorDirective.get()) {
            return;
        }
        LOGGER.d("Aborting %s...", name);
        if (starter.isRunning()) {
            starter.abort();
        }
        triggeredStarter.set(false);
        orderedTriggers.forEach(Child::abort);
        Optional.ofNullable(updateSender)
                .ifPresent(UpdateSender::close);
        if (DirectorDirective.FAIL == directorDirective.get()) {
            directorDirective.set(DirectorDirective.SHUT_DOWN);
            final Throwable error = orderedTriggers.stream()
                    .map(Child::getError)
                    .filter(Objects::nonNull)
                    .findAny()
                    .orElseGet(() -> new RuntimeException("Could not find child error"));
            throw new RuntimeException("Grouping trigger failed", error);
        }
        directorDirective.set(DirectorDirective.SHUT_DOWN);
    }

    public boolean isRunning() {
        return null != directorThread && directorThread.isAlive();
    }

    public void setStarterConfig(final TriggerConfig starterConfig) {
        if (null != this.starterConfig) {
            throw new IllegalArgumentException("starter is already set");
        }
        this.starterConfig = starterConfig;
    }

    public void setRuntimeEnvironment(final RuntimeEnvironment runtimeEnvironment) {
        this.runtimeEnvironment = runtimeEnvironment;
    }

    private synchronized void start() {
        if (null == directorThread) {
            listenToStarter();
            directorThread = runtimeEnvironment.newRun(this::tick, "group director");
            directorThread.start();
        }
    }

    private void listenToStarter() {
        starter = ServiceManager.loadByConfig(starterConfig);
        starter.awaitAction(() -> {
            triggeredStarter.set(true);
            starter.abort();
        });
    }

    private void tick() {
        while (DirectorDirective.GO == directorDirective.get()) {
            if (triggeredStarter.get()) {
                childIterator = orderedTriggers.iterator();
                triggeredStarter.set(false);
                triggerStatusUpdate(START);
            }
            assertMaxAreRunning();
            try {
                Thread.sleep(pauseBetweenRuns);
            } catch (final InterruptedException e) {
                throw new RuntimeException("Nightmare!", e);
            }
        }
        shutDown();
        LOGGER.d("GroupingDirector for %s successfully aborted", name);
    }

    private void assertMaxAreRunning() {
        if (null != childIterator) {
            final long size = orderedTriggers.stream()
                    .filter(Child::isAlive)
                    .count();
            if (size < maxParallel && childIterator.hasNext()) {
                childIterator.next().startRun(runtimeEnvironment, this);
            } else if (!childIterator.hasNext() && 0 == size) {
                triggerStatusUpdate(END);
                childIterator = null;
                listenToStarter();
            }
        }
    }

    void handleError(final Child child) {
        if (wouldNotSendTwoUpdatesForOneOccurrence()) {
            triggerStatusUpdate(ERROR);
        }
        processErrorStrategy(child);
    }

    private void processErrorStrategy(final Child child) {
        switch (errorStrategy) {
            case STOP:
                directorDirective.set(DirectorDirective.FAIL);
                return;
            case RESTART_CHILD:
                LOGGER.warn("Error occurred: restarting grouped child");
                child.reset();
                child.startRun(runtimeEnvironment, this);
                break;
            case RESTART_ALL:
            case RESTART_LISTENING:
                triggerStatusUpdate(END);
                LOGGER.warn("Error occurred: restarting grouped children");
                orderedTriggers.forEach(Child::abort);
                orderedTriggers.forEach(Child::reset);
                if (ErrorStrategy.RESTART_ALL == errorStrategy) {
                    triggeredStarter.set(true);
                } else {
                    childIterator = null;
                    listenToStarter();
                }
        }
    }

    private boolean wouldNotSendTwoUpdatesForOneOccurrence() {
        return eventsToUpdate.contains(ERROR) && (
                ErrorStrategy.STOP == errorStrategy ||
                ErrorStrategy.RESTART_CHILD == errorStrategy ||
                !eventsToUpdate.contains(END));
    }

    private void triggerStatusUpdate(final UpdateEvent updateEvent) {
        if (null != updateSender && eventsToUpdate.contains(updateEvent)) {
            updateSender.send(orderedTriggers);
        }
    }

    public void setMaxParallel(final int max) {
        maxParallel = max;
    }

    public void setPauseBetweenRuns(final long value) {
        pauseBetweenRuns = value;
    }

    public void setUpdateSender(final UpdateSender updateSender, final List<UpdateEvent> eventsToUpdate) {
        this.updateSender = updateSender;
        this.eventsToUpdate = eventsToUpdate;
    }

    public void setErrorStrategy(final ErrorStrategy errorStrategy) {
        this.errorStrategy = errorStrategy;
    }

    public void clear() {
        Optional.ofNullable(updateSender)
                .ifPresent(UpdateSender::clear);
    }
}