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

import com.webxells.dis.api.Logger;
import com.webxells.dis.api.config.description.Description;
import com.webxells.dis.rest.RestConfig;
import com.webxells.dis.rest.execution.BodyPublisherOfInputStream;
import com.webxells.dis.rest.execution.HttpMethod;
import com.webxells.dis.rest.execution.Request;
import com.webxells.dis.rest.execution.Response;
import com.webxells.dis.logging.LoggerProxyFactory;
import java.io.InputStream;
import java.util.Objects;

@Description("Writes to/Reads from body")
public class Body implements ContentStrategy {
    private static final Logger LOGGER = LoggerProxyFactory.logger(Body.class);

    private String content;

    @Override
    public void setContent(final Object content) {
        this.content = String.valueOf(content);
    }

    @Override
    public void parseInputRequest(final HttpMethod method, final Request.Builder builder,
                                  final RestConfig restConfig) {
        LOGGER.trace(String.format("Body content:%n%s", content));
        builder.setBodyPublisher(BodyPublisherOfInputStream.byString(Objects.requireNonNullElse(content,"")));
    }


    @Override
    public InputStream parseOutputRequest(final Response response) {
        return response.body();
    }

}