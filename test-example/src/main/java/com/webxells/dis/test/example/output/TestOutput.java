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
package com.webxells.dis.test.example.output;

import com.webxells.dis.api.config.ConfigurableByType;
import com.webxells.dis.api.config.MappingConfiguration;
import com.webxells.dis.api.config.MappingPart;
import com.webxells.dis.api.output.Output;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

public class TestOutput implements Output<TestOutputConfig> {
    private static final Map<Thread, List<Integer>> ALL_HASHES = new HashMap<>();
    private static final Map<Thread, List<MappingConfiguration>> ALL_ASSIGNED_MAPPING_CONFIG = new HashMap<>();

    private final List<Integer> hashes = new LinkedList<>();
    private final List<MappingConfiguration> assignedMappingConfig = new LinkedList<>();

    private boolean startRun = false;
    private boolean endRun = false;
    private TestOutputConfig config;

    public static List<Integer> getAllHashes() {
        return ALL_HASHES.computeIfAbsent(Thread.currentThread(), a -> new LinkedList<>());
    }

    public static List<MappingConfiguration> getAllAssignedMappingConfig() {
        return ALL_ASSIGNED_MAPPING_CONFIG.computeIfAbsent(Thread.currentThread(), a -> new LinkedList<>());
    }

    public static void clear() {
        ALL_HASHES.clear();
        ALL_ASSIGNED_MAPPING_CONFIG.clear();
    }

    public TestOutput(TestOutputConfig config) {
        this.config = config;
        Optional.ofNullable(config.getSelfReference())
                .ifPresent(a -> a.set(this));
    }

    @Override
    public void write(final MappingConfiguration to) {
        hashes.add(to.partsByDestination(config.getName()).stream()
                .flatMap(a -> Stream.concat(
                        Stream.of(a),
                        a.getSubData().stream()
                                .flatMap(b -> b.parts().stream())))
                .map(MappingPart::value)
                .filter(Optional::isPresent)
                .mapToInt(a -> a.get().hashCode())
                .sum());
        assignedMappingConfig.add(to);
        ALL_ASSIGNED_MAPPING_CONFIG.computeIfAbsent(Thread.currentThread(), a -> new LinkedList<>()).add(to);
        ALL_HASHES.computeIfAbsent(Thread.currentThread(), a -> new LinkedList<>()).add(hashes.get(hashes.size() - 1));
    }

    @Override
    public String getName() {
        return config.getName();
    }

    public List<Integer> getHashes() {
        return hashes;
    }

    @Override
    public void start() {
        startRun = true;
    }

    @Override
    public void end() {
        endRun = true;
    }

    public boolean isStartRun() {
        return startRun;
    }

    public boolean isEndRun() {
        return endRun;
    }

    public List<MappingConfiguration> getAssignedMappingConfig() {
        return assignedMappingConfig;
    }
}