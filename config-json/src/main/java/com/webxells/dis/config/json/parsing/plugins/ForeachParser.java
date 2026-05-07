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

import com.webxells.dis.config.json.parsing.DisonElementReader;
import com.webxells.dis.config.json.parsing.DisonJsonTransformer;
import com.webxells.dis.config.json.parsing.element.DisonArrayReader;
import com.webxells.dis.config.json.parsing.element.DisonMethodReader;
import com.webxells.dis.config.json.parsing.element.DisonPrimitiveReader;
import com.webxells.dis.config.json.parsing.element.DisonRawReader;
import com.webxells.dis.plain.output.Echo;
import com.webxells.dis.plain.output.EchoConfig;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ForeachParser implements DisonPlugin {
    public static class Manager implements DisonPluginManager {

        @Override
        public DisonPlugin isCompetent(final String disCommand) {
            if ("--dis-foreach".equals(disCommand)) {
                return new ForeachParser();
            }
            if ("--dis-foreach-template".equals(disCommand)) {
                return new ForeachTemplateParser();
            }
            return null;
        }
    }

    public static class ForeachDisonReader {
        private final StringBuilder result = new StringBuilder();

        public void add(final String elementReader) {
            if (0 < result.length()) {
                result.append(",");
            }
            result.append(elementReader);
        }

        public Optional<DisonElementReader> output() {
            return Optional.of(new DisonRawReader(result.toString()));
        }

        public void add(final DisonElementReader element) {
            add(element instanceof DisonPrimitiveReader<?> p ? String.valueOf(p.read()) : element.writeJson());
        }
    }

    @Override
    public Optional<DisonElementReader> handle(final DisonElementReader disonElementReader, final DisonJsonTransformer disonJsonTransformer) {
        if (disonElementReader instanceof DisonMethodReader method) {
            return handleMethodCall(method, disonJsonTransformer);
        }
        disonJsonTransformer.plugInError("Unexpected dison type: ".concat(disonElementReader.getType().toString()));
        return Optional.empty();
    }

    private Optional<DisonElementReader> handleMethodCall(final DisonMethodReader disonMethodReader, final DisonJsonTransformer disonJsonTransformer) {
        final List<DisonElementReader> parameters = disonMethodReader.getParameters();
        final List<DisonElementReader> array = getFirstArray(parameters, disonJsonTransformer);
        final String template = TemplateParser.getString(parameters, disonJsonTransformer, 1);
        if (null == array || array.isEmpty() || null == template || template.isBlank()) {
            return Optional.empty();
        }
        return parseForEach(array, template, parameters, disonJsonTransformer);
    }

    private List<DisonElementReader> getFirstArray(final List<DisonElementReader> parameters, final DisonJsonTransformer disonJsonTransformer) {
        if (parameters.isEmpty()) {
            disonJsonTransformer.plugInError( "method expects at least one parameter");
        } else {
            DisonElementReader reader = parameters.getFirst();
            while (reader instanceof DisonMethodReader method) {
                reader = method.call();
            }
            if (reader instanceof DisonArrayReader array) {
                return array.getContent();
            }
            disonJsonTransformer.plugInError("method expects first parameter is string or method returning string");
        }
        return null;
    }

    protected Optional<DisonElementReader> parseForEach(final List<DisonElementReader> array, final String template,
                                                        final List<DisonElementReader> parameters, final DisonJsonTransformer disonJsonTransformer) {
        final ForeachDisonReader result = new ForeachDisonReader();
        for (final DisonElementReader current : array) {
            final EchoConfig config = new EchoConfig();
            config.setTemplate(template.replaceAll("\\\\\"", "\""));
            config.setCharsToEscape(List.of('"'));
            result.add(Echo.parse(config, Map.of("current",
                    current instanceof DisonPrimitiveReader<?> p ? String.valueOf(p.read()) : current.writeJson())));
        }
        return result.output();
    }
}