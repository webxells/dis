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
package com.webxells.dis.rest;

import com.webxells.dis.api.config.MappingConfiguration;
import com.webxells.dis.api.resource.ParentResource;
import com.webxells.dis.api.resource.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

public abstract class PassConfiguration {
    protected final List<Rest> restResources = new ArrayList<>();

    protected void findAll(final Resource resource) {
        if (resource instanceof Rest rest) {
            restResources.add(rest);
        }
        if (resource instanceof ParentResource parent) {
            parent.getChildren().forEach(this::findAll);
        }
    }

    protected void setCurrent(final MappingConfiguration to) {
        restResources.stream()
                .flatMap(a -> Stream.concat(Stream.of(a.getResponseContentStrategy()), a.getRequestContentStrategy().stream()))
                .filter(Objects::nonNull)
                .forEach(a -> a.setContent(to));
    }

}