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

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.webxells.dis.json.JsonException;
import com.webxells.dis.json.config.JsonMappingPart;
import com.webxells.dis.json.input.JsonInputConfig;
import com.webxells.dis.json.refinement.JsonType;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class Parser {
    private enum Status {
        SUCCESS, FAIL
    }

    private final JsonReader jsonReader;
    private final String iterationPath;
    private final boolean singleObject;
    private final boolean lenient;
    private final Map<String, JsonInputConfig.ReadType> readTypes;
    private Status currentState = Status.SUCCESS;
    private boolean open = true;

    public Parser(final InputStream receive, final JsonInputConfig configuration) throws IOException, JsonException {
        this.jsonReader = new JsonReader(new BufferedReader(new InputStreamReader(receive)));
        this.jsonReader.setLenient(configuration.isLenient());
        singleObject = configuration.isSingleObject();
        lenient = configuration.isLenient();
        readTypes = Optional.ofNullable(configuration.getReadTypes())
                .orElse(Map.of());
        skipToIterationPath(new JsonPath(configuration.getIterationPath(), configuration.isLenient()));
        iterationPath = getIterationPath();
    }

    private Parser(final JsonReader jsonReader, final boolean singleObject, final Parser parent) {
        this.jsonReader = jsonReader;
        this.singleObject = singleObject;
        lenient = parent.lenient;
        iterationPath = getIterationPath();
        readTypes = parent.readTypes;
    }

    private String getIterationPath() {
        final String currentPath = jsonReader.getPath();
        return currentPath.substring(0, currentPath.length() - (singleObject ? 0 : 3));
    }

    public boolean hasNext() throws IOException {
        return open && jsonReader.hasNext() && nextNewReadableObject();
    }

    public void parse(final List<JsonPath> paths) throws IOException, JsonException {
        if (Status.FAIL == currentState) {
            return;
        }
        switch (jsonReader.peek()) {
            case BEGIN_ARRAY:
                parseArray(paths);
                break;
            case BEGIN_OBJECT:
                parseObject(paths);
                break;
            case STRING:
            case NUMBER:
            case BOOLEAN:
            case NULL:
                scanForValues(paths);
        }
    }

    private void parseArray(final List<JsonPath> paths) throws IOException, JsonException {
        beginArray();
        while(jsonReader.hasNext()) {
            parse(paths);
        }
        endArray();
    }

    private void parseObject(final List<JsonPath> paths) throws IOException, JsonException {
        beginObject();
        while(jsonReader.hasNext()) {
            jsonReader.nextName();
            if (jsonReader.peek() == JsonToken.NULL) {
                jsonReader.nextNull();
                continue;
            }
            scanForValues(paths);
        }
        endObject();
    }

    private void scanForValues(final List<JsonPath> paths) throws JsonException, IOException {
        final List<JsonPath> depending = filterPathsDependingOfCurrent(paths);
        if (depending.isEmpty()) {
            jsonReader.skipValue();
        } else {
            final List<JsonPath> matches = filterPathsMatchingCurrent(depending);
            if (matches.isEmpty()) {
                parse(depending);
            } else {
                setValue(matches);
            }
        }
    }

    private void setValue(final List<JsonPath> matches) throws IOException, JsonException {
        JsonPath previousMatch = null;
        for (final JsonPath match : matches) {
            if (match.getMappingPartReference().filter(a -> a instanceof JsonMappingPart).isPresent()) {
                final JsonMappingPart jsonMappingPart = (JsonMappingPart) match.getMappingPartReference().get();
                jsonMappingPart.parseValue(jsonReader);
                return;
            }
            if (null == match.getMultiMapping() || match.getMultiMapping().isEmpty() || match.getMultiMapping().get(0).isEmpty()) {
                if (null != previousMatch) {
                    match.setValue(previousMatch.getValue());
                } else {
                    setValue(match);
                    previousMatch = match;
                }
            } else {
                parseChildrenMappings(match);
            }
        }
    }

    private void setValue(final JsonPath match) throws JsonException, IOException {
        final JsonInputConfig.ReadType readType = readTypes.getOrDefault(getPathWithoutArrayIndex(),
                JsonInputConfig.ReadType.JSON_VALUE);
        switch (match.getJsonType().orElse(JsonType.Type.DEFAULT)) {
            case ARRAY_TO_MULTIPLE_DATASET_PIECES -> readToMultiplePieces(match, readType);
            default -> {
                final String value = getValue(readType);
                match.setValue(null == value ? null : List.of(value));
            }
        }
    }

    private void readToMultiplePieces(final JsonPath match, final JsonInputConfig.ReadType readType) throws IOException, JsonException {
        switch (jsonReader.peek()) {
            case BEGIN_ARRAY -> {
                final List<String> values = new ArrayList<>();
                jsonReader.beginArray();
                while (JsonToken.END_ARRAY != jsonReader.peek()) {
                    values.add(getValue(readType));
                }
                jsonReader.endArray();
                match.setValue(values);
            }
            case NULL -> match.setValue(null);
            default -> throw new JsonException("could not read to multiple pieces: " + jsonReader.peek());
        }
    }

    private String getPathWithoutArrayIndex() {
        return jsonReader.getPath().replaceAll("\\[\\d+\\]", "[]");
    }

    private String getValue(final JsonInputConfig.ReadType readType) throws JsonException, IOException {
        switch (readType) {
            case RAW:
                return createCurrentJsonValueAsRawString();
            case JSON_VALUE:
                return getCurrentScalarValue(jsonReader);
        }
        throw new IllegalArgumentException("invalid read type: " + readType);
    }

    private String createCurrentJsonValueAsRawString() throws IOException, JsonException {
        return NextValueToRawStringRenderer.parse(jsonReader);
    }


    private void parseChildrenMappings(final JsonPath paths) throws IOException, JsonException {
        final List<JsonPath> originalPaths = paths.getMultiMapping().get(0);
        final List<List<JsonPath>> result = new LinkedList<>();
        beginArray();
        if (nextNewReadableObject() ) {
            readByChildParser(originalPaths, result);
        } else if (nextPrimitive()) {
            readRootPrimitives(originalPaths, result);
        }
        endArray();
        if (result.isEmpty()) {
            result.add(createCopy(originalPaths));
        }
        paths.setChildrenMappings(result);
    }

    private boolean nextPrimitive() throws IOException {
        switch (jsonReader.peek()) {
            case STRING:
            case NUMBER:
            case BOOLEAN:
            case NULL:
                return true;
        }
        return false;
    }

    private void readRootPrimitives(final List<JsonPath> originalPaths, final List<List<JsonPath>> result) throws IOException, JsonException {
        while(nextPrimitive()) {
            final List<JsonPath> currentPaths = createCopy(originalPaths);
            for (final JsonPath a : currentPaths) {
                if (a.matches("$")) {
                    setValue(a);
                }
            }
            result.add(currentPaths);
        }
    }

    private void readByChildParser(final List<JsonPath> originalPaths, final List<List<JsonPath>> result) throws IOException, JsonException {
        final Parser childParser = new Parser(jsonReader, false, this);
        while (nextNewReadableObject()) {
            final List<JsonPath> currentPaths = createCopy(originalPaths);
            childParser.parse(currentPaths);
            result.add(currentPaths);
        }
    }

    private boolean nextNewReadableObject() throws IOException {
        return JsonToken.BEGIN_OBJECT == jsonReader.peek() || JsonToken.BEGIN_ARRAY == jsonReader.peek();
    }

    private List<JsonPath> createCopy(final List<JsonPath> originalPaths) {
        return originalPaths.stream()
                .map(JsonPath::copy)
                .collect(Collectors.toList());
    }

    public void close() throws IOException {
        jsonReader.close();
        open = false;
    }

    public static String getCurrentScalarValue(final JsonReader jsonReader) throws IOException, JsonException {
        switch(jsonReader.peek()) {
            case STRING:
            case NUMBER:
                return jsonReader.nextString();
            case BOOLEAN:
                return String.valueOf(jsonReader.nextBoolean());
            case NULL:
                jsonReader.nextNull();
                return null;
            default:
                final String type = String.valueOf(jsonReader.peek());
                if (jsonReader.isLenient()) {
                    jsonReader.skipValue();
                    return type;
                }
                throw new JsonException("Scalar value expected - found: ".concat(type));
        }
    }

    private List<JsonPath> filterPathsMatchingCurrent(final List<JsonPath> paths) {
        final String currentPath = calculateRelativePath();
        return paths.stream()
                .filter(a -> a.matches(currentPath))
                .collect(Collectors.toList());
    }

    private String calculateRelativePath() {
        final String path = jsonReader.getPath().substring(iterationPath.length());
        return "$".concat(singleObject ? path : path.substring(path.indexOf(']') + 1));
    }

    private void skipToIterationPath(final JsonPath jsonPath) throws IOException, JsonException {
        try {
            skipToPath(jsonPath);
        } catch (final Throwable throwable) {
            if (!lenient) {
                throw throwable;
            }
            currentState = Status.FAIL;
        }
        if (singleObject) {
            return;
        }
        if (jsonReader.peek() != JsonToken.BEGIN_ARRAY) {
            throw new JsonException("Array expected for iteration path");
        }
        beginArray();
    }

    private List<JsonPath> filterPathsDependingOfCurrent(final List<JsonPath> paths) {
        final String currentPath = calculateRelativePath();
        return paths.stream()
                .filter(a -> a.isPartOf(currentPath))
                .collect(Collectors.toList());
    }

    //@todo: refactor to use ::parse for iteration path
    private void skipToPath(final JsonPath jsonPath) throws IOException, JsonException {
        for (Path.Element element : jsonPath.getObjectPath()) {
            if (element instanceof Path.NamedElement) {
                beginObject();
                error(skipToMember(((Path.NamedElement) element).name), "Member not found");
            }
            if (element instanceof Path.NamedArray) {
                if (jsonReader.peek() != JsonToken.BEGIN_ARRAY) {
                    throw new JsonException("Array expected");
                }
                beginArray();
                error(skipArrayContent(((Path.NamedArray) element).index - 1), "Array index not reached");
            }
        }
        if(!jsonPath.matches(jsonReader.getPath())) {
            throw new JsonException(String.format("Path does not match: (provided) %s != (generated) %s",
                    jsonPath.getPath(), jsonReader.getPath()));
        }
    }

    private void error(final Status status, final String message) throws JsonException {
        if (status == Status.FAIL) {
            throw new JsonException(message);
        }
    }

    private Status skipToMember(final String name) throws IOException {
        while (jsonReader.hasNext() && jsonReader.peek() != JsonToken.END_OBJECT) {
            final String currentName = jsonReader.nextName();
            if (currentName.equals(name)) {
                return Status.SUCCESS;
            }
            jsonReader.skipValue();
        }
        return Status.FAIL;
    }

    private Status skipArrayContent(final int times) throws IOException {
        for (int i = 0; i < times; i++) {
            if (jsonReader.peek() == JsonToken.END_ARRAY) {
                return Status.FAIL;
            }
            jsonReader.skipValue();
        }
        return Status.SUCCESS;
    }

    private void beginObject() throws IOException {
        jsonReader.beginObject();
    }

    private void endObject() throws IOException {
        jsonReader.endObject();
    }

    private void beginArray() throws IOException {
        jsonReader.beginArray();
    }

    private void endArray() throws IOException {
        jsonReader.endArray();
    }

    public void assertInObject()  throws IOException {
        if (JsonToken.BEGIN_OBJECT != jsonReader.peek()) {
            throw new IOException("no object starting");
        }
    }
}