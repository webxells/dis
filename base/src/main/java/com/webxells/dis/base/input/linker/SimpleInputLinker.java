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


import com.webxells.dis.api.config.InputConfig;
import com.webxells.dis.api.config.description.Alias;
import com.webxells.dis.api.config.description.Required;
import com.webxells.dis.api.error.DisException;
import com.webxells.dis.api.input.Input;
import com.webxells.dis.api.input.linker.JoinLinker;
import com.webxells.dis.boot.ServiceManager;

public abstract class SimpleInputLinker implements JoinLinker {
    @Required
    @Alias("input")
    protected InputConfig inputConfig;
    private Input<? extends InputConfig> input;

    @Override
    public void start() throws DisException {
        getInput().start();
    }

    @Override
    public void end() throws DisException {
        getInput().end();
    }

    @Override
    public String getInputName(){
        return inputConfig.getName();
    }

    public void setInputConfig(final InputConfig inputConfig) {
        this.inputConfig = inputConfig;
    }

    protected Input<? extends InputConfig> getInput() {
        if (null == input) {
            input = ServiceManager.loadByConfig(inputConfig);
        }
        return input;
    }
}

