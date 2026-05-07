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
package com.webxells.dis.plain.validator;

import com.webxells.dis.base.SimpleDatasetPiece;
import com.webxells.dis.test.cases.SimpleTestCase;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StartsWithTest extends SimpleTestCase {

    @Test
    void test() {
        String start = random("start");
        StartsWith fixture = new StartsWith();
        fixture.setValue(start);

        assertTrue(fixture.validate(new SimpleDatasetPiece(start), null));
        assertTrue(fixture.validate(new SimpleDatasetPiece(start.concat(random())), null));
        assertFalse(fixture.validate(new SimpleDatasetPiece(random().concat(start)), null));
        assertFalse(fixture.validate(new SimpleDatasetPiece(random("something else")), null));
        assertFalse(fixture.validate(new SimpleDatasetPiece(null), null));
        assertFalse(fixture.validate(null, null));
    }

}