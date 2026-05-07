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
package com.webxells.dis.base.trigger.input;

import com.webxells.dis.api.config.MappingConfiguration;
import com.webxells.dis.api.config.description.Description;
import com.webxells.dis.api.error.InputOutputError;
import com.webxells.dis.api.input.Input;

@Description("Triggers if input has anything to read")
public class HasAnything implements TriggerStrategy {
    public static final HasAnything INSTANCE = new HasAnything();

    @Override
    public boolean shouldTrigger(final Input<?> input, final MappingConfiguration parts) throws InputOutputError {
        return input.hasNext();
    }
}