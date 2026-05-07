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
package com.webxells.dis.base.resource;

import com.webxells.dis.api.config.description.Internal;
import com.webxells.dis.api.error.InputOutputError;
import com.webxells.dis.api.resource.Resource;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

@Internal
public class StringResource implements Resource {
    private final ByteArrayOutputStream output = new ByteArrayOutputStream();

    @Override
    public OutputStream send() throws InputOutputError {
        return output;
    }

    @Override
    public InputStream receive() throws InputOutputError {
        throw new UnsupportedOperationException("Input string resource makes no sense");
    }

    @Override
    public String getType() {
        return StringResource.class.getName();
    }

    public ByteArrayOutputStream getOutput() {
        return output;
    }
}