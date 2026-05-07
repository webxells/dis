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

import com.webxells.dis.api.config.MappingPart;
import com.webxells.dis.api.error.InvalidDatasetException;
import com.webxells.dis.base.SimpleDatasetPiece;
import com.webxells.dis.base.config.StableMappingPart;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AddToValueTest {

    @Test
    void test() throws InvalidDatasetException {
        AddToValue fixture = new AddToValue();
        MappingPart part = new StableMappingPart(null);
        part.getDataset().getContent().add(new SimpleDatasetPiece("first"));

        fixture.manipulate(new SimpleDatasetPiece("second"), part);
        assertEquals(2, part.getDataset().getContent().size());
        assertEquals("first", part.getDataset().getContent().get(0).value().get());
        assertEquals("second", part.getDataset().getContent().get(1).value().get());

        fixture.setClearOther(true);
        fixture.manipulate(new SimpleDatasetPiece("third"), part);
        assertEquals(1, part.getDataset().getContent().size());
        assertEquals("third", part.getDataset().getContent().get(0).value().get());


    }
}