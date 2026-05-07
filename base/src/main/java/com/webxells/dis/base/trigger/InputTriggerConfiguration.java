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
package com.webxells.dis.base.trigger;

import com.webxells.dis.api.Logger;
import com.webxells.dis.api.config.InputConfig;
import com.webxells.dis.api.config.description.Default;
import com.webxells.dis.api.config.description.Description;
import com.webxells.dis.api.config.description.Required;
import com.webxells.dis.api.error.InvalidApi;
import com.webxells.dis.base.config.SimpleMappingPart;
import com.webxells.dis.base.trigger.input.HasAnything;
import com.webxells.dis.base.trigger.input.TriggerStrategy;
import com.webxells.dis.logging.LoggerProxyFactory;
import java.util.List;

@Description("Triggers whenever input returns data that validates TriggerStrategy")
public class InputTriggerConfiguration extends SimpleConcurrentTriggerConfig {
    private static final Logger LOGGER = LoggerProxyFactory.logger(InputTriggerConfiguration.class);

    @Description("Used input to collect data")
    @Required
    private InputConfig input;
    @Description("MappingParts used while reading from input. " +
            "@REMINDER: These parts will be added in MappingPart on InputTriggerInput-reading. " +
            "(Skip InputTriggerInput to omit MappingPart extending)")
    @Required
    private List<SimpleMappingPart> parts;
    @Description("")
    @Default("Triggers if input reads anything")
    private TriggerStrategy triggerStrategy = HasAnything.INSTANCE;
    @Description("Creates fresh input every validation")
    @Default("false")
    private boolean restartEveryRead;

    @Override
    public String getType() {
        return InputTrigger.class.getName();
    }

    @Override
    public void validate() throws InvalidApi {
        if (null == input || null == parts || parts.isEmpty()) {
            throw new InvalidApi("input and parts are required");
        }
        input.validate();
        if (getSleepInterval() < 1000) {
            LOGGER.warn("Reminder: SleepInterval should be high enough to not disturb data source");
        }
    }

    public InputConfig getInput() {
        return input;
    }

    public void setInput(final InputConfig input) {
        this.input = input;
    }

    public List<SimpleMappingPart> getParts() {
        return parts;
    }

    public void setParts(final List<SimpleMappingPart> parts) {
        this.parts = parts;
    }

    public TriggerStrategy getTriggerStrategy() {
        return triggerStrategy;
    }

    public void setTriggerStrategy(final TriggerStrategy triggerStrategy) {
        this.triggerStrategy = triggerStrategy;
    }

    public boolean shouldRestartEveryRead() {
        return restartEveryRead;
    }

    public void setRestartEveryRead(final boolean restartEveryRead) {
        this.restartEveryRead = restartEveryRead;
    }
}