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
package com.webxells.dis.base.discover.input;

import com.webxells.dis.api.config.InputConfig;
import com.webxells.dis.api.config.MappingConfiguration;
import com.webxells.dis.api.error.DisException;
import com.webxells.dis.api.error.InputOutputError;
import com.webxells.dis.api.input.Input;

public class GroupingResult implements Input<GroupingResult.Config> {
    record Config(String name) implements InputConfig {
        @Override
        public String getType() {
            return GroupingResult.class.getName();
        }

        @Override
        public String getName() {
            return name;
        }
    }

    @Override
    public int read(final MappingConfiguration from) throws InputOutputError {
        return 0;
    }

    @Override
    public boolean hasNext() throws InputOutputError {
        return false;
    }

    @Override
    public String getName() {
        return "";
    }

    @Override
    public void start() throws DisException {

    }

    @Override
    public void end() throws DisException {

    }
}