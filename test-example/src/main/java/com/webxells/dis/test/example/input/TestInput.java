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
import com.webxells.dis.api.error.InputOutputError;
import com.webxells.dis.api.input.Input;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class TestInput implements Input<TestInputConfig> {
    private static final List<MappingConfiguration> assignedMappingConfig = new LinkedList<>();
    private static LinkedList<Map<String, List<DatasetPiece>>> data;
    private static boolean start;
    private static boolean end;
    private int i = 0;
    private boolean shouldWaitForSomeTestAssertions = false;
    private boolean shouldTriggerException = false;

    private final String name;
    private final NestedTestClass nestedTestClass;

    public TestInput(TestInputConfig config) {
        nestedTestClass = config.getNestedTestClass();
        name = config.getName();
        shouldWaitForSomeTestAssertions = config.isShouldWaitForSomeTestAssertions();
        shouldTriggerException = config.isShouldTriggerException();
    }

    public String getSomeStringOfNestedTestClass() {
        if (null == nestedTestClass) {
            return null;
        }
        return nestedTestClass.getSomeString();
    }

    @Override
    public synchronized int read(final MappingConfiguration from) throws InputOutputError {
        if (shouldTriggerException) {
            throw new RuntimeException("Ohhhhh.... sorry");
        }
        if (shouldWaitForSomeTestAssertions) {
            try {
                Thread.sleep(1000);
                shouldWaitForSomeTestAssertions = false;
            } catch (final InterruptedException ignored) { }
        }
        if (!hasNext()) {
            return 0;
        }
        final Map<String, List<DatasetPiece>> currentRow = data.pop();
        from.partsBySource(name).stream()
            .filter(a -> currentRow.containsKey(a.getInput().getPath()))
            .forEach(a -> a.getDataset().collect(currentRow.get(a.getInput().getPath())));
        assignedMappingConfig.add(from);
        return currentRow.size();
    }

    @Override
    public boolean hasNext() throws InputOutputError {
        return !data.isEmpty();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void start() {
        start = true;
    }

    @Override
    public void end() {
        end = true;
    }

    public static void setDataWithMultipleValue(final List<Map<String, List<DatasetPiece>>> data) {
        TestInput.data = new LinkedList<>(data);
        start = false;
        end = false;
        assignedMappingConfig.clear();
    }

    public static void setData(final List<Map<String, DatasetPiece>> data) {
        setDataWithMultipleValue(data.stream()
                .map(a -> a.entrySet().stream()
                        .collect(Collectors.toMap(Map.Entry::getKey, b -> List.of(b.getValue())))
                )
                .collect(Collectors.toList()));
    }

    public static List<MappingConfiguration> getAssignedMappingConfig() {
        return assignedMappingConfig;
    }

    public static boolean isStart() {
        return start;
    }

    public static boolean isEnd() {
        return end;
    }

    public void activateShouldWaitForSomeTestAssertions() {
        shouldWaitForSomeTestAssertions = true;
    }

    public void activateShouldTriggerException() {
        shouldTriggerException = true;
    }
}