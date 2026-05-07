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
package com.webxells.dis.base;

import com.webxells.dis.api.MappingPortrayal;
import com.webxells.dis.api.config.MappingPart;
import com.webxells.dis.api.config.MappingPoint;
import com.webxells.dis.api.config.description.Default;
import com.webxells.dis.api.config.description.Description;

import java.util.Optional;

@Description("Simple description of mapping part by source, reference and path")
public class SimpleMappingPortrayal implements MappingPortrayal {
    @Description("Path of mapping part")
    private String path;
    @Description("Reference of mapping part")
    private String reference;
    @Default("INPUT")
    @Description("Source of mapping part")
    private Source source = Source.INPUT;
    @Default("false")
    @Description("Raises an error if mapping part was not found")
    private boolean required;

    public static MappingPortrayal source(final MappingPart a) {
        return new SimpleMappingPortrayal(Source.INPUT, a.getInput().getReference(), a.getInput().getPath());
    }

    public static MappingPortrayal destination(final MappingPart a) {
        return new SimpleMappingPortrayal(Source.OUTPUT, a.getOutput().getReference(), a.getOutput().getPath());
    }
    public static MappingPortrayal auto(final MappingPart a) {
        return auto(a, Source.INPUT);
    }

    public static MappingPortrayal auto(final MappingPart a, final Source firstGuess) {
        final MappingPoint first = Source.INPUT == firstGuess ? a.getInput() : a.getOutput();
        if (null == first || null == first.getReference() || null == first.getPath()) {
            return Source.INPUT == firstGuess ? destination(a) : source(a);
        }
        return Source.INPUT == firstGuess ? source(a) : destination(a);
    }

    public SimpleMappingPortrayal() { }

    public SimpleMappingPortrayal(final String reference, final String path) {
        this.reference = reference;
        this.path = path;
    }

    public SimpleMappingPortrayal(final Source source, final String reference, final String path) {
        this.source = source;
        this.reference = reference;
        this.path = path;
    }

    @Override
    public String getPath() {
        return path;
    }

    @Override
    public String getReference() {
        return reference;
    }

    @Override
    public Source getSource() {
        return source;
    }

    @Override
    public boolean matches(final MappingPart mappingPart) {
        return null != mappingPart && Optional.ofNullable(source)
                .map(a -> source == Source.INPUT ? mappingPart.getInput() : mappingPart.getOutput())
                .filter(a -> matchProperty(a.getReference(), reference))
                .filter(a -> matchProperty(a.getPath(), path))
                .isPresent();
    }

    @Override
    public boolean isRequired() {
        return required;
    }

    public void setRequired(final boolean isRequired) {
        required = isRequired;
    }

    private boolean matchProperty(final String value1, final String value2) {
        return (null == value1 && null == value2) || (null != value1 && value1.equals(value2));
    }

    public void setPath(final String path) {
        this.path = path;
    }

    public void setReference(final String reference) {
        this.reference = reference;
    }

    public void setSource(final Source source) {
        this.source = source;
    }
}