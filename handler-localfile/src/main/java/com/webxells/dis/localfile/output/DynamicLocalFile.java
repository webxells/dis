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
package com.webxells.dis.localfile.output;

import com.webxells.dis.api.config.MappingConfiguration;
import com.webxells.dis.api.error.DisException;
import com.webxells.dis.api.error.InputOutputError;
import com.webxells.dis.api.error.InvalidApi;
import com.webxells.dis.api.output.Output;
import com.webxells.dis.base.resource.StringResource;
import com.webxells.dis.boot.ServiceManager;
import com.webxells.dis.localfile.resource.LocalFile;
import com.webxells.dis.plain.output.Echo;
import com.webxells.dis.plain.output.EchoConfig;
import java.util.HashSet;
import java.util.Set;

public class DynamicLocalFile implements Output<DynamicLocalFileConfig> {
    private final DynamicLocalFileConfig config;
    private final EchoConfig echoConfig;
    private final Set<String> currentWrittenFiles = new HashSet<>();

    private Output<?> child;
    private String currentPath;

    public DynamicLocalFile(final DynamicLocalFileConfig config) {
        this.config = config;
        echoConfig = new EchoConfig();
        echoConfig.setName(getName());
        echoConfig.setTemplate(config.getPathTemplate());
    }

    @Override
    public void validate() throws InvalidApi {
        if (null == config || null == config.getConfig() || null == config.getName() || null == config.getPathTemplate()) {
            throw new InvalidApi("required fields missing");
        }
    }

    @Override
    public void write(final MappingConfiguration to) throws InputOutputError {
        try {
            bootChild(to);
        } catch (final DisException e) {
            throw new InputOutputError("Could not boot child config", e);
        }
        child.write(to);
    }

    private void bootChild(final MappingConfiguration to) throws DisException {
        final String newPath = createCurrentPath(to);
        if (null != currentPath && currentPath.equals(newPath)) {
            return;
        }
        currentPath = newPath;
        end();
        ServiceManager.overwriteResourceOfConfig(config.getConfig(), createCurrentLocalFile());
        child = ServiceManager.loadByConfig(config.getConfig());
        child.start();
    }

    private LocalFile createCurrentLocalFile() {
        final LocalFile result = new LocalFile();
        result.setPath(currentPath);
        result.setAppend(currentWrittenFiles.contains(currentPath) || config.isAppend());
        result.setFileNameContainsDateFormat(config.isFileNameContainsDateFormat());
        result.setCreateIfNotExists(config.isCreateIfNotExists());
        result.setFileName(config.getFileName());
        currentWrittenFiles.add(currentPath);
        return result;
    }

    private String createCurrentPath(final MappingConfiguration to) throws DisException {
        final StringResource tmp = new StringResource();
        echoConfig.setSender(tmp);
        final Echo echo = new Echo(echoConfig);
        echo.start();
        echo.write(to);
        echo.end();
        return tmp.getOutput().toString();
    }

    @Override
    public String getName() {
        return config.getName();
    }

    @Override
    public void start() { }

    @Override
    public void end() throws DisException {
        if (null != child) {
            child.end();
        }
        currentWrittenFiles.clear();
    }
}