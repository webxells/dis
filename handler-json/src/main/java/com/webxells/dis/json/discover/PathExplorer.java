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
package com.webxells.dis.json.discover;

import com.webxells.dis.api.Logger;
import com.webxells.dis.api.config.description.Description;
import com.webxells.dis.api.discover.DiscoverSummary;
import com.webxells.dis.api.discover.Explorer;
import com.webxells.dis.api.discover.PathDiscoverer;
import com.webxells.dis.base.discover.SimpleExplorerSummary;
import com.webxells.dis.logging.LoggerProxyFactory;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

public class PathExplorer implements Explorer {
    public static class JsonRun implements Explorer.Run {
        private static final Logger LOGGER = LoggerProxyFactory.logger(PathExplorer.class);

        private final JsonExplorerParser reader;
        private final SimpleExplorerSummary explorerSummary = new SimpleExplorerSummary("text/json");
        private final AtomicLong maxPathKey = new AtomicLong();
        public JsonRun(final File file) {
            try {
                reader = new JsonExplorerParser(new BufferedReader(new InputStreamReader(new FileInputStream(file))));
            } catch (final FileNotFoundException e) {
                throw new RuntimeException("could not init json reader", e);
            }
        }

        @Override
        public DiscoverSummary explore(final DiscoverSummary discoverSummary) {
            if (explorerSummary.hasRun()) {
                throw new IllegalStateException("already explored");
            }
            try {
                iterateThrough(discoverSummary.getPathDiscoverer());
            } catch (final IOException e) {
                throw new RuntimeException("json parse error", e);
            }
            explorerSummary.setDatasets(maxPathKey.get() + 1);
            explorerSummary.hasRun();
            discoverSummary.setExplorerSummary(explorerSummary);
            return discoverSummary;
        }

        private void iterateThrough(final List<PathDiscoverer.Run> pathDiscoverer) throws IOException {
            while(reader.hasNext()) {
                final Optional<JsonValue> next = reader.next();
                next.ifPresent(value -> {
                    pathDiscoverer.forEach(b -> b.investigate(value.generalizedPath(), value.value()));
                    fetchMaxPathValueForGuessedIterationPath(value.path());
                });
            }
        }

        private void fetchMaxPathValueForGuessedIterationPath(final String path) {
            JsonValue.ARRAY_KEYS.matcher(path).results()
                    .map(a -> Long.valueOf(a.group(1)))
                    .forEach(a -> {
                        synchronized (maxPathKey) {
                            if (a > maxPathKey.get()) {
                                maxPathKey.set(a);
                            }
                        }
                    });
        }

        @Override
        public boolean isCapable() {
            try {
                return JsonExplorerParser.NecessaryToken.VALUE != reader.peekReader();
            } catch (final IOException e) {
                LOGGER.debug("error while trying to read json: ".concat(e.getMessage()));
            }
            return false;
        }
    }

    @Override
    public Run newRun(final File file) {
        return new JsonRun(file);
    }
}
