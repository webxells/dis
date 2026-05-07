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
import com.webxells.dis.config.json.parsing.element.DisonBooleanReader;
import com.webxells.dis.config.json.parsing.element.DisonMethodReader;
import com.webxells.dis.config.json.parsing.element.DisonObjectReader;
import com.webxells.dis.config.json.parsing.element.DisonPrimitiveReader;
import com.webxells.dis.config.json.parsing.element.DisonStringReader;
import com.webxells.dis.config.json.parsing.intern.DequeStorage;
import com.webxells.dis.plain.output.Echo;
import com.webxells.dis.plain.output.EchoConfig;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;

public class VarParser implements ResilientPlugin {
    public static class Manager implements DisonPluginManager {
        private final VarParser instance = new VarParser();

        @Override
        public DisonPlugin isCompetent(final String disCommand) {
            return switch (disCommand) {
                case DIS_COMMAND -> instance;
                case DIS_COMMAND + "-default" -> new VarParserDefault(instance);
                case DIS_COMMAND + "-delete" -> new VarDelete(instance);
                default -> null;
            };
        }
    }

    public static final String DIS_COMMAND = "--dis-var";

    private final Map<String, DisonElementReader> registry = new HashMap<>();
    private final DequeStorage tempRegistry = new DequeStorage();

    public void addValueToTemp(final String name, final DisonElementReader value) {
        tempRegistry.add(name, value);
    }

    public void addTempValues(final Map<String, DisonElementReader> varMapping) {
        final Map<String, DisonElementReader> result = new HashMap<>();
        resolveMethods(varMapping, result::put);
        tempRegistry.addLast(result);
    }

    public void clearTempValues() {
        tempRegistry.removeLast();
    }

    private void resolveMethods(final Map<String, DisonElementReader> varMapping,
                                                           BiConsumer<String, DisonElementReader> consumer) {
        varMapping.forEach((key, b) -> resolveMethodToString(key, b, consumer));
    }

    private void resolveMethodToString(final String key, DisonElementReader element,
                                       final BiConsumer<String, DisonElementReader> consumer) {
        while (element instanceof DisonMethodReader) {
            element = ((DisonMethodReader) element).call();
        }
        consumer.accept(key, element);
    }

    @Override
    public Optional<DisonElementReader> handle(final DisonElementReader disonElementReader,
                                               final DisonJsonTransformer disonJsonTransformer) {
        switch (disonElementReader.getType()) {
            case OBJECT:
                resolveMethods(((DisonObjectReader) disonElementReader).getAsMap(), registry::put);
                return Optional.empty();
            case METHOD:
                return handleMethodCall(disonElementReader, disonJsonTransformer);
        }
        disonJsonTransformer.plugInError("Unexpected dison type: ".concat(disonElementReader.getType().toString()));
        return Optional.empty();
    }

    public boolean issetVariable(final String variableName) {
        return null != getDirectVariable(variableName, false);
    }

    Optional<DisonElementReader> getVariable(final String variableName) {
        return Optional.ofNullable(getDirectVariable(variableName, false));
    }

    void addValue(final String key, final DisonElementReader value) {
        resolveMethodToString(key, value, registry::put);
    }

    private void saveMapToStringOnly(final Map<String, DisonElementReader> source, final Map<String, String> destination, final DisonElementReader dontParseSelf, final DisonJsonTransformer disonJsonTransformer) {
        source.forEach((key, element) -> getStringResult(element, dontParseSelf)
                .ifPresent(a -> destination.put(key, a)));
    }

    private Optional<String> getStringResult(DisonElementReader element, final DisonElementReader dontParseSelf) {
        while (element instanceof DisonMethodReader method && element != dontParseSelf) {
            element = method.call();
        }
        if (element instanceof DisonPrimitiveReader<?> primitive) {
            return Optional.of(String.valueOf(primitive.read()));
        }
        return Optional.empty();
    }

    private Optional<DisonElementReader> handleMethodCall(final DisonElementReader disonElementReader, final DisonJsonTransformer disonJsonTransformer) {
        final List<DisonElementReader> parameters = ((DisonMethodReader) disonElementReader).getParameters();
        if (parameters.isEmpty()) {
            return disonJsonTransformer.plugInError("var method arguments required");
        }
        final DisonElementReader parameter = parameters.getFirst();
        if (parameter instanceof DisonStringReader stringReader) {
            boolean evaluateNames = getEvaluateNamesValue(parameters, disonJsonTransformer);
            return parse(stringReader.read(), evaluateNames, disonElementReader,  disonJsonTransformer);
        }
        if (parameter instanceof DisonObjectReader objectReader) {
            resolveMethods(objectReader.getAsMap(), registry::put);
            return Optional.empty();
        }
        return disonJsonTransformer.plugInError("var method expects string parameter - found: ".concat(parameter.describeSelf()));
    }

    private boolean getEvaluateNamesValue(final List<DisonElementReader> parameters, final DisonJsonTransformer disonJsonTransformer) {
        if (1 < parameters.size()) {
            final DisonElementReader booleanReader = parameters.get(1);
            if (!(booleanReader instanceof DisonBooleanReader)) {
                disonJsonTransformer.plugInError("var method expects boolean parameter as seconds - found: ".concat(booleanReader.describeSelf()));
            } else {
                return ((DisonBooleanReader) booleanReader).read();
            }
        }
        return false;
    }

    private Optional<DisonElementReader> parse(final String template, final boolean evaluateNames,
                                               final DisonElementReader dontParseSelf, final DisonJsonTransformer disonJsonTransformer) {
        final DisonElementReader directAttempt = getDirectVariable(template, evaluateNames);
        if (null == directAttempt) {
            final String result = parseTemplate(template, dontParseSelf, disonJsonTransformer);
            return evaluateNames ? parse(result, false, dontParseSelf, disonJsonTransformer) :
                    Optional.of(DisonPrimitiveReader.parseString(result));
        }
        return Optional.of(directAttempt);
    }

    private DisonElementReader getDirectVariable(final String template, final boolean evaluateNames) {
        if (evaluateNames) {
            return null;
        }
        final String name = template.indexOf('$') == 0 ? template.substring(1) : template;
        return searchTempRegistryForName(name)
                .orElseGet(() -> registry.get(name));
    }

    private Optional<DisonElementReader> searchTempRegistryForName(final String name) {
        final Iterator<Map<String, DisonElementReader>> mapIterator = tempRegistry.iterator();
        while(mapIterator.hasNext()) {
            final DisonElementReader disonElementReader = mapIterator.next().get(name);
            if (null != disonElementReader) {
                return Optional.of(disonElementReader);
            }
        }
        return Optional.empty();
    }

    private String parseTemplate(final String template, final DisonElementReader dontParseSelf, final DisonJsonTransformer disonJsonTransformer) {
        final Map<String, String> valueStore = buildTempCombinedMap(disonJsonTransformer, dontParseSelf);
        final EchoConfig config = new EchoConfig();
        config.setTemplate(String.format("%s%s", template.contains("$") ? "" : "$", template));
        config.setCharsToEscape(List.of('"'));
        final String result = Echo.parse(config, valueStore);
        return config.getTemplate().equals(result) ? template : result;
    }

    private Map<String, String> buildTempCombinedMap(final DisonJsonTransformer disonJsonTransformer, final DisonElementReader dontParseSelf) {
        final Map<String, String> result = new HashMap<>();
        saveMapToStringOnly(registry, result, dontParseSelf, disonJsonTransformer);
        tempRegistry.forEach(a -> saveMapToStringOnly(a, result, dontParseSelf, disonJsonTransformer));
        return result;
    }

    public void delete(final String key) {
        registry.remove(key);
        tempRegistry.forEach(a -> a.remove(key));
    }
}