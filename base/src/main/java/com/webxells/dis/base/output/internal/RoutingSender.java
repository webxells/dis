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
package com.webxells.dis.base.output.internal;

import com.webxells.dis.api.error.InputOutputError;
import com.webxells.dis.api.resource.Resource;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class RoutingSender implements Resource {
    public class RoutingStream extends OutputStream {
        @Override
        public void write(final int b) throws IOException {
            if (shouldWrite()) {
                current.write(b);
            }
        }

        @Override
        public void flush() throws IOException {
            if (shouldWrite()) {
                current.flush();
            }
        }

        @Override
        public void write( final byte[] b) throws IOException {
            if (shouldWrite()) {
                current.write(b);
            }
        }

        @Override
        public void write(final byte[] b, final int off, final int len) throws IOException {
            if (shouldWrite()) {
                current.write(b, off, len);
            }
        }

    }

    private OutputStream current;
    private final boolean lenient;

    public RoutingSender(final boolean lenient) {
        this.lenient = lenient;
    }

    public void setCurrent(final OutputStream current) {
        this.current = current;
    }

    public void reset() {
        current = null;
    }

    @Override
    public OutputStream send() throws InputOutputError {
        return new RoutingStream();
    }

    @Override
    public InputStream receive() throws InputOutputError {
        throw new UnsupportedOperationException("Routing receive makes no sense");
    }

    @Override
    public String getType() {
        return RoutingSender.class.getName();
    }

    private boolean shouldWrite() throws IOException {
        if (lenient && null == current) {
            return false;
        }
        assertValidConfig();
        return true;
    }

    private void assertValidConfig() throws IOException {
        if (null == current) {
            throw new IOException("No current OutputStream for routing");
        }
    }
}