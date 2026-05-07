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
package com.webxells.dis.workflow.service.internal;

import com.webxells.dis.api.Logger;
import com.webxells.dis.api.RuntimeEnvironment;
import com.webxells.dis.api.config.JobConfig;
import com.webxells.dis.api.trigger.Trigger;
import com.webxells.dis.api.workflow.Job.JobState;
import com.webxells.dis.boot.RawThreadEnvironment;
import com.webxells.dis.boot.WorkflowSecurity;
import com.webxells.dis.event.EventManager;
import com.webxells.dis.logging.LoggerProxyFactory;
import com.webxells.dis.workflow.service.RestartPolicy;
import com.webxells.dis.workflow.service.ServiceJob;
import com.webxells.dis.workflow.service.ServiceSupplier;
import com.webxells.dis.workflow.service.config.ServiceJobConfig;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Director {

    private enum State {
        RUNNING, DISABLED, STOPPED
    }

    private record History(JobState jobState, LocalDateTime start, LocalDateTime end) { }

    private static class Entity {
        private final RestartPolicy restartPolicy;
        private final List<History> history = new ArrayList<>();
        private final ServiceJob job;

        private RuntimeEnvironment.Run run;
        private JobState jobState;
        private LocalDateTime start;
        private LocalDateTime end;

        private Entity(final JobConfig jobConfig) {
            job = new ServiceJob(jobConfig);
            restartPolicy = jobConfig instanceof ServiceJobConfig serviceJobConfig ?
                    serviceJobConfig.getMonitoringPolicy() : RestartPolicy.ERROR;
        }

        JobState realState() {
            return jobState;
        }

        JobState state() {
            return JobState.WAITING == jobState && null != end ? JobState.SUCCESS : jobState;
        }

        void state(final JobState jobState) {
            this.jobState = jobState;
        }

        RestartPolicy monitoringPolicy() {
            return restartPolicy;
        }

        void start() {
            start = LocalDateTime.now();
        }

        void end() {
            end = LocalDateTime.now();
        }

        public void reset() {
            if (null != start) {
                history.add(new History(state(), start, end));
            }
            jobState = null;
            start = end = null;
        }
    }

    private static final Logger LOGGER = LoggerProxyFactory.logger(Director.class);
    private static final EventManager EVENT_MANAGER = EventManager.instance();

    private final Map<String, Entity> jobs;
    private final Queue<ServiceJob> triggerQueue = new ConcurrentLinkedQueue<>();
    private final int maxParallel;
    private final long monitoringInterval;
    private final RuntimeEnvironment runtimeEnvironment;
    private final ServiceSecurity serviceSecurity;
    private final Map<String, List<String>> openEventContexts;
    private final boolean exposeJobStatus;

    private RuntimeEnvironment.Run selfRun;

    private volatile State state = State.STOPPED;

    public Director(final List<JobConfig> configurations, final ServiceSupplier serviceSupplier) {
        monitoringInterval = serviceSupplier.getMonitoringInterval();
        maxParallel = serviceSupplier.getMaxParallel();
        openEventContexts = serviceSupplier.getOpenEventContexts();
        runtimeEnvironment = serviceSupplier.getRuntimeEnvironment();
        exposeJobStatus = serviceSupplier.isExposeJobStatus();
        serviceSecurity = getServiceSecurity();
        jobs = createJobsMapping(configurations);
    }

    private ServiceSecurity getServiceSecurity() {
        final WorkflowSecurity instance = WorkflowSecurity.instance();
        if (instance instanceof ServiceSecurity theServiceSecurity) {
            return theServiceSecurity;
        }
        throw new IllegalStateException("Service Security is not instance of ServiceSecurity");
    }

    private Map<String, Entity> createJobsMapping(final List<JobConfig> configurations) {
        final Map<String, Entity> result = new LinkedHashMap<>();
        for (final JobConfig jobConfig : configurations) {
            final Entity entity = new Entity(jobConfig);
            serviceSecurity.addJob(jobConfig.getName(), entity.job);
            result.put(entity.job.getJobName(), entity);
        }
        return result;
    }

    private void createEventRedirections() {
        openEventContexts.entrySet().stream()
                .filter(a -> null != a.getValue() && jobExists(a.getKey()))
                .forEach(a ->
                        EVENT_MANAGER.redirect(
                                jobs.get(a.getKey()),
                                a.getValue().stream()
                                        .filter(this::jobExists)
                                        .map(jobs::get)
                                        .collect(Collectors.toList()))
                );
    }


    private boolean jobExists(final String key) {
        return null != key && jobs.containsKey(key);
    }

    public void start() {
        if (null != openEventContexts && !openEventContexts.isEmpty()) {
            LOGGER.i("Redirecting events...");
            createEventRedirections();
        }
        state = State.RUNNING;
        LOGGER.info("Starting director...");
        selfRun = RawThreadEnvironment.instance()
                .newRun(this::monitor, "monitoring");
        selfRun.start();
        LOGGER.i("Enabling triggers...");
        jobs.forEach(this::startTriggers);
    }

    private void startTriggers(final String name, Entity entity) {
        entity.job.ready();
        for (final Trigger<?> trigger : entity.job.getTriggers()) {
            trigger.awaitAction(() -> trigger(entity.job, entity, trigger.getPileStrategy()));
        }
    }

    private void trigger(final ServiceJob serviceJob, final Entity entity,
                         final Trigger.TriggerPileStrategy pileStrategy) {
        final RuntimeEnvironment.Run run = entity.run;
        boolean startNew = true;
        if (null != run && run.isAlive()) {
            startNew = handlePile(serviceJob, entity, pileStrategy);
        }
        if (startNew) {
            triggerQueue.add(serviceJob);
        }
    }



    private boolean handlePile(final ServiceJob job, final Entity entity,
                               final Trigger.TriggerPileStrategy pileStrategy) {
        final String jobName = job.getJobName();
        switch (pileStrategy) {
            case WAIT -> {
                LOGGER.d("Waiting for previous job run to finish %s", jobName);
                while (Optional.ofNullable(entity.run)
                        .map(RuntimeEnvironment.Run::isAlive)
                        .orElse(false)) {
                    try {
                        Thread.sleep(countInstanceInQueue(job) * 2000 + 1000);
                    } catch (final InterruptedException e) {
                        throw new RuntimeException("Nightmare!", e);
                    }
                }
            }
            case IGNORE -> {
                LOGGER.d("Ignoring job trigger for %s - already running", jobName);
                return false;
            }
            case ABORT_RUNNING -> {
                LOGGER.w("Aborting running job for fresh triggered run: %s", jobName);
                entity.run.abort();
            }
            case ERROR -> throw new RuntimeException("Job triggered before ending: " + jobName);
        }
        return true;
    }

    private long countInstanceInQueue(final ServiceJob job) {
        return triggerQueue.stream()
                .filter(a -> a == job)
                .count();
    }

    public void stop() {
        if (isRunning() && selfRun != null && selfRun.isAlive()) {
            state = State.STOPPED;
            try {
                Thread.sleep((int) (monitoringInterval * 1.1));
            } catch (final InterruptedException e) {
                throw new RuntimeException("Nightmare!", e);
            }
            if (selfRun.isAlive()) {
                LOGGER.d("Failed stopping monitoring - forcing shut down...");
                selfRun.abort();
            }
        }
    }

    public void join() throws InterruptedException {
        if (selfRun != null) {
            selfRun.join();
        }
    }

    private void monitor() {
        while(selfRun.isAlive() && State.STOPPED != state) {
            processWaitingJobs();
            jobs.forEach(this::checkState);
            try {
                Thread.sleep(monitoringInterval);
            } catch (final InterruptedException ignored) {}
        }
        LOGGER.d("Director Stopped");
    }

    private void processWaitingJobs() {
        ServiceJob current;
        while (isRunning() && null != (current = triggerQueue.poll()) && spaceForMore()) {
            final Entity entity = jobs.get(current.getJobName());
            if (null == entity) {
                throw new RuntimeException("Unknown job queued: " + current.getJobName());
            }
            startJob(entity);
        }
    }

    private void startJob(final Entity entity) {
        final String jobName = entity.job.getJobName();
        entity.reset();
        entity.run = runtimeEnvironment.newRun(() -> {
            serviceSecurity.addAllowance(jobName);
            if (null != openEventContexts && !openEventContexts.isEmpty()) {
                getMyAllowances(jobName)
                        .forEach(serviceSecurity::addAllowance);
            }
            entity.start();
            entity.job.start();
            entity.end();
            serviceSecurity.reset(jobName);
        }, jobName);
        entity.run.start();
    }

    private Stream<String> getMyAllowances(final String name) {
        return openEventContexts.entrySet().stream()
                .filter(a -> a.getValue().contains(name))
                .map(Map.Entry::getKey);

    }

    private boolean spaceForMore() {
        return jobs.values().stream()
                .map(a -> a.run)
                .filter(Objects::nonNull)
                .filter(RuntimeEnvironment.Run::isAlive)
                .count() < maxParallel;
    }

    public boolean isSomethingRunning() {
        return jobs.values().stream()
                .anyMatch(a -> JobState.RUNNING == a.realState());

    }

    private void checkState(final String name, final Entity entity) {
        final ServiceJob serviceJob = entity.job;
        final JobState current = serviceJob.state();
        checkForChangedState(current, entity, serviceJob.toString());
        if (isRunning()) {
            switch (serviceJob.state()) {
                case ERROR:
                case STOPPED: {
                    if (RestartPolicy.RESTART.equals(entity.monitoringPolicy())) {
                        LOGGER.i("Restarting serviceJob: ".concat(serviceJob.toString()));
                        triggerQueue.add(serviceJob);
                        LOGGER.d("ServiceJobConfig restarted");
                        serviceJob.ready();
                    }
                }
            }
        }
    }

    private boolean isRunning() {
        return State.RUNNING == state;
    }

    private void checkForChangedState(final JobState current, final Entity entity, final String description) {
        if (!current.equals(entity.realState())) {
            if (null != entity.realState()) {
                LOGGER.i(String.format("ServiceJobConfig (%s) is on state %s", description, current));
            }
            entity.state(current);
        }
    }

    public void disable() {
        LOGGER.d("Disabling Triggers");
        jobs.values().stream()
                .map(a -> a.job)
                .flatMap(a -> a.getTriggers().stream())
                .forEach(Trigger::abort);
        LOGGER.d("Clearing run queue");
        triggerQueue.clear();
    }

    public void interrupt() {
        jobs.values().stream()
                .map(a -> a.job)
                .forEach(a -> {
                    a.stop();
                    EVENT_MANAGER.reset(a);
                });
    }

    public Map<String, Object> parseJobStatus() {
        if (!exposeJobStatus) {
            return Map.of();
        }
        final Map<String, Object> result = new LinkedHashMap<>();
        for (final Map.Entry<String, Entity> current : jobs.entrySet()) {
            final Entity entity = current.getValue();
            final Map<String, Object> entry = new HashMap<>();
            result.put( current.getKey(), entry);
            entry.put("state", entity.state());
            entry.put("start", entity.start);
            entry.put("end", entity.end);
            final List<Map<String, Object>> history = new ArrayList<>();
            for (final History past : entity.history) {
                final Map<String, Object> historyEntry = new HashMap<>();
                historyEntry.put("state", past.jobState);
                historyEntry.put("start", past.start);
                historyEntry.put("end", past.end);
                history.add(historyEntry);
            }
            entry.put("history", history);
        }
        return result;
    }

}