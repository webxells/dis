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
package com.webxells.dis.hash.input;

import com.webxells.dis.api.config.InputConfig;
import com.webxells.dis.api.hash.Manager;
import com.webxells.dis.api.resource.Resource;

public class HashConfig implements InputConfig {
    private String name;
    private Resource receiver;
    private Manager manager;

    @Override
    public String getType() {
        return Hash.class.getName();
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public Resource getReceiver() {
        return receiver;
    }

    public void setReceiver(final Resource receiver) {
        this.receiver = receiver;
    }

    public Manager getManager() {
        return manager;
    }

    public void setManager(final Manager manager) {
        this.manager = manager;
    }
}