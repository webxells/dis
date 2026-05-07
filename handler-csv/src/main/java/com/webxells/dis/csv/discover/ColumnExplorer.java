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
package com.webxells.dis.csv.discover;

import com.webxells.dis.api.Logger;
import com.webxells.dis.api.discover.DiscoverSummary;
import com.webxells.dis.api.discover.Explorer;
import com.webxells.dis.api.discover.PathDiscoverer;
import com.webxells.dis.base.discover.SimpleExplorerSummary;
import com.webxells.dis.csv.CsvInputConfig;
import com.webxells.dis.csv.discover.intern.CsvConfigurationGuesser;
import com.webxells.dis.csv.discover.intern.HeaderExplorer;
import com.webxells.dis.csv.internal.CsvReader;
import com.webxells.dis.csv.internal.CsvRow;
import com.webxells.dis.logging.LoggerProxyFactory;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

public class ColumnExplorer implements Explorer {
    private static final Logger LOGGER = LoggerProxyFactory.logger(ColumnExplorer.class);

    private int maxRowsToBeExplored;
    public static class CsvRun implements Explorer.Run {

        private final File file;
        private final int maxRowsToBeExplored;

        private final SimpleExplorerSummary explorerSummary = new SimpleExplorerSummary("text/csv");
        private CsvConfigurationGuesser csvConfigurationGuesser;

        public CsvRun(final File file, final int maxRowsToBeExplored) {
            this.file = file;
            this.maxRowsToBeExplored = maxRowsToBeExplored;
        }

        @Override
        public DiscoverSummary explore(final DiscoverSummary discoverSummary) {
            try {
                iterateThrough(discoverSummary.getPathDiscoverer());
            } catch (final IOException e) {
                throw new RuntimeException(e);
            }
            discoverSummary.setExplorerSummary(explorerSummary);
            return discoverSummary;
        }

        private void iterateThrough(final List<PathDiscoverer.Run> pathDiscoverer) throws IOException {
            final HeaderExplorer headerExplorer = exploreHeaders();
            final CsvRow header = headerExplorer.getHeaders();
            final int headerLength = header.length();
            CsvRow row;
            int datasets = 0;
            try (final CsvReader csvReader = createNewCsvReader()) {
                if (headerExplorer.hasHeaders()) {
                    csvReader.read();
                }
                while (null != (row = clean(csvReader.read()))) {
                    datasets++;
                    for (int i = 0, m = Math.min(headerLength, row.length()); i < m; i++) {
                        final String currentHeader = header.getByIndex(i).value();
                        final String currentValue = row.getByIndex(i).value();
                        pathDiscoverer.forEach(a -> a.investigate(currentHeader, currentValue));
                    }
                }
            }
            explorerSummary.setDatasets(datasets);
            explorerSummary.setFileTypeFormat(createFileTypeFormatString(headerExplorer.hasHeaders()));
            explorerSummary.setHasRun(true);
        }

        private String createFileTypeFormatString(final boolean hasHeader) {
            return String.format(
                    "{\"fieldSeparator\":\"%s\",\"valueDelimiter\":\"%s\",\"skipLines\":\"%d\",\"hasHeader\":\"%s\", \"lineEnding\":\"%s\"}",
                    csvConfigurationGuesser.getFieldSeparator(),
                    Optional.ofNullable(csvConfigurationGuesser.getValueSeparator())
                            .map(a -> '"' == a ? "\\\"" : String.valueOf(a))
                            .orElse(""),
                    csvConfigurationGuesser.getSkipLines(), hasHeader ? "true" : "false",
                    csvConfigurationGuesser.getLineEnding());
        }

        private HeaderExplorer exploreHeaders() throws IOException {
            try (final CsvReader newCsvReader = createNewCsvReader()) {
                return new HeaderExplorer(newCsvReader);
            }
        }

        private CsvReader createNewCsvReader() throws IOException {
            return new CsvReader(new FileInputStream(file), buildConfig());
        }

        private CsvInputConfig buildConfig() {
            final CsvInputConfig csvConfig = new CsvInputConfig();
            csvConfig.setFieldSeparator(csvConfigurationGuesser.getFieldSeparator());
            csvConfig.setIgnoringEmptyLines(true);
            csvConfig.setFirstLineAsHeaders(false);
            csvConfig.setValueSeparator(csvConfigurationGuesser.getValueSeparator());
            csvConfig.setLineEnding(csvConfigurationGuesser.getLineEnding());
            csvConfig.setSkipLines(csvConfigurationGuesser.getSkipLines());
            return csvConfig;
        }

        @Override
        public boolean isCapable() {
            try {
                csvConfigurationGuesser = new CsvConfigurationGuesser(file, maxRowsToBeExplored);
                return true;
            } catch (final Throwable e) {
                LOGGER.debug(String.format("Error on guessing csv configuration (%s): %s", e.getClass().getName(), e.getMessage()));
            }
            return false;
        }
    }

    public static CsvRow clean(final CsvRow read) {
        if (null != read) {
            read.clean();
        }
        return read;
    }

    @Override
    public Run newRun(final File file) {
        return new CsvRun(file, maxRowsToBeExplored);
    }

    public void setMaxRowsToBeExplored(final int maxRowsToBeExplored) {
        this.maxRowsToBeExplored = maxRowsToBeExplored;
    }
}