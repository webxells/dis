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
package com.webxells.dis.info.internal.config.transformer;

import com.webxells.dis.info.internal.ForEachObject;
import com.webxells.dis.info.internal.clazz.Implementation;
import com.webxells.dis.info.internal.clazz.Interface;
import com.webxells.dis.info.internal.config.RootConfiguration;
import java.util.List;

public class Modules extends SubData<Interface, Implementation> {

    public Modules(final RootConfiguration.Configuration configuration) {
        super(configuration, List.of(
                new ImplementationDescription(configuration),
                new ImplementationName(configuration),
                new Options(configuration),
                new Refinements(configuration)));
    }

    public String path() {
        return "modules";
    }

    @Override
    protected ForEachObject<Implementation> getObjects(final Interface current) {
        return current;
    }

}