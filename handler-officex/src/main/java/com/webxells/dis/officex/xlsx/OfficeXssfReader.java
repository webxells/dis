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
package com.webxells.dis.officex.xlsx;

import com.webxells.dis.api.config.MappingConfiguration;
import com.webxells.dis.api.error.InputOutputError;
import com.webxells.dis.api.error.InvalidApi;
import com.webxells.dis.base.SimpleDatasetPiece;
import com.webxells.dis.officex.Reader;
import com.webxells.dis.officex.xlsx.internal.WorksheetReader;
import com.webxells.dis.officex.xlsx.internal.XssfReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.xml.stream.XMLStreamException;

abstract class OfficeXssfReader<T extends OfficeXssfReaderConfig> extends Reader<T> {
    protected final OfficeXssfReaderConfig config;
    private final Map<String, List<String>> firstLineMapping = new HashMap<>();
    private final OfficeXssfReaderConfig.CellAccessType cellAccessType;
    private final MultiValueStrategy multiValueStrategy;

    protected XssfReader xssfReader;
    protected WorksheetReader worksheetReader;

    static String createExcelByIndex(final String path) {
        final StringBuilder result = new StringBuilder();
        int index;
        try {
            index = Integer.parseInt(path);
        } catch (final NumberFormatException e) {
            throw new IllegalArgumentException("Not an excel index: " + path);
        }
        if (1 > index) {
            throw new IllegalArgumentException("Index should be positive: " + index);
        }
        while (index > 0) {
            final int current = --index % 26;
            result.append((char) (current + 65));
            index = index / 26;
        }
        return result.reverse().toString();
    }

    public OfficeXssfReader(final OfficeXssfReaderConfig config) {
        super(config);
        this.config = config;
        cellAccessType = config.getCellAccessType();
        multiValueStrategy = config.getMultiValueStrategy();
    }

    @Override
    public void validate() throws InvalidApi {
        super.validate();
    }

    @Override
    public int read(final MappingConfiguration from) throws InputOutputError {
        try {
            final XssfReader.XssfRow xssfRow = xssfReader.newRow();
            return from.partsBySource(getName()).stream()
                    .filter(a -> null != a.getInput().getPath())
                    .mapToInt(a -> multiValueStrategy.reduce(xssfRow.find(mapPath(a.getInput().getPath())))
                            .mapToInt(b -> {
                                a.getDataset().collect(new SimpleDatasetPiece(b));
                                return 1;
                            })
                            .sum())
                    .sum();
        } catch (final XMLStreamException e) {
            throw new InputOutputError("Error processing Xssf sheet", e);
        }
    }

    private List<String> mapPath(final String path) {
        return switch (cellAccessType) {
            case INDEX_COUNT -> List.of(createExcelByIndex(path));
            case EXCEL_REFERENCE -> List.of(path);
            case VALUE_OF_FIRST_LINE -> Optional.ofNullable(firstLineMapping.get(path))
                    .orElseGet(List::of);
        };
    }

    @Override
    public boolean hasNext() throws InputOutputError {
        try {
            return xssfReader.hasNext();
        } catch (final XMLStreamException e) {
            throw new InputOutputError("error while reading xssf", e);
        }
    }

    @Override
    public String getName() {
        return config.getName();
    }

    @Override
    public void start() throws InputOutputError {
        super.start();
        try {
            worksheetReader = new WorksheetReader(this, config.isSkipEmptyRows());
        } catch (final XMLStreamException | IOException e) {
            throw new InputOutputError("Could not load worksheet", e);
        }
    }

    protected void startXssfReader() throws InputOutputError {
        if (null != xssfReader) {
            try {
                skipLines();
                readFirstLine();
            } catch (final XMLStreamException e) {
                throw new InputOutputError("Could not start sheet", e);
            }
        }
    }

    @Override
    public void end() throws InputOutputError {
        super.end();
        try {
            if (null != xssfReader) {
                xssfReader.end();
            }
            if (null != worksheetReader) {
                worksheetReader.end();
            }
        } catch (final XMLStreamException e) {
            throw new InputOutputError("Could not close resources", e);
        }
    }

    private void skipLines() throws XMLStreamException {
        for (int i = config.getSkipLines(); i-- > 0;) {
            xssfReader.skipRow();
        }
    }

    private void readFirstLine() throws XMLStreamException {
        if (cellAccessType == OfficeXssfReaderConfig.CellAccessType.VALUE_OF_FIRST_LINE) {
            final XssfReader.XssfRow first = xssfReader.newRow();
            first.all().forEach(a -> a.getValue().forEach(b ->
                    firstLineMapping.computeIfAbsent(b, c -> new ArrayList<>()).add(a.getKey())));
        }
    }
}