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
package com.webxells.dis.base.discover.output;

import com.webxells.dis.api.discover.PathDiscoverer;
import com.webxells.dis.api.error.InvalidApi;
import com.webxells.dis.base.refinement.SimpleRefinement;

public class Discoverer extends SimpleRefinement {
    private PathDiscoverer pathDiscoverer;

    @Override
    public void validate() throws InvalidApi {
        if (null == pathDiscoverer) {
            throw new InvalidApi("path discoverer is required");
        }
    }

    public PathDiscoverer getPathDiscoverer() {
        return pathDiscoverer;
    }

    public void setPathDiscoverer(final PathDiscoverer pathDiscoverer) {
        this.pathDiscoverer = pathDiscoverer;
    }
}