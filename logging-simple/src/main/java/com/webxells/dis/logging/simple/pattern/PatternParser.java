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
package com.webxells.dis.logging.simple.pattern;

import com.webxells.dis.logging.simple.pattern.path.Date;
import com.webxells.dis.logging.simple.pattern.path.ExceptionEntity;
import com.webxells.dis.logging.simple.pattern.path.Level;
import com.webxells.dis.logging.simple.pattern.path.Name;
import com.webxells.dis.logging.simple.pattern.path.PatternEntity;
import com.webxells.dis.logging.simple.pattern.path.Message;
import com.webxells.dis.logging.simple.pattern.path.NewLine;
import com.webxells.dis.logging.simple.pattern.path.StringEntity;
import com.webxells.dis.logging.simple.pattern.path.Thread;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class PatternParser {
    private static final Map<String, Function<String, PatternEntity>> REGISTERED_VARIABLE_TYPES = Map.of(
            "thread", Thread::new,
            "n", a -> new NewLine(),
            "msg", Message::new,
            "date", Date::new,
            "level", Level::new,
            "name", Name::new,
            "exception", ExceptionEntity::new);
    private final byte[] rawPattern;
    private final StringBuilder current = new StringBuilder();

    final List<PatternEntity> resultPatterns = new LinkedList<>();

    private boolean inVariableName;
    private boolean inVariableDef;

    public static List<PatternEntity> parse(final String pattern) {
        final PatternParser parser = new PatternParser(pattern);
        return parser.parse();
    }

    private PatternParser(final String rawPattern) {
        this.rawPattern = rawPattern.getBytes();
    }

    private List<PatternEntity> parse() {
        String variableName = "";
        for (int i = 0, m = rawPattern.length; i <= m; i++) {
            final byte c = i < m ? rawPattern[i] : 0;
            if (i == m) {
                if (inVariableName) {
                    addNewPatternPath(cutCurrentAsString());
                } else {
                    addCurrentStringAsString();
                }
            } else if (c == 36 && !inVariableDef) {
                if (inVariableName) {
                    addNewPatternPath(cutCurrentAsString());
                } else {
                    addCurrentStringAsString();
                    inVariableName = true;
                }
            } else if (inVariableDef && c == 41) {
                inVariableDef = false;
                inVariableName = false;
                addNewPatternPath(variableName, cutCurrentAsString());
                variableName = "";
            } else if (!inVariableDef && inVariableName && !isVariableNameChar(c)) {
                if (c == 40) {
                    inVariableDef = true;
                    variableName = cutCurrentAsString();
                } else {
                    inVariableName = false;
                    addNewPatternPath(cutCurrentAsString());
                    current.append((char) c);
                }
            } else {
                current.append((char) c);
            }
        }
        return resultPatterns;
    }

    private void addNewPatternPath(final String variableName) {
        addNewPatternPath(variableName, "");
    }

    private void addNewPatternPath(final String variableName, final String variableDef) {
        resultPatterns.add(REGISTERED_VARIABLE_TYPES.getOrDefault(variableName.toLowerCase(),
                (a) -> new StringEntity(recreateHowItLooked(variableName, variableDef)))
                    .apply(variableDef));
    }

    private String recreateHowItLooked(final String variableName, final String variableDef) {
        return String.format("$%s%s", variableName, variableDef.isEmpty() ? "" : String.format("(%s)", variableDef));
    }

    private boolean isVariableNameChar(final byte current) {
        return (current > 64 && current < 91) || (current > 96 && current < 123);
    }

    private void addCurrentStringAsString() {
        if (current.length() > 0) {
            resultPatterns.add(new StringEntity(cutCurrentAsString()));
        }
    }

    private String cutCurrentAsString() {
        current.trimToSize();
        final String result = current.toString();
        current.setLength(0);
        return result;
    }


}