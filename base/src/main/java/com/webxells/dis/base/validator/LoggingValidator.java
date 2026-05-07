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
package com.webxells.dis.base.validator;

import com.webxells.dis.api.Logger.LogLevel;
import com.webxells.dis.api.config.description.Default;
import com.webxells.dis.api.config.description.Description;
import com.webxells.dis.api.validator.Validator;
import java.util.Optional;

public abstract class LoggingValidator implements Validator {
    @Description("Log message printed if Validator fails")
    @Default("None")
    private String fail;

    @Description("Log weight of message")
    @Default("DEBUG")
    private LogLevel level = LogLevel.DEBUG;

    @Override
    public Optional<String> getFailMessage() {
        return Optional.ofNullable(fail);
    }

    @Override
    public LogLevel getFailLevel() {
        return level;
    }

    public void setFail(final String fail) {
        this.fail = fail;
    }

    public void setLevel(final LogLevel level) {
        this.level = level;
    }
}