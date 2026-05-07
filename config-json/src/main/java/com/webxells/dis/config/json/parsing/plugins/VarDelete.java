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
import com.webxells.dis.config.json.parsing.element.DisonArrayReader;
import com.webxells.dis.config.json.parsing.element.DisonMethodReader;
import com.webxells.dis.config.json.parsing.element.DisonPrimitiveReader;
import com.webxells.dis.config.json.parsing.element.DisonStringReader;
import java.util.List;
import java.util.Optional;

public class VarDelete implements DisonPlugin {
    private final VarParser varParser;

    public VarDelete(final VarParser varParser) {
        this.varParser = varParser;
    }

    @Override
    public Optional<DisonElementReader> handle(final DisonElementReader disonElementReader,
                                               final DisonJsonTransformer disonJsonTransformer) {
        if (disonElementReader instanceof DisonMethodReader methodReader) {
            final List<DisonElementReader> parameters = methodReader.getParameters();
            if (parameters.size() == 1) {
                final DisonElementReader parameter = parameters.getFirst();
                if (parameter instanceof DisonStringReader stringParameter) {
                    return handleSingleCall(stringParameter.read(), disonJsonTransformer);
                }
                if (parameter instanceof DisonArrayReader arrayParameter) {
                    return handleMultiCall(arrayParameter, disonJsonTransformer);
                }
                return disonJsonTransformer.plugInError("Unexpected dison type: ".concat(parameter.describeSelf()));
            }
            return disonJsonTransformer.plugInError("Too much parameters");
        }
        return disonJsonTransformer.plugInError("Unexpected dison type: ".concat(disonElementReader.getType().toString()));
    }

    private Optional<DisonElementReader> handleMultiCall(final DisonArrayReader disonElementReader, final DisonJsonTransformer disonJsonTransformer) {
        for (final DisonElementReader child : disonElementReader.getContent()) {
            if (child instanceof DisonPrimitiveReader<?>) {
                handleSingleCall(String.valueOf(((DisonPrimitiveReader<?>) child).read()), disonJsonTransformer);
            } else {
                disonJsonTransformer.plugInError("array of String is required - got: ".concat(child.describeSelf()));
            }
        }
        return Optional.empty();
    }

    private Optional<DisonElementReader> handleSingleCall(final String key, final DisonJsonTransformer disonJsonTransformer) {
        varParser.delete(key);
        return Optional.empty();
    }

}