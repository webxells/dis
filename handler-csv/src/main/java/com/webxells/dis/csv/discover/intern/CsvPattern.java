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
package com.webxells.dis.csv.discover.intern;

import java.util.ArrayList;
import java.util.List;

import static com.webxells.dis.csv.discover.intern.CsvPattern.OWNING_RESULT.MINE;
import static com.webxells.dis.csv.discover.intern.CsvPattern.OWNING_RESULT.NOPE;

public class CsvPattern {
    enum OWNING_RESULT {
        MINE, LOOKS_LIKE, NOPE
    }
    private final List<CsvElement> elements;

    public CsvPattern(final List<CsvElement> elements) {
        if (null == elements || elements.isEmpty()) {
            throw new IllegalArgumentException("pattern without elements ");
        }
        this.elements = new ArrayList<>(elements);
    }

    public boolean match(final String rawLne) {
        elements.forEach(CsvElement::resetRow);
        int currentElement = 0;
        int currentStringPos = 0;
        final int lineSize = rawLne.length();
        while (lineSize > currentStringPos) {
            final OWNING_RESULT currentTrust = elements.get(currentElement).match(rawLne, currentStringPos);
            if (MINE != currentTrust) {
                final int nextElement = nextElement(currentElement);
                elements.get(nextElement).resetWord();
                final OWNING_RESULT nextTrust = elements.get(nextElement).match(rawLne, currentStringPos);
                if (NOPE == nextTrust && NOPE == currentTrust) {
                    return false;
                } else if (NOPE != nextTrust){
                    currentElement = nextElement;
                }
            }
            currentStringPos++;
        }
        return true;
    }

    private int nextElement(final int currentElement) {
        return  currentElement + 1  < elements.size() ? currentElement + 1 : 0;
    }

    public Character getValueSeparator() {
        return elements.stream()
                .filter(a -> a instanceof Value)
                .map(a -> (Value) a)
                .findAny()
                .map(Value::getChar)
                .orElse(null);
    }

    public Character getFieldSeparator() {
        return elements.stream()
                .filter(a -> a instanceof FieldSeparator)
                .map(a -> (FieldSeparator) a)
                .findAny()
                .map(FieldSeparator::getChar)
                .orElse(null);
    }

}