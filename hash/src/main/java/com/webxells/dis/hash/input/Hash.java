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
package com.webxells.dis.hash.input;

import com.webxells.dis.api.config.MappingConfiguration;
import com.webxells.dis.api.error.InputOutputError;
import com.webxells.dis.api.error.InvalidApi;
import com.webxells.dis.api.hash.Manager;
import com.webxells.dis.api.input.Input;
import com.webxells.dis.base.SimpleDatasetPiece;
import java.io.IOException;

public class Hash implements Input<HashConfig> {
    private final HashConfig config;
    private final Manager manager;
    
    private boolean hasNext;

    public Hash(final HashConfig config) {
        this.config = config;
        manager = config.getManager();
    }

    @Override
    public void validate() throws InvalidApi {
        if (null == manager) {
            throw new InvalidApi("required manager missing");
        }
    }

    @Override
    public int read(final MappingConfiguration from) throws InputOutputError {
        try {
            hasNext = false;
            final String result = manager.newTask().hash(config.getReceiver().receive());
            return from.partsBySource(getName()).stream()
                    .mapToInt(a -> {
                        a.getDataset().collect(new SimpleDatasetPiece(result));
                        return 1;
                    }).sum();
        } catch (final IOException e) {
            throw new InputOutputError("Could not read from receiver", e);
        }
    }

    @Override
    public boolean hasNext() {
        return hasNext;
    }

    @Override
    public String getName() {
        return config.getName();
    }

    @Override
    public void start() {
        hasNext = true;
    }

    @Override
    public void end() {
        hasNext = false;
    }
}