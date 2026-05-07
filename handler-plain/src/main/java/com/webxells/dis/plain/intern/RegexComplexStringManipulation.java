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
package com.webxells.dis.plain.intern;

import com.webxells.dis.api.config.MappingPart;
import com.webxells.dis.api.config.description.Description;
import com.webxells.dis.api.error.InvalidDatasetException;
import java.util.Optional;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

public abstract class RegexComplexStringManipulation extends RegexManipulation {
    public enum NoResultStrategy {
        @Description("does nothing") IGNORE,
        @Description("deletes found pattern") EMPTY,
        @Description("throws an Error") ERROR
    }

    @Description("regex group replaced by manipulator result; defaults to 0 (whole match)")
    protected int groupToReplace;
    @Description("what to do if regex is found but manipulator clears dataset; defaults to EMPTY")
    protected NoResultStrategy noResultStrategy = NoResultStrategy.EMPTY;

    @Override
    protected String compileResult(final String original, final Matcher matcher, final MappingPart part) throws InvalidDatasetException {
        final StringBuilder result = new StringBuilder(original);
        int size;
        int change = 0;
        for (MatchResult match : matcher.results().collect(Collectors.toList())) {
            size = result.length();
            change(result, match, change, part);
            change+= result.length() - size;
        }
        return result.toString();
    }

    protected void change(final StringBuilder string, final MatchResult match, final int changedResultDifference, final MappingPart part) throws InvalidDatasetException {
        final int groupReplace = getGroup(groupToReplace, match);
        string.replace(match.start(groupReplace) + changedResultDifference, match.end(groupReplace) + changedResultDifference,
                complexManipulation(match, part));
    }

     protected abstract String complexManipulation(final MatchResult match, final MappingPart part) throws InvalidDatasetException;


    protected int getGroup(final Integer groupDefinition, final MatchResult match) throws InvalidDatasetException {
        final int result = Optional.ofNullable(groupDefinition)
                .orElse(0);
        if (match.groupCount() < result) {
            throw new InvalidDatasetException("invalid group provided: "+ groupDefinition);
        }
        return result;
    }


    public void setGroupToReplace(final int groupToReplace) {
        this.groupToReplace = groupToReplace;
    }


    public void setNoResultStrategy(final NoResultStrategy noResultStrategy) {
        this.noResultStrategy = noResultStrategy;
    }
}