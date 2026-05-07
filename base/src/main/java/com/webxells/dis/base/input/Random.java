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
package com.webxells.dis.base.input;

import com.webxells.dis.api.Logger;
import com.webxells.dis.api.config.MappingConfiguration;
import com.webxells.dis.api.config.MappingPart;
import com.webxells.dis.api.input.Input;
import com.webxells.dis.base.SimpleDatasetPiece;
import com.webxells.dis.base.config.RandomInputMappingPart;
import com.webxells.dis.logging.LoggerProxyFactory;
import java.security.SecureRandom;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

public class Random implements Input<RandomConfig> {
    private static final Logger LOGGER = LoggerProxyFactory.logger(Random.class);

    private final long defaultMax;
    private final long defaultMin;
    private final int defaultValueSize;
    private final boolean defaultSecureGeneration;
    private final String name;

    private boolean done;
    private SecureRandom secureRandom;

    public Random(final RandomConfig config) {
        name = config.getName();
        defaultValueSize = config.getValueSize();
        defaultSecureGeneration = config.isSecureGeneration();
        defaultMax = config.getMax();
        defaultMin = config.getMin();
    }

    @Override
    public int read(final MappingConfiguration from) {
        done = true;
        final AtomicInteger found = new AtomicInteger();
        from.partsBySource(name)
                .forEach(mappingPart -> {
                    setRandomValues(mappingPart);
                    found.incrementAndGet();
                });
        return found.get();
    }

    private void setRandomValues(final MappingPart mappingPart) {
        if (mappingPart instanceof RandomInputMappingPart) {
            final RandomInputMappingPart randomPart = (RandomInputMappingPart) mappingPart;
            createRandom(mappingPart,
                    Optional.ofNullable(randomPart.getMax()).orElse(defaultMax),
                    Optional.ofNullable(randomPart.getMin()).orElse(defaultMin),
                    Optional.ofNullable(randomPart.getValueSize()).orElse(defaultValueSize),
                    Optional.ofNullable(randomPart.isSecureGeneration()).orElse(defaultSecureGeneration));
        } else {
            createRandom(mappingPart, defaultMax, defaultMin, defaultValueSize, defaultSecureGeneration);
        }
    }

    private void createRandom(final MappingPart mappingPart,
                              final long max, final long min, int valueSize, final boolean secureGeneration) {
        assertValidBoundary(max);
        assertValidBoundary(min);
        while (valueSize-- > 0) {
            mappingPart.getDataset().collect(new SimpleDatasetPiece(
                    String.valueOf(createBothLimitRandomString(max + 1, min, secureGeneration))));
        }
    }

    private void assertValidBoundary(final long longValue) {
        if (longValue > Long.MAX_VALUE / 2 || longValue < Long.MIN_VALUE / 2) {
            throw new IllegalArgumentException("Invalid random boundary: " + longValue);
        }
    }

    private long createBothLimitRandomString(final long max, final long min, final boolean secureGeneration) {
        if (secureGeneration) {
            return createSecureLong(max, min);
        }
        return (long) ((Math.random() * (max - min)) + min);
    }

    private long createSecureLong(final long max, final long min) {
        logOutOfBoundsBoundary(max);
        logOutOfBoundsBoundary(min);
        return (long) getSecureRandom().nextInt((int) (max - min)) + min;
    }

    private void logOutOfBoundsBoundary(final long longValue) {
        if (longValue < Integer.MIN_VALUE / 2 || longValue > Integer.MAX_VALUE / 2) {
            LOGGER.d("random boundary %d is out of bounds - will be truncated to Integer max/min", longValue);
        }
    }

    private SecureRandom getSecureRandom() {
        if (null == secureRandom) {
            secureRandom = new SecureRandom();
        }
        return secureRandom;
    }

    @Override
    public boolean hasNext() {
        return !done;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void start() {
        done = false;
    }

    @Override
    public void end() {
    }
}