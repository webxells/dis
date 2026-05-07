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
package com.webxells.dis.localfile.input;

import com.webxells.dis.api.config.InputConfig;
import com.webxells.dis.api.config.MappingConfiguration;
import com.webxells.dis.api.error.DisException;
import com.webxells.dis.api.error.InputOutputError;
import com.webxells.dis.api.error.InvalidApi;
import com.webxells.dis.api.input.Input;
import com.webxells.dis.boot.ServiceManager;
import com.webxells.dis.localfile.resource.LocalFile;
import java.io.File;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class DynamicLocalFile implements Input<DynamicLocalFileConfig> {
    private final DynamicLocalFileConfig config;
    private final InputConfig childConfig;
    private Input<?> child;

    private Queue<File> queue;

    public DynamicLocalFile(final DynamicLocalFileConfig config) {
        this.config = config;
        childConfig = config.getChild();
    }

    @Override
    public void validate() throws InvalidApi {
        if (null == config.getSource() || null == childConfig) {
            throw new InvalidApi("fields child and source are mandatory");
        }
    }

    @Override
    public int read(final MappingConfiguration from) throws InputOutputError {
        assertQueueCreated(from);
        if (queue.isEmpty() && childRequiresUpdate()) {
            return 0;
        }
        return getChild().read(from);
    }

    private boolean childRequiresUpdate() throws InputOutputError {
        return null == child || !child.hasNext();
    }

    private Input<?> getChild() throws InputOutputError {
        if (childRequiresUpdate()) {
            try {
                if (null != child) {
                    child.end();
                }
                if (queue.isEmpty()) {
                    throw new InputOutputError("dont read from empty queue!");
                }
                ServiceManager.overwriteResourceOfConfig(childConfig, new LocalFile(queue.poll().getAbsolutePath()));
                child = ServiceManager.loadByConfig(childConfig);
                child.start();
            } catch (final DisException e) {
                throw new InputOutputError("Could not start child configuration", e);
            }
        }
        return child;
    }

    private void assertQueueCreated(final MappingConfiguration from) {
        if (null == queue || config.isSingleRead()) {
            queue = new ConcurrentLinkedQueue<>();
            from.getByPortrayal(config.getSource()).stream()
                    .flatMap(a -> a.getDataset().getContent().stream())
                    .flatMap(a -> a.value().stream())
                    .map(File::new)
                    .filter(File::exists)
                    .forEach(queue::add);
        }
    }

    @Override
    public boolean hasNext() throws InputOutputError {
        return !childRequiresUpdate() ||
                (null == queue || 0 < queue.size());
    }

    @Override
    public String getName() {
        return config.getName();
    }

    @Override
    public void start() { }

    @Override
    public void end() throws DisException {
        queue = null;
        if (null != child) {
            child.end();
            child = null;
        }
    }
}