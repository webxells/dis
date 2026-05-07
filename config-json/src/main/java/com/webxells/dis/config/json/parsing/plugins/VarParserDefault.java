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
import com.webxells.dis.config.json.parsing.element.DisonObjectReader;
import com.webxells.dis.config.json.parsing.element.DisonPrimitiveReader;
import com.webxells.dis.config.json.parsing.element.DisonStringReader;
import java.util.List;
import java.util.Optional;

public class VarParserDefault implements ResilientPlugin {
    private final VarParser varParser;

    public VarParserDefault(final VarParser varParser) {
        this.varParser = varParser;
    }

    @Override
    public Optional<DisonElementReader> handle(final DisonElementReader disonElementReader,
                                               final DisonJsonTransformer disonJsonTransformer) {
        if (disonElementReader.getType() == DisonElement.METHOD) {
            return handleMethodCall((DisonMethodReader) disonElementReader, disonJsonTransformer);
        }
        if (disonElementReader.getType() == DisonElement.OBJECT) {
            return handleObjectCall((DisonObjectReader) disonElementReader, disonJsonTransformer);
        }
        return disonJsonTransformer.plugInError("Unexpected dison type: ".concat(disonElementReader.getType().toString()));
    }

    private Optional<DisonElementReader> handleObjectCall(final DisonObjectReader disonElementReader, final DisonJsonTransformer disonJsonTransformer) {
        disonElementReader.getAsMap().forEach((a, b) -> {
            if (varParser.getVariable(a).isEmpty()) {
                varParser.addValueToTemp(a, b);
            }
        });
        return Optional.empty();
    }

    private Optional<DisonElementReader> handleMethodCall(final DisonMethodReader disonElementReader, final DisonJsonTransformer disonJsonTransformer) {
        final List<DisonElementReader> parameters = disonElementReader.getParameters();
        if (2 == parameters.size()) {
            final String variableName = getStringParameterValue(parameters.get(0));
            final DisonElementReader defaultValue = parameters.get(1);
            if (!(null == variableName || null == defaultValue)) {
                return Optional.of(varParser.getVariable(
                        0 == variableName.indexOf('$') && 1 < variableName.length() ?
                                variableName.substring(1) : variableName)
                                    .orElse(defaultValue));
            }
        }
        return disonJsonTransformer.plugInError("var-default requires two to-string-resolving parameters (variableName, defaultValue)");
    }

    private String getStringParameterValue(DisonElementReader parameter) {
        final DisonPrimitiveReader<?> primitiveParameter = getPrimitiveParameterValue(parameter);
        if (primitiveParameter instanceof DisonStringReader stringReader) {
            return stringReader.read();
        }
        return null;
    }

    private DisonPrimitiveReader<?> getPrimitiveParameterValue(DisonElementReader parameter) {
        while (parameter instanceof DisonMethodReader) {
            parameter = ((DisonMethodReader) parameter).call();
        }
        if (parameter instanceof DisonPrimitiveReader<?> primitiveReader) {
            return primitiveReader;
        }
        return null;
    }

}