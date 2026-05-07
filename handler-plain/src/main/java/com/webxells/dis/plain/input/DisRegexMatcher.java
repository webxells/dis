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
package com.webxells.dis.plain.input;

import com.webxells.dis.plain.Rule;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

class DisRegexMatcher {
    private final static Pattern REGEX_GROUP_PATTERN = Pattern.compile("\\(\\?<([^>]+)>");
    private final Matcher matcher;
    private final List<String> allNamedGroups;
    private boolean found;

    public DisRegexMatcher(final Rule rule, final String content) {
        matcher = matchRule(rule, content);
        allNamedGroups = getAllNamedGroups();
    }

    public Optional<String> group(final String path) {
        if (found) {
            if (allNamedGroups.contains(path)) {
                return Optional.of(matcher.group(path));
            } else if (path.chars().allMatch(Character::isDigit)) {
                return Optional.of(matcher.group(Integer.parseInt(path)));
            }
        }
        return Optional.empty();
    }

    public void find() {
        found = matcher.find();
    }

    public boolean found() {
        return found;
    }

    @SuppressWarnings("MagicConstant")
    private Matcher matchRule(final Rule rule, final String content) {
        return Pattern.compile(rule.getRegex(), rule.optionsToFlagValue()).matcher(content);
    }


    private List<String> getAllNamedGroups() {
        return REGEX_GROUP_PATTERN.matcher(matcher.pattern().pattern())
                .results().map(a -> a.group(1)).collect(Collectors.toList());
    }
}