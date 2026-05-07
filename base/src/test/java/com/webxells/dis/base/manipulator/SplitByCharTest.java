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
import com.webxells.dis.api.config.MappingPart;
import com.webxells.dis.api.error.InvalidApi;
import com.webxells.dis.api.error.InvalidDatasetException;
import com.webxells.dis.base.SimpleDatasetPiece;
import com.webxells.dis.base.config.SimpleMappingPart;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SplitByCharTest {

    @Test
    void test() throws InvalidApi, InvalidDatasetException {
        SplitByChar fixture = new SplitByChar();
        assertThrows(InvalidApi.class, fixture::validate);
        fixture.setChars(List.of(",", ";"));
        fixture.validate();
        final DatasetPiece piece = new SimpleDatasetPiece("a,b,c;asd;fgfgfg");
        final MappingPart part = new SimpleMappingPart(null);
        part.getDataset().collect(piece);
        fixture.manipulate(piece, part);
        assertEquals(5, part.getDataset().getContent().size());
        assertEquals("a", part.getDataset().getContent().get(0).value().get());
        assertEquals("b", part.getDataset().getContent().get(1).value().get());
        assertEquals("c", part.getDataset().getContent().get(2).value().get());
        assertEquals("asd", part.getDataset().getContent().get(3).value().get());
        assertEquals("fgfgfg", part.getDataset().getContent().get(4).value().get());
    }

    @Test
    void testMultiValue() throws InvalidApi, InvalidDatasetException {
        SplitByChar fixture = new SplitByChar();
        assertThrows(InvalidApi.class, fixture::validate);
        fixture.setChars(List.of(",", ";"));
        fixture.validate();
        final DatasetPiece piece1 = new SimpleDatasetPiece("a,b,c");
        final DatasetPiece piece2 = new SimpleDatasetPiece("asd;fgfgfg");
        final MappingPart part = new SimpleMappingPart(null);
        part.getDataset().collect(List.of(piece1, piece2));
        fixture.manipulate(piece1, part);
        assertEquals(5, part.getDataset().getContent().size());
        assertEquals("a", part.getDataset().getContent().get(0).value().get());
        assertEquals("b", part.getDataset().getContent().get(1).value().get());
        assertEquals("c", part.getDataset().getContent().get(2).value().get());
        assertEquals("asd", part.getDataset().getContent().get(3).value().get());
        assertEquals("fgfgfg", part.getDataset().getContent().get(4).value().get());
    }

}