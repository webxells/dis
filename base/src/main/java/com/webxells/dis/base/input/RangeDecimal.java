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
import com.webxells.dis.api.error.InputOutputError;
import com.webxells.dis.api.error.InvalidApi;
import com.webxells.dis.api.input.Input;
import com.webxells.dis.base.SimpleDatasetPiece;
import java.math.BigDecimal;

public class RangeDecimal implements Input<RangeDecimalConfig> {
    private final BigDecimal start;
    private final BigDecimal end;
    private final BigDecimal step;
    private final String name;

    private BigDecimal current;

    public RangeDecimal(final RangeDecimalConfig config) {
        name = config.getName();
        this.start = new BigDecimal(config.getStart());
        this.end = new BigDecimal(config.getEnd());
        this.step = new BigDecimal(config.getStep());
    }

    public void validate() throws InvalidApi {
        if (null == name) {
            throw new InvalidApi("Input name must be specified");
        }
        if (0 == step.compareTo(BigDecimal.ZERO) || (step.compareTo(BigDecimal.ZERO) > 0 && end.compareTo(start) < 0 ||
                (step.compareTo(BigDecimal.ZERO) < 0 && end.compareTo(start) > 0))) {
            throw new InvalidApi("Invalid range config: Respect field descriptions!");
        }
    }

    @Override
    public int read(final MappingConfiguration from) throws InputOutputError {
        current = current.add(step);
        return from.partsBySource(name).stream()
                .mapToInt(a -> {
                    a.getDataset().collect(new SimpleDatasetPiece(String.valueOf(current)));
                    return 1;
                })
                .sum();
    }

    @Override
    public boolean hasNext() throws InputOutputError {
        return current.add(step).compareTo(end) <= 0;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void start() {
        current = start.subtract(step);
    }

    @Override
    public void end() { }
}