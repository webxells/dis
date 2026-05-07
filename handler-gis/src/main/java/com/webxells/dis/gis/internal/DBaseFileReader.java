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

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class DBaseFileReader {
// https://www.independent-software.com/dbase-dbf-dbt-file-format.html

    private final DateTimeFormatter dateFormatter;
    private final BufferedByteBuffer buffer;
    private final List<DBaseField> fields;
    private final Charset charset;

    public DBaseFileReader(final Path file, final String dateFormat,
                           final Charset charset) throws IOException {
        this.charset = charset;
        if (!Files.exists(file)) {
            throw new IOException("Cannot find file: " + file);
        }
        dateFormatter = DateTimeFormatter.ofPattern(dateFormat);
        buffer = new BufferedByteBuffer(Files.newInputStream(file));
        final BufferedByteBuffer.BufferReader bufferReader = buffer.littleEndian();

        assertVersion(bufferReader.getByte());

        bufferReader.skip(7); //skip last update date and rowsCount

        final short headerSize = bufferReader.getShort();

        bufferReader.skip(22); //skip rowSize and reserved dBase bytes

        fields = parseFields(bufferReader);
        if (bufferReader.position() < headerSize) {
            bufferReader.skip(headerSize - bufferReader.position());
        }
    }

    public Optional<Map<String, String>> read() throws IOException {
        final BufferedByteBuffer.BufferReader bufferReader = buffer.littleEndian();
        final byte deletion = bufferReader.getByte();
        if (0x2a == deletion) {
            return Optional.empty();
        }
        final Map<String, String> result = new HashMap<>();
        for (final DBaseField current : fields) {
            current.parseValue(bufferReader)
                            .ifPresent(a -> result.put(current.getName(), a));
        }
        return Optional.of(result);
    }

    void close() throws IOException {
        buffer.close();
    }

    Charset getCharset() {
        return charset;
    }

    DateTimeFormatter getDateFormatter() {
        return dateFormatter;
    }

    private List<DBaseField> parseFields(final BufferedByteBuffer.BufferReader bufferReader) throws IOException {
        final  List<DBaseField> result = new ArrayList<>();
        byte firstByte;
        while (0x0d != (firstByte = bufferReader.getByte())) {
            result.add(new DBaseField(firstByte, bufferReader, this));
        }
        return result;
    }

    private void assertVersion(final byte version) throws IOException {
        if (0x03 != version) {
            throw new IOException("Invalid dbf file version: " + version);
        }
    }
}