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
import com.webxells.dis.api.config.description.Default;
import com.webxells.dis.api.config.description.Description;
import com.webxells.dis.api.config.description.Required;
import java.util.Optional;

public class CliUserInputConfig implements InputConfig {
    @Required
    @Description("Reference of this handler")
    private String name;
    @Description("User prompt - prints before input is awaited")
    private String prompt;
    @Description("If nothing is provided, this will be printed")
    private String defaultValue;
    @Description("Timeout after Input uses defaultValue")
    @Default("waiting endlessly")
    private int timeoutSeconds;
    @Default("1")
    private int maxLinesToFetch = 1;
    @Description("end of line used for input after each line")
    @Default("System line separator")
    private String endOfLine = System.lineSeparator();

    @Override
    public String getType() {
        return CliUserInput.class.getName();
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public int getTimeoutSeconds() {
        return timeoutSeconds;
    }

    public void setTimeoutSeconds(final int timeoutSeconds) {
        this.timeoutSeconds = timeoutSeconds;
    }

    public Optional<String> getPrompt() {
        return Optional.ofNullable(prompt);
    }

    public void setPrompt(final String prompt) {
        this.prompt = prompt;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(final String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public int getMaxLinesToFetch() {
        return maxLinesToFetch;
    }

    public void setMaxLinesToFetch(final int maxLinesToFetch) {
        this.maxLinesToFetch = maxLinesToFetch;
    }

    public String getEndOfLine() {
        return endOfLine;
    }

    public void setEndOfLine(final String endOfLine) {
        this.endOfLine = endOfLine;
    }
}