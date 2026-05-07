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
import com.webxells.dis.api.config.MappingPoint;
import com.webxells.dis.api.config.OutputConfig;
import com.webxells.dis.api.config.description.Default;
import com.webxells.dis.api.config.description.Description;
import com.webxells.dis.api.config.description.Internal;
import com.webxells.dis.api.config.description.Required;
import com.webxells.dis.api.error.DisException;
import com.webxells.dis.api.error.InvalidApi;
import com.webxells.dis.api.output.Output;
import com.webxells.dis.base.config.SimpleMappingPoint;
import com.webxells.dis.base.resource.StringResource;
import com.webxells.dis.boot.ServiceManager;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Description("Overwrite by taking result of an Output configuration")
public class OutputSetter implements OverruleSetter {
    @Required
    @Description("Result will be written in current mapping part")
    private OutputConfig output;
    @Description("Takes all existing mapping parts into account")
    @Default("false")
    private boolean skipReferenceValidation;
    @Description("Mapping parts with this reference will used for the output")
    private String aliasReference;
    @Description("Defines mapping parts to use for the output")
    private List<MappingPortrayal> mapping;
    private StringResource stringResource;
    @Description("Does not write value into current mapping part")
    @Default("false")
    private boolean skipWriting;

    @Override
    public String getValue(final DatasetPiece currentPiece, final MappingPart mappingPart) {
        return fromOutput(startOutput(), mappingPart.getConfiguration());
    }

    private String fromOutput(final Output<?> output, final MappingConfiguration configuration) {
        try {
            output.start();
            output.write(createConfiguration(configuration));
            final String result = skipWriting ? null : stringResource.getOutput().toString();
            output.end();
            return result;
        } catch (final DisException e) {
            throw new RuntimeException("Something failed on generating output", e);
        }
    }

    private MappingConfiguration createConfiguration(final MappingConfiguration configuration) {
        final MappingConfiguration copied = configuration.copy();
        final String outputName = output.getName();
        if (!skipReferenceValidation) {
            copied.parts().retainAll(getRetainingParts(copied));
        }
        copied.parts().forEach(a -> a.setOutput(new SimpleMappingPoint(outputName, getPath(a))));
        return copied;
    }

    private Collection<MappingPart> getRetainingParts(final MappingConfiguration configuration) {
        if (null == aliasReference) {
            return mapping.stream()
                    .flatMap(a -> configuration.getByPortrayal(a).stream())
                    .collect(Collectors.toList());
        }
        return configuration.parts().stream()
                .filter(a -> Optional.ofNullable(a.getOutput())
                        .map(MappingPoint::getReference)
                        .isPresent())
                .filter(a -> aliasReference.equals(a.getOutput().getReference()))
                .collect(Collectors.toList());
    }

    public void setAliasReference(final String aliasReference) {
        this.aliasReference = aliasReference;
    }

    private String getPath(final MappingPart a) {
        if (null != a) {
            if (hasValidPath(a.getOutput())) {
                return a.getOutput().getPath();
            }
            if (hasValidPath(a.getInput())) {
                return a.getInput().getPath();
            }
        }
        return null;
    }

    private boolean hasValidPath(final MappingPoint mappingPoint) {
        return !(null == mappingPoint || null == mappingPoint.getPath());
    }

    private Output<?> startOutput() {
        if (!skipWriting) {
            stringResource = new StringResource();
            ServiceManager.overwriteResourceOfConfig(output, stringResource);
        }
        return ServiceManager.loadByConfig(output);
    }

    @Override
    public void validate() throws InvalidApi {
        if (null == output || null == output.getName() ||
                (!skipReferenceValidation && (
                        (null == mapping || mapping.isEmpty()) &&
                        null == aliasReference))) {
            throw new InvalidApi("Required properties missing");
        }
    }

    public void setOutput(final OutputConfig outputConfig) {
        this.output = outputConfig;
    }

    public void setSkipReferenceValidation(final boolean skipReferenceValidation) {
        this.skipReferenceValidation = skipReferenceValidation;
    }

    public void setMapping(final List<MappingPortrayal> mapping) {
        this.mapping = mapping;
    }

    public void setSkipWriting(final boolean skipWriting) {
        this.skipWriting = skipWriting;
    }
}
