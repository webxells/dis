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

import com.webxells.dis.api.Logger;
import com.webxells.dis.config.json.parsing.DisonElementReader;
import com.webxells.dis.config.json.parsing.DisonJsonTransformer;
import com.webxells.dis.config.json.parsing.element.DisonBooleanReader;
import com.webxells.dis.config.json.parsing.element.DisonMethodReader;
import com.webxells.dis.config.json.parsing.element.DisonObjectReader;
import com.webxells.dis.config.json.parsing.element.DisonRawReader;
import com.webxells.dis.config.json.parsing.plugins.ifoperation.And;
import com.webxells.dis.config.json.parsing.plugins.ifoperation.Empty;
import com.webxells.dis.config.json.parsing.plugins.ifoperation.Equals;
import com.webxells.dis.config.json.parsing.plugins.ifoperation.Isset;
import com.webxells.dis.config.json.parsing.plugins.ifoperation.Not;
import com.webxells.dis.config.json.parsing.plugins.ifoperation.Or;
import com.webxells.dis.logging.LoggerProxyFactory;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class If implements DisonPlugin {
    public static class Manager implements DisonPluginManager {
        @Override
        public DisonPlugin isCompetent(final String disCommand) {
            return switch (disCommand) {
                case "--dis-if" -> new If();
                case "--and" -> new And();
                case "--or" -> new Or();
                case "--not" -> new Not();
                case "--isset" -> new Isset();
                case "--equals" -> new Equals();
                case "--empty" -> new Empty();
                default -> null;
            };
        }
    }

    private static final Logger LOGGER = LoggerProxyFactory.logger(If.class);

    @Override
    public Optional<DisonElementReader> handle(final DisonElementReader disonElementReader, final DisonJsonTransformer disonJsonTransformer) {
        if (disonElementReader instanceof DisonObjectReader) {
            return parseObject(((DisonObjectReader) disonElementReader).getAsMap(), disonJsonTransformer);
        }
        if (disonElementReader instanceof DisonMethodReader) {
            final List<DisonElementReader> parameters = ((DisonMethodReader) disonElementReader).getParameters();
            if (1 == parameters.size() && parameters.get(0) instanceof DisonObjectReader) {
                return parseObject(((DisonObjectReader) parameters.get(0)).getAsMap(), disonJsonTransformer);
            }
        }
        disonJsonTransformer.plugInError("Unexpected dison type or malformed method: ".concat(disonElementReader.getType().toString()));
        return Optional.empty();
    }

    private Optional<DisonElementReader> parseObject(final Map<String, DisonElementReader> parameters, final DisonJsonTransformer disonJsonTransformer) {
        if (parameters.containsKey("if") && (parameters.containsKey("then") || parameters.containsKey("else"))) {
            final Boolean result = validates(parameters.get("if"), disonJsonTransformer);
            if (null != result) {
                return  result ? parse(parameters.get("then"), disonJsonTransformer) :parse(parameters.get("else"), disonJsonTransformer);
            }
        } else {
            disonJsonTransformer.plugInError("if then/else are mandatory");
        }
        return Optional.empty();
    }

    private Boolean validates(final DisonElementReader anIf, final DisonJsonTransformer disonJsonTransformer) {
        if (anIf instanceof DisonMethodReader) {
            final DisonElementReader result = ((DisonMethodReader) anIf).call();
            if (result instanceof DisonBooleanReader) {
                return ((DisonBooleanReader) result).read();
            }
            disonJsonTransformer.plugInError("result of if statement is no boolean - found: ".concat(result.describeSelf()));
            return null;
        }
        disonJsonTransformer.plugInError("Method for if statement required");
        return null;
    }

    private Optional<DisonElementReader> parse(final DisonElementReader result,
                                               final DisonJsonTransformer disonJsonTransformer) {
        return Optional.ofNullable(result)
                .map(a -> disonJsonTransformer.removeIncompatibleElements(a,false));
    }

}