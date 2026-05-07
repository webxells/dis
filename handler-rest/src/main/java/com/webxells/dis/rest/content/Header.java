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
package com.webxells.dis.rest.content;

import com.webxells.dis.api.MappingPortrayal;
import com.webxells.dis.api.config.MappingConfiguration;
import com.webxells.dis.api.config.description.Description;
import com.webxells.dis.rest.RestConfig;
import com.webxells.dis.rest.execution.HttpMethod;
import com.webxells.dis.rest.execution.Request;
import com.webxells.dis.rest.execution.Response;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
@Description("Writes to/Reads from a rest header entry")
public class Header implements ContentStrategy {
    private static final Map<Thread, MappingConfiguration> REGISTRY = new HashMap<>();

    @Description("Delimiter for multiple values")
    private String delimiter = ";";
    @Description("Header name")
    private String name;
    @Description("Part containing value")
    private MappingPortrayal value;

    @Override
    public void setContent(final Object content) {
        if (content instanceof MappingConfiguration) {
            REGISTRY.put(Thread.currentThread(), (MappingConfiguration) content);
        }
    }

    @Override
    public void parseInputRequest(final HttpMethod method, final Request.Builder builder,
                                  final RestConfig restConfig) {
        Optional.ofNullable(REGISTRY.get(Thread.currentThread()))
                .filter(a -> null != value)
                .flatMap(a -> a.getByPortrayal(value))
                .ifPresent(a -> builder.header(name, a.getDataset().getContent().stream()
                            .flatMap(b -> b.value().stream())
                            .collect(Collectors.joining(delimiter))));
    }

    @Override
    public InputStream parseOutputRequest(final Response response) {
        return new ByteArrayInputStream(response
                .getHeaders()
                    .get(name).stream()
                        .collect(Collectors.joining(delimiter))
                    .getBytes());
    }

    public void setDelimiter(final String delimiter) {
        this.delimiter = delimiter;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public void setValue(final MappingPortrayal value) {
        this.value = value;
    }
}