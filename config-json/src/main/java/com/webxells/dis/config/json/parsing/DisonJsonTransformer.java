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
package com.webxells.dis.config.json.parsing;

import com.webxells.dis.api.Logger;
import com.webxells.dis.config.json.intern.SourceMapping;
import com.webxells.dis.config.json.parsing.element.DisonArrayReader;
import com.webxells.dis.config.json.parsing.element.DisonObjectReader;
import com.webxells.dis.config.json.parsing.element.DisonRawReader;
import com.webxells.dis.config.json.parsing.element.DisonStringReader;
import com.webxells.dis.config.json.parsing.intern.DisonReader;
import com.webxells.dis.config.json.parsing.intern.PluginFactory;
import com.webxells.dis.config.json.parsing.plugins.DisonPlugin;
import com.webxells.dis.logging.LoggerProxyFactory;
import java.io.File;
import java.util.Deque;
import java.util.LinkedList;
import java.util.Optional;
import java.util.Set;

public class DisonJsonTransformer {
    record Settings(Set<DisonErrorStrategy> errorStrategy, String currentPath,
                            PluginFactory pluginFactory, boolean mapConfiguration, String mappingRoot) {}

    public enum JsonElementStage {
        OBJECT, ARRAY, ASSIGNMENT
    }

    private static final Logger LOGGER = LoggerProxyFactory.logger(DisonJsonTransformer.class);
    private static final String DEFAULT_NAME = "[root]";

    private final Set<DisonErrorStrategy> errorStrategy;
    private final StringBuilder result = new StringBuilder();
    private final StringBuilder varBuffer = new StringBuilder();
    private final Deque<JsonElementStage> currentStages = new LinkedList<>();
    private final Deque<String> objectMappings = new LinkedList<>();
    private final String currentPath;
    private final PluginFactory pluginFactory;
    private final boolean mapConfiguration;

    private boolean inVar;
    private boolean insideString;
    private DisonReader disonReader;
    private boolean dontRevealDisCall;
    private String name = DEFAULT_NAME;
    private boolean rootVariableAssignment;

    DisonJsonTransformer(final Settings settings) {
        this.errorStrategy = settings.errorStrategy;
        this.currentPath = settings.currentPath.endsWith(File.separator) ?
                settings.currentPath.substring(0, settings.currentPath.length() - 1) : settings.currentPath;
        this.pluginFactory = settings.pluginFactory;
        this.mapConfiguration = settings.mapConfiguration;
        Optional.ofNullable(settings.mappingRoot)
                .ifPresent(a -> name = a);
    }

    public String parseForChild(final String resourceContent, final String path, final String name) {
        final DisonJsonTransformer child = new DisonJsonTransformer(
                new Settings(errorStrategy, path, pluginFactory, mapConfiguration, name));
        return child.toJson(resourceContent);
    }

    /**
     * parses dison String to json String
     *
     * @param resourceContent String of dison file
     * @return String of json interpretation
     */
    public String parse(final String resourceContent) {
        clear();
        return toJson(resourceContent);
    }

    public Optional<DisonElementReader> plugInError(final String message) {
        error(DisonErrorStrategy.PLUGIN_ERROR, message);
        return Optional.empty();
    }

    public boolean inObject() {
        return !currentStages.isEmpty() && currentStages.getLast() == JsonElementStage.OBJECT;
    }

    public boolean inArray() {
        return !currentStages.isEmpty() && currentStages.getLast() == JsonElementStage.ARRAY;
    }

    public String getCurrentPath() {
        return currentPath;
    }

    public void error(final DisonErrorStrategy flag, final String message) {
        if (errorStrategy.contains(flag)) {
            throw new DisonParsingError(message);
        }
        LOGGER.e("Skipped error: ".concat(message));
    }

    public Optional<DisonPlugin> matchAPlugin(final String varName) {
        final Optional<DisonPlugin> matchingPlugins = pluginFactory.find(varName);
        if (matchingPlugins.isEmpty()) {
            error(DisonErrorStrategy.UNKNOWN_COMMAND, "No plugin found for command: ".concat(varName));
            result.append(String.format("'%s'", varName));
        }
        return matchingPlugins;
    }


    public DisonElementReader removeIncompatibleElements(final DisonElementReader other, final boolean preventMultiArrays) {
        if (null == other) {
            return null;
        }
        if ((inObject() && other instanceof DisonObjectReader) ||
                (preventMultiArrays && inArray() && other instanceof DisonArrayReader)) {
            return new DisonRawReader(removeIncompatibleElements(other.writeJson(), preventMultiArrays));
        }
        return other;
    }

    public String removeIncompatibleElements(final String other, final boolean preventMultiArrays) {
        if (null == other) {
            return null;
        }
        final StringBuilder result = new StringBuilder(other);
        if (inObject()) {
            removeFirstOccurrenceOfCharIfAny(result, '{', false);
            removeFirstOccurrenceOfCharIfAny(result, '}', true);
        } else if (preventMultiArrays && inArray()) {
            removeFirstOccurrenceOfCharIfAny(result, '[', false);
            removeFirstOccurrenceOfCharIfAny(result, ']', true);
        }
        return result.toString();
    }

    public void pushStage(final JsonElementStage stage) {
        currentStages.add(stage);
    }

    public void removeStage() {
        currentStages.removeLast();
    }

    private void removeFirstOccurrenceOfCharIfAny(final StringBuilder string, final char character, final boolean backwards) {
        for (int current = backwards ? string.length() - 1 : 0, max = backwards ? 0 : string.length();
             (backwards && (current > max)) || (!backwards && (current < max)); current = current + (backwards ? -1 : 1)) {
            if (String.valueOf(string.charAt(current)).isBlank()) {
                continue;
            }
            if (string.charAt(current) == character) {
                string.delete(current, current + 1);
            }
            return;
        }
    }

    private synchronized String toJson(final String resourceContent) {
        disonReader = new DisonReader(resourceContent, this);
        while (disonReader.readNext()) {
            final char currentChar = disonReader.currentChar();
            if ('\'' == currentChar && !insideString) {
                handleApostrophe();
            } else {
                if (inVar) {
                    if ('(' == currentChar) {
                        handleMethod();
                        continue;
                    }
                    varBuffer.append(currentChar);
                } else {
                    if (shouldSkip(currentChar)) {
                        continue;
                    }
                    if ('"' == currentChar && disonReader.currentCharNotEscaped()) {
                        insideString = !insideString;
                    } else if (!insideString && !rootVariableAssignment) {
                        openNewStage(currentChar);
                        closeAnyStages(currentChar);
                    }
                    if (rootVariableAssignment) {
                        rootVariableAssignment = false;
                        continue;
                    }
                    result.append(currentChar);
                }
            }
        }
        return result.toString();
    }

    private void mapObject() {
        if (mapConfiguration) {
            final DisonReader.CurrentPosition position = disonReader.getCurrentPosition();
            objectMappings.push(String.format("%s@%s:%s", name, position.line(), position.column()));
        }
    }

    private void markObject() {
        if (mapConfiguration && 0 < objectMappings.size()) {
            if (!inEmptyObject()) {
                result.append(String.format(",\"%s\":%s",
                        SourceMapping.getMappingKey(), DisonStringReader.escape(objectMappings.getFirst())));
            }
            objectMappings.pop();
        }
    }

    private boolean inEmptyObject() {
        for (int i = result.length(); i-- > 0; ) {
            final char current = result.charAt(i);
            if (Character.isWhitespace(current)) {
                continue;
            }
            return '{' == current;
        }
        return false;
    }

    private boolean shouldSkip(final char currentChar) {
        if (dontRevealDisCall && !Character.isWhitespace(currentChar)) {
            dontRevealDisCall = false;
            switch (currentChar) {
                case ',':
                    return true;
                case ')':
                case '}':
                case ']':
                    deletePreviousComma();
            }
        }
        return false;
    }

    private void deletePreviousComma() {
        for (int i = result.length() - 1; i-- > 0; ) {
            final char current = result.charAt(i);
            if (Character.isWhitespace(current)) {
                continue;
            }
            if (',' == current) {
                result.deleteCharAt(i);
            }
            return;
        }
    }

    private void closeAnyStages(final char currentChar) {
        if ('}' == currentChar || ']' == currentChar) {
            if (currentStages.getLast() == JsonElementStage.ASSIGNMENT) {
                currentStages.removeLast();
            }
            if ('}' == currentChar) {
                markObject();
            }
            currentStages.removeLast();
        } else if (',' == currentChar && !currentStages.isEmpty() && currentStages.getLast() == JsonElementStage.ASSIGNMENT) {
            currentStages.removeLast();
        }
    }

    private void openNewStage(final char currentChar) {
        if ('{' == currentChar) {
            currentStages.add(JsonElementStage.OBJECT);
            mapObject();
        } else if ('[' == currentChar) {
            currentStages.add(JsonElementStage.ARRAY);
        } else if (':' == currentChar) {
            currentStages.add(JsonElementStage.ASSIGNMENT);
        }
    }

    private void clear() {
        popBuffer(varBuffer);
        popBuffer(result);
        inVar = false;
        insideString = false;
        currentStages.clear();
        pluginFactory.clear();
    }

    private void handleMethod() {
        matchAPlugin().ifPresent(this::handleMethodPlugin);
        inVar = false;
    }

    private void handleMethodPlugin(final DisonPlugin disonPlugin) {
        handlePlugin(disonPlugin, disonReader.createNextDisonMethod(disonPlugin));
    }

    private void handlePlugin(final DisonPlugin disonPlugin, final DisonElementReader element) {
        disonPlugin.handle(element, this)
                .map(DisonElementReader::writeJson)
                .filter(a -> !a.isBlank())
                .ifPresentOrElse(result::append, this::makeItVanish);
    }

    private void makeItVanish() {
        dontRevealDisCall = true;
    }

    private void handleApostrophe() {
        if (inVar) {
            disonReader.readNext();
            matchAPlugin().ifPresent(this::handleAssigningPlugin);
        } else {
            rootVariableAssignment = disonReader.getCurrentPosition().onStart();
        }
        inVar = !inVar;
    }

    private void handleAssigningPlugin(final DisonPlugin disonPlugin) {
        handlePlugin(disonPlugin, disonReader.getNextAssignedDisonElement());
        if (!rootVariableAssignment) {
            disonReader.rewindLast();
        }
    }

    private Optional<DisonPlugin> matchAPlugin() {
        return matchAPlugin(popBuffer(varBuffer));
    }

    private String popBuffer(final StringBuilder varBuffer) {
        final String result = varBuffer.toString();
        varBuffer.setLength(0);
        varBuffer.trimToSize();
        return result;
    }
}