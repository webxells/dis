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
package com.webxells.dis.plain.manipulator;

import com.webxells.dis.api.DatasetPiece;
import com.webxells.dis.api.MappingOperation;
import com.webxells.dis.api.MappingPortrayal;
import com.webxells.dis.api.config.MappingConfiguration;
import com.webxells.dis.api.config.MappingPart;
import com.webxells.dis.api.config.description.Default;
import com.webxells.dis.api.config.description.Description;
import com.webxells.dis.api.config.description.Required;
import com.webxells.dis.api.error.InvalidApi;
import com.webxells.dis.api.error.InvalidDatasetException;
import com.webxells.dis.api.manipulator.Manipulator;
import com.webxells.dis.api.manipulator.SingleCallForAllValuesManipulator;
import com.webxells.dis.base.SimpleDatasetPiece;
import com.webxells.dis.base.config.SimpleMappingConfiguration;
import com.webxells.dis.base.config.SimpleMappingPart;
import com.webxells.dis.base.config.SimpleMappingPoint;
import com.webxells.dis.base.config.StableMappingPart;
import com.webxells.dis.plain.intern.RegexComplexStringManipulation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.regex.MatchResult;
import java.util.stream.Collectors;

@Description("Imports regex to subData that then runs as normal mapping configuration (limited capabilities); " +
        "named groups are not recognized")
public class RegexToSubDataManipulation extends RegexComplexStringManipulation {
    public static class SubDataPart {
        public SimpleMappingPoint input;
        public SimpleMappingPoint output;
        public List<Manipulator> operations;
        public SubData subData;
    }

    public static class SubData {
        @Required
        public List<SubDataPart> parts;
    }

    public enum TooManyValuesStrategy {
        @Description("Picks first value") FIRST, @Description("Picks last value") LAST, @Description("Raises error") FAIL
    }

    @Description("Input reference in subData used to read regular expression into mapping parts")
    @Default("regex")
    private String regexReference = "regex";
    @Required
    @Description("Portrayal in subData that will hold replace value")
    private MappingPortrayal resultPortrayal;
    private MappingConfiguration subData;
    @Description("What to do if too many values in 'resultPortrayal' found")
    @Default("FIRST")
    private TooManyValuesStrategy tooManyValuesStrategy = TooManyValuesStrategy.FIRST;


    @Override
    public void validate() throws InvalidApi {
        super.validate();
        if (null == subData || null == resultPortrayal) {
            throw new InvalidApi("required fields missing");
        }
    }

    @Override
    protected String complexManipulation(final MatchResult match, final MappingPart part) throws InvalidDatasetException {
        resetSubData(subData, part.getConfiguration());
        importMatchData(match);
        runOperations(subData);
        return runResultStrategy(resultDataset(), match.group(groupToReplace));
    }

    private List<DatasetPiece> resultDataset() {
        return subData.getByPortrayal(resultPortrayal)
                .map(a -> a.getDataset().getContent())
                .orElse(List.of());
    }

    private String runResultStrategy(final List<DatasetPiece> pieces, final String toReplace) throws InvalidDatasetException {
        switch (pieces.size()) {
            case 1:
                final Optional<String> value = pieces.get(0).value();
                if (value.isPresent()) {
                    return value.get();
                }
            case 0:
                return noResultStrategy(toReplace);
        }
        switch (tooManyValuesStrategy) {
            case FIRST:
                return findFirstValue(pieces, toReplace);
            case LAST:
                final List<DatasetPiece> copy = new ArrayList<>(pieces);
                Collections.reverse(copy);
                return findFirstValue(copy, toReplace);
        }
        throw new InvalidDatasetException("too many values to replace: "+ toReplace);
    }

    private String findFirstValue(final List<DatasetPiece> list, final String toReplace) throws InvalidDatasetException {
        final Optional<String> first = list.stream()
                .flatMap(a -> a.value().stream())
                .findFirst();
        if (first.isPresent()) {
            return first.get();
        }
        return noResultStrategy(toReplace);
    }

    private String noResultStrategy(final String toReplace) throws InvalidDatasetException {
        switch (noResultStrategy) {
            case IGNORE:
                return toReplace;
            case EMPTY:
                return "";
        }
        throw new InvalidDatasetException("no value found to replace: " + toReplace);
    }

    private void runOperations(final MappingConfiguration configuration) throws InvalidDatasetException {
        for (MappingPart part : configuration.parts()) {
            for (final MappingOperation operation : part.getOperations()) {
                if (operation instanceof Manipulator) {
                    triggerManipulation((Manipulator) operation, part);
                }
            }
            for (final MappingConfiguration subDatum : List.copyOf(part.getSubData())) {
                runOperations(subDatum);
            }
        }
    }

    private void triggerManipulation(final Manipulator operation, final MappingPart part) throws InvalidDatasetException {
        final List<DatasetPiece> partContent = part.getDataset().getContent();
        final List<DatasetPiece> content = partContent.isEmpty() ? List.of(new SimpleDatasetPiece(null)) :
                List.copyOf(partContent);
        for (final DatasetPiece datasetPiece : content) {
            operation.manipulate(datasetPiece, part);
            if (operation instanceof SingleCallForAllValuesManipulator) {
                break;
            }
        }
        if (partContent.isEmpty() && 1 == content.size() && content.get(0).value().isPresent()) {
            part.getDataset().collect(content.get(0));
        }
    }

    private void importMatchData(final MatchResult match) {
        for (int i = 0, m = match.groupCount(); i <= m; i++) {
            final int current = i;
            findParts(String.valueOf(i))
                    .forEach(a -> a.getDataset().collect(new SimpleDatasetPiece(match.group(current))));
        }
    }

    private List<MappingPart> findParts(final String path) {
        return subData.parts().stream()
                .filter(a -> null != a.getInput())
                .filter(a -> regexReference.equals(a.getInput().getReference()))
                .filter(a -> path.equals(a.getInput().getPath()))
                .collect(Collectors.toList());
    }

    private void resetSubData(final MappingConfiguration toReset, final MappingConfiguration parent) {
        toReset.parts().forEach(a -> {
            a.getDataset().clear();
            a.getSubData().forEach(subData -> {
                subData.clear();
                resetSubData(subData, toReset);
            });
        });
        toReset.setParent(parent);
    }


    private MappingConfiguration createSubDataMappingConfiguration(final SubData subData) {
        final MappingConfiguration result = new SimpleMappingConfiguration(null);
        subData.parts.forEach(subDataPart -> {
            final SimpleMappingPart part = new StableMappingPart(result, Optional.ofNullable(subDataPart.input)
                    .orElseGet(SimpleMappingPoint::new), Optional.ofNullable(subDataPart.output)
                    .orElseGet(SimpleMappingPoint::new));
            part.setOperations(Optional.ofNullable(subDataPart.operations).stream()
                    .flatMap(Collection::stream)
                    .collect(Collectors.toList()));
            Optional.ofNullable(subDataPart.subData)
                    .ifPresent(a -> part.getSubData().add(createSubDataMappingConfiguration(a)));
            result.parts().add(part);
        });
        return result;
    }

    @Required
    @Description("mapping configuration to handle found regex")
    public void setSubData(final SubData subData) {
        this.subData = createSubDataMappingConfiguration(subData);
    }

    public void setResultPortrayal(final MappingPortrayal resultPortrayal) {
        this.resultPortrayal = resultPortrayal;
    }

    public void setRegexReference(final String regexReference) {
        this.regexReference = regexReference;
    }

    public void setTooManyValuesStrategy(final TooManyValuesStrategy tooManyValuesStrategy) {
        this.tooManyValuesStrategy = tooManyValuesStrategy;
    }

}