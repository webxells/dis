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
package com.webxells.dis.info.input;

import com.webxells.dis.api.MappingPortrayal;
import com.webxells.dis.api.config.InputConfig;
import com.webxells.dis.api.config.MappingConfiguration;
import com.webxells.dis.api.config.MappingPart;
import com.webxells.dis.api.config.MappingPoint;
import com.webxells.dis.api.config.description.Default;
import com.webxells.dis.api.config.description.Description;
import com.webxells.dis.api.config.description.Required;
import com.webxells.dis.api.input.Input;
import com.webxells.dis.api.manipulator.Manipulator;
import com.webxells.dis.api.output.Output;
import com.webxells.dis.api.resource.Resource;
import com.webxells.dis.api.trigger.Trigger;
import com.webxells.dis.api.validator.Validator;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Description("Delivers all implementations that can be used to build a DIS config")
public class AvailableImplementationsConfig implements InputConfig {
    private static final List<String> DEFAULT_INTERFACES = List.of(
            Input.class.getName(),
            Output.class.getName(),
            Trigger.class.getName(),
            Validator.class.getName(),
            Manipulator.class.getName(),
            Resource.class.getName(),
            MappingPart.class.getName(),
            MappingPoint.class.getName(),
            MappingPortrayal.class.getName(),
            MappingConfiguration.class.getName()
    );

    @Description("Reference of this handler")
    @Required
    private String name;
    @Description("if set all missing mapping parts will be created by Input")
    @Default("true")
    private boolean createMissingMappingParts = true;
    @Description("if set knownMapping is created as Map<Interface, Interface>")
    @Default("false")
    private boolean knownMappingAsMap;
    @Description("if set only selected interfaces will be shown")
    @Default("all available dis related interfaces")
    private List<String> interfaces;
    @Description("interfaces will be loaded additionally to interfaces, useful for enhancing all available interfaces")
    private List<String> additionalInterfaces;

    @Override
    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    @Override
    public String getType() {
        return AvailableImplementations.class.getName();
    }

    public boolean isCreateMissingMappingParts() {
        return createMissingMappingParts;
    }

    public void setCreateMissingMappingParts(final boolean createMissingMappingParts) {
        this.createMissingMappingParts = createMissingMappingParts;
    }

    public boolean isKnownMappingAsMap() {
        return knownMappingAsMap;
    }

    public void setKnownMappingAsMap(final boolean knownMappingAsMap) {
        this.knownMappingAsMap = knownMappingAsMap;
    }

    public List<String> getInterfaces() {
        final List<String> result = new ArrayList<>(Objects.requireNonNullElse(interfaces, DEFAULT_INTERFACES));
        if (null != additionalInterfaces) {
            result.addAll(additionalInterfaces);
        }
        return result;
    }

    public void setInterfaces(final List<String> interfaces) {
        this.interfaces = interfaces;
    }

    public void setAdditionalInterfaces(final List<String> additionalInterfaces) {
        this.additionalInterfaces = additionalInterfaces;
    }
}