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
import com.webxells.dis.api.error.InvalidApi;
import com.webxells.dis.api.error.InvalidDatasetException;
import com.webxells.dis.base.SimpleDatasetPiece;
import com.webxells.dis.base.binary.DataPool;
import com.webxells.dis.test.cases.SimpleTestCase;
import java.io.IOException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SaveInDataPoolTest extends SimpleTestCase {

    @Test
    void test() throws InvalidApi, IOException, InvalidDatasetException {
        final String content = random();
        final String index = random();
        DataPool result = new DataPool();
        result.setIndex(index);
        DatasetPiece datasetPiece = new SimpleDatasetPiece(content);
        final SaveInDataPool fixture = new SaveInDataPool();
        assertThrows(InvalidApi.class, fixture::validate);
        fixture.setIndex(index);
        fixture.validate();
        fixture.setName(random());
        fixture.setMimeType(random());
        assertFalse(DataPool.isPresent(index));
        fixture.validate(datasetPiece, null);
        assertTrue(DataPool.isPresent(index));
        assertEquals(content, new String(result.getContent().readAllBytes()));
        assertTrue(datasetPiece.value().isPresent());

        DataPool.remove(index);
        fixture.setResetPart(true);
        assertFalse(DataPool.isPresent(index));
        fixture.manipulate(datasetPiece, null);
        assertTrue(DataPool.isPresent(index));
        assertEquals(content, new String(result.getContent().readAllBytes()));
        assertTrue(datasetPiece.value().isEmpty());
    }
  
}