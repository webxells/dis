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
package com.webxells.dis.base.manipulator.setter;

import com.webxells.dis.api.DatasetPiece;
import com.webxells.dis.api.MappingPortrayal;
import com.webxells.dis.api.config.MappingConfiguration;
import com.webxells.dis.api.config.MappingPart;
import com.webxells.dis.api.config.Refinement;
import com.webxells.dis.api.config.description.Description;
import com.webxells.dis.api.config.description.Required;
import com.webxells.dis.api.error.DisException;
import com.webxells.dis.api.error.InvalidApi;
import com.webxells.dis.api.input.linker.JoinLinker;
import com.webxells.dis.base.SimpleDatasetPiece;
import com.webxells.dis.base.SimpleMappingPortrayal;
import com.webxells.dis.base.config.SimpleMappingConfiguration;
import com.webxells.dis.base.config.SimpleMappingPoint;
import com.webxells.dis.base.config.StableMappingPart;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Description("Loads data into the current mapping part from a new source")
public class InputSetter implements OverruleSetter {
    public static class OverwriteMapping {
        @Required
        public String sourcePath;
        @Required
        public MappingPortrayal target;
        public void validate() throws InvalidApi {
            if (null == sourcePath || null == target) {
                throw new InvalidApi("OverwriteMapping incomplete");
            }
        }

    }

    @Required
    @Description("Links input datasource to read data from")
    private JoinLinker linker;

    @Required(xor = {"runValuesAs","overwrites"})
    @Description("Defines path to locate value in input data")
    private String valueSourcePath;
    @Required(xor = {"runValuesAs","valueSourcePath"})
    @Description("Sets data of mapping parts with values from this input")
    private List<OverwriteMapping> overwrites;
    @Description("Uses data from other mappings to be used in loading the source, e.g. in a url")
    private List<MappingPortrayal> readOnly;
    private String instanceName = String.valueOf(System.identityHashCode(this));
    @Required(xor = {"valueSourcePath","overwrites"})
    @Description("Uses data from a mapping part in JoinLinker")
    private MappingPortrayal runValuesAs;

    @Override
    public void validate() throws InvalidApi {
        if (null == linker || ((null == overwrites || overwrites.isEmpty()) &&
                null == runValuesAs && null == valueSourcePath)) {
            throw new InvalidApi("Essential properties not provided");
        }
        if (null != overwrites) {
            for (final OverwriteMapping overwrite : overwrites) {
                overwrite.validate();
            }
        }

    }

    @Override
    public String getValue(final DatasetPiece currentPiece, final MappingPart mappingPart) {
        try {
            Optional.ofNullable(linker.getInputName())
                    .ifPresent(a -> instanceName = a);
            final MappingConfiguration configuration = createOwnConfig(currentPiece, mappingPart.getConfiguration(), mappingPart.getRefinements());
            linker.start();
            final int readData = linker.getData(configuration);
            linker.end();
            if (0 < readData) {
                overwriteData(configuration, mappingPart.getConfiguration());
            }
            return Optional.ofNullable(valueSourcePath)
                    .flatMap(a -> configuration.getByPortrayal(
                            new SimpleMappingPortrayal(instanceName, valueSourcePath))
                            .map(MappingPart::value))
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .orElse(null);
        } catch (DisException e) {
            throw new RuntimeException("Linker failed", e);
        }
    }

    public void setReadOnly(final List<MappingPortrayal> readOnly) {
        this.readOnly = readOnly;
    }

    private MappingConfiguration createOwnConfig(final DatasetPiece currentPiece, final MappingConfiguration rootConfiguration,
                                                 final List<Refinement> refinements) {
        final SimpleMappingConfiguration configuration = new SimpleMappingConfiguration();
        createRunAsValuePart(currentPiece, configuration, refinements);
        createValueSourcePart(configuration);
        createReadOnlyParts(rootConfiguration, configuration);
        createOverwriteParts(rootConfiguration, configuration);
        return configuration;
    }

    private void createOverwriteParts(final MappingConfiguration rootConfiguration, final SimpleMappingConfiguration configuration) {
        Optional.ofNullable(overwrites).stream()
                .flatMap(Collection::stream)
                .flatMap(a -> copyPart(a, rootConfiguration, configuration).stream())
                .forEach(configuration::addPart);
    }

    private Optional<MappingPart> copyPart(final OverwriteMapping mapping, final MappingConfiguration old, final SimpleMappingConfiguration newConfiguration) {
        final Optional<MappingPart> result = old.getByPortrayal(mapping.target)
                .map(a -> a.copy(newConfiguration));
        result.ifPresent(a -> {
            a.setInput(new SimpleMappingPoint(instanceName, mapping.sourcePath));
            a.setOutput(new SimpleMappingPoint(instanceName, mapping.target.getPath()));
        });
        return result;
    }

    private void createReadOnlyParts(final MappingConfiguration rootConfiguration, final SimpleMappingConfiguration configuration) {
        Optional.ofNullable(readOnly).ifPresent(list ->
                configuration.setParts(list.stream()
                        .map(a -> new StableMappingPart(configuration, new SimpleMappingPoint()
                                , new SimpleMappingPoint(instanceName, a.getPath())) {{
                            rootConfiguration.getByPortrayal(a).ifPresent(
                                    b -> {
                                        b.getDataset().getContent()
                                                .forEach(c -> getDataset().collect(new SimpleDatasetPiece(c.value().orElse(null))));
                                        b.getSubData()
                                                .forEach(c  -> addSubData(c.copy()));
                                    });
                        }})
                        .collect(Collectors.toList())
                ));
    }

    private void createValueSourcePart(final SimpleMappingConfiguration configuration) {
        Optional.ofNullable(valueSourcePath).ifPresent(a -> configuration.addPart(new StableMappingPart(configuration,
            new SimpleMappingPoint(instanceName, a),
                new SimpleMappingPoint(instanceName, a))));
    }

    private void createRunAsValuePart(final DatasetPiece currentPiece, final SimpleMappingConfiguration configuration, final List<Refinement> refinements) {
        Optional.ofNullable(runValuesAs)
                .ifPresent(a -> configuration.addPart(new StableMappingPart(configuration,
                        new SimpleMappingPoint(instanceName, a.getPath()),
                        new SimpleMappingPoint(instanceName,a.getPath())) {{
                            getDataset().collect(currentPiece);
                            addAllRefinements(refinements);
                }}));
    }

    private void overwriteData(final MappingConfiguration copiedConfiguration, final MappingConfiguration configuration) {
        Optional.ofNullable(overwrites)
                .ifPresent(list -> list.forEach(a -> {
                    copiedConfiguration.getByPortrayal(new SimpleMappingPortrayal(instanceName, a.sourcePath))
                            .ifPresent(b -> configuration.getByPortrayal(a.target)
                                .ifPresent(c -> {
                                    c.getDataset().clear();
                                    c.getDataset().collect(b.getDataset().getContent());
                                    c.getSubData().clear();
                                    c.getSubData().addAll(resetParent(b.getSubData(), configuration));
                                }));
                }));
    }

    private List<MappingConfiguration> resetParent(final List<MappingConfiguration> subData,
                                                   final MappingConfiguration parent) {
        subData.forEach(a -> a.setParent(parent));
        return subData;
    }

    @Override
    public String getType() {
        return InputSetter.class.getName();
    }

    public void setLinker(final JoinLinker linker) {
        this.linker = linker;
    }

    public void setValueSourcePath(final String valueSourcePath) {
        this.valueSourcePath = valueSourcePath;
    }

    public void setOverwrites(final List<OverwriteMapping> overwrites) {
        this.overwrites = overwrites;
    }

    public void setRunValuesAs(final MappingPortrayal runValuesAs) {
        this.runValuesAs = runValuesAs;
    }
}