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

import com.webxells.dis.api.error.InvalidApi;
import com.webxells.dis.base.SimpleDatasetPiece;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RegularExpressionTest {

    @Test
    void testDefault() {
        RegularExpression fixture = new RegularExpression();
        assertThrows(InvalidApi.class, fixture::validate);
        fixture.setPattern("\\d+");
        assertTrue(fixture.validate(new SimpleDatasetPiece("123asd"), null));
        assertFalse(fixture.validate(new SimpleDatasetPiece("asd"), null));
    }

    @Test
    void testMatchWhole() {
        RegularExpression fixture = new RegularExpression();
        fixture.setPattern("\\d+");
        fixture.setMatchWhole(true);
        assertTrue(fixture.validate(new SimpleDatasetPiece("123123"), null));
        assertFalse(fixture.validate(new SimpleDatasetPiece("123asd"), null));
        assertFalse(fixture.validate(new SimpleDatasetPiece("asd"), null));
    }

    @Test
    void testValueRequired() {
        RegularExpression fixture = new RegularExpression();
        fixture.setPattern(".*");
        assertTrue(fixture.validate(new SimpleDatasetPiece(null), null));
        fixture.setValueRequired(true);
        assertFalse(fixture.validate(new SimpleDatasetPiece(null), null));
    }

}