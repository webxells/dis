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

import com.webxells.dis.server.rest.http.DisResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.List;

public class BodyStreamBuffer extends OutputStream {
    private final List<Byte> buffer = new LinkedList<>();
    private final OutputStream stream;
    private final DisResponse response;

    public BodyStreamBuffer(final DisResponse response, final OutputStream responseBody) {
        this.response = response;
        this.stream = responseBody;
    }

    @Override
    public void write(final int b) {
        buffer.add((byte) b);
    }

    public void writeToBody() throws IOException {
        for (final byte b : buffer) {
            stream.write(b);
        }
    }

    public long getLength() {
        return buffer.size();
    }

    @Override
    public void close() throws IOException {
        response.send(200);
        stream.close();
    }
}