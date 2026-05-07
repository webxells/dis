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
package com.webxells.dis.rest.cache.intern;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;

public class RemnantReaderInputStream extends InputStream {
    private final BufferedReader reader;

    public RemnantReaderInputStream(final BufferedReader reader) {
        this.reader = reader;
    }

    @Override
    public int read() throws IOException {
        return reader.read();
    }

    @Override
    public void close() throws IOException {
        reader.close();
    }

    @Override
    public void reset() throws IOException {
        reader.reset();
    }
}