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
package com.webxells.dis.plain;

import com.webxells.dis.api.config.description.Description;
import java.util.Set;
import java.util.regex.Pattern;

public enum Option {
    @Description("Enables case-insensitive matching")
    CASE_INSENSITIVE(Pattern.CASE_INSENSITIVE),
    @Description("In multiline mode the expressions ^ and $ match just after or just before, respectively, a line terminator " +
            "or the end of the input sequence. By default, these expressions only match at the beginning and the end of " +
            "the entire input sequence.")
    MULTILINE(Pattern.MULTILINE),
    @Description("When this flag is specified then the input string that specifies the pattern is treated as a sequence of " +
            "literal characters. Metacharacters or escape sequences in the input sequence will be given no special meaning.")
    LITERAL(Pattern.LITERAL),
    @Description("In dotall mode, the expression . matches any character, including a line terminator. By default, "+
            "this expression does not match line terminators.")
    DOTALL(Pattern.DOTALL),
    @Description("When this flag is specified then case-insensitive matching, when enabled by the CASE_INSENSITIVE flag, " +
            "is done in a manner consistent with the Unicode Standard. By default, case-insensitive matching assumes " +
            "that only characters in the US-ASCII charset are being matched.")
    UNICODE_CASE(Pattern.UNICODE_CASE),
    @Description("When this flag is specified then two characters will be considered to match if, and only if, " +
            "their full canonical decompositions match. The expression \"a\\u030A\", for example, will match the string " +
            "\"\\u00E5\" when this flag is specified. By default, matching does not take canonical equivalence into account.")
    CANON_EQ(Pattern.CANON_EQ),
    @Description("When this flag is specified then the (US-ASCII only) Predefined character classes and POSIX character " +
            "classes are in conformance with Unicode Technical Standard #18: Unicode Regular Expressions Annex " +
            "C: Compatibility Properties.")
    UNICODE_CHARACTER_CLASS(Pattern.UNICODE_CHARACTER_CLASS),
    @Description("Permits whitespace and comments in pattern.")
    COMMENTS(Pattern.COMMENTS),
    @Description("In this mode, only the '\\n' line terminator is recognized in the behavior of ., ^, and $.")
    UNIX_LINES(Pattern.UNIX_LINES),
    @Description("UNICODE_CHARACTER_CLASS + UNICODE_CASE")
    UNICODE(Pattern.UNICODE_CHARACTER_CLASS | Pattern.UNICODE_CASE);

    private final int flagValue;

    public static int toFlagValue(final Set<Option> options) {
        return options.stream()
                .map(Option::getFlagValue)
                .reduce(0, (a, b) -> a | b);
    }

    Option(final int flagValue) {
        this.flagValue = flagValue;
    }

    public int getFlagValue() {
        return flagValue;
    }
}