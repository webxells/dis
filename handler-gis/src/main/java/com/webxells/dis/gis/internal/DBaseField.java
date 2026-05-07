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

import com.webxells.dis.gis.internal.BufferedByteBuffer.BufferReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

public class DBaseField {
    interface ValueParsingFunction {
        Optional<String> parseFromBuffer(BufferReader bufferReader) throws IOException;
    }

    private static final DateTimeFormatter DBASE_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final String name;
    private final int length;
    private final ValueParsingFunction valueParser;
    private final Charset charset;
    private final DateTimeFormatter dateFormatter;

    public DBaseField(final byte firstByte, final BufferReader bufferReader,
                      final DBaseFileReader dBaseFileReader) throws IOException {
        charset = dBaseFileReader.getCharset();
        dateFormatter = dBaseFileReader.getDateFormatter();
        name = parseName(firstByte, bufferReader, charset);
        valueParser = mapValueParsing((char) bufferReader.getByte());
        bufferReader.skip(4); //skip data address
        length = Optional.of(bufferReader.getByte())
                    .map(a -> 0 > a ? a + 256 : a)
                    .get();
        bufferReader.skip(15); //skip unused bytes
    }

    public String getName() {
        return name;
    }

    public Optional<String> parseValue(final BufferReader buffer) throws IOException {
        return valueParser.parseFromBuffer(buffer);
    }

    private ValueParsingFunction mapValueParsing(final char type) {
        return switch (Character.toLowerCase(type)) {
            case 'c', 'f', 'n' -> a -> Optional.of(a.getString(length, charset).trim());
            case 'l' -> this::parseBoolean;
            case 'd' -> this::parseDate;
            default -> a -> {
                throw new IllegalStateException("Unknown dBase III type: " + type);
            };
        };
    }

    private Optional<String> parseDate(final BufferReader bufferReader) throws IOException {
        return Optional.of(dateFormatter.format(DBASE_DATE_FORMAT.parse(bufferReader.getString(8, charset))));
    }

    private Optional<String> parseBoolean(final BufferReader bufferReader) throws IOException {
        final char value = (char) bufferReader.getByte();
        return Optional.ofNullable(switch (Character.toLowerCase(value)) {
            case 'y', 't' -> "true";
            case 'n', 'f' -> "false";
            default -> null;
        });
    }

    private String parseName(final byte firstByte, final BufferReader bufferReader,
                             final Charset charset) throws IOException {
        final byte[] nameByte = new byte[11];
        nameByte[0] = firstByte;
        int i = 1;
        for (final byte aByte : bufferReader.getBytes(10)) {
            nameByte[i++] = aByte;
        }
        return new String(nameByte, charset).trim();
    }
}