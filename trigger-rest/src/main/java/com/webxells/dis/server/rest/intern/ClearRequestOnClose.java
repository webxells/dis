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
package com.webxells.dis.server.rest.intern;

import com.webxells.dis.api.rest.Request;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.function.Function;

public class ClearRequestOnClose extends InputStream {
    private final Request request;
    private final InputStream source;

    public ClearRequestOnClose(final Request request, final Function<Request, InputStream> function) {
        this.request = request;
        source = function.apply(request);
    }

    @Override
    public int read() throws IOException {
        return source.read();
    }

    @Override
    public void close() throws IOException {
        source.close();
        request.clear();
    }

    @Override
    public int read(final byte[] b) throws IOException {
        return source.read(b);
    }

    @Override
    public int read(final byte[] b, final int off, final int len) throws IOException {
        return source.read(b, off, len);
    }

    @Override
    public byte[] readAllBytes() throws IOException {
        return source.readAllBytes();
    }

    @Override
    public byte[] readNBytes(final int len) throws IOException {
        return source.readNBytes(len);
    }

    @Override
    public int readNBytes(final byte[] b, final int off, final int len) throws IOException {
        return source.readNBytes(b, off, len);
    }

    @Override
    public long skip(final long n) throws IOException {
        return source.skip(n);
    }

    @Override
    public int available() throws IOException {
        return source.available();
    }

    @Override
    public synchronized void mark(final int readlimit) {
        source.mark(readlimit);
    }

    @Override
    public synchronized void reset() throws IOException {
        source.reset();
    }

    @Override
    public boolean markSupported() {
        return source.markSupported();
    }

    @Override
    public long transferTo(final OutputStream out) throws IOException {
        return source.transferTo(out);
    }
}