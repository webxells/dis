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

import com.webxells.dis.api.config.description.Description;
import com.webxells.dis.api.config.description.Internal;
import com.webxells.dis.api.config.description.Required;
import com.webxells.dis.rest.RestConfig;
import com.webxells.dis.rest.content.paginating.NumericParameterChange;
import com.webxells.dis.rest.execution.HttpMethod;
import com.webxells.dis.rest.execution.Request;
import com.webxells.dis.rest.url.ParameterParser;
import java.util.List;
import java.util.Optional;

@Description("Alters rest parameters after each call")
public class NumericParameters implements PaginatingContentStrategy {
    @Required
    private List<NumericParameterChange> parameterChanges;
    private long currentRound;

    @Override
    @Internal
    public void setContent(final long round) {
        currentRound = round;
    }

    @Override
    public void parseInputRequest(final HttpMethod method, final Request.Builder builder,
                                  final RestConfig restConfig) {
        Optional.ofNullable(restConfig.getUrlQueryParameters())
                .map(ParameterParser::getParameters)
                .ifPresent(a -> {
                    parameterChanges.forEach(b -> {
                        if (0 == currentRound) {
                            setMissingStartValues(a.get(b.getName()), b);
                        }
                        a.put(b.getName(), String.valueOf(b.calcCurrent(currentRound)));
                    });
                    builder.uri(restConfig.getUrlQueryParameters().parseToUri(restConfig.getBaseUrl()));
                });
    }

    private void setMissingStartValues(final String a, final NumericParameterChange b) {
        if (null == b.getStartValue()) {
            try {
                b.setStartValue(Long.parseLong(a));
            } catch (final NumberFormatException e) {
                b.setStartValue(0);
            }
        }
    }

    public void setParameterChanges(final List<NumericParameterChange> parameterChanges) {
        this.parameterChanges = parameterChanges;
    }
}