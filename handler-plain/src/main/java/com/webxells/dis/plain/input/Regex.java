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

import com.webxells.dis.api.config.MappingConfiguration;
import com.webxells.dis.api.config.MappingPart;
import com.webxells.dis.api.error.DisException;
import com.webxells.dis.api.error.InputOutputError;
import com.webxells.dis.api.input.Input;
import com.webxells.dis.base.SimpleDatasetPiece;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class Regex implements Input<RegexConfig> {
    protected final RegexConfig config;
    protected List<DisRegexMatcher> matcher;
    private final String thisName;
    protected String content;
    private Boolean hasNext;

    public Regex(final RegexConfig config) {
        this.config = Objects.requireNonNull(config);
        thisName = config.getName();
    }

    @Override
    public int read(final MappingConfiguration mappingConfiguration) throws InputOutputError {
        if (hasNext()) {
            hasNext = null;
            final List<MappingPart> parts = mappingConfiguration.partsBySource(thisName);
            readRules(new LinkedList<>(parts));
            return parts.size();
        }
        return 0;
    }

    @Override
    public boolean hasNext() throws InputOutputError {
        if (null == hasNext) {
            matcher.forEach(DisRegexMatcher::find);
            hasNext = matcher.stream().anyMatch(DisRegexMatcher::found);
        }
        return hasNext;
    }

    @Override
    public String getName() {
        return config.getName();
    }

    protected void readRules(final List<MappingPart> from) {
        matcher.forEach(a -> matcherToDatasetPieces(a, from.iterator()));
        from.forEach(a -> a.getDataset().collect(new SimpleDatasetPiece(null)));
    }

    private void matcherToDatasetPieces(final DisRegexMatcher matcher, final Iterator<MappingPart> from) {
        while (from.hasNext()) {
            MappingPart next = from.next();
            String path = next.getInput().getPath();
            matcher.group(path).ifPresent(a -> {
                next.getDataset().collect(new SimpleDatasetPiece(a));
                from.remove();
            });
        }
    }

    @Override
    public void start() throws DisException {
        try {
            try (InputStream s = config.getReceiver().receive()) {
                content = new String(s.readAllBytes());
            }
            createMatchers();
        } catch (final IOException e) {
            throw new InputOutputError("error receiving content", e);
        }
    }

    protected void createMatchers() {
        matcher = config.getRules().stream()
                .map(a -> new DisRegexMatcher(a, content))
                .collect(Collectors.toUnmodifiableList());
    }

    @Override
    public void end() {
        content = null;
    }
}