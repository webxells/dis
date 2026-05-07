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
import com.webxells.dis.config.json.parsing.element.DisonObjectReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ForeachTemplateParser extends ForeachParser {

    @Override
    protected Optional<DisonElementReader> parseForEach(final List<DisonElementReader> array, final String template,
                                                        final List<DisonElementReader> parameters, final DisonJsonTransformer disonJsonTransformer) {
        final ForeachDisonReader result = new ForeachDisonReader();
        final TemplateParser parser = (TemplateParser) disonJsonTransformer.matchAPlugin(TemplateParser.DIS_COMMAND)
                .filter(a -> a instanceof TemplateParser)
                .orElse(null);
        if (null == parser) {
            disonJsonTransformer.plugInError("template plugin required");
            return Optional.empty();
        }
        final Map<String, DisonElementReader> varMapping = getOptionalVarMapping(parameters, disonJsonTransformer);
        for (final DisonElementReader current : array) {
            parser.parse(template, new DisonObjectReader(createMapping(varMapping, current), disonJsonTransformer), disonJsonTransformer)
                    .ifPresent(result::add);
        }
        return result.output();
    }

    private Map<String, DisonElementReader> getOptionalVarMapping(final List<DisonElementReader> parameters, final DisonJsonTransformer disonJsonTransformer) {
        return Optional.ofNullable(TemplateParser.getOptionalVarMapping(parameters, disonJsonTransformer, 2))
                        .map(disonObjectReader -> new HashMap<>(disonObjectReader.getAsMap()))
                        .orElseGet(HashMap::new);
    }


    private Map<String, DisonElementReader> createMapping(final Map<String, DisonElementReader> varMapping, final DisonElementReader current) {
        varMapping.put("current", current);
        return varMapping;
    }

}