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
package com.webxells.dis.config.json.parsing.plugins;

import com.google.gson.stream.JsonWriter;
import com.webxells.dis.config.json.parsing.DisonElement;
import com.webxells.dis.config.json.parsing.DisonElementReader;
import com.webxells.dis.config.json.parsing.DisonJsonTransformer;
import com.webxells.dis.config.json.parsing.element.DisonMethodReader;
import com.webxells.dis.config.json.parsing.element.DisonRawReader;
import com.webxells.dis.config.json.parsing.element.DisonStringReader;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.Files;
import java.util.List;
import java.util.Optional;

public class LocalFile implements DisonPlugin {
    public static class Manager implements DisonPluginManager {
        @Override
        public DisonPlugin isCompetent(final String disCommand) {
            return DIS_COMMAND.equals(disCommand) ? new LocalFile() : null;
        }
    }

    public static final String DIS_COMMAND = "--dis-local-file";

    @Override
    public Optional<DisonElementReader> handle(final DisonElementReader disonElementReader,
                                               final DisonJsonTransformer disonJsonTransformer) {
        if (DisonElement.METHOD == disonElementReader.getType()) {
            return handleMethodCall((DisonMethodReader) disonElementReader, disonJsonTransformer);
        }
        disonJsonTransformer.plugInError("Unexpected dison type: ".concat(disonElementReader.describeSelf()));
        return Optional.empty();
    }

    private Optional<DisonElementReader> handleMethodCall(final DisonMethodReader disonElementReader, final DisonJsonTransformer disonJsonTransformer) {
        final List<DisonElementReader> parameters = disonElementReader.getParameters();
        final File file = getValidFile(parameters, disonJsonTransformer);
        if (null != file) {
            try {

                return Optional.of(new DisonRawReader(escape(new String(Files.readAllBytes(file.toPath())))));
            } catch (final IOException e) {
                disonJsonTransformer.plugInError(String.format("file could not be read: %s%nerror message%s",
                        file.getPath(), e.getMessage()));
            }
        }
        return Optional.empty();
    }

    private String escape(final String value) {
        try {
            try (final StringWriter stringWriter = new StringWriter();
                 final JsonWriter jsonWriter = new JsonWriter(stringWriter)) {
                jsonWriter.value(value);
                return stringWriter.toString();
            }
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    private File getValidFile(final List<DisonElementReader> parameters, final DisonJsonTransformer disonJsonTransformer) {
        if (!parameters.isEmpty()) {
            final DisonElementReader elementReader = parameters.getFirst();
            if (elementReader instanceof DisonStringReader stringReader) {
                final File file = new File(stringReader.read());
                if (file.exists() && file.canRead()) {
                    return file.getAbsoluteFile();
                }
                disonJsonTransformer.plugInError("file does not exist or not readable: ".concat(file.getPath()));
            } else {
                disonJsonTransformer.plugInError("var method expects string parameter - found:".concat(elementReader.describeSelf()));
            }
        }
        return null;
    }

}