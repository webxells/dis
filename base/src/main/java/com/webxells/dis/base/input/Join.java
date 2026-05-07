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
package com.webxells.dis.base.input;

import com.webxells.dis.api.config.InputConfig;
import com.webxells.dis.api.config.MappingConfiguration;
import com.webxells.dis.api.error.DisException;
import com.webxells.dis.api.error.InputOutputError;
import com.webxells.dis.api.input.Input;
import com.webxells.dis.api.input.SkippableInput;
import com.webxells.dis.api.input.linker.JoinLinker;
import com.webxells.dis.boot.ServiceManager;
import java.util.List;
import java.util.Objects;

public class Join implements SkippableInput<JoinConfiguration> {
    private final List<JoinLinker> children;
    private final Input<? extends InputConfig> root;
    private final String name;

    public Join(final JoinConfiguration config) {
        children = config.getChildren();
        name = config.getName();
        root = ServiceManager.loadByConfig(Objects.requireNonNull(config.getRootConfig()));
    }

    @Override
    public int hashCode() {
        return Objects.hash(children, root, getName());
    }

    @Override
    public int read(final MappingConfiguration from, final boolean skip) throws InputOutputError {
        int readValues = root.read(from);
        if (0 == readValues || skip) {
            return 0;
        }
        for (JoinLinker a : children) {
            readValues+= a.getData(from);
        }
        return readValues;
    }

    @Override
    public boolean hasNext() throws InputOutputError {
        return root.hasNext();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void start() throws DisException {
        root.start();
        for (JoinLinker child : children) {
            child.start();
        }
    }

    @Override
    public void end() throws DisException {
        root.end();
        for (JoinLinker child : children) {
            child.end();
        }
    }
}