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

import com.webxells.dis.config.json.parsing.DisonParsingError;
import com.webxells.dis.config.json.parsing.element.DisonMethodReader;
import com.webxells.dis.config.json.parsing.element.DisonStringReader;
import com.webxells.dis.test.cases.SimpleTestCase;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ErrorTest extends SimpleTestCase {

    @Test
    void test() {
        Error.Manager manager = new Error.Manager();
        DisonPlugin fixture = manager.isCompetent(Error.DIS_COMMAND);
        assertInstanceOf(Error.class, fixture);
        assertThrows(DisonParsingError.class,
                () -> fixture.handle(new DisonMethodReader(fixture, List.of(), null), null));
        String random = random();
        boolean catched = false;
        try {
            fixture.handle(new DisonMethodReader(fixture, List.of(new DisonStringReader(random)), null), null);
        } catch (DisonParsingError error) {
            catched = true;
            assertEquals("Dison user error triggered: " + random, error.getMessage());
        }
        assertTrue(catched);
    }

}