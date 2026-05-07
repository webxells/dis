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
package com.webxells.dis.base.output;

import com.webxells.dis.api.config.MappingConfiguration;
import com.webxells.dis.api.config.OutputConfig;
import com.webxells.dis.api.error.DisException;
import com.webxells.dis.api.error.InputOutputError;
import com.webxells.dis.api.output.Output;
import com.webxells.dis.base.SimpleDatasetPiece;
import com.webxells.dis.base.output.internal.RoutingSender;
import com.webxells.dis.boot.ServiceManager;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

public class Router implements Output<RouterConfig> {
    private final RoutingSender routingSender;
    private final RouterConfig configuration;
    private final Map<RouterConfig.RoutingEndPoint, OutputStream> endPoints = new HashMap<>();
    private Output<?> child;
    private OutputStream noneFoundStream;

    public Router(final RouterConfig configuration) {
        this.configuration = configuration;
        routingSender = new RoutingSender(configuration.isStartForAll());
    }

    @Override
    public void write(final MappingConfiguration to) throws InputOutputError {
        boolean noneFound = true;
        for (final Map.Entry<RouterConfig.RoutingEndPoint, OutputStream> current : endPoints.entrySet()) {
            final RouterConfig.RoutingEndPoint routingEndPoint = current.getKey();
            if (to.getByPortrayal(routingEndPoint.portrayal)
                    .map(part -> routingEndPoint.condition.validate(new SimpleDatasetPiece(part.value().orElse(null)),
                            part))
                    .filter(a -> a)
                    .isPresent()) {
                noneFound = false;
                write(current.getValue(), to);
            }
        }
        if (noneFound && null != noneFoundStream) {
            write(noneFoundStream, to);
        }
        routingSender.reset();
    }

    private void write(final OutputStream value, final MappingConfiguration to) throws InputOutputError {
        routingSender.setCurrent(value);
        child.write(to);
    }

    @Override
    public String getName() {
        return configuration.getName();
    }

    @Override
    public void start() throws DisException {
        for (final RouterConfig.RoutingEndPoint endPointConfig : configuration.getEndPoints()) {
            endPoints.put(endPointConfig,
                    endPointConfig.endPoint == null ? OutputStream.nullOutputStream() : endPointConfig.endPoint.send());
        }
        if (null != configuration.getNoneFound()) {
            noneFoundStream = configuration.getNoneFound().send();
        }
        final OutputConfig config = configuration.getChild();
        ServiceManager.overwriteResourceOfConfig(config, routingSender);
        child = ServiceManager.loadByConfig(config);
        startChild();
    }

    private void startChild() throws DisException {
        if (configuration.isStartForAll()) {
            for (final OutputStream current : endPoints.values()) {
                restart(current);
            }
            if (null != noneFoundStream) {
                restart(noneFoundStream);
            }
            routingSender.reset();
        }
        child.start();
    }

    private void restart(final OutputStream stream) throws DisException {
        routingSender.setCurrent(stream);
        child.start();
        child.end();
    }

    @Override
    public void end() throws DisException {
        try {
            for (final OutputStream stream : endPoints.values()) {
                stream.close();
            }
            if (null != noneFoundStream) {
                noneFoundStream.close();
            }
        } catch (final IOException e) {
            throw new InputOutputError("Could not close stream", e);
        }
    }
}