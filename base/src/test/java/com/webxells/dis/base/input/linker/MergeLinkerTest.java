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
package com.webxells.dis.base.input.linker;

import com.webxells.dis.api.DatasetPiece;
import com.webxells.dis.api.config.MappingConfiguration;
import com.webxells.dis.api.error.DisException;
import com.webxells.dis.base.SimpleDatasetPiece;
import com.webxells.dis.base.config.SimpleMappingConfiguration;
import com.webxells.dis.test.example.input.TestInput;
import com.webxells.dis.test.example.input.TestInputConfig;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;


class MergeLinkerTest {
    @Test
    void test() throws DisException {
        MappingConfiguration mapping = new SimpleMappingConfiguration();
        List<Map<String, DatasetPiece>> list = new LinkedList<>() {{
            add(Map.of(random(), new SimpleDatasetPiece(random())));
            add(Map.of(random(), new SimpleDatasetPiece(random()), random(), new SimpleDatasetPiece(random())));
        }};
        TestInput.setData(list);
        MergeLinker fixture = new MergeLinker();
        fixture.setInputConfig(new TestInputConfig());
        assertFalse(TestInput.isEnd());
        assertFalse(TestInput.isStart());
        fixture.start();
        assertTrue(TestInput.isStart());
        assertFalse(TestInput.isEnd());
        int data1 = fixture.getData(mapping);
        int data2 = fixture.getData(mapping);
        assertEquals(0, fixture.getData(mapping));
        assertSame(1, data1);
        assertSame(2, data2);
        fixture.end();
        assertTrue(TestInput.isEnd());
    }

    private String random() {
        return String.valueOf(Math.round(Math.random() * 85743));
    }
}