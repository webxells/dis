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

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OwnPackageFreeloaderTest {
    @Test
    void testApache() {
        Freeloader fixture = Freeloader.Type.APACHE_COMMONS.create();
        assertInstanceOf(ApacheCommonsAdapter.class, fixture);
        fixture.register();
    }

    @Test
    void testLog4J() {
        Freeloader fixture = Freeloader.Type.LOG4J2.create();
        assertInstanceOf(Log4J2Adapter.class, fixture);
        fixture.register();
    }

    @Test
    void testSlf4J() {
        Freeloader fixture = Freeloader.Type.SLF4J.create();
        assertInstanceOf(Slf4JAdapter.class, fixture);
        fixture.register();
    }

}