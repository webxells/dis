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
package com.webxells.dis.base.config;

import com.webxells.dis.api.Dataset;
import com.webxells.dis.api.DatasetPiece;
import com.webxells.dis.api.Logger;
import com.webxells.dis.api.MappingOperation;
import com.webxells.dis.api.config.MappingConfiguration;
import com.webxells.dis.api.config.MappingPart;
import com.webxells.dis.api.config.MappingPoint;
import com.webxells.dis.api.config.Refinement;
import com.webxells.dis.api.config.description.Default;
import com.webxells.dis.api.config.description.Description;
import com.webxells.dis.api.validator.Validator;
import com.webxells.dis.base.SimpleDataset;
import com.webxells.dis.base.SimpleDatasetPiece;
import com.webxells.dis.logging.LoggerProxyFactory;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Description("Simple / default MappingPart (unstable)")
public class SimpleMappingPart implements MappingPart {
    private static final Logger LOGGER = LoggerProxyFactory.logger(SimpleMappingPart.class);

    private MappingPoint input;
    private MappingPoint output;
    private List<MappingOperation> operations;
    @Description("Strategy" +
            " to handle failing data validations")
    private Validator.ErrorStrategy validatorErrorStrategy = Validator.ErrorStrategy.CONTINUE_NEXT_READ;
    @Description("Strategy to pick single value on multiple values present")
    @Default("FIRST")
    private MappingPart.MultiToSingleSelectStrategy multiToSingleSelectStrategy = MultiToSingleSelectStrategy.FIRST;
    private final MappingConfiguration configuration;
    private final Dataset dataset = new SimpleDataset();
    @Description("Mapping configuration within this MappingPart")
    private final List<MappingConfiguration> subData = new ArrayList<>();
    @Description("Refinements are special flags that may alter normal workflow of several modules")
    private final List<Refinement> refinements = new ArrayList<>();


    public SimpleMappingPart(final MappingConfiguration configuration) {
        this.configuration = configuration;
    }

    public SimpleMappingPart(final MappingConfiguration configuration, final MappingPoint input,
                             final MappingPoint output) {
        this(configuration);
        this.input = Objects.requireNonNull(input);
        this.output =Objects.requireNonNull(output);
    }

    @Override
    public String toString() {
        return "MappingPart{" +
                "in=" + input +
                ", out=" + output +
                ", dataset(" + dataset.getContent().size() + ")=" + dataset +
                ", operations=" + (null == operations ? null : operations.size()) +
                ", subData=" + subData.size() +
                ", refinements =" + refinements.size() +
                ", validatorErrorStrategy=" + validatorErrorStrategy +
                ", multiToSingleSelectStrategy=" + multiToSingleSelectStrategy +
                '}';
    }

    @Override
    public MappingPoint getInput() {
        return input;
    }

    @Override
    public MappingPoint getOutput() {
        return output;
    }

    @Override
    public Dataset getDataset() {
        return dataset;
    }

    @Override
    public Optional<String> value() {
        return multiToSingle(dataset.getContent())
                .flatMap(DatasetPiece::value);
    }

    protected <T> Optional<T> multiToSingle(final List<T> content) {
        if (!content.isEmpty()) {
            switch (multiToSingleSelectStrategy) {
                case ERROR:
                    if (content.size() > 1) {
                        throw new RuntimeException("Unexpected multi value occurrence:");
                    }
                case FIRST:
                    return Optional.of(content.getFirst());
                case LAST:
                    return Optional.of(content.getLast());
            }
        }
        return Optional.empty();
    }

    @Override
    public MappingConfiguration getConfiguration() {
        return configuration;
    }

    @Override
    public List<MappingOperation> getOperations() {
        return operations;
    }

    @Override
    public Validator.ErrorStrategy getValidatorErrorStrategy() {
        return validatorErrorStrategy;
    }

    @Override
    public List<MappingConfiguration> getSubData() {
        return subData;
    }

    @Override
    public boolean hasRefinement(final Class<? extends Refinement> clazz) {
        return getDependingRefinements().anyMatch(a -> clazz.isAssignableFrom(a.getClass()));
    }

    @Override
    public <T extends Refinement> Optional<T> getFirstRefinement(final Class<T> clazz) {
        return getRefinements(clazz)
                .findFirst();
    }

    @Override
    public <T extends Refinement> List<T> getAllRefinements(final Class<T> clazz) {
        return getRefinements(clazz)
                .toList();
    }

    @Override
    public List<Refinement> getRefinements() {
        return refinements;
    }

    @SuppressWarnings("unchecked")
    private <T extends Refinement> Stream<T> getRefinements(final Class<T> clazz) {
        return getDependingRefinements()
                .filter(a -> clazz.isAssignableFrom(a.getClass()))
                .map(a -> (T) a);
    }

    private Stream<Refinement> getDependingRefinements() {
        final List<String> stackTrace = Arrays.stream(Thread.currentThread().getStackTrace())
                .map(StackTraceElement::getClassName)
                .toList();
        return refinements.stream()
                .filter(a -> a.getCoveredClasses().isEmpty() ||
                        a.getCoveredClasses().stream().anyMatch(stackTrace::contains));
    }

    @Override
    public void clear() {
        clearDataset();
        final List<MappingConfiguration> subData = getSubData();
        if (!subData.isEmpty()) {
            final MappingConfiguration firstSubData = getSubData().get(0);
            subData.retainAll(List.of(firstSubData));
            firstSubData.clear();
            if (firstSubData.parts().isEmpty()) {
                subData.removeFirst();
            }
        }
    }

    protected void clearDataset() {
        getDataset().clear();
    }


    @Override
    public MappingPart copy() {
        return copy(configuration);
    }

    @Override
    public MappingPart copy(final MappingConfiguration configuration) {
        final MappingPart result = getACopy(configuration);
        Optional.ofNullable(input).ifPresent(a ->
                result.setInput(new SimpleMappingPoint(a.getReference(), a.getPath())));
        Optional.ofNullable(output).ifPresent(a ->
                result.setOutput(new SimpleMappingPoint(a.getReference(), a.getPath())));
        Optional.ofNullable(operations)
                .ifPresent(a -> result.setOperations(List.copyOf(a)));
        result.addAllRefinements(refinements);
        result.copyValues(this);
        result.setMultiToSingleSelectStrategy(multiToSingleSelectStrategy);
        result.setValidatorErrorStrategy(validatorErrorStrategy);
        return result;
    }

    protected MappingPart getACopy(final MappingConfiguration configuration) {
        return new SimpleMappingPart(configuration);
    }

    @Override
    public void copyValues(final MappingPart other) {
        other.getSubData().forEach(
                a -> addSubData(a.copy(configuration)));
        other.getDataset().getContent().forEach(
                a -> dataset.collect(new SimpleDatasetPiece(a.value().orElse(null))));
    }

    @Override
    public MultiToSingleSelectStrategy getMultiToSingleSelectStrategy() {
        return multiToSingleSelectStrategy;
    }

    @Override
    public void setValidatorErrorStrategy(final Validator.ErrorStrategy errorStrategy) {
        validatorErrorStrategy = errorStrategy;
    }

    @Override
    public void addSubData(final MappingConfiguration subConfiguration) {
        copyMeta(configuration, subConfiguration);
        subData.add(subConfiguration);
    }

    @Override
    public void addSubData(final List<MappingConfiguration> subConfiguration) {
        subConfiguration.forEach(a -> copyMeta(configuration, a));
        subData.addAll(subConfiguration);
    }

    @Override
    @Deprecated
    public void setSubData(final List<MappingConfiguration> subConfiguration) {
        if (0 < subConfiguration.size()) {
            LOGGER.w("subData must not be defined as list");
            addSubData(subConfiguration);
        }
    }

    @Override
    public void setSubData(final MappingConfiguration subData) {
        addSubData(subData);
    }

    public void setRefinements(final List<Refinement> refinements) {
        addAllRefinements(refinements);
    }

    @Override
    public void addAllRefinements(final List<Refinement> refinements) {
        this.refinements.addAll(Objects.requireNonNull(refinements));
    }

    @Override
    public void addRefinement(final Refinement refinement) {
        refinements.add(refinement);
    }

    @Override
    public void setInput(final MappingPoint input) {
        this.input = Objects.requireNonNull(input);
    }

    @Override
    public void setOperations(final List<MappingOperation> operations) {
        this.operations = operations;
    }

    @Override
    public void setOutput(final MappingPoint output) {
        this.output = Objects.requireNonNull(output);
    }

    public void setMultiToSingleSelectStrategy(final MultiToSingleSelectStrategy multiToSingleSelectStrategy) {
        this.multiToSingleSelectStrategy = multiToSingleSelectStrategy;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final SimpleMappingPart that = (SimpleMappingPart) o;
        return Objects.equals(input, that.input) &&
                Objects.equals(output, that.output) &&
                Objects.equals(operations, that.operations) &&
                Objects.equals(refinements, that.refinements) &&
                subDataIsEqual(that.subData) &&
                validatorErrorStrategy == that.validatorErrorStrategy &&
                multiToSingleSelectStrategy == that.multiToSingleSelectStrategy;
    }

    @Override
    public int hashCode() {
        return Objects.hash(input, output, operations, validatorErrorStrategy, multiToSingleSelectStrategy, refinements,
                subData.stream().flatMap(a -> a.parts().stream()).collect(Collectors.toList()));
    }

    private void copyMeta(final MappingConfiguration configuration, final MappingConfiguration subConfiguration) {
        subConfiguration.setParent(configuration);
        subConfiguration.setJobConfig(configuration.getJobConfig());
    }

    private boolean subDataIsEqual(final List<MappingConfiguration> otherSubData) {
        if (otherSubData != null && otherSubData.size() == subData.size()) {
            for (int i = subData.size() - 1; i >= 0; i--) {
                if (!subData.get(i).parts().equals(otherSubData.get(i).parts())) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }
}