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
package com.webxells.dis.logging.simple.freeloader;

import com.webxells.dis.api.Logger;
import com.webxells.dis.logging.LoggerProxyFactory;

public class Slf4JAdapter implements Freeloader {
    private static final Logger LOGGER = LoggerProxyFactory.logger(Slf4JAdapter.class);

    @Override
    public void register() {
        LOGGER.d("Slf4J freeloading is automatically done by adding dis-logging-freeloader-slf4j as dependency ");
    }
}