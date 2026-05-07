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

import com.webxells.dis.config.json.parsing.DisonElement;
import com.webxells.dis.config.json.parsing.DisonElementReader;
import com.webxells.dis.config.json.parsing.DisonJsonTransformer;
import com.webxells.dis.config.json.parsing.element.DisonMethodReader;
import com.webxells.dis.config.json.parsing.element.DisonStringReader;
import java.util.List;
import java.util.Optional;

public class Environment implements DisonPlugin {
    public static class Manager implements DisonPluginManager {
        @Override
        public DisonPlugin isCompetent(final String disCommand) {
            return DIS_COMMAND.equals(disCommand) ? new Environment() : null;
        }
    }

    public static final String DIS_COMMAND = "--dis-environment";

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
        return Optional.ofNullable(getParameterName(parameters, disonJsonTransformer))
                .map(a -> new DisonStringReader(
                        Optional.ofNullable(System.getenv(a)).orElse("")));
    }

    private String getParameterName(final List<DisonElementReader> parameters, final DisonJsonTransformer disonJsonTransformer) {
        if (!parameters.isEmpty()) {
            final DisonElementReader elementReader = parameters.getFirst();
            if (elementReader instanceof DisonStringReader stringReader) {
                return stringReader.read();
            } else {
                disonJsonTransformer.plugInError("var method expects string parameter - found:".concat(elementReader.describeSelf()));
            }
        }
        return null;
    }

}