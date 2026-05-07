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

import com.webxells.dis.json.JsonException;
import com.webxells.dis.json.output.JsonOutputConfig;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

public class Composer {
    private final BufferedWriter writer;
    private final JsonOutputConfig configuration;
    private final Deque<String> endings = new LinkedList<>();
    private final Map<String, Integer> currentArrayIndexMap = new HashMap<>();
    private boolean commaOnNewWrite;

    public Composer(final OutputStream outputStream, final JsonOutputConfig configuration) throws IOException {
        writer = new BufferedWriter(new OutputStreamWriter(outputStream));
        this.configuration = configuration;
    }

    public void start(final boolean writeToIterationPath) throws IOException, JsonException {
        if (writeToIterationPath) {
            writeToPath(getIterationPath(), true, "");
            if (!configuration.isSingleObject()) {
                openArray();
            }
        }
    }

    private String getIterationPath() {
        return Optional.ofNullable(configuration.getIterationPath()).orElse("$");
    }

    public void write(final List<ComposingJsonPath> values) throws IOException, JsonException {
        if (commaOnNewWrite) {
            comma();
        }
        openObject();
        String previous = "$.";
        boolean comma = false;
        for (final ComposingJsonPath current : sorted(values)) {
            writeToPath(current.getPath(), previous, comma);
            writeValue(current);
            previous = current.getPath();
            comma = true;
        }
        closeCurrent(previous);
        commaOnNewWrite = true;
        currentArrayIndexMap.clear();
    }

    public void close() throws IOException {
        flush(true);
        writer.close();
    }

    public void flush(final boolean end) throws IOException {
        if (end) {
            while(endings.size() > 0) {
                end();
            }
            commaOnNewWrite = false;
        }
        writer.flush();
    }

    private void writeToPath(final String path, final String previous, final boolean comma)
            throws JsonException, IOException {
        final int i = getDifferenceStart(previous, path);
        closeDifference(previous.substring(i).toCharArray());
        if (comma) {
            comma();
        }
        writeToPath(path.substring(i), false, path.substring(0, i));
    }

    private int getFromStartingJsonPathByDifference(final String path, int startDifference) {
        while(startDifference > 0 && !('.' == path.charAt(startDifference - 1) || '[' == path.charAt(startDifference - 1))) {
            startDifference--;
        }
        return startDifference;
    }

    private int getDifferenceStart(final String previous, final String path) {
        final char[] chars = previous.toCharArray();
        int result = 0;
        for (int m = chars.length; result < m; result++) {
            if (path.length() <= result || chars[result] != path.charAt(result)) {
                break;
            }
        }
        return getFromStartingJsonPathByDifference(path, result);
    }

    private void closeDifference(final char[] difference) throws IOException {
        for (int i = difference.length - 1; i-- > 0;) {
            switch (difference[i]) {
                case '.':
                case '[':
                    end();
                    break;
            }
        }
    }

    private void writeToPath(final String path, final boolean forceRootPath, final String prefix) throws JsonException, IOException {
        final PathComposer pathComposer = new PathComposer(path, forceRootPath, prefix);
        pathComposer.write();
    }

    private void buildArray(final String path, Integer arrayIndex, final String prefix) throws IOException {
        final String pathForIndexMap = prefix.concat(path);
        Integer currentArrayIndex = currentArrayIndexMap.get(pathForIndexMap);
        if (null == currentArrayIndex) {
            openArray();
            currentArrayIndex = 0;
        }
        if (null == arrayIndex) {
            arrayIndex = currentArrayIndex;
        }
        while (currentArrayIndex++ < arrayIndex) {
            writeNull();
            comma();
        }
        currentArrayIndexMap.put(pathForIndexMap, currentArrayIndex);
    }

    private void writeNull() throws IOException {
        writer.write("null");
    }

    private void comma() throws IOException {
        writer.write(",");
    }

    private void assertValidSyntax(final boolean errorCheck, final String errorMessage, final String path) throws JsonException {
        if (errorCheck) {
            throw new JsonException(errorMessage, path);
        }
    }

    private boolean lastWasArray() {
        return endings.size() > 0 && "]".equals(endings.getFirst());
    }

    private void name(final String name) throws IOException {
        writer.write(String.format("\"%s\":", escapeJsonStringSpecialCharacters(name)));
    }

    private void openObject() throws IOException {
        writer.write("{");
        endings.push("}");
    }

    private void openArray() throws IOException {
        writer.write("[");
        endings.push("]");
    }

    private void end() throws IOException {
        writer.write(endings.pop());
    }

    private void closeCurrent(final String previous) throws IOException {
        final int i = getDifferenceStart(previous, "$");
        closeDifference(previous.substring(i).toCharArray());
    }

    private void writeValue(final ComposingJsonPath current) throws IOException, JsonException {
        switch (current.getType()) {
            case NUMERIC:
            case DECIMAL:
            case STRING:
            case BOOLEAN:
            case RAW:
                writeSimpleValue(current);
                break;
            case OBJECT:
                throw new JsonException("Scalar or list type expected");
            case LIST:
                writeList(current.getValue());
                break;
            case NULL:
                writeNull();
                break;
            default:
                throw new RuntimeException("unknown json type: " + current.getType());
        }
    }

    private void writeSimpleValue(final ComposingJsonPath current) throws IOException, JsonException {
        for (int i = 0, m = current.getValue().size(); i == 0 || (i < m && lastWasArray()); i++) {
            if (i > 0) {
                comma();
            }
            writeSimpleValueByIndex(current, lastWasArray() ? i : null);
        }
    }

    private void writeSimpleValueByIndex(final ComposingJsonPath current, final Integer i) throws JsonException, IOException {
        switch (current.getType()) {
            case NUMERIC:
            case DECIMAL:
            case STRING:
            case RAW:
                writeScalarValue(current, i);
                break;
            case BOOLEAN:
                writeRawValue(isSomeSortOfFalse(getSingleScalarValue(current.getValue(), i)) ? "false" : "true");
                break;
            default:
                throw new RuntimeException("unknown simple json type: " + current.getType());
        }
    }

    private void writeList(final List<String> value) throws IOException {
        openArray();
        int currentRound = 0;
        for (final String current : value) {
            if (currentRound++ > 0) {
                comma();
            }
            //@todo: not only string
            writeString(current);
        }
        end();
    }

    private void writeScalarValue(final ComposingJsonPath current, final Integer index) throws IOException, JsonException {
        final String scalarValue = getSingleScalarValue(current.getValue(), index);
        if (null == scalarValue) {
            writeNull();
            return;
        }
        switch (current.getType()) {
            case RAW:
                writer.write(scalarValue);
                break;
            case NUMERIC:
                assertValidNumber(scalarValue, false);
                writeRawValue(scalarValue);
                break;
            case DECIMAL:
                assertValidNumber(scalarValue, true);
                writeRawValue(scalarValue);
                break;
            case STRING:
                writeString(scalarValue);
                break;
        }
    }

    private void writeString(final String value) throws IOException {
        writer.write(String.format("\"%s\"", escapeJsonStringSpecialCharacters(value)));
    }

    private boolean isSomeSortOfFalse(final String value) {
        return null == value || value.isBlank() || "false".equalsIgnoreCase(value) || "0".equals(value);
    }

    private void assertValidNumber(final String value, final boolean pointAllowed) throws JsonException {
        if (!isValidNumber(value, pointAllowed)) {
            throw new JsonException("Non decimal value: ".concat(value));
        }
    }

    private boolean isValidNumber(final String value, final boolean pointAllowed) {
        final AtomicBoolean firstPoint = new AtomicBoolean(pointAllowed);
        return value.chars()
                .allMatch(a -> Character.isDigit(a) || ((a == '.') && firstPoint.getAndSet(false)));
    }

    private void writeRawValue(final String scalarValue) throws IOException {
        writer.write(scalarValue);
    }

    private String getSingleScalarValue(final List<String> current, final Integer index) {
        if (current.size() == 0) {
            return null;
        }
        if (null == index) {
            return multiToOnValue(current);
        }
        return current.get(index);
    }

    private String multiToOnValue(final List<String> current) {
        switch (configuration.getMultipleValuesForScalarFieldStrategy()) {
            case FIRST:
                return current.get(0);
            case LAST:
                return current.get(current.size() - 1);
            case ERROR:
            default:
                throw new RuntimeException("Expected only one value for json scalar but found multiple");
        }
    }


    private List<ComposingJsonPath> sorted(final List<ComposingJsonPath> values) {
        return values.stream()
                .sorted(new NaturalStringOrder<>(ComposingJsonPath::getPath))
                .collect(Collectors.toList());
    }


    private String escapeJsonStringSpecialCharacters(final String name) {
        return name
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\r", "\\r")
                .replace("\t", "\\t")
                .replace("\f", "\\f")
                .replace("\b", "\\b")
                .replace("\n", "\\n");
    }

    private class PathComposer {

        private final String path;
        private final boolean forceRootPath;
        private final String prefix;
        private StringBuilder currentName;
        private StringBuilder arrayIndex;
        private Boolean booted;
        private int currentIndex;

        public PathComposer(final String path, final boolean forceRootPath, final String prefix) {
            this.path = path;
            this.forceRootPath = forceRootPath;
            this.prefix = prefix;
        }

        public void write() throws JsonException, IOException {
            final char[] charArray = path.toCharArray();
            for (int charArrayLength = charArray.length; currentIndex < charArrayLength; currentIndex++) {
                final char current = charArray[currentIndex];
                switch (current) {
                    case '$':
                        handleDollar(currentIndex);
                        continue;
                    case '.':
                        handleNewObject();
                        continue;
                    case '[':
                        openNewArray();
                        continue;
                    case ']':
                        handleArray();
                        continue;
                }
                if (inArray()) {
                    addToArrayIndex(current);
                } else {
                    addToName(current);
                }
            }
            tryName();
        }

        private void handleDollar(final int currentIndex) throws JsonException {
            if (0 == currentIndex) {
                handleRoot();
                return;
            }
            addToName('$');
        }

        private void handleRoot() throws JsonException {
            assertValidSyntax(null != booted || !forceRootPath, "illegal $");
            booted = false;
        }

        private boolean inArray() {
            return null != arrayIndex;
        }

        private void assertValidSyntax(final boolean errorCheck, final String errorMessage) throws JsonException {
            Composer.this.assertValidSyntax(errorCheck, errorMessage, path);
        }

        private void addToArrayIndex(final char current) throws JsonException {
            assertValidSyntax(!Character.isDigit(current), "numeric index required");
            if (null == arrayIndex) {
                arrayIndex = new StringBuilder();
            }
            arrayIndex.append(current);
        }

        private void addToName(final char current) throws JsonException {
            assertStarted();
            assertValidSyntax(inArray(), "numeric index required");
            if (null == currentName) {
                currentName = new StringBuilder();
            }
            currentName.append(current);
        }

        private void openNewArray() throws JsonException {
            assertStarted();
            assertValidSyntax(inArray() || !booted(), "illegal [");
            arrayIndex = new StringBuilder();
        }

        private boolean booted() {
            if (!forceRootPath) {
                return true;
            }
            final boolean result = booted;
            booted = true;
            return result;
        }

        private void handleNewObject() throws JsonException, IOException {
            assertStarted();
            assertValidSyntax(null == currentName && (!lastWasArray() && booted()), "illegal .");
            tryName();
            openObject();
        }

        private void assertStarted() throws JsonException {
            assertValidSyntax(null == booted && forceRootPath, "only root paths allowed");
        }

        private void handleArray() throws IOException, JsonException {
            if (isBeginningPartOfArrayPath()) {
                currentName = null;
                return;
            }
            if (isArrayWithoutIndex()) {
                return;
            }
            assertValidSyntax(!inArray(), "illegal ]");
            tryName();
            buildArray(path.substring(0, currentIndex - arrayIndex.length() - 1),
                    arrayIndex.length() > 0 ? Integer.parseInt(arrayIndex.toString()) : null, prefix);
            arrayIndex = null;
        }

        private boolean isArrayWithoutIndex() {
            return lastWasArray() && !inArray() &&
                    !forceRootPath && null == currentName &&
                    null == booted;
        }

        private boolean isBeginningPartOfArrayPath() {
            return lastWasArray() && !inArray() &&
                    !forceRootPath && null != currentName &&
                    isValidNumber(currentName.toString(), false) &&
                    null == booted;

        }

        private void tryName() throws IOException {
            if (null != currentName) {
                name(currentName.toString());
                currentName = null;
            }
        }
    }
}