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

import com.webxells.dis.api.config.description.Default;
import com.webxells.dis.api.config.description.Description;
import com.webxells.dis.api.config.description.Required;
import com.webxells.dis.api.error.InputOutputError;
import com.webxells.dis.api.error.InvalidApi;
import com.webxells.dis.rest.RestConfig;
import com.webxells.dis.rest.execution.HttpMethod;
import com.webxells.dis.rest.execution.Request;
import com.webxells.dis.rest.execution.Response;
import com.webxells.dis.localfile.resource.PreSavedTempFile;
import java.io.InputStream;

@Description("Content will be pre saved temporary locally and therefore read from it")
public class PreSavedTempContent implements ContentStrategy {
    @Description("Origin content source")
    @Required
    private ContentStrategy child;
    @Description("Path to temporary directory")
    @Default("Temporary directory path of Java")
    private String locallyPreSaveDirectory = System.getProperty("java.io.tmpdir");

    @Override
    public void validate() throws InvalidApi {
        if (null == this.child) {
            throw new InvalidApi("PreSavedTempContent must have a child strategy");
        }
    }

    @Override
    public void setContent(final Object content) {
        child.setContent(content);
    }

    @Override
    public void parseInputRequest(final HttpMethod method, final Request.Builder builder, final RestConfig restConfig) {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public InputStream parseOutputRequest(final Response response) {
        try {
            return PreSavedTempFile.createPreSavedInputStream(child.parseOutputRequest(response), locallyPreSaveDirectory);
        } catch (final InputOutputError e) {
            throw new RuntimeException("Could not create pre-saved temporary file", e);
        }
    }

    public void setChild(final ContentStrategy child) {
        this.child = child;
    }

    public void setLocallyPreSaveDirectory(final String locallyPreSaveDirectory) {
        this.locallyPreSaveDirectory = locallyPreSaveDirectory;
    }
}