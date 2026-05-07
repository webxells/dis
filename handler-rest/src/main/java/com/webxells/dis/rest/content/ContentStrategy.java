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

import com.webxells.dis.api.config.ConfigurableByType;
import com.webxells.dis.api.config.description.Description;
import com.webxells.dis.api.config.description.Internal;
import com.webxells.dis.rest.RestConfig;
import com.webxells.dis.rest.execution.HttpMethod;
import com.webxells.dis.rest.execution.Request;
import com.webxells.dis.rest.execution.Response;
import java.io.InputStream;

@Description("Defines handling of request or response contents")
public interface ContentStrategy extends ConfigurableByType {
     @Internal
     public void setContent(final Object content);

     public void parseInputRequest(final HttpMethod method, final Request.Builder builder,
                                   final RestConfig restConfig);

     public InputStream parseOutputRequest(final Response response);
}