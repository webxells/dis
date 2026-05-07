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
package com.webxells.dis.hash.intern;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

public class FixedBufferedInputStream {
    private final BufferedInputStream bis;
    private final int size;
    private byte[] buffer;
    private int bufferLength;

    public FixedBufferedInputStream(final InputStream in, final int size) {
        bis = new BufferedInputStream(in, size);
        this.size = size;
    }

    public byte[] read() throws IOException {
        final byte[] result = new byte[size];
        int resultFilled = 0;
        if (null == buffer) {
            fillNext();
        }
        if (-1 == bufferLength) {
            return null;
        }
        do {
            final int length = Math.min(bufferLength, size - resultFilled);
            System.arraycopy(buffer, 0, result, resultFilled, length);
            resultFilled += length;
            if (bufferLength > length) {
                saveUnreadDataForNextCall(length);
            } else {
                fillNext();
            }
        } while (-1 < bufferLength && size > resultFilled);
        return trimLastArray(result, resultFilled);
    }

    private void saveUnreadDataForNextCall(final int length) {
        final int tmpLength = bufferLength - length;
        final byte[] tmp = new byte[tmpLength];
        System.arraycopy(buffer, length, tmp, 0, tmpLength);
        buffer = tmp;
        bufferLength = tmpLength;
    }

    public void close() throws IOException {
        bis.close();
    }

    private byte[] trimLastArray(final byte[] result, final int resultFilled) {
        if (resultFilled < size) {
            final byte[] tmp = new byte[resultFilled];
            System.arraycopy(result, 0, tmp, 0, resultFilled);
            return tmp;
        }
        return result;
    }

    private void fillNext() throws IOException {
        if (-1 < bufferLength) {
            buffer = new byte[size];
            bufferLength = bis.read(buffer);
        }
    }
}