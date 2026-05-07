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
package com.webxells.dis.test.example.resource;

import com.webxells.dis.api.error.InputOutputError;
import com.webxells.dis.api.resource.MultiResource;
import com.webxells.dis.api.resource.Resource;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

public class TestMultipleResource implements MultiResource {
    private List<InputStream> streams;
    private int current;

    public TestMultipleResource(List<InputStream> streams) {
        this.streams = streams;
    }

    @Override
    public RefreshResult refresh() {
        current++;
        return RefreshResult.UNKNOWN;
    }

    @Override
    public void reset() {
        current = 0;
    }

    @Override
    public OutputStream send() throws InputOutputError {
        return null;
    }

    @Override
    public InputStream receive() throws InputOutputError {
        return streams.size() > current ? streams.get(current) : null;
    }

    @Override
    public String getType() {
        return null;
    }
}
