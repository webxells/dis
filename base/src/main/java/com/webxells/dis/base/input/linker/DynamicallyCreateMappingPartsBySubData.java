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
package com.webxells.dis.base.input.linker;

import com.webxells.dis.api.MappingPortrayal;
import com.webxells.dis.api.config.MappingConfiguration;
import com.webxells.dis.api.config.MappingPart;
import com.webxells.dis.api.config.description.Description;
import com.webxells.dis.api.config.description.Required;
import com.webxells.dis.api.error.DisException;
import com.webxells.dis.api.error.InputOutputError;
import com.webxells.dis.api.error.InvalidApi;
import com.webxells.dis.api.input.linker.JoinLinker;
import com.webxells.dis.base.config.SimpleMappingPart;
import com.webxells.dis.base.config.SimpleMappingPoint;

@Description("Before linking creates mapping parts by provided sub data (rootPortrayal) using name of linker as reference and value of " +
        "portrayed sub data mapping part as path")
public class DynamicallyCreateMappingPartsBySubData implements JoinLinker {
    @Required
    @Description("Loads data into the new created mapping parts")
    private JoinLinker childLinker;
    @Required
    @Description("Root mapping part of the sub data")
    private MappingPortrayal rootPortrayal;
    @Required
    @Description("Sub data whose path will be used for the new created mapping parts")
    private MappingPortrayal subDataPortrayalToPathAsValue;

    @Override
    public void validate() throws InvalidApi {
        if (null == childLinker || null == rootPortrayal || null == subDataPortrayalToPathAsValue) {
            throw new InvalidApi("required fields missing");
        }
    }

    @Override
    public int getData(final MappingConfiguration from) throws InputOutputError {
        final String childReference = childLinker.getInputName();
        from.getByPortrayal(rootPortrayal).stream()
                .flatMap(a -> a.getSubData().stream())
                .flatMap(a -> a.getByPortrayal(subDataPortrayalToPathAsValue).stream())
                .flatMap(a -> a.value().stream())
                .filter(a -> !a.isBlank())
                .forEach(a -> from.parts().add(createNewSimpleMappingPart(from, childReference, a)));
        return childLinker.getData(from);
    }

    private MappingPart createNewSimpleMappingPart(final MappingConfiguration configuration, final String reference,
                                                   final String path) {
        return new SimpleMappingPart(configuration,
                new SimpleMappingPoint(reference, path),
                new SimpleMappingPoint());
    }

    @Override
    public String getInputName() {
        return childLinker.getInputName();
    }

    @Override
    public void start() throws DisException {
        childLinker.start();
    }

    @Override
    public void end() throws DisException {
        childLinker.end();
    }

    public void setChildLinker(final JoinLinker childLinker) {
        this.childLinker = childLinker;
    }

    public void setRootPortrayal(final MappingPortrayal rootPortrayal) {
        this.rootPortrayal = rootPortrayal;
    }

    public void setSubDataPortrayalToPathAsValue(final MappingPortrayal subDataPortrayalToPathAsValue) {
        this.subDataPortrayalToPathAsValue = subDataPortrayalToPathAsValue;
    }
}