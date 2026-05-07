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
package com.webxells.dis.event;

import com.webxells.dis.api.Logger;
import com.webxells.dis.api.RuntimeEnvironment;
import com.webxells.dis.boot.RawThreadEnvironment;
import com.webxells.dis.logging.LoggerProxyFactory;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

public class EventManager {
    public class ContextProxy {
        private final Object context;

        public ContextProxy(final Object context) {
            this.context = context;
        }

        public void redirect(final Object from) {
            EventManager.this.redirect(from, context);
        }

        public <T extends Event> void on(final Class<T> eventClass, final Action<? extends T> action) {
            EventManager.this.on(eventClass, action, context);
        }

        public <T extends Event> void trigger(final T event) {
            EventManager.this.trigger(event, context);
        }

        public <T extends Event> void reset(final Class<T> eventClass) {
            EventManager.this.reset(eventClass, context);
        }

        public Object getContext() {
            return context;
        }
    }

    private final static Logger LOGGER = LoggerProxyFactory.logger(EventManager.class);
    private final static EventManager INSTANCE = new EventManager();
    private final static RuntimeEnvironment runtimeEnvironment = RawThreadEnvironment.instance();

    private final Map<Object, Map<Class<? extends Event>, List<Action<?>>>> registry = new HashMap<>();
    private final Map<Object, Set<Object>> triggerRedirections = new HashMap<>();

    public static EventManager instance() {
        return INSTANCE;
    }

    private EventManager() { }

    public void redirect(final Object from, final Object to) {
        triggerRedirections.computeIfAbsent(from, a -> new HashSet<>()).add(to);
    }

    public void redirect(final Object from, final List<Object> to) {
        to.forEach(a -> redirect(from, a));
    }

    public <T extends Event> void on(final Class<T> eventClass, final Action<? extends T> action) {
        LOGGER.d(String.format("registering new action for event: %s for %s", eventClass, action));
        on(eventClass, action, Thread.currentThread());
    }

    public <T extends Event> void on(final Class<T> eventClass, final Action<? extends T> action,
                                     final Object context) {
        LOGGER.d(String.format("registering new action for event: %s for %s", eventClass, action));
        registry.computeIfAbsent(context, a -> new HashMap<>())
                    .computeIfAbsent(eventClass, (a) -> new LinkedList<>())
                        .add(action);
    }

    public <T extends Event> void trigger(final T event) {
        trigger(event, Thread.currentThread());
    }

    public <T extends Event> void trigger(final T event, final Object context) {
        LOGGER.t(String.format("trigger event: %s", event));
        Stream.concat(Stream.of(context), triggerRedirections.getOrDefault(context, Set.of()).stream())
                .forEach(currentContext -> registry.getOrDefault(currentContext, Map.of()).keySet().stream()
                        .filter(a -> a.isAssignableFrom(event.getClass()))
                        .flatMap(a -> registry.get(currentContext).get(a).stream())
                        .forEach(a -> startOnEvent(a, event)));
    }

    private <T extends Event> void startOnEvent(final Action<?> action, final T event) {
        if (event instanceof ConcurrentEvent) {
            runtimeEnvironment.newRun(() -> startEvent(action, event)).start();
        } else {
            startEvent(action, event);
        }
    }

    @SuppressWarnings("unchecked")
    private <T extends Event> void startEvent(final Action<?> action, final T event) {
        ((Action<T>) action).onEvent(event);
    }

    public void reset(final Object context) {
        LOGGER.d(String.format("reset for context: %s", context));
        Optional.ofNullable(registry.get(context))
                .ifPresent(Map::clear);
        Optional.ofNullable(triggerRedirections.get(context))
                        .ifPresent(Set::clear);
    }

    public <T extends Event> void reset(final Class<T> eventClass) {
        reset(eventClass, Thread.currentThread());
    }

    public <T extends Event> void reset(final Class<T> eventClass, final Object context) {
        if (registry.containsKey(context) && registry.get(context).containsKey(eventClass)) {
            registry.get(context).get(eventClass).clear();
        }
    }
}