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
import com.webxells.dis.config.json.parsing.plugins.DisonPlugin;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

public abstract class BooleanOperation implements DisonPlugin {
    @Override
    public Optional<DisonElementReader> handle(final DisonElementReader disonElementReader, final DisonJsonTransformer disonJsonTransformer) {
        final List<Boolean> parameters = getBooleanParameters(disonElementReader, disonJsonTransformer);
        if (parameters.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(new DisonBooleanReader(validateOperation(parameters, disonJsonTransformer)));
    }

    protected abstract boolean validateOperation(final List<Boolean> parameters, final DisonJsonTransformer disonJsonTransformer);

    private List<Boolean> getBooleanParameters(final DisonElementReader disonElementReader, final DisonJsonTransformer disonJsonTransformer) {
        if (disonElementReader instanceof DisonMethodReader) {
            final List<Boolean> result = new LinkedList<>();
            ((DisonMethodReader) disonElementReader).getParameters().forEach(a -> {
               while (a instanceof DisonMethodReader) {
                   a = ((DisonMethodReader) a).call();
               }
               if (a instanceof DisonBooleanReader) {
                   result.add(((DisonBooleanReader) a).read());
                   return;
               }
               disonJsonTransformer.plugInError("parameter does not resolve to boolean - found: ".concat(a.describeSelf()));
               disonJsonTransformer.plugInError("skipped");
            });
            return result;
        }
        disonJsonTransformer.plugInError("Only as method");
        return List.of();
    }
}