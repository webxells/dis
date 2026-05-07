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

import java.util.function.Supplier;

public interface Freeloader {
    enum Type {
        NATIVE(NativeLoggerAdapter::new), SYSTEM(SystemLoggerAdapter::new),
        LOG4J2(Log4J2Adapter::new), SLF4J(Slf4JAdapter::new), APACHE_COMMONS(ApacheCommonsAdapter::new);

        private final Supplier<Freeloader> createFreeloader;

        Type(Supplier<Freeloader> createFreeloader) {
            this.createFreeloader = createFreeloader;
        }

        public Freeloader create() {
            return createFreeloader.get();
        }

    }

    void register();
}