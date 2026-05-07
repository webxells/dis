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
package com.webxells.dis.base.input.linker;

import com.webxells.dis.api.Logger;
import com.webxells.dis.api.config.MappingConfiguration;
import com.webxells.dis.api.config.description.Default;
import com.webxells.dis.api.config.description.Description;
import com.webxells.dis.api.config.description.Required;
import com.webxells.dis.api.error.DisException;
import com.webxells.dis.api.error.InputOutputError;
import com.webxells.dis.api.error.InvalidApi;
import com.webxells.dis.api.input.linker.JoinLinker;
import com.webxells.dis.logging.LoggerProxyFactory;

@Description("Skips error that may occur in child JoinLinker")
public class SkipErrorLinker implements JoinLinker {
    private static final Logger LOGGER = LoggerProxyFactory.logger(SkipErrorLinker.class);

    @Description("Does not skip error while booting the child JoinLinker")
    @Default("false")
    private boolean enableBootError;
    @Description("Does not skip error while reading data")
    @Default("false")
    private boolean enableDataError;
    @Required
    private JoinLinker child;

    @Override
    public void validate() throws InvalidApi {
        if (null == child) {
            throw new InvalidApi("child is required");
        }
    }

    @Override
    public int getData(final MappingConfiguration from) throws InputOutputError {
        try {
            return child.getData(from);
        } catch (final InputOutputError e) {
            if (enableDataError) {
                throw e;
            }
            LOGGER.d("Linker data error skipped: ".concat(getInputName()));
        }
        return 0;
    }

    @Override
    public String getInputName() {
        return child.getInputName();
    }

    @Override
    public void start() throws DisException {
        try {
            child.start();
        } catch (final DisException e) {
            if (enableBootError) {
                throw e;
            }
            LOGGER.d("Linker boot in error skipped: ".concat(getInputName()));
        }
    }

    @Override
    public void end() throws DisException {
        try {
            child.end();
        } catch (final DisException e) {
            if (enableBootError) {
                throw e;
            }
            LOGGER.d("Linker boot out error skipped: ".concat(getInputName()));
        }
    }

    @Override
    public String getType() {
        return SkipErrorLinker.class.getName();
    }

    public void setEnableBootError(final boolean enableBootError) {
        this.enableBootError = enableBootError;
    }

    public void setChild(final JoinLinker child) {
        this.child = child;
    }

    public void setEnableDataError(final boolean enableDataError) {
        this.enableDataError = enableDataError;
    }
}