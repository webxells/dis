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
import com.webxells.dis.officex.xlsx.selector.SheetSelector;
import javax.xml.stream.XMLStreamException;

public class XssfMultiSheetInput extends OfficeXssfReader<XssfMultiSheetInputConfig> {
    private final SheetSelector selector;

    public XssfMultiSheetInput(final XssfMultiSheetInputConfig config) {
        super(config);
        selector = config.getSelector();
    }

    @Override
    public void validate() throws InvalidApi {
        super.validate();
    }

    @Override
    public int read(final MappingConfiguration from) throws InputOutputError {
        assertValidReader();
        return super.read(from);
    }

    private void assertValidReader() throws InputOutputError {
        getNext();
        if (null == xssfReader) {
            throw new InputOutputError("No next sheet instance");
        }
    }

    @Override
    public boolean hasNext() throws InputOutputError {
        getNext();
        return null != xssfReader && super.hasNext();
    }

    @Override
    public void start() throws InputOutputError {
        super.start();
        worksheetReader.loadSelected(selector);
    }

    private void getNext() throws InputOutputError {
        try {
            if (null == xssfReader || !xssfReader.hasNext()) {
                xssfReader = worksheetReader.getNext()
                        .orElse(null);
                startXssfReader();
            }
        } catch (final XMLStreamException e) {
            throw new InputOutputError("Could not init next reader", e);
        }
    }
}