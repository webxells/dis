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
package com.webxells.dis.test.cases;

import com.webxells.dis.api.DatasetPiece;
import com.webxells.dis.api.config.MappingConfiguration;
import com.webxells.dis.api.config.MappingPart;
import com.webxells.dis.api.config.MappingPoint;
import com.webxells.dis.api.config.Refinement;
import com.webxells.dis.base.config.SimpleMappingConfiguration;
import com.webxells.dis.base.config.SimpleMappingPoint;
import com.webxells.dis.base.config.StableMappingPart;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static com.webxells.dis.test.cases.SimpleTestCase.createDataSetPiece;
import static com.webxells.dis.test.cases.SimpleTestCase.random;

public class ConfigurationBuilder {
    public static class PartBuilder {
        public enum PartBuilderState {
            IGNORE, RANDOM
        }

        private final PartBuilderState state;
        private final List<DatasetPiece> content = new LinkedList<>();
        private final List<MappingConfiguration> subData = new LinkedList<>();

        private MappingPoint input;
        private MappingPoint output;
        private String inputReference;
        private String outputReference;
        private List<Refinement> refinements = new LinkedList<>();

        public PartBuilder() {
            this(PartBuilderState.IGNORE);
        }

        public PartBuilder(PartBuilderState state) {
            this.state = state;
        }

        public PartBuilder setInput(MappingPoint input) {
            this.input = input;
            return this;
        }

        public PartBuilder setInput(String reference, String path) {
            this.input = new SimpleMappingPoint(reference, path);
            return this;
        }

        public PartBuilder setOutput(MappingPoint output) {
            this.output = output;
            return this;
        }

        public PartBuilder setOutput(String reference, String path) {
            this.output = new SimpleMappingPoint(reference, path);
            return this;
        }

        public PartBuilder setInputReference(String reference) {
            this.inputReference = reference;
            return this;
        }

        public PartBuilder setOutputReference(String reference) {
            this.outputReference = reference;
            return this;
        }

        public PartBuilder withRefinements(Refinement... refinements) {
            this.refinements = Arrays.asList(refinements);
            return this;
        }


        public PartBuilder withContent(String... strings) {
            return withContent(Arrays.asList(strings));
        }


        public PartBuilder withContent(List<String> stringList) {
            content.addAll(stringList.stream()
                    .map(SimpleTestCase::createDataSetPiece)
                    .toList());
            return this;
        }


        public PartBuilder withContent() {
            content.add(createDataSetPiece());
            return this;
        }

        public PartBuilder withNullContent() {
            content.add(createDataSetPiece(null));
            return this;
        }

        public PartBuilder addSubData(MappingConfiguration configuration) {
            subData.add(configuration);
            return this;
        }

        public PartBuilder addSameSubData(MappingConfiguration configuration) {
            if (subData.size() > 0) {
                overwriteMappingPointsInSubData(configuration, subData.get(0));
            }
            subData.add(configuration);
            return this;
        }

        private void overwriteMappingPointsInSubData(final MappingConfiguration configuration,
                                                     final MappingConfiguration toCopy) {
            if (configuration.size() != toCopy.size()) {
                throw new RuntimeException("SubData sizes dont match");
            }
            for (int i = 0, m = toCopy.size(); i < m; i++) {
                MappingPart partToCopy = toCopy.parts().get(i);
                MappingPart partToCopyTo = configuration.parts().get(i);

                partToCopyTo.setInput(partToCopy.getInput());
                partToCopyTo.setOutput(partToCopy.getOutput());
                if (partToCopy.getSubData().size() > 0) {
                    if (partToCopy.getSubData().size() != partToCopyTo.getSubData().size()) {
                        throw new RuntimeException("SubSubData sizes dont match");
                    }
                    for (int j = 0, n = partToCopy.getSubData().size(); j < n; j++) {
                        overwriteMappingPointsInSubData(partToCopy.getSubData().get(j),
                                partToCopyTo.getSubData().get(j));
                    }
                }
            }
        }

        protected MappingPart build(MappingConfiguration configuration) {
            StableMappingPart part = new StableMappingPart(configuration);
            createMappingPoint(part::setInput, input, inputReference);
            createMappingPoint(part::setOutput, output, outputReference);
            part.getDataset().collect(content);
            subData.forEach(a -> a.setParent(configuration));
            part.getSubData().addAll(subData);
            part.addAllRefinements(refinements);
            return part;
        }

        private void createMappingPoint(Consumer<MappingPoint> consumer, MappingPoint mappingPoint,
                                        String reference) {
            if (null != mappingPoint || null != reference || PartBuilderState.RANDOM == state) {
                consumer.accept(Optional.ofNullable(mappingPoint)
                        .orElse(new SimpleMappingPoint(
                                Optional.ofNullable(reference).orElse(random()), random())));
            }
        }
    }

    private String input;
    private String output;

    private final List<MappingPart> parts = new LinkedList<>();
    protected final SimpleMappingConfiguration configuration = new SimpleMappingConfiguration();

    public static PartBuilder newPart() {
        return new PartBuilder();
    }

    public static PartBuilder newPart(PartBuilder.PartBuilderState state) {
        return new PartBuilder(state);
    }

    public ConfigurationBuilder() {
    }

    public ConfigurationBuilder setFollowingInputResource(String name) {
        input = name;
        return this;
    }

    public ConfigurationBuilder setFollowingOutputResource(String name) {
        output = name;
        return this;
    }

    public ConfigurationBuilder addPart(PartBuilder partBuilder) {
        parts.add(partBuilder
                .setInputReference(input)
                .setOutputReference(output)
                .build(configuration));
        return this;
    }

    public ConfigurationBuilder addPart(MappingPart mappingPart) {
        parts.add(mappingPart);
        return this;
    }

    public SimpleMappingConfiguration build() {
        configuration.setParts(parts);
        return configuration;
    }
}