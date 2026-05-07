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

import java.util.List;

import static com.webxells.dis.csv.discover.intern.CsvPattern.OWNING_RESULT.LOOKS_LIKE;
import static com.webxells.dis.csv.discover.intern.CsvPattern.OWNING_RESULT.MINE;
import static com.webxells.dis.csv.discover.intern.CsvPattern.OWNING_RESULT.NOPE;

public class Value implements CsvElement{

    private static class Word {
        private boolean started;
        private boolean finished;
        private boolean hasOpenSeparator;
    }

    public static final List<Character> LIKELY_CHARS = List.of('"', '\'');

    private Character separator;
    private boolean inEscapingSequence;

    private Word currentWord;
    @Override
    public void resetRow() {
        separator = null;
        resetWord();
    }

    @Override
    public void resetWord() {
        inEscapingSequence = false;
        currentWord = new Word();
    }

    @Override
    public CsvPattern.OWNING_RESULT match(final String rawLne, final int pos) {
        if (inEscapingSequence) {
            inEscapingSequence = false;
            return MINE;
        }
        if (currentWord.finished) {
            return NOPE;
        }
        final char current = rawLne.charAt(pos);
        if (!currentWord.started) {
            currentWord.started = true;
            if (null == separator) {
                if (isASeparatorChar(current)) {
                    separator = current;
                    currentWord.hasOpenSeparator = true;
                    return MINE;
                }
            }
            currentWord.hasOpenSeparator = null != separator && separator == current;
        } else if (null != separator && separator == current) {
            if (pos + 1 < rawLne.length()) {
                final char next = rawLne.charAt(pos + 1);
                if (separator == next) {
                    inEscapingSequence = true;
                    return MINE;
                }
            }
            currentWord.finished = true;
            return MINE;
        }
        return currentWord.hasOpenSeparator ? MINE : LOOKS_LIKE;
    }

    private boolean isASeparatorChar(final char current) {
        return LIKELY_CHARS.contains(current);
    }

    public Character getChar() {
        return separator;
    }
}