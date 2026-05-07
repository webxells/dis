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
import com.webxells.dis.config.json.parsing.element.DisonMethodReader;
import com.webxells.dis.config.json.parsing.element.DisonObjectReader;
import com.webxells.dis.config.json.parsing.element.DisonRawReader;
import com.webxells.dis.config.json.parsing.element.DisonStringReader;
import com.webxells.dis.config.json.parsing.intern.DequeStorage;
import com.webxells.dis.logging.LoggerProxyFactory;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

public class TemplateParser implements DisonPlugin {
    public static class Manager implements DisonPluginManager {

        @Override
        public DisonPlugin isCompetent(final String disCommand) {
            if (DIS_COMMAND.equals(disCommand)) {
                return new TemplateParser();
            }
            return null;
        }
    }

    private static final Logger LOGGER = LoggerProxyFactory.logger(TemplateParser.class);
    private static final DequeStorage STORAGE = new DequeStorage();
    public static final String DIS_COMMAND = "--dis-template";


    @Override
    public Optional<DisonElementReader> handle(final DisonElementReader disonElementReader, final DisonJsonTransformer disonJsonTransformer) {
        if (disonElementReader instanceof DisonMethodReader) {
            return handleMethodCall(disonElementReader, disonJsonTransformer);
        }
        if (disonElementReader instanceof DisonObjectReader) {
            STORAGE.addLast(((DisonObjectReader) disonElementReader).getAsMap());
        } else {
            disonJsonTransformer.plugInError("Unexpected dison type: ".concat(disonElementReader.getType().toString()));
        }
        return Optional.empty();
    }

    protected Optional<DisonElementReader> handleMethodCall(final DisonElementReader disonElementReader, final DisonJsonTransformer disonJsonTransformer) {
        final List<DisonElementReader> parameters = ((DisonMethodReader) disonElementReader).getParameters();
        final String first = getFirstString(parameters, disonJsonTransformer);
        if (null == first) {
            return Optional.empty();
        }
        return fetchContent(first, getOptionalVarMapping(parameters, disonJsonTransformer, 1), disonJsonTransformer);
    }

    static String getFirstString(final List<DisonElementReader> parameters, final DisonJsonTransformer disonJsonTransformer) {
        return getString(parameters, disonJsonTransformer, 0);
    }

    static String getString(final List<DisonElementReader> parameters, final DisonJsonTransformer disonJsonTransformer, final int index) {
        if (parameters.size() <= index) {
            disonJsonTransformer.plugInError( "method expects at least" + index + "parameter");
        } else {
            final DisonElementReader reader = parameters.get(index);
            if (reader instanceof DisonStringReader) {
                return ((DisonStringReader) reader).read();
            }
            if (reader instanceof DisonMethodReader) {
                final DisonElementReader methodResult = ((DisonMethodReader) reader).call();
                if (methodResult instanceof DisonStringReader) {
                    return ((DisonStringReader) methodResult).read();
                }
            }
            disonJsonTransformer.plugInError("method expects " + index + " parameter is string or method returning string");
        }
        return null;
    }

    static DisonObjectReader getOptionalVarMapping(final List<DisonElementReader> parameters, final DisonJsonTransformer disonJsonTransformer, final int index) {
        if (index < parameters.size()) {
            final DisonElementReader varMapping = parameters.get(index);
            if (varMapping instanceof DisonObjectReader map) {
                return map;
            }
            disonJsonTransformer.plugInError("include method expects second parameter is mapping object (<string, string>)");
        }
        return null;
    }

    private Optional<DisonElementReader> fetchContent(final String name, final DisonObjectReader varMapping, final DisonJsonTransformer disonJsonTransformer) {
        final String realName = parseName(name, disonJsonTransformer);
        return parse(realName, varMapping, disonJsonTransformer);
    }

    Optional<DisonElementReader> parse(final String realName, final DisonObjectReader varMapping, final DisonJsonTransformer disonJsonTransformer) {
        final VarParser varParser = startTempVarMapping(varMapping, disonJsonTransformer);
        final Optional<DisonElementReader> result = Optional.ofNullable(readContent(realName, disonJsonTransformer))
                .map(a -> disonJsonTransformer.parseForChild(a,  calculateNextPath(realName, disonJsonTransformer), realName))
                .map(a -> disonJsonTransformer.removeIncompatibleElements(a, isPreventMultiArray()).trim())
                .filter(this::noEmptyObject)
                .map(DisonRawReader::new);
        clearTempVarMapping(varParser);
        return result;
    }

    private boolean noEmptyObject(final String content) {
        final AtomicInteger objectConstruct = new AtomicInteger();
        return !(content.chars().allMatch(current -> {
            if ('{' == current) {
                return objectConstruct.compareAndSet(0, 1);
            }
            if ('}' == current) {
                return objectConstruct.compareAndSet(1, 2);
            }
            return Character.isWhitespace(current);
        }) && 2 == objectConstruct.get());
    }


    private void clearTempVarMapping(final VarParser varParser) {
        if (null != varParser) {
            varParser.clearTempValues();
        }
    }

    private VarParser startTempVarMapping(final DisonObjectReader varMapping, final DisonJsonTransformer disonJsonTransformer) {
        if (null != varMapping) {
            final VarParser varParser = (VarParser) disonJsonTransformer.matchAPlugin(VarParser.DIS_COMMAND)
                    .filter(a -> a instanceof VarParser)
                    .orElse(null);
            if (null == varParser) {
                disonJsonTransformer.plugInError("include with mapping object requires VarParser plugin");
                LOGGER.trace("Ignoring provided variables");
            } else {
                varParser.addTempValues(varMapping.getAsMap());
            }
            return varParser;
        }
        return null;
    }

    protected String parseName(final String name, final DisonJsonTransformer disonJsonTransformer) {
        return name;
    }

    protected boolean isPreventMultiArray() {
        return false;
    }

    protected String calculateNextPath(final String name, final DisonJsonTransformer disonJsonTransformer) {
        return disonJsonTransformer.getCurrentPath();
    }

    protected String readContent(final String name, final DisonJsonTransformer disonJsonTransformer) {
        return STORAGE.get(name)
                .map(DisonElementReader::writeJson)
                .orElse(null);
    }

}