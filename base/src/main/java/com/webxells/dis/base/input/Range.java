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

import com.webxells.dis.api.config.MappingConfiguration;
import com.webxells.dis.api.error.DisException;
import com.webxells.dis.api.error.InputOutputError;
import com.webxells.dis.api.input.Input;
import com.webxells.dis.base.SimpleDatasetPiece;

public class Range implements Input<RangeConfig> {
    private final int start;
    private final int end;
    private final int step;
    private final String name;

    private int current;

    public Range(final RangeConfig config) {
        name = config.getName();
        this.start = config.getStart();
        this.end = config.getEnd();
        this.step = config.getStep();
    }

    @Override
    public int read(final MappingConfiguration from) throws InputOutputError {
        current = current + step;
        return from.partsBySource(name).stream()
                .mapToInt(a -> {
                    a.getDataset().collect(new SimpleDatasetPiece(String.valueOf(current)));
                    return 1;
                })
                .sum();
    }

    @Override
    public boolean hasNext() throws InputOutputError {
        return current + step <= end;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void start() {
        current = start - step;
    }

    @Override
    public void end() { }
}