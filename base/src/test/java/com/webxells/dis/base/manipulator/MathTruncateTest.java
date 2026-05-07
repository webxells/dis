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
package com.webxells.dis.base.manipulator;

import com.webxells.dis.api.DatasetPiece;
import com.webxells.dis.api.error.InvalidDatasetException;
import com.webxells.dis.base.SimpleDatasetPiece;import com.webxells.dis.test.cases.SimpleTestCase;
import com.webxells.dis.test.cases.ConfigurationBuilder;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MathTruncateTest extends SimpleTestCase {

    @Test
    void test() throws InvalidDatasetException {
        MathTruncate fixture = new MathTruncate();

        double value = random(1.1);
        DatasetPiece datasetPiece = new SimpleDatasetPiece(String.valueOf(value));
        assertTrue(datasetPiece.value().get().contains("."));
        fixture.manipulate(datasetPiece, null);
        assertFalse(datasetPiece.value().get().contains("."));
        assertEquals(String.valueOf((long) java.lang.Math.floor(value)), datasetPiece.value().get());

    }

}