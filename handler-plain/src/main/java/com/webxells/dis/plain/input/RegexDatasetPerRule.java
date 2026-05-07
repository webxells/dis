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

import com.webxells.dis.api.config.MappingPart;
import com.webxells.dis.api.config.description.Description;
import com.webxells.dis.api.error.InputOutputError;
import com.webxells.dis.base.SimpleDatasetPiece;
import com.webxells.dis.plain.Rule;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class RegexDatasetPerRule extends Regex {
    private final static Pattern REGEX_GROUP_PATTERN = Pattern.compile("\\(\\?<([^>]+)>.+\\)");
    private final Iterator<Rule> rules;

    public RegexDatasetPerRule(final RegexConfig config) {
        super(config);
        rules = config.getRules().iterator();
    }

    @Override
    protected void readRules(List<MappingPart> parts) {
        final Rule rule = rules.next();
        final Matcher matcher = generateMatcher(rule);
        matcherToDatasetPieces(matcher, parts);
    }

    @Override
    protected void createMatchers() { }


    @SuppressWarnings("MagicConstant")
    private Matcher generateMatcher(final Rule rule) {
        return Pattern.compile(rule.getRegex(), rule.optionsToFlagValue()).matcher(content);
    }


    private void matcherToDatasetPieces(final Matcher matcher, final List<MappingPart> parts) {
        final List<String> allNamedGroups = getAllNamedGroups(matcher.pattern().pattern());
        final boolean found = matcher.find();
        parts.stream()
                .filter(a -> a.getInput().getReference().equals(config.getName()))
                .forEach(a -> searchForGroup(a, found, matcher, allNamedGroups));
    }

    private void searchForGroup(final MappingPart config, final boolean found, final Matcher matcher,
                                    final List<String> allNamedGroups) {
        final String path = config.getInput().getPath();
        String content = null;
        if (found) {
            if (allNamedGroups.contains(path)) {
                content = matcher.group(path);
            } else if (path.chars().allMatch(Character::isDigit)) {
                content = matcher.group(Integer.parseInt(path));
            }
        }
        config.getDataset().collect(new SimpleDatasetPiece(content));
    }

    private List<String> getAllNamedGroups(final String pattern) {
        return REGEX_GROUP_PATTERN.matcher(pattern).results().map(a -> a.group(1)).collect(Collectors.toList());
    }

    @Override
    public boolean hasNext() throws InputOutputError {
        return rules.hasNext();
    }

}
