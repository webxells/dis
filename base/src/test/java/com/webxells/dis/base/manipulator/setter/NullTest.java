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
package com.webxells.dis.base.manipulator.setter;

import com.webxells.dis.base.SimpleDatasetPiece;
import com.webxells.dis.base.config.SimpleMappingPart;
import com.webxells.dis.test.cases.SimpleTestCase;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NullTest extends SimpleTestCase {

    @Test
    void test() {
        Null fixture = new Null();
        SimpleMappingPart part = new SimpleMappingPart(null);
        part.getDataset().collect(List.of(new SimpleDatasetPiece(random()), new SimpleDatasetPiece(random())));
        assertNull(fixture.getValue(null, part));
        assertEquals(1, part.getDataset().getContent().size());
        assertTrue(part.getDataset().getContent().get(0).value().isEmpty());
    }

}