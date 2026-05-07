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
package com.webxells.dis.plain.output.parser;

import com.webxells.dis.api.config.MappingConfiguration;
import com.webxells.dis.api.config.MappingPart;
import com.webxells.dis.api.config.MappingPoint;
import com.webxells.dis.plain.MultiMapping;
import com.webxells.dis.plain.output.EchoConfig;
import com.webxells.dis.plain.output.EchoConfig.ValueSource;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class EchoParser {
    private final EchoConfig config;
    private final boolean evaluateVarNames;
    private final boolean hideUnknown;
    private final Map<String, MultiMapping> multiMapping;
    private final ValueSource valueSource;

    private List<MappingPart> parts;
    private String template;
    private Map<String, String> staticValues;

    public EchoParser(final MappingConfiguration mappingConfiguration, final EchoConfig config) {
        this.config = config;
        hideUnknown = config.isHideUnknownVariables();
        multiMapping = config.getMultiMapping();
        valueSource = config.getValueSource();
        evaluateVarNames = config.isEvaluateVarNames();
        parts = mappingConfiguration.parts();
    }

    private EchoParser(final EchoParser parent) {
        this.config = parent.config;
        multiMapping = config.getMultiMapping();
        valueSource = config.getValueSource();
        evaluateVarNames = config.isEvaluateVarNames();
        parts = parent.parts;
        staticValues = parent.staticValues;
        hideUnknown = parent.hideUnknown;
    }

    public void setStaticValues(final Map<String, String> values) {
        staticValues = values;
    }

    private boolean isCorrectMappingPoint(final MappingPoint output) {
        return null != output && null != output.getReference() && output.getReference().equals(config.getName());
    }

    public String parse(final String template) {
        this.template = Objects.requireNonNull(template);
        StringBuilder result = new StringBuilder();
        Part next;
        while(null != (next = getNextPart())) {
            result.append(handlePart(next));
        }
        return result.toString();
    }

    private String handlePart(final Part next) {
        switch (next.getType()) {
            case TEXT:
                return next.getContent();
            case SINGLE_VARIABLE:
                return getSingleVariableContent(getEvaluatedName(next.getContent()));
            case MULTIPLE_VARIABLE:
                return getMultiVariableContent(next.getContent());
        }
        throw new UnsupportedOperationException("Unknown type: ".concat(next.getType().toString()));
    }

    private String getEvaluatedName(final String varName) {
        return evaluateVarNames ? new EchoParser(this).parse(varName) : varName;
    }

    private String escapePlain(final String content) {
        if (null == config.getCharsToEscape()) {
            return content;
        }
        final char escapeChar = config.getEscapeChar();
        final Deque<Character> charsToEscape = new LinkedList<>(config.getCharsToEscape());
        final AtomicReference<String> result = new AtomicReference<>(content);
        charsToEscape.forEach(a -> result.set(result.get().replace(String.valueOf(a), String.format("%s%s",
                escapeChar, a))));
        return result.toString();
    }

    private String getMultiVariableContent(final String variableName) {
        final MultiMapping currentMultiMapping = multiMapping.get(variableName);
        if (null != currentMultiMapping) {
            if (null != staticValues) {
                throw new UnsupportedOperationException("no multi mappings by <string,string> map");
            }
            final String content = MultiMapping.SourceType.MULTI_CONFIGURATION == currentMultiMapping.getSourceType() ?
                    getMultiConfigurationContent(variableName, currentMultiMapping) :
                    getMultiValueContent(variableName, currentMultiMapping);
            if (content != null) return content;
        }
        return name('*', variableName);
    }

    private String name(final char varIndicator, final String variableName) {
        if (hideUnknown) {
            return "";
        }
        return varIndicator + variableName;
    }

    private String getMultiValueContent(final String variableName, final MultiMapping currentMultiMapping) {
        final StringBuilder builder = new StringBuilder();
        getPartsStreamFilteredByVariableName(variableName)
                .flatMap(a -> a.getDataset().getContent().stream())
                .forEach(a -> {
                    if (builder.length() > 0) {
                        builder.append(currentMultiMapping.getDelimiter());
                    }
                    builder.append(a.value().orElse(""));
                });
        if (builder.length() > 0) {
            return String.format("%s%s%s", currentMultiMapping.getBefore(), builder.toString(),
                    currentMultiMapping.getAfter());
        }
        return null;
    }

    private String getMultiConfigurationContent(final String variableName, final MultiMapping currentMultiMapping) {
        final StringBuilder builder = new StringBuilder();
        getPartsStreamFilteredByVariableName(variableName)
                .filter(a -> !a.getSubData().isEmpty())
                .map(MappingPart::getSubData)
                .findAny()
                    .ifPresent(a -> {
                        a.forEach(b -> {
                            builder.append(0 == builder.length() ? currentMultiMapping.getBefore() : currentMultiMapping.getDelimiter());
                            final EchoParser childParser = new EchoParser(b, config);
                            builder.append(childParser.parse(currentMultiMapping.getTemplate()));
                        });
                        builder.append(currentMultiMapping.getAfter());
                    });
        if (builder.length() > 0) {
            return builder.toString();
        }
        return null;
    }

    private String getSingleVariableContent(final String variableName) {
        if (null != staticValues) {
            return Optional
                    .ofNullable(staticValues.get(variableName))
                    .orElseGet(() -> name('$', variableName));
        }
        return getPartsStreamFilteredByVariableName(variableName)
                .flatMap(mappingPart -> mappingPart.value().stream())
                .map(this::escapePlain)
                .findAny()
                    .orElseGet(() -> name('$', variableName));
    }

    private Stream<MappingPart> getPartsStreamFilteredByVariableName(final String variableName) {
        switch (valueSource) {
            case INPUT:
            case OUTPUT:
            case BOTH:
                return getRightStreamFilteredByVariableName(variableName, valueSource);
            case INPUT_OUTPUT:
                return mergedPartStream(variableName, ValueSource.INPUT, ValueSource.OUTPUT);
            case OUTPUT_INPUT:
                return mergedPartStream(variableName, ValueSource.OUTPUT, ValueSource.INPUT);
        }
        throw new UnsupportedOperationException("Invalid value Source: " + valueSource);
    }

    private Stream<MappingPart> mergedPartStream(final String variableName, final ValueSource mainSource,
                                                 final ValueSource fallBack) {
        final List<MappingPart> temp = getRightStreamFilteredByVariableName(variableName, mainSource)
                .collect(Collectors.toList());
        return temp.isEmpty() ? getRightStreamFilteredByVariableName(variableName, fallBack) : temp.stream();
    }

    private Stream<MappingPart> getRightStreamFilteredByVariableName(final String variableName,
                                                                 final ValueSource valueSource) {
        return parts.stream()
                .filter(mappingPart1 -> nameFilter(mappingPart1, valueSource))
                .filter(mappingPart -> pathFilter(mappingPart, variableName, valueSource));
    }

    private boolean pathFilter(final MappingPart mappingPart, final String variableName, final ValueSource valueSource) {
        return  variableName.equals(getPathBySource(mappingPart, valueSource));
    }

    private String getPathBySource(final MappingPart mappingPart, final ValueSource valueSource) {
        switch (valueSource) {
            case INPUT:
                return mappingPart.getInput().getPath();
            case OUTPUT:
                return mappingPart.getOutput().getPath();
            case BOTH:
                return isCorrectMappingPoint(mappingPart.getOutput()) ? mappingPart.getOutput().getPath() :
                        mappingPart.getInput().getPath();
        }
        throw new UnsupportedOperationException("Invalid value Source: " + valueSource);
    }

    private boolean nameFilter(final MappingPart mappingPart, final ValueSource valueSource) {
        switch (valueSource) {
            case INPUT:
                return isCorrectMappingPoint(mappingPart.getInput());
            case OUTPUT:
                return isCorrectMappingPoint(mappingPart.getOutput());
            case BOTH:
                return isCorrectMappingPoint(mappingPart.getOutput()) || isCorrectMappingPoint(mappingPart.getInput());
        }
        throw new UnsupportedOperationException("Invalid value Source: " + valueSource);
    }

    private Part getNextPart() {
        boolean isVariable = false;
        boolean isCurvedVar = false;
        int lastIndex = 0;
        ParserPartType type = ParserPartType.TEXT;
        for (int i = 0, m = template.length(); i < m; i++) {
            final char current = template.charAt(i);
            lastIndex = i;
            if (isVariable && charOutOfSimpleVariableName(current)) {
                if (current == 123) {
                    if (i == 1) {
                        isCurvedVar = true;
                    } else if (isCurvedVar) {
                        throw new SyntaxError("Syntax error! Found open bracket but no ending: ", template);
                    }
                } else {
                    if (isCurvedVar && current == 125) {
                        lastIndex++;
                        isCurvedVar = false;
                    }
                    if (!isCurvedVar) {
                        break;
                    }
                }
            } else if (!isVariable && (42 == current || 36 == current) && notEscaped(i)) {
                if (i > 0) {
                    return createNewText(i);
                }
                isVariable = true;
                type = 42 == current ? ParserPartType.MULTIPLE_VARIABLE : ParserPartType.SINGLE_VARIABLE;
            }
        }
        if (isVariable && isCurvedVar) {
            throw new SyntaxError("Syntax error! Found open bracket but no ending: ", template);
        }
        return createPart(lastIndex, type);
    }

    private boolean charOutOfSimpleVariableName(final char current) {
        return ((current < 48 || current > 57) &&
                    (current < 65 || current > 90) &&
                    (current < 97 || current > 122) &&
                    current != 95 && current != 45) &&
                !(current == 36 && evaluateVarNames);
    }
    private Part createNewText(final int till) {
        final Part text = Objects.requireNonNull(createPart(till, ParserPartType.TEXT));
        return new Part(
                text.getContent().replaceAll("\\\\\\$", "\\$").replace("\\*", "*"),
                ParserPartType.TEXT);
    }

    private Part createPart(int till, final ParserPartType parserPartType) {
        if (till > 0) {
            if (template.length() == till + 1 && !charOutOfSimpleVariableName(template.charAt(till))) {
                till++;
            }
            final String content = getTextTill(till, parserPartType);
            template = template.substring(till);
            return new Part(content, parserPartType);
        } else if (template.length() > 0) {
            final String content = template;
            template = "";
            return new Part(content, ParserPartType.TEXT);
        }
        return null;
    }

    private String getTextTill(final int till, final ParserPartType parserPartType) {
        String content;
        if (ParserPartType.TEXT == parserPartType) {
            content = template.substring(0, till);
        } else {
            content = template.substring(1, till);
            if (content.startsWith("{") && content.endsWith("}")) {
                content = content.substring(1, content.length() -1);
            }
        }
        return content;
    }

    private boolean notEscaped(int current) {
        int escaped = 0;
        while (current > 0 && template.charAt(--current) == '\\') {
            escaped++;
        }
        return escaped % 2 == 0;
    }
}