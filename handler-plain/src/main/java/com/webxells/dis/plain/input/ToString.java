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
package com.webxells.dis.plain.input;

import com.webxells.dis.api.config.MappingConfiguration;
import com.webxells.dis.api.error.InputOutputError;
import com.webxells.dis.api.input.Input;
import com.webxells.dis.base.SimpleDatasetPiece;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.atomic.AtomicInteger;

public class ToString implements Input<ToStringConfiguration> {
    private final ToStringConfiguration configuration;
    private String content;
    private boolean hasNext;

    public ToString(final ToStringConfiguration configuration) {
        this.configuration = configuration;
    }

    @Override
    public int read(final MappingConfiguration from) {
        hasNext = false;
        final AtomicInteger result = new AtomicInteger();
        from.partsBySource(getName())
                .forEach(a -> {
                    a.getDataset().collect(new SimpleDatasetPiece(content));
                    result.incrementAndGet();
                });
        return result.get();
    }

    @Override
    public boolean hasNext() {
        return hasNext;
    }

    @Override
    public String getName() {
        return configuration.getName();
    }

    @Override
    public void start() throws InputOutputError {
        try {
            final InputStream receive = configuration.getReceiver().receive();
            content = new String(receive.readAllBytes());
            receive.close();
        } catch (final IOException e) {
            throw new InputOutputError("Could not read from receiver", e);
        }
        hasNext = true;
    }

    @Override
    public void end() {
        content = null;
        hasNext = false;
    }
}
