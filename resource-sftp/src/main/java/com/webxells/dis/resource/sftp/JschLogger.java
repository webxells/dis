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
package com.webxells.dis.resource.sftp;

import com.jcraft.jsch.Logger;
import com.webxells.dis.logging.LoggerProxyFactory;

public class JschLogger implements Logger {
    private static final com.webxells.dis.api.Logger LOGGER = LoggerProxyFactory.logger("JSch-Logger");

    @Override
    public boolean isEnabled(final int level) {
        return true;
    }

    @Override
    public void log(final int level, final String message) {
        switch (level) {
            case Logger.DEBUG -> LOGGER.d(message);
            case Logger.INFO -> LOGGER.i(message);
            case Logger.WARN -> LOGGER.w(message);
            case Logger.ERROR -> LOGGER.e(message);
            case Logger.FATAL -> LOGGER.f(message);
        }
    }
}