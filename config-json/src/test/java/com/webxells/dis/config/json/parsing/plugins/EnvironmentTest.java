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
package com.webxells.dis.config.json.parsing.plugins;

import com.webxells.dis.config.json.parsing.element.DisonMethodReader;
import com.webxells.dis.config.json.parsing.element.DisonStringReader;
import com.webxells.dis.test.cases.SimpleTestCase;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;

class EnvironmentTest extends SimpleTestCase {

    @Test
    void testWithUnknownEnvironment() {
        String unknown = "unknown-key-_-so-i-hope-%s".concat(random());
        assertNull(System.getenv(unknown), "narf, this should not happen");
        DisonMethodReader methodCall = new DisonMethodReader(null, List.of(new DisonStringReader(unknown)), null);
        Environment fixture = new Environment();
        assertEquals("\"\"", fixture.handle(methodCall, null).get().writeJson());
    }

    @Test
    void test() {
        Map<String, String> getenv = System.getenv();
        assertFalse(getenv.isEmpty(), "narf, this should not happen");
        String name = getenv.keySet().iterator().next();
        String value = getenv.get(name);
        DisonMethodReader methodCall = new DisonMethodReader(null, List.of(new DisonStringReader(name)), null);
        Environment fixture = new Environment();
        assertEquals(String.format("\"%s\"", value), fixture.handle(methodCall, null).get().writeJson());
    }

}