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
import com.webxells.dis.api.config.MappingPart;
import com.webxells.dis.api.config.description.Description;
import com.webxells.dis.api.config.description.Required;
import com.webxells.dis.api.error.InvalidApi;
import com.webxells.dis.rest.RestConfig;
import com.webxells.dis.rest.execution.HttpMethod;
import com.webxells.dis.rest.execution.Request;
import com.webxells.dis.rest.execution.Response;
import java.io.InputStream;
import java.util.List;
import java.util.function.Function;


@Description("Altering http method based of dataset rules")
public class DatasetAlteringMethod implements ContentStrategy {
    public static class Rule {
        public enum Type {
            ISSET(a -> a.value().isPresent()),
            EMPTY(a -> a.value().isEmpty());

            private final Function<MappingPart, Boolean> checkFunction;

            Type(final Function<MappingPart, Boolean> checkFunction) {
                this.checkFunction = checkFunction;
            }
        }

        @Required
        public Rule.Type checkType;
        @Required
        public HttpMethod resultMethod;
        @Required
        public MappingPortrayal mappingPortrayal;
    }

    @Required
    private List<Rule> rules;
    private MappingConfiguration rootDataset;

    @Override
    public void validate() throws InvalidApi {
        if (null == rules || rules.stream().anyMatch(a -> null == a.checkType || null == a.resultMethod || null == a.mappingPortrayal)) {
            throw new InvalidApi("rules is full required");
        }
    }

    @Override
    public void setContent(final Object content) {
        if (content instanceof MappingConfiguration) {
            rootDataset = (MappingConfiguration) content;
        }
    }

    @Override
    public void parseInputRequest(final HttpMethod method, final Request.Builder builder,
                                  final RestConfig restConfig) {
        rules.stream()
                .filter(a -> rootDataset.getByPortrayal(a.mappingPortrayal)
                            .map(b -> a.checkType.checkFunction.apply(b))
                            .orElse(false))
                .forEach(a -> builder.method(a.resultMethod));
    }

    @Override
    public InputStream parseOutputRequest(final Response response) {
        throw new UnsupportedOperationException("Not implemented");
    }

    public void setRules(final List<Rule> rules) {
        this.rules = rules;
    }

}