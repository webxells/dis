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
package com.webxells.dis.config.json.parsing.plugins.ifoperation;

import com.webxells.dis.config.json.parsing.DisonElementReader;
import com.webxells.dis.config.json.parsing.DisonJsonTransformer;
import com.webxells.dis.config.json.parsing.element.DisonBooleanReader;
import com.webxells.dis.config.json.parsing.element.DisonMethodReader;
import com.webxells.dis.config.json.parsing.element.DisonStringReader;
import com.webxells.dis.config.json.parsing.plugins.DisonPlugin;
import com.webxells.dis.config.json.parsing.plugins.VarParser;
import java.util.List;
import java.util.Optional;

public class Isset implements DisonPlugin {
    @Override
    public Optional<DisonElementReader> handle(final DisonElementReader disonElementReader, final DisonJsonTransformer disonJsonTransformer) {
        if (disonElementReader instanceof DisonMethodReader) {
            final List<DisonElementReader> parameters = ((DisonMethodReader) disonElementReader).getParameters();
            if (parameters.isEmpty()) {
                return disonJsonTransformer.plugInError("parameter required");
            }
            final VarParser varParser = getVarParser(disonJsonTransformer);
            if (null == varParser) {
                return Optional.empty();
            }
            return Optional.of(new DisonBooleanReader(validateParameters(parameters, disonJsonTransformer, varParser)));
        }
        return disonJsonTransformer.plugInError("callable only as method");
    }

    private boolean validateParameters(final List<DisonElementReader> parameters, final DisonJsonTransformer disonJsonTransformer, final VarParser varParser) {
        for (DisonElementReader a : parameters) {
            while (a instanceof DisonMethodReader) {
                a = ((DisonMethodReader) a).call();
            }
            if (!(a instanceof DisonStringReader)) {
                disonJsonTransformer.plugInError("string parameters required");
                disonJsonTransformer.plugInError("skipping");
                continue;
            }
            if (!varParser.issetVariable(((DisonStringReader) a).read())) {
                return false;
            }
        }
        return true;
    }

    private VarParser getVarParser(final DisonJsonTransformer disonJsonTransformer) {
        final Optional<DisonPlugin> varParser = disonJsonTransformer.matchAPlugin(VarParser.DIS_COMMAND);
        if (varParser.isEmpty()) {
            disonJsonTransformer.plugInError("VarParser required by --isset");
            disonJsonTransformer.plugInError("resolving to false");
            return null;
        }
        return (VarParser) varParser.get();
    }
}