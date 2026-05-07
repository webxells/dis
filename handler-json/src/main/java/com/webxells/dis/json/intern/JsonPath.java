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
package com.webxells.dis.json.intern;

import com.webxells.dis.api.DatasetPiece;
import com.webxells.dis.api.config.MappingConfiguration;
import com.webxells.dis.api.config.MappingPart;
import com.webxells.dis.api.config.MappingPoint;
import com.webxells.dis.json.config.JsonMappingPart;
import com.webxells.dis.json.refinement.JsonType;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class JsonPath {
    private static final Pattern JSON_PATH = Pattern.compile(
            "((\\$((\\[(\\d*(]\\[\\d*)*)])*)?)|\\G)\\.([\\p{L}$\\d\\-_]+)((\\[(\\d*(]\\[\\d*)*)])*)?");

    private final String path;
    private final List<Path.Element> objectPath;

    private List<String> value;
    private boolean valueSet;
    private List<List<JsonPath>> childrenMappings;
    private MappingPart mappingPartReference;
    private boolean isJsonMappingPart;

    public static JsonPath createJsonPathByOutputMappingPart(final MappingPart part, final boolean lenient) {
        return createJsonPathByMappingPart(part, lenient, false);
    }

    public static JsonPath createJsonPathByInputMappingPart(final MappingPart part, final boolean lenient) {
        return createJsonPathByMappingPart(part, lenient, true);
    }

    private static JsonPath createJsonPathByMappingPart(final MappingPart part, final boolean lenient,
                                                        final boolean isInput) {
        final JsonPath result = new JsonPath((isInput ? part.getInput() : part.getOutput()).getPath(), lenient);
        if (part.getSubData().isEmpty()) {
            part.value().ifPresent(a -> result.setValue(
                    part.getDataset().getContent().stream()
                            .map(DatasetPiece::value)
                            .filter(Optional::isPresent)
                            .map(Optional::get)
                            .collect(Collectors.toList())));
        } else {
            result.setChildrenMappings(createChildrenMappings(part.getSubData(), lenient, isInput));
        }
        result.setMappingPartReference(part);
        return result;
    }

    private static List<List<JsonPath>> createChildrenMappings(final List<MappingConfiguration> subData, final boolean lenient, final boolean isInput) {
        final List<List<JsonPath>> result = new LinkedList<>();
        subData.forEach(a -> result.add(a.parts().stream()
                .filter(b -> (isInput && isValidMappingPoint(b.getInput())) || (!isInput && isValidMappingPoint(b.getOutput())))
                .map(part -> createJsonPathByMappingPart(part, lenient, isInput))
                .collect(Collectors.toList())));
        return result;
    }

    private static boolean isValidMappingPoint(final MappingPoint mappingPoint) {
        return null != mappingPoint && null != mappingPoint.getPath();
    }

    public JsonPath(String path, final boolean lenient) {
        if (!"$".equals(Objects.requireNonNull(path).substring(0, 1))) {
            if (lenient) {
                path = "$.".concat(path);
            } else {
                throw new IllegalArgumentException("Only root paths are allowed");
            }
        }
        this.path = path;
        objectPath = new ArrayList<>(countParts(path.toCharArray()));
        splitToObjectPath();
    }

    private int countParts(final char[] path) {
        int result = 0;
        for (int i = 0, m = path.length; i < m; i++) {
            if ('.' == path[i] || ('[' == path[i] && i > 0 && ']' == path[i - 1])) {
                result++;
            }
        }
        return result;
    }

    private JsonPath(final String path, final List<Path.Element> objectPath) {
        this.path = path;
        this.objectPath = objectPath;
    }

    public String getPath() {
        return path;
    }

    public boolean matches(final String currentPath) {
        return path.equals(currentPath);
    }

    public boolean isPartOf(final String currentPath) {
        final int currentLength = currentPath.length();
        if (path.length() <= currentLength) {
            return path.equals(currentPath);
        }
        final char charAfterCurrentPath = path.charAt(currentLength);
        return path.startsWith(currentPath) && ('.' == charAfterCurrentPath || '[' == charAfterCurrentPath);
    }

    public void setValue(final List<String> value) {
        this.value = value;
        valueSet = true;
    }

    public List<String> getValue() {
        return value;
    }

    public boolean isValueSet() {
        return valueSet;
    }

    public List<Path.Element> getObjectPath() {
        return objectPath;
    }

    private void splitToObjectPath() {
        JSON_PATH.matcher(path).results()
                .flatMap(a -> {
                    final List<Path.Element> result = new LinkedList<>();
                    if (!Objects.requireNonNullElse(a.group(2), "").isEmpty()) {
                        if (Objects.requireNonNullElse(a.group(5), "").isEmpty()) {
                            result.add(new Path.AnonymousObject());
                        } else {
                            result.addAll(createArrays(a.group(5)));
                        }
                    }
                    if (Objects.requireNonNullElse(a.group(10), "").isEmpty() && !"[]".equals(a.group(8))) {
                        result.add(new Path.NamedObject(a.group(7)));
                    } else {
                        result.addAll(createArrays(a.group(10), a.group(7)));
                    }
                    return result.stream();
                })
                .forEach(objectPath::add);
    }

    private List<Path.Element> createArrays(final String group) {
        return createArrays(group, null);
    }

    private List<Path.Element> createArrays(final String group, final String name) {
        final String[] split = group.split("]\\[");
        final List<Path.Element> result = new LinkedList<>();
        for (int i = 0, m = split.length; i < m; i++) {
            final Integer index = split[i].isEmpty() ? null : Integer.parseInt(split[i]);
            result.add(null == name || i > 0 ? new Path.AnonymousArray(index) : new Path.NamedArray(name, index));
        }
        return result;
    }

    public void setChildrenMappings(final List<List<JsonPath>> childrenMappings) {
        valueSet = true;
        this.childrenMappings = childrenMappings;
    }

    public List<List<JsonPath>> getMultiMapping() {
        return childrenMappings;
    }

    public JsonPath copy() {
        final JsonPath copy = new JsonPath(path, objectPath);
        copy.childrenMappings = childrenMappings;
        Optional.ofNullable(value)
                .ifPresent(a -> copy.value = List.copyOf(a));
        copy.valueSet = valueSet;
        Optional.ofNullable(childrenMappings)
                .ifPresent(a -> copy.childrenMappings = copyMultiMapping());
        copy.mappingPartReference = mappingPartReference;
        return copy;
    }

    private List<List<JsonPath>> copyMultiMapping() {
        return childrenMappings.stream()
                .map(a -> a.stream().map(JsonPath::copy)
                        .collect(Collectors.toList()))
                .collect(Collectors.toList());
    }

    public Optional<MappingPart> getMappingPartReference() {
        return Optional.ofNullable(mappingPartReference);
    }

    public Optional<JsonType.Type> getJsonType() {
        return getMappingPartReference()
                .flatMap(a -> a.getFirstRefinement(JsonType.class))
                .map(JsonType::getJsonType);
    }

    public void setMappingPartReference(final MappingPart mappingPartReference) {
        this.mappingPartReference = mappingPartReference;
        isJsonMappingPart = mappingPartReference instanceof JsonMappingPart;
    }

    public int valueSize() {
        return isJsonMappingPart ? ((JsonMappingPart) mappingPartReference).valueSize() : isValueSet() ? 1 : 0;
    }

    public boolean isJsonMappingPart() {
        return isJsonMappingPart;
    }
}