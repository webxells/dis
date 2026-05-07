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
package com.webxells.dis.base.output;

import com.webxells.dis.api.Logger;
import com.webxells.dis.api.config.MappingConfiguration;
import com.webxells.dis.api.error.InputOutputError;
import com.webxells.dis.api.output.Output;
import com.webxells.dis.api.resource.Resource;
import com.webxells.dis.base.binary.DataPool;
import com.webxells.dis.logging.LoggerProxyFactory;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Optional;

public class DataPoolOutput implements Output<DataPoolConfiguration> {
    public enum NoDataStrategy {
        EMPTY_FILE, ERROR, IGNORE
    }

    private static final Logger LOGGER = LoggerProxyFactory.logger(DataPoolOutput.class);

    private final Resource sender;
    private final String name;
    private final NoDataStrategy noDataStrategy;

    public DataPoolOutput(final DataPoolConfiguration config) {
        sender = config.getSender();
        name = config.getName();
        noDataStrategy = config.getNoDataStrategy();
    }

    @Override
    public void write(final MappingConfiguration to) throws InputOutputError {
        if (!DataPool.isPresent(name)) {
            LOGGER.d("Nothing found in DataPool: " + name);
            switch (noDataStrategy) {
                case ERROR:
                    throw new InputOutputError("Data pool is empty: " + name);
                case IGNORE:
                    return;
            }
        }
        transfer();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void start() { }

    @Override
    public void end() { }

    private void transfer() throws InputOutputError {
        final DataPool data = new DataPool(name);
        try (final OutputStream send = sender.send();
             final InputStream input = Optional.ofNullable(data.getContent())
                     .orElseGet(() -> new ByteArrayInputStream(new byte[0]))) {
            input.transferTo(send);
            send.flush();
        } catch (final IOException e) {
            throw new InputOutputError("Could not transfer data", e);
        }
    }
}