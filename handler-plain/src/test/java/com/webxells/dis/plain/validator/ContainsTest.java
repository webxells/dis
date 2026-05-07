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

import com.webxells.dis.api.error.InvalidApi;
import com.webxells.dis.base.SimpleDatasetPiece;
import com.webxells.dis.test.cases.SimpleTestCase;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ContainsTest extends SimpleTestCase {

    @Test
    void test() {
        String random = random();
        Contains fixture = new Contains();
        assertThrows(InvalidApi.class, fixture::validate);
        fixture.setValue(random.substring(1, 3));
        assertTrue(fixture.validate(new SimpleDatasetPiece(random), null));
        fixture.setValue(random.substring(0, 3));
        assertTrue(fixture.validate(new SimpleDatasetPiece(random), null));
        fixture.setValue(random());
        assertFalse(fixture.validate(new SimpleDatasetPiece(random), null));
        assertFalse(fixture.validate(new SimpleDatasetPiece(null), null));
        assertFalse(fixture.validate(new SimpleDatasetPiece(""), null));
    }

    @Test
    void testMultiple() throws InvalidApi {
        String random = random();
        Contains fixture = new Contains();
        assertThrows(InvalidApi.class, fixture::validate);
        fixture.setValues(List.of(random(), random.substring(1, 3), random()));
        fixture.validate();
        assertTrue(fixture.validate(new SimpleDatasetPiece(random), null));
        fixture.setValues(List.of("123", "123123", "123"));
        assertFalse(fixture.validate(new SimpleDatasetPiece(random), null));
        assertFalse(fixture.validate(new SimpleDatasetPiece(null), null));
        assertFalse(fixture.validate(new SimpleDatasetPiece(""), null));
    }

}