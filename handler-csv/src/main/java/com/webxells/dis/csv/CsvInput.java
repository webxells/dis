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
package com.webxells.dis.csv;

import com.webxells.dis.api.config.MappingConfiguration;
import com.webxells.dis.api.config.MappingPart;
import com.webxells.dis.api.error.InputOutputError;
import com.webxells.dis.api.input.Input;
import com.webxells.dis.csv.internal.CsvReader;
import com.webxells.dis.csv.internal.CsvRow;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CsvInput extends CsvOperation<CsvInputConfig> implements Input<CsvInputConfig> {
    private CsvReader csvReader;

    public CsvInput(final CsvInputConfig config) {
        super(config);
    }

    @Override
    public int read(final MappingConfiguration from) throws InputOutputError {
        assertStarted(csvReader);
        final CsvRow row = readCsvRow();
        if (null == row) {
            return 0;
        }
        try {
            return mapToPart(row, from);
        } catch (final IOException e) {
            throw new InputOutputError("Could not match rows", e);
        }
    }

    private int mapToPart(final CsvRow row, final MappingConfiguration mappingConfiguration) throws IOException {
        final SameColumnStrategy sameColumn = config.getSameColumn();
        final UnknownColumnStrategy unknownColumn = config.getUnknownColumn();
        final CsvRow.ColumnSorter<MappingPart> sorter = row.sorter();
        sorter
                .matcher((a, b) -> sameColumn.match(a, b, mappingConfiguration))
                .unknown(a -> unknownColumn.handle(a, mappingConfiguration));

        return sorter.match(groupByPath(mappingConfiguration.partsBySource(getName())));
    }

    private Map<String, List<MappingPart>> groupByPath(final List<MappingPart> mappingParts) {
        final Map<String, List<MappingPart>> result = new HashMap<>();
        for (final MappingPart mappingPart : mappingParts) {
            final String path = mappingPart.getInput().getPath();
            if (null != path) {
                if (!result.containsKey(path)) {
                    result.put(path, new ArrayList<>());
                }
                result.get(path).add(mappingPart);
            }
        }
        return result;
    }

    @Override
    public boolean hasNext() throws InputOutputError {
        assertStarted(csvReader);
        try {
            return csvReader.hasNext();
        } catch (IOException e) {
            throw new InputOutputError("Could not peek next line", e);
        }
    }

    @Override
    public void start() throws InputOutputError {
        assertNotStarted(csvReader);
        try {
            csvReader = new CsvReader(config.getReceiver().receive(), config);
        } catch (final IOException e) {
            throw new InputOutputError("Could not read first lines", e);
        }
    }

    @Override
    public void end() throws InputOutputError {
        if (null != csvReader) {
            try {
                csvReader.close();
            } catch (final IOException e) {
                throw new InputOutputError("Could not close csv reader", e);
            }
            csvReader = null;
        }
    }

    private CsvRow readCsvRow() throws InputOutputError {
        try {
            return csvReader.read();
        } catch (final IOException e) {
            throw new InputOutputError("Could not read from reader", e);
        }
    }
}