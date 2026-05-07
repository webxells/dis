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
package com.webxells.dis.base.discover.output;

import com.webxells.dis.api.config.MappingConfiguration;
import com.webxells.dis.api.config.MappingPart;
import com.webxells.dis.api.discover.DiscoverResult;
import com.webxells.dis.api.discover.PathDiscoverer;
import com.webxells.dis.api.error.InputOutputError;
import com.webxells.dis.api.output.Output;
import com.webxells.dis.base.discover.SimpleDiscoverResult;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class Grouping implements Output<GroupingConfig> {
    private class LastString implements PathDiscoverer.Run {
        private String lastValue;

        @Override
        public void investigate(final String path, final String value) {
            if (null != value && !value.isBlank()) {
                lastValue = value;
            }
        }

        @Override
        public Map<String, List<DiscoverResult>> summarize() {
            return Map.of(dummyPath, List.of(new SimpleDiscoverResult(lastValue, 0)));
        }

        @Override
        public String name() {
            return null;
        }
    }

    private final String name;
    private final String dummyPath = String.valueOf(System.identityHashCode(this));
    private final Map<MappingPart, PathDiscoverer.Run> runs =  new HashMap<>();

    public Grouping(final GroupingConfig config) {
        name = config.getName();
    }

    @Override
    public void write(final MappingConfiguration to) throws InputOutputError {
        assertRunsAvailable(to);
        to.partsByDestination(name).stream()
                .filter(runs::containsKey)
                .forEach(a -> runs.get(a).investigate(dummyPath, a.value().orElse(null)));
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void start() {
        runs.clear();
    }

    @Override
    public void end() { }

    private void assertRunsAvailable(final MappingConfiguration to) {
        if (runs.isEmpty()) {
            to.partsByDestination(name)
                    .forEach(part -> {
                        final Optional<Discoverer> discoverer = part.getFirstRefinement(Discoverer.class);
                        runs.put(part, discoverer
                                .map(Discoverer::getPathDiscoverer)
                                .map(PathDiscoverer::newRun)
                                .orElseGet(LastString::new));
                    });
        }
    }
}