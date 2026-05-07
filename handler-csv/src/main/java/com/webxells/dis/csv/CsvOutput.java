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
import com.webxells.dis.api.error.DisException;
import com.webxells.dis.api.error.InputOutputError;
import com.webxells.dis.api.output.Output;
import com.webxells.dis.csv.internal.CsvWriter;
import java.io.IOException;

public class CsvOutput extends CsvOperation<CsvOutputConfig> implements Output<CsvOutputConfig> {
    private final CsvOutputConfig config;
    private CsvWriter csvWriter;

    public CsvOutput(final CsvOutputConfig config) {
        super(config);
        this.config = config;
    }

    @Override
    public void write(final MappingConfiguration to) throws InputOutputError {
        assertStarted(csvWriter);

        final CsvWriter.RowWriter rowWriter = csvWriter.newRow();
        to.partsByDestination(config.getName())
                .forEach(a -> rowWriter.write(a.getOutput().getPath(), a.value().orElse(null)));

        try {
            rowWriter.flush();
        } catch (final IOException e) {
            throw new InputOutputError("Could not write Csv file", e);

        }
    }


    @Override
    public void start() throws DisException {
        assertNotStarted(csvWriter);
        csvWriter = new CsvWriter(config.getSender().send(), config);
    }

    @Override
    public void end() {
        assertStarted(csvWriter);
        closeStream(csvWriter);
        csvWriter = null;
    }
}