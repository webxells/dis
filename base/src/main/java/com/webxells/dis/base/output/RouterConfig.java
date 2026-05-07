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
package com.webxells.dis.base.output;

import com.webxells.dis.api.MappingPortrayal;
import com.webxells.dis.api.config.OutputConfig;
import com.webxells.dis.api.config.description.Description;
import com.webxells.dis.api.config.description.Required;
import com.webxells.dis.api.resource.Resource;
import com.webxells.dis.api.validator.Validator;
import java.util.List;

@Description("Writes data of provided Output into multiple resources if validation of provided Validator was successful")
public class RouterConfig implements OutputConfig {

    public static class RoutingEndPoint {
        @Required
        public Validator condition;
        @Required
        public Resource endPoint;
        public MappingPortrayal portrayal;
    }

    @Required
    @Description("Reference of this handler")
    private String name;
    @Description("Defines on which condition what values should to be send to which destination")
    private List<RoutingEndPoint> endPoints;
    @Required
    @Description("Output used for data generation")
    private OutputConfig child;
    @Description("Resource used, if none other was used")
    private Resource noneFound;
    @Description("Ignores error hacking into Output (e.g. no sender in Output defined)")
    private boolean lenient;
    private boolean startForAll;


    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getType() {
        return Router.class.getName();
    }

    public boolean isLenient() {
        return lenient;
    }

    public void setLenient(final boolean lenient) {
        this.lenient = lenient;
    }

    public OutputConfig getChild() {
        return child;
    }

    public void setChild(final OutputConfig child) {
        this.child = child;
    }

    public List<RoutingEndPoint> getEndPoints() {
        return endPoints;
    }

    public void setEndPoints(final List<RoutingEndPoint> endPoints) {
        this.endPoints = endPoints;
    }

    public Resource getNoneFound() {
        return noneFound;
    }

    public void setNoneFound(final Resource noneFound) {
        this.noneFound = noneFound;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public boolean isStartForAll() {
        return startForAll;
    }

    public void setStartForAll(final boolean startForAll) {
        this.startForAll = startForAll;
    }
}