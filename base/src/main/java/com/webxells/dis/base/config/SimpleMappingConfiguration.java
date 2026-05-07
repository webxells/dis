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

import com.webxells.dis.api.MappingPortrayal;
import com.webxells.dis.api.config.JobConfig;
import com.webxells.dis.api.config.MappingConfiguration;
import com.webxells.dis.api.config.MappingPart;
import com.webxells.dis.api.config.MappingPoint;
import com.webxells.dis.api.config.description.Internal;
import com.webxells.dis.base.SimpleMappingPortrayal;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class SimpleMappingConfiguration implements MappingConfiguration {
    private final List<MappingPart> parts = new LinkedList<>();
    private JobConfig jobConfig;
    private MappingConfiguration parent;

    public SimpleMappingConfiguration() { }
    public SimpleMappingConfiguration(final JobConfig jobConfig) {
        this.jobConfig = jobConfig;
    }

    public void setParts(final List<MappingPart> parts) {
        this.parts.addAll(parts);
    }

    public void addPart(final MappingPart part) {
        parts.add(part);
    }

    @Override
    public List<MappingPart> parts() {
        return parts;
    }

    @Override
    public List<MappingPart> partsBySource(final String source) {
        return parts.stream()
                .filter(a -> filterMappingPointByReference(a.getInput(), source))
                .collect(Collectors.toList());
    }

    @Override
    public List<MappingPart> partsByDestination(final String destination) {
        return parts.stream()
                .filter(a -> filterMappingPointByReference(a.getOutput(), destination))
                .collect(Collectors.toList());
    }

    @Override
    public MappingConfiguration copy() {
        return copy(parent);
    }

    @Override
    public MappingConfiguration copy(final MappingConfiguration parent) {
        final SimpleMappingConfiguration result = new SimpleMappingConfiguration();
        result.setParent(parent);
        result.setJobConfig(jobConfig);
        parts.stream()
                .map(a -> a.copy(result))
                .forEach(result::addPart);
        return result;
    }

    @Override
    public Optional<MappingConfiguration> parent() {
        return Optional.ofNullable(parent);
    }

    @Override
    public Optional<MappingPart> getByPortrayal(final MappingPortrayal portrayal) {
        return byPortrayal(portrayal).stream()
                .findAny();
    }

    @Override
    public List<MappingPart> getAllByPortrayal(final MappingPortrayal portrayal) {
        return byPortrayal(portrayal);
    }

    private List<MappingPart> byPortrayal(final MappingPortrayal portrayal) {
        final List<MappingPart> result = parts.stream()
                .filter(a -> {
                    final MappingPoint mappingPoint = SimpleMappingPortrayal.Source.INPUT.equals(portrayal.getSource()) ?
                            a.getInput() : a.getOutput();
                    return null != mappingPoint && portrayalEqualsMappingPoint(portrayal.getReference(),
                            mappingPoint.getReference()) && portrayalEqualsMappingPoint(portrayal.getPath(),
                            mappingPoint.getPath());
                }).collect(Collectors.toList());
        if (portrayal.isRequired() && result.isEmpty()) {
            throw new RuntimeException("mandatory part not found");
        }
        return result;
    }

    private boolean portrayalEqualsMappingPoint(final String valueOfPortrayal, final String valueOfMapping) {
        if (null == valueOfPortrayal && null == valueOfMapping) {
            return true;
        }
        if (null == valueOfPortrayal || null == valueOfMapping) {
            return false;
        }
        return valueOfPortrayal.equals(valueOfMapping);
    }


    @Override
    public int size() {
        return parts.size();
    }

    @Override
    public void clear() {
        retainStableParts(parts());
        parts().forEach(MappingPart::clear);
    }

    @Override
    public String getJobName() {
        return jobConfig.getName();
    }

    @Override
    public JobConfig getJobConfig() {
        return jobConfig;
    }

    @Internal
    @Override
    public void setJobConfig(final JobConfig jobConfig) {
        this.jobConfig = jobConfig;
        parts.stream()
                .flatMap(a -> a.getSubData().stream())
                .forEach(a -> a.setJobConfig(jobConfig));
    }

    private void retainStableParts(final List<MappingPart> list) {
        list.retainAll(list.stream()
                .filter(MappingPart::isStable)
                .collect(Collectors.toList()));
    }

    private boolean filterMappingPointByReference(final MappingPoint point, final String reference) {
        return null != point && null != point.getReference() && point.getReference().equals(reference);
    }

    @Override
    @Internal
    public void setParent(final MappingConfiguration parent) {
        this.parent = parent;
    }
}