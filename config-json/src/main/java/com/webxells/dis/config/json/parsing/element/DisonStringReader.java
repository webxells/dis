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
package com.webxells.dis.config.json.parsing.element;

import com.google.gson.stream.JsonWriter;
import com.webxells.dis.config.json.parsing.DisonElement;
import java.io.IOException;
import java.io.StringWriter;

public class DisonStringReader extends DisonPrimitiveReader<String> {

    public static String escape(final String content) {
        try {
            try (final StringWriter stringWriter = new StringWriter();
                 final JsonWriter jsonWriter = new JsonWriter(stringWriter)) {
                jsonWriter.value(content);
                return stringWriter.toString();
            }
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    public DisonStringReader(final String content) {
        super(content);
    }

    @Override
    public DisonElement getType() {
        return DisonElement.STRING;
    }

    @Override
    public String writeJson() {
        return String.format("\"%s\"", content);
    }
}