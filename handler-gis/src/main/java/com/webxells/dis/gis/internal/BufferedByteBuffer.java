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
package com.webxells.dis.gis.internal;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;

public class BufferedByteBuffer {
    class LittleEndian extends BufferReader {
        @Override
        void setOrder() {
            buffer.order(ByteOrder.LITTLE_ENDIAN);
        }
    }

    class BigEndian extends BufferReader {
        @Override
        void setOrder() {
            buffer.order(ByteOrder.BIG_ENDIAN);
        }
    }

    public abstract class BufferReader {

        abstract void setOrder();

        private void setEndian() {
            if (null != buffer) {
                setOrder();
            }
        }

        public int getInt() throws IOException {
            assertNotEmpty(4);
            setEndian();
            return buffer.getInt();
        }

        public short getShort() throws IOException {
            assertNotEmpty(2);
            setEndian();
            return buffer.getShort();
        }

        public byte getByte() throws IOException {
            assertNotEmpty(1);
            setEndian();
            return buffer.get();
        }

        public byte[] getBytes(final int size) throws IOException {
            assertNotEmpty(size);
            setEndian();
            final byte[] result = new byte[size];
            buffer.get(result);
            return result;
        }

        public void skip(final int steps) throws IOException {
            assertNotEmpty(steps);
            setEndian();
            buffer.position(buffer.position() + steps);
        }

        public double getDouble() throws IOException {
            assertNotEmpty(8);
            setEndian();
            return buffer.getDouble();
        }

        public int position() {
            return null == buffer ? position : buffer.position() + position;
        }

        public String getString(final int length, final Charset charset) throws IOException {
            return new String(getBytes(length), charset);
        }
    }

    private static final int BUFFER_SIZE = 8192;

    private final BufferedInputStream file;
    private final int bufferSize;

    private ByteBuffer buffer;
    private ByteBuffer buffer2;
    private volatile int position;
    private boolean endReached;

    public BufferedByteBuffer(final InputStream inputStream) {
        this(inputStream, BUFFER_SIZE);
    }

    public BufferedByteBuffer(final InputStream inputStream, final int bufferSize) {
        if (10 > bufferSize) {
            throw new IllegalArgumentException("bufferSize must be more than 10");
        }
        file = new BufferedInputStream( inputStream, bufferSize * 2);
        this.bufferSize = bufferSize;
    }

    public int minRemaining() throws IOException {
        assertNotEmpty();
        return (hasRemaining(buffer) ? buffer.remaining() : 0) + (hasRemaining(buffer2) ? buffer2.remaining() : 0);
    }

    public BufferReader bigEndian() {
        return new BigEndian();
    }

    public BufferReader littleEndian() {
        return new LittleEndian();
    }

    public void close() throws IOException {
        file.close();
        if (null != buffer) {
            buffer.clear();
            buffer = null;
        }
        if (null != buffer2) {
            buffer2.clear();
            buffer2 = null;
        }
    }

    private void assertNotEmpty() throws IOException {
        assertNotEmpty(0);
    }

    private synchronized void assertNotEmpty(final int minSize) throws IOException {
        if (!hasRemaining(buffer) && hasRemaining(buffer2)) {
            position+= buffer.position();
            buffer = buffer2;
            buffer2 = null;
        }
        buffer = assertBufferNotEmpty(buffer);
        buffer2 = assertBufferNotEmpty(buffer2);
        if (0 < minSize && !endReached) {
            final int bufferLeft = buffer.remaining();
            final int buffer2Missing = minSize - bufferLeft;
            if (0 < buffer2Missing && bufferLeft < minSize && hasRemaining(buffer2) && buffer2.remaining() >= buffer2Missing) {
                final byte[] missing =  new byte[buffer2Missing];
                buffer2.get(missing);
                final ByteBuffer newBuffer = ByteBuffer.allocate(bufferLeft + buffer2Missing);
                newBuffer.put(buffer);
                newBuffer.put(missing);
                newBuffer.rewind();
                position+= buffer.position() - buffer2Missing;
                buffer = newBuffer;
            }
        }
    }

    private boolean hasRemaining(final ByteBuffer buffer) {
        return null != buffer && buffer.hasRemaining();
    }

    private ByteBuffer assertBufferNotEmpty(final ByteBuffer buffer) throws IOException {
        if (!endReached) {
            if (!hasRemaining(buffer)) {
                return createBuffer();
            }
            return buffer;
        }
        return null == buffer || 1 > buffer.remaining() ? null : buffer;
    }

    private ByteBuffer createBuffer() throws IOException {
        final byte[] buffer = new byte[bufferSize];
        final int read = file.read(buffer);
        if (read == -1) {
            endReached = true;
            return null;
        }
        return ByteBuffer.wrap(buffer, 0, read);
    }
}