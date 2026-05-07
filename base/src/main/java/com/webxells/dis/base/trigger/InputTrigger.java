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
package com.webxells.dis.base.trigger;

import com.webxells.dis.api.Logger;
import com.webxells.dis.api.config.InputConfig;
import com.webxells.dis.api.config.MappingConfiguration;
import com.webxells.dis.api.config.MappingPart;
import com.webxells.dis.api.error.DisException;
import com.webxells.dis.api.error.InputOutputError;
import com.webxells.dis.api.error.InvalidApi;
import com.webxells.dis.api.input.Input;
import com.webxells.dis.base.config.SimpleMappingConfiguration;
import com.webxells.dis.base.config.SimpleMappingPart;
import com.webxells.dis.base.trigger.input.TriggerStrategy;
import com.webxells.dis.boot.ServiceManager;
import com.webxells.dis.logging.LoggerProxyFactory;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class InputTrigger extends SimpleConcurrentTrigger<InputTriggerConfiguration> {
    public static class InputStorage {
        private final Queue<List<MappingPart>> mappingParts = new ConcurrentLinkedQueue<>();
        private final String name;

        private InputStorage(final String name) {
            this.name = name;
        }

        public boolean hasNext() {
            return 0 < mappingParts.size();
        }

        public List<MappingPart> next() {
            return mappingParts.poll();
        }
    }

    private static final Logger LOGGER = LoggerProxyFactory.logger(InputTrigger.class);
    private static final Map<Thread, List<InputStorage>> REGISTRY = new ConcurrentHashMap<>();
    private static final Map<Thread, List<MappingPart>> LAST_SET = new ConcurrentHashMap<>();

    private final InputConfig inputConfig;
    private final MappingConfiguration mappingConfiguration;
    private final TriggerStrategy triggerStrategy;
    private final boolean restart;

    private Input<?> input;

    public InputTrigger(final InputTriggerConfiguration config) {
        super(config);
        inputConfig = config.getInput();
        mappingConfiguration = newConfiguration(config.getParts());
        triggerStrategy = config.getTriggerStrategy();
        restart = config.shouldRestartEveryRead();
    }

    public static InputStorage registerInput(final String name) {
        final InputStorage result = new InputStorage(name);
        registry()
                .add(result);
        Optional.ofNullable(LAST_SET.get(Thread.currentThread()))
                .ifPresent(result.mappingParts::add);
        return result;
    }

    private static List<InputStorage> registry() {
        return REGISTRY.computeIfAbsent(Thread.currentThread(), a -> new LinkedList<>());
    }

    @Override
    protected boolean shouldTrigger() {
        try {
            mappingConfiguration.parts().forEach(MappingPart::clear); //dont delete unstable mapping parts
            final boolean result = triggerStrategy.shouldTrigger(getInput(), mappingConfiguration);
            copyReadInput();
            if(!result) {
                sleep();
            }
            return result;
        } catch (final InputOutputError e) {
            throw new RuntimeException("Unable to read input", e);
        }
    }

    private void copyReadInput() {
        final String name = inputConfig.getName();
        for (InputStorage a : registry()) {
            if (name.equals(a.name)) {
                a.mappingParts.add(mappingConfiguration.copy().parts());
            }
        }
        LAST_SET.put(Thread.currentThread(), mappingConfiguration.copy().parts());
    }

    private Input<?> getInput() {
        if (null == input || restart) {
            endInput();
            LOGGER.d("Starting embedded input %s", inputConfig.getName());
            input = ServiceManager.loadByConfig(inputConfig);
            try {
                input.validate();
            } catch (final InvalidApi e) {
                throw new RuntimeException("Could not start input", e);
            }
        }
        return input;
    }

    @Override
    public void abort() {
        endInput();
        REGISTRY.forEach((a, b) -> b.clear());
        super.abort();
    }

    private void endInput() {
        try {
            if (null != input) {
                input.end();
            }
        } catch (final DisException e) {
            throw new RuntimeException("Could not end input", e);
        }
    }

    private MappingConfiguration newConfiguration(final List<SimpleMappingPart> parts) {
        final SimpleMappingConfiguration result = new SimpleMappingConfiguration();
        result.parts().addAll(parts);
        return result;
    }
}