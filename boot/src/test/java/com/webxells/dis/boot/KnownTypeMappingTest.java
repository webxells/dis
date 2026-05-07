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
package com.webxells.dis.boot;

import com.webxells.dis.test.cases.SimpleTestCase;
import java.util.Map;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class KnownTypeMappingTest extends SimpleTestCase {

    @Test
    void test() {
        String map = random();
        KnownTypeMapping.add(this.getClass(), map);
        KnownTypeMapping.force(SimpleTestCase.class, this.getClass().getName());

        assertTrue(KnownTypeMapping.isKnown(this.getClass()));
        assertEquals(map, KnownTypeMapping.getKnown(this.getClass()));
        assertEquals(this.getClass().getName(), KnownTypeMapping.getForced(SimpleTestCase.class));
        assertEquals(this.getClass(), KnownTypeMapping.getForcedMapping(SimpleTestCase.class));
        assertEquals(Map.of(this.getClass(), map), KnownTypeMapping.getKnown());
    }

}