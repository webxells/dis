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
package com.webxells.dis.xml;

import com.webxells.dis.api.config.MappingConfiguration;
import com.webxells.dis.api.config.MappingPart;
import com.webxells.dis.api.error.DisException;
import com.webxells.dis.api.error.InputOutputError;
import com.webxells.dis.api.input.Input;
import com.webxells.dis.base.SimpleDatasetPiece;
import com.webxells.dis.xml.internal.MultiPath;
import com.webxells.dis.xml.internal.XmlParsingError;
import com.webxells.dis.xml.internal.XmlPath;
import com.webxells.dis.xml.internal.XmlReader;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.xml.stream.XMLStreamException;

public class XmlInput implements Input<XmlConfig> {
    private XmlConfig config;
    private XmlReader xmlReader;

    public XmlInput(final XmlConfig config) {
        this.config = config;
    }

    @Override
    public int read(final MappingConfiguration mappingConfiguration) throws InputOutputError {
        try {
            final Map<MappingPart, XmlPath> path = new HashMap<>();
            final List<String> rawReads = Optional.ofNullable(config.getRawReads()).orElse(List.of());
            final List<XmlPath> rawReadsMapping = new LinkedList<>();
            for (final MappingPart part : filterParts(mappingConfiguration)) {
                final XmlPath xmlPath = convertToPath(part);
                if (!(xmlPath instanceof MultiPath) && rawReads.contains(part.getInput().getPath())) {
                    rawReadsMapping.add(xmlPath);
                }
                path.put(part, xmlPath);
            }
            xmlReader.setRawReads(rawReadsMapping);
            final Map<XmlPath, XmlPath> result = xmlReader.readNext(new HashSet<>(path.values()));
            assignResultsToMappingParts(result, path, false);
            return result.keySet().stream().mapToInt(this::countResult).sum();
        } catch (final Exception e) {
            throw new InputOutputError("Error reading xml input", e);
        }
    }

    private List<MappingPart> filterParts(final MappingConfiguration mappingConfiguration) {
        return mappingConfiguration.partsBySource(config.getName()).stream()
                .filter(a -> null != a.getInput() && null != a.getInput().getPath())
                .collect(Collectors.toList());
    }

    private int countResult(final XmlPath xmlPath) {
        return xmlPath instanceof MultiPath ?
                    ((MultiPath) xmlPath).getValues().stream()
                        .flatMap(Collection::stream)
                        .flatMap(a -> a.keySet().stream())
                        .mapToInt(this::countResult).sum() : null == xmlPath.getValue() ? 0 : 1;
    }

    private void assignResultsToMappingParts(final Map<XmlPath, XmlPath> result, final Map<MappingPart, XmlPath> path,
            final boolean inMultiContext) {
        for (final MappingPart part : path.keySet()) {
            if (result.containsKey(path.get(part))) {
                final XmlPath resultPath = result.get(path.get(part));
                if (!part.getSubData().isEmpty() && resultPath instanceof MultiPath) {
                    assignMultiPath(part, (MultiPath) resultPath);
                } else {
                    if (null != resultPath.getValue() || inMultiContext) {
                        part.getDataset().collect(new SimpleDatasetPiece(resultPath.getValue()));
                    }
                }
            }
        }
    }

    private void assignMultiPath(final MappingPart part, final MultiPath multiPath) {
        final List<List<Map<XmlPath, XmlPath>>> values = multiPath.getValues();
        final Map<MappingPart, XmlPath> children = multiPath.getChildren();
        for (int i = 0, m = values.size(); i < m; i++) {
            final List<Map<XmlPath, XmlPath>> currentValues = values.get(i);
            assignMultiPathValuesToChildren(currentValues, children, getAssociatedSubData(part, i));
        }
    }

    private void assignMultiPathValuesToChildren(final List<Map<XmlPath, XmlPath>> values,
                                                 final Map<MappingPart, XmlPath> children,
                                                 final List<MappingPart> subData) {
        for (final Map<XmlPath, XmlPath> currentValue : values) {
            final Map<MappingPart, XmlPath> newChildren = subData.stream()
                    .filter(children::containsKey)
                    .collect(Collectors.toMap(a -> a, a -> currentValue.get(children.get(a))));
            assignResultsToMappingParts(currentValue, newChildren, true);
        }
    }

    private List<MappingPart> getAssociatedSubData(final MappingPart part, final int index) {
        final MappingConfiguration root = part.getSubData().get(0);
        while(part.getSubData().size() <= index) {
            final MappingConfiguration copy = root.copy();
            copy.clear();
            part.getSubData().add(copy);
        }
        return part.getSubData().get(index).parts();
    }

    private XmlPath convertToPath(final MappingPart part) throws XmlParsingError {
        if (part.getSubData().isEmpty()) {
            return new XmlPath(part.getInput().getPath());
        }
        final Map<MappingPart, XmlPath> children = new HashMap<>();
        for (final MappingPart mappingPart : part.getSubData().get(0).parts()) {
            if (null != mappingPart.getInput() && config.getName().equals(mappingPart.getInput().getReference())) {
                final XmlPath xmlPath = convertToPath(mappingPart);
                children.put(mappingPart, xmlPath);
            }
        }
        return new MultiPath(part.getInput().getPath(), children);
    }

    @Override
    public boolean hasNext() throws InputOutputError {
        try {
            return xmlReader.hasNext();
        } catch (XMLStreamException e) {
            throw new InputOutputError("Error peeking", e);
        }
    }

    @Override
    public String getName() {
        return config.getName();
    }

    @Override
    public void start() throws DisException {
        try {
            xmlReader = new XmlReader(config.getReceiver().receive(), config);
        } catch (XMLStreamException | XmlParsingError e) {
            throw new InputOutputError("Invalid input resource", e);
        }
    }

    @Override
    public void end() throws DisException {
        try {
            xmlReader.close();
        } catch (XMLStreamException e) {
            throw new InputOutputError("Some error closing xml source occured", e);
        }
    }
}