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
package com.webxells.dis.config.json.parsing.intern;

import com.webxells.dis.config.json.parsing.DisonElementReader;
import com.webxells.dis.config.json.parsing.DisonJsonTransformer;
import com.webxells.dis.config.json.parsing.DisonParsingError;
import com.webxells.dis.config.json.parsing.element.DisonArrayReader;
import com.webxells.dis.config.json.parsing.element.DisonBooleanReader;
import com.webxells.dis.config.json.parsing.element.DisonDoubleReader;
import com.webxells.dis.config.json.parsing.element.DisonLongReader;
import com.webxells.dis.config.json.parsing.element.DisonMethodReader;
import com.webxells.dis.config.json.parsing.element.DisonNullReader;
import com.webxells.dis.config.json.parsing.element.DisonObjectReader;
import com.webxells.dis.config.json.parsing.element.DisonStringReader;
import com.webxells.dis.config.json.parsing.plugins.DisonPlugin;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class DisonReader {
    public record CurrentPosition(int line, int column) {
        public boolean onStart() {
            return 1 == line && 1 == column;
        }
    }

    private static class Position {
        private int line = 1;
        private int column;
        private int oldColumn;

        public CurrentPosition getCurrentPosition() {
            return new CurrentPosition(line, column);
        }

        private void newLine(final Direction direction) {
            switch (direction) {
                case FORWARDS -> {
                    line++;
                    oldColumn = column;
                    column = 0;
                }
                case BACKWARDS -> {
                    line--;
                    column = oldColumn;
                }
            }
        }

        private void next(final Direction direction) {
            column = column + direction.amount;
        }
    }

    private enum Direction {
        FORWARDS(1), BACKWARDS(-1);

        private final int amount;

        Direction(final int amount) {
            this.amount = amount;
        }
    }

    private final String dison;
    private final int contentLength;
    private final DisonJsonTransformer disonJsonTransformer;
    private final Position position = new Position();

    private int currentPosition = -1;
    private char currentChar;
    private boolean inString;
    private boolean inComment;
    private boolean lastMovedBackwards;

    public DisonReader(final String dison, final DisonJsonTransformer disonJsonTransformer) {
        this.dison = dison;
        contentLength = dison.length();
        this.disonJsonTransformer = disonJsonTransformer;
    }

    public CurrentPosition getCurrentPosition() {
        return position.getCurrentPosition();
    }

    public boolean readNext() {
        lastMovedBackwards = false;
        return move(Direction.FORWARDS);
    }

    public void rewindLast() {
        if (lastMovedBackwards) {
            throw new IllegalStateException("Could be inside comment or string now");
        }
        lastMovedBackwards = true;
        move(Direction.BACKWARDS);
    }

    private boolean move(final Direction direction) {
        final int i = direction.amount;
        if (contentLength > i + currentPosition) {
            currentPosition = i + currentPosition;
            currentChar = dison.charAt(currentPosition);
            if (i > 0 && !inComment && '"' == currentChar && currentCharNotEscaped()) {
                inString = !inString;
            }
            handlePosition(direction);
            return true;
        }
        return false;
    }

    private void handlePosition(final Direction direction) {
        if ('\n' == currentChar) {
            position.newLine(direction);
        } else {
            position.next(direction);
        }
    }

    public char currentChar() {
        skipComment();
        return currentChar;
    }

    private void skipComment() {
        while(skipComment("//", "\n") || skipComment("/*", "*/")) {
            skipWhiteSpaces();
        }
    }

    private boolean skipComment(final String start, final String end) {
        if (!inString && startingNow(start)) {
            inComment = true;
            while (readNext()) {
                if (startingNow(end)) {
                    readNext(end.length());
                    inComment = false;
                    return true;
                }
            }
        }
        return false;
    }

    private boolean startingNow(final String string) {
        final int end = currentPosition + string.length();
        return end <= contentLength && string.equals(dison.substring(currentPosition, end));
    }

    public char currentChar(final int positionChanged) {
        if (contentLength > currentPosition + positionChanged && -1 < currentPosition + positionChanged) {
            return dison.charAt(currentPosition + positionChanged);
        }
        throw new StringIndexOutOfBoundsException();
    }

    public DisonElementReader createNextDisonMethod(final DisonPlugin disonPlugin) {
        requireNext();
        return parseMethodParameters(disonPlugin);
    }

    public DisonElementReader getNextAssignedDisonElement() {
        skipWhiteSpaces();
        if (':' != currentChar()) {
            throw new DisonParsingError(error("Dison element expected"));
        }
        readNext();
        return getNextDisonElement();
    }

    public boolean currentCharNotEscaped() {
        int countEscapeChars = 0;
        int current = currentPosition - 1;
        while(current > -1 && '\\' == dison.charAt(current--)) {
            countEscapeChars++;
        }
        return 0 == countEscapeChars % 2;
    }

    protected DisonElementReader handlePrimitive() {
        final DisonElementReader primitive = createPrimitive();
        if (null == primitive) {
            throw new DisonParsingError(error("unexpected char"));
        }
        return primitive;
    }

    protected DisonElementReader createPrimitive() {
        if ('"' == currentChar) {
            return createString();
        }
        if (isCurrentNumeric() || '-' == currentChar || '+' == currentChar) {
            return createNumeric();
        }
        if ('t' == currentChar || 'T' == currentChar || 'f' == currentChar || 'F' == currentChar) {
            return createBoolean();
        }
        if ('n' == currentChar || 'N' == currentChar) {
            return createNull();
        }
        return null;
    }

    protected DisonElementReader getNextDisonElement() {
        skipWhiteSpaces();
        switch (currentChar()) {
            case '[':
                requireNext();
                return createArray();
            case '{':
                return createObject();
            case '-':
            case '\'':
                if (isMethodStarting()) {
                    return createMethodWithName();
                }
        }
        return handlePrimitive();
    }

    private boolean isMethodStarting() {
        return currentPosition < contentLength && '-' == currentChar(+1) &&
                !('\'' == currentChar && '-' != currentChar(+2));
    }

    private DisonElementReader createMethodWithName() {
        skip('\'');
        final int nameStart = currentPosition;
        while ('(' != currentChar() && '\'' != currentChar()) {
            requireNext();
        }
        final boolean isBlockCommand = '\'' == currentChar();
        final String pluginName = dison.substring(nameStart, currentPosition).trim();
        requireNext();
        return disonJsonTransformer.matchAPlugin(pluginName)
                .map(plugin -> {
                    final DisonElementReader result = isBlockCommand ? parseObjectCommand(plugin) : parseMethodParameters(plugin);
                    skip('\'');
                    return result;
                })
                .orElseThrow(() -> new DisonParsingError("no plugin found with name: ".concat(pluginName)));
    }

    private DisonElementReader parseObjectCommand(final DisonPlugin plugin) {
        return plugin.handle(Optional.of(getNextAssignedDisonElement())
                .filter(a -> a instanceof DisonObjectReader)
                .map(a -> (DisonObjectReader) a)
                .orElseThrow(() -> new DisonParsingError("object required")), disonJsonTransformer)
                        .orElseGet(DisonMethodReader::empty);
    }

    private DisonElementReader parseMethodParameters(final DisonPlugin plugin) {
        final List<DisonElementReader> result = new LinkedList<>();
        skipWhiteSpaces();
        while(')' != currentChar()) {
            result.add(getNextDisonElement());
            skipComma();
        }
        //closing
        readNext();
        return new DisonMethodReader(plugin, result, disonJsonTransformer);
    }

    private void skipComma() {
        skip(',');
    }

    private void skip(final char character) {
        skipWhiteSpaces();
        if (character == currentChar()) {
            requireNext();
        }
        skipWhiteSpaces();
    }

    private DisonElementReader createArray() {
        final List<DisonElementReader> rest = new LinkedList<>();
        skipWhiteSpaces();
        while(']' != currentChar()) {
            rest.add(getNextDisonElement());
            skipWhiteSpaces();
            if (',' == currentChar()) {
                requireNext();
                continue;
            }
            if (']' != currentChar()) {
                throw new DisonParsingError(error("malformed array"));
            }
        }
        readNext();
        return new DisonArrayReader(rest, disonJsonTransformer);
    }

    private void readNext(final int times) {
        for (int i = times; i-- > 0 ;) {
            readNext();
        }
    }

    private void requireNext() {
        if (!readNext()) {
            throw new DisonParsingError(error("unexpected end"));
        }
    }

    private DisonElementReader createObject() {
        requireNext();
        final Map<String, DisonElementReader> result = new LinkedHashMap<>();
        skipWhiteSpaces();
        while ('"' == currentChar()) {
            result.put(getNextStringTillEnd(), getNextAssignedDisonElement());
            skipComma();
        }
        skipWhiteSpaces();
        skipComment();
        if ('}' != currentChar()) {
            throw new DisonParsingError(error("malformed object"));
        }
        readNext();
        return new DisonObjectReader(result, disonJsonTransformer);
    }

    private DisonElementReader createNull() {
        if (startsOnCurrentPosition("null")) {
            readNext(4);
            return new DisonNullReader();
        }
        throw new DisonParsingError(error("unexpected char"));
    }

    private boolean isCurrentNumeric() {
        return isNumeric(currentChar);
    }

    private boolean isNumeric(final char character) {
        return character > 47 && character < 58;
    }

    private DisonElementReader createBoolean() {
        if (startsOnCurrentPosition("true")) {
            readNext(4);
            return new DisonBooleanReader(true);
        }
        if (startsOnCurrentPosition("false")) {
            readNext(5);
            return new DisonBooleanReader(false);
        }
        throw new DisonParsingError(error("unexpected char"));
    }

    private boolean startsOnCurrentPosition(final String string) {
        final int length = string.length();
        if (contentLength >= currentPosition + length) {
            return dison.substring(currentPosition, currentPosition + length).equalsIgnoreCase(string);
        }
        return false;
    }

    private DisonElementReader createNumeric() {
        final int start = currentPosition;
        boolean withPoint = false;
        boolean withExponent = false;
        while (readNext()) {
            if ('.' == currentChar) {
                if (withPoint || !(isNumeric(currentChar(-1)) && isNumeric(currentChar(+1)))) {
                    throw new DisonParsingError(error("malformed double (with point) detected"));
                }
                withPoint = true;
            } else if (!isCurrentNumeric()) {
                if (('-' == currentChar || '+' == currentChar) && isExponentChar(currentChar(-1))) {
                    continue;
                }
                if (!withExponent && isExponentChar(currentChar)) {
                    withExponent = true;
                    continue;
                }
                if (isCurrentBlanc() || ',' == currentChar || ';' == currentChar || '}' == currentChar || ']' == currentChar || ')' == currentChar) {
                    final String content = dison.substring(start, currentPosition);
                    return withPoint || withExponent ? createDouble(content) : createLong(content);
                }
                break;
            }
        }
        throw new DisonParsingError(error("malformed numeric element"));
    }

    private boolean isExponentChar(final char toTest) {
        return'E' == toTest || 'e' == toTest;
    }

    private DisonDoubleReader createDouble(final String content) {
        return new DisonDoubleReader(Double.parseDouble(content));
    }

    private DisonLongReader createLong(final String content) {
        return new DisonLongReader(Long.parseLong(content));
    }

    private String getNextStringTillEnd() {
        final int start = currentPosition + 1;
        while(readNext()) {
            if ('"' == currentChar && currentCharNotEscaped()) {
                requireNext();
                return dison.substring(start, currentPosition - 1);
            }
        }
        throw new DisonParsingError("never ending string detected");
    }

    private DisonStringReader createString() {
        return new DisonStringReader(getNextStringTillEnd());
    }

    private String error(final String message) {
        return String.format("%s (%s) on pos %d ", message, currentChar, currentPosition);
    }

    private void skipWhiteSpaces() {
        while(isCurrentBlanc()) {
            readNext();
        }
    }

    private boolean isCurrentBlanc() {
        return String.valueOf(currentChar).isBlank();
    }
}