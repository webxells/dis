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
import com.webxells.dis.config.json.parsing.element.DisonLongReader;
import com.webxells.dis.config.json.parsing.element.DisonMethodReader;
import java.util.List;
import java.util.Optional;

public class Random implements DisonPlugin {
    public static class Manager implements DisonPluginManager {
        @Override
        public DisonPlugin isCompetent(final String disCommand) {
            return DIS_COMMAND.equals(disCommand) ? new Random() : null;
        }
    }

    public static final String DIS_COMMAND = "--dis-random";
    private static final long DEFAULT_MAX = 10000000;

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
        final long max = getMaxValue(parameters, disonJsonTransformer);
        return Optional.of(new DisonLongReader(java.lang.Math.round(java.lang.Math.random() * max)));
    }

    private long getMaxValue(final List<DisonElementReader> parameters, final DisonJsonTransformer disonJsonTransformer) {
        if (!parameters.isEmpty()) {
            final DisonElementReader longReader = parameters.get(0);
            if (longReader instanceof DisonLongReader) {
                return  ((DisonLongReader) longReader).read();
            } else {
                disonJsonTransformer.plugInError("var method expects numeric parameter - found:".concat(longReader.describeSelf()));
            }
        }
        return DEFAULT_MAX;
    }

}