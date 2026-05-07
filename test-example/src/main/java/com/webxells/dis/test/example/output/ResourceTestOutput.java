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
package com.webxells.dis.test.example.output;

import com.webxells.dis.api.config.MappingConfiguration;
import com.webxells.dis.api.error.DisException;
import com.webxells.dis.api.error.InputOutputError;
import com.webxells.dis.api.output.Output;
import java.io.IOException;
import java.io.OutputStream;

public class ResourceTestOutput implements Output<ResourceTestOutputConfig> {
    private final ResourceTestOutputConfig config;
    private OutputStream stream;

    public ResourceTestOutput(final ResourceTestOutputConfig config) {
        this.config = config;
    }

    @Override
    public void write(final MappingConfiguration to) throws InputOutputError {
        try {
            stream.write(to.size());
        } catch (IOException e) {
            throw new InputOutputError("failed", e);
        }
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public void start() throws DisException {
        stream = config.getSender().send();
    }

    @Override
    public void end() throws DisException {
        try {
            stream.flush();
            stream.close();
        } catch (IOException e) {
            throw new InputOutputError("failed", e);
        }
    }
}
