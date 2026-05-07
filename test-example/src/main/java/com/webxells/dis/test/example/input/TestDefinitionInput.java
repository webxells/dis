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
package com.webxells.dis.test.example.input;

import com.webxells.dis.api.DatasetPiece;
import com.webxells.dis.api.config.MappingConfiguration;
import com.webxells.dis.api.error.DisException;
import com.webxells.dis.api.error.InputOutputError;
import com.webxells.dis.api.input.Input;
import com.webxells.dis.base.SimpleDatasetPiece;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class TestDefinitionInput implements Input<TestDefinitionInputConfiguration> {
    private final LinkedList<Map<String, String>> data;
    private final TestDefinitionInputConfiguration.EventLogger logger;
    private final String name;

    public TestDefinitionInput(TestDefinitionInputConfiguration configuration) {
        data = configuration.getData();
        logger = configuration.logger();
        name = configuration.getName();
    }

    @Override
    public int read(final MappingConfiguration from) throws InputOutputError {
        if (data.isEmpty() || data.peek().isEmpty()) {
            logger.log("READ_FAILED");
            data.pop();
            return 0;
        }
        logger.log("READ_SUCCEEDED");
        final Map<String, String> currentRow = data.pop();
        final AtomicInteger counter = new AtomicInteger();
        from.partsBySource(name).stream()
                .filter(a -> currentRow.containsKey(a.getInput().getPath()))
                .forEach(a -> {
                    a.getDataset().collect(new SimpleDatasetPiece(currentRow.get(a.getInput().getPath())));
                    counter.incrementAndGet();
                });
        return counter.get();
    }

    @Override
    public boolean hasNext() {
        logger.log("HAS_NEXT");
        if (!data.isEmpty() && data.peek().isEmpty()) {
           data.pop();
           return false;
        }
        return !data.isEmpty();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void start() {
        logger.log("START");
    }

    @Override
    public void end() {
        logger.log("END");
    }
}
