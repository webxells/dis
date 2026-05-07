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
package com.webxells.dis.base.manipulator;

import com.webxells.dis.api.DatasetPiece;
import com.webxells.dis.api.MappingPortrayal;
import com.webxells.dis.api.config.MappingConfiguration;
import com.webxells.dis.api.config.MappingPart;
import com.webxells.dis.api.config.description.Alias;
import com.webxells.dis.api.config.description.Default;
import com.webxells.dis.api.config.description.Description;
import com.webxells.dis.api.config.description.Required;
import com.webxells.dis.api.error.InvalidApi;
import com.webxells.dis.api.error.InvalidDatasetException;
import com.webxells.dis.api.manipulator.Manipulator;
import com.webxells.dis.base.SimpleDatasetPiece;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Description("Splits value into separated values by using a regex")
public class SplitByRegularExpression implements Manipulator {
    public enum SplitIdentifier {
        START, END
    }
    public enum PartNotFoundStrategy {
        ERROR, EMPTY, NULL, IGNORE
    }

    public static class Destination {
        @Description("Index of the split value to put in this destination")
        public int part;
        @Description("Mapping part to put the split value in")
        public MappingPortrayal destination;
    }

    @Description("In how many parts the given value will be split at most")
    @Default("Number of partDestinations")
    private int maxParts;
    @Required
    private Pattern pattern;
    @Alias("destinations")
    @Description("Location of the split values")
    private List<Destination> partDestinations;
    @Default("START")
    private SplitIdentifier splitIdentifier = SplitIdentifier.START;
    @Description("How to handle case that a destined part was not found")
    private PartNotFoundStrategy partNotFoundStrategy = PartNotFoundStrategy.EMPTY;
    @Description("Removes existing data from location of the split values")
    private boolean overwriteDestinations;
    @Description("Regular expression will ignore already split values")
    private boolean ignorePatternInDestinations;

    @Override
    public void validate() throws InvalidApi {
        if (null == pattern) {
            throw new InvalidApi("Field regex required");
        }
    }

    @Override
    public void manipulate(final DatasetPiece currentPiece, final MappingPart mappingPart) throws InvalidDatasetException {
        final String string = currentPiece.value().orElse("");
        final Matcher matcher = pattern.matcher(string);
        int current = 0;
        int lastEnding = 0;
        final List<String> result = new ArrayList<>(maxParts > 0 ? maxParts : partDestinations.size());
        while (matcher.find() && (1 > maxParts || current+++1 < maxParts)) {
            final int splitIndex = getSplitIndex(matcher,
                    ignorePatternInDestinations ? SplitIdentifier.START : splitIdentifier);
            result.add(string.substring(lastEnding, splitIndex));
            lastEnding = ignorePatternInDestinations ? getSplitIndex(matcher, SplitIdentifier.END) : splitIndex;
        }
        result.add(string.substring(lastEnding));
        resolveDestinations(result, mappingPart.getConfiguration());
    }

    private void resolveDestinations(final List<String> result, final MappingConfiguration configuration) {
        partDestinations.forEach(destination ->
                configuration.getByPortrayal(destination.destination).ifPresent(part -> {
                    if (overwriteDestinations) {
                        part.getDataset().clear();
                    }

                    if (result.size() <= destination.part) {
                        handlePartNotFoundStrategy(part, destination.part);
                    } else {
                        part.getDataset().collect(new SimpleDatasetPiece(result.get(destination.part)));
                    }
        }));
    }

    private void handlePartNotFoundStrategy(final MappingPart part, final int partIndex) {
        String value = null;
        switch (partNotFoundStrategy) {
            case ERROR:
                throw new RuntimeException("Part not found: ".concat(String.valueOf(partIndex)));
            case IGNORE:
                return;
            case EMPTY:
                value = "";
        }
        part.getDataset().collect(new SimpleDatasetPiece(value));
    }

    private int getSplitIndex(final Matcher matcher, final SplitIdentifier splitIdentifier) {
        switch (splitIdentifier) {
            case START:
                return matcher.start();
            case END:
                return matcher.end();
            default:
                throw new RuntimeException("Invalid splitIdentifier: ".concat(splitIdentifier.toString()));
        }
    }

    public void setMaxParts(final int maxParts) {
        this.maxParts = maxParts;
    }

    public void setPattern(final String pattern) {
        this.pattern = Pattern.compile(pattern);
    }

    public void setPartDestinations(final List<Destination> partDestinations) {
        this.partDestinations = partDestinations;
    }

    public void setSplitIdentifier(final SplitIdentifier splitIdentifier) {
        this.splitIdentifier = splitIdentifier;
    }

    public void setPartNotFoundStrategy(final PartNotFoundStrategy partNotFoundStrategy) {
        this.partNotFoundStrategy = partNotFoundStrategy;
    }

    public void setOverwriteDestinations(boolean overwriteDestinations) {
        this.overwriteDestinations = overwriteDestinations;
    }

    public void setIgnorePatternInDestinations(boolean ignorePatternInDestinations) {
        this.ignorePatternInDestinations = ignorePatternInDestinations;
    }
}
