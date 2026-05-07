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

import com.webxells.dis.api.config.MappingConfiguration;
import com.webxells.dis.api.error.InputOutputError;
import com.webxells.dis.api.input.Input;
import com.webxells.dis.base.trigger.InputTrigger;
import java.util.Collection;
import java.util.Optional;

public class TriggerInput implements Input<TriggerInputConfig> {
    private final String name;
    private InputTrigger.InputStorage inputStorage;


    public TriggerInput(final TriggerInputConfig config) {
        name = config.getName();
    }

    @Override
    public int read(final MappingConfiguration from) throws InputOutputError {
        return Optional.ofNullable(inputStorage)
                .map(InputTrigger.InputStorage::next).stream()
                .flatMap(Collection::stream)
                .filter(a -> !a.isStable()) //dont alter config permanent
                .mapToInt(part -> {
                    from.parts().add(part);
                    return 1;
                })
                .sum();
    }

    @Override
    public boolean hasNext() throws InputOutputError {
        return null != inputStorage && inputStorage.hasNext();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void start() {
        inputStorage = InputTrigger.registerInput(name);
    }

    @Override
    public void end() {
        inputStorage = null;
    }
}