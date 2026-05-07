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

import com.webxells.dis.api.error.InputOutputError;
import com.webxells.dis.api.error.InvalidApi;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.stream.XMLStreamException;

public class CellReader extends OfficeXssfReader<CellReaderConfig> {
    private final Map<String, List<String>> firstLineMapping = new HashMap<>();
    private final String sheetName;
    private final int sameNameSheetNumber;

    public CellReader(final CellReaderConfig config) {
        super(config);
        sheetName = config.getSheetName();
        sameNameSheetNumber = config.getSameNameSheetNumber();
    }

    @Override
    public void validate() throws InvalidApi {
        super.validate();
        if (null == sheetName && 0 < sameNameSheetNumber) {
            throw new InvalidApi("SheetName is required");
        }
    }

    @Override
    public void start() throws InputOutputError {
        super.start();
        try {
            xssfReader = worksheetReader.createXssfReader(sheetName, sameNameSheetNumber);
            startXssfReader();
        } catch (final IOException | XMLStreamException e) {
            throw new InputOutputError("Could not load worksheet " + sheetName, e);
        }
    }
}