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

import com.webxells.dis.api.error.InvalidApi;
import com.webxells.dis.api.error.InvalidDatasetException;
import com.webxells.dis.base.binary.DataPool;
import com.webxells.dis.base.binary.RawData;
import com.webxells.dis.test.cases.SimpleTestCase;
import org.junit.jupiter.api.Test;

import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.*;

public class CopyDataPoolContentToIndexTest extends SimpleTestCase {

    @Test
    void test() throws Exception {
        String originalIndex = random();
        String copyIndex = random();

        String name = random();
        String mimeType= random();
        int size = random(0);

        InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("test.hex");
        RawData rawData = new RawData(inputStream);
        rawData.setName(name);
        rawData.setMimeType(mimeType);
        rawData.setSize(size);
        DataPool.registerData(originalIndex, rawData);

        CopyDataPoolContentToIndex fixture = new CopyDataPoolContentToIndex();
        fixture.setRegisteredIndex(originalIndex);
        fixture.setCopyIndex(copyIndex);

        fixture.manipulate(null, null);

        DataPool dataPool = new DataPool();
        dataPool.setIndex(copyIndex);
        assertTrue(0 < dataPool.getContent().available());
        assertEquals(name, dataPool.getName());
        assertEquals(mimeType, dataPool.getMimeType());
        assertEquals(size, dataPool.getSize());

        dataPool.setIndex(originalIndex);
        assertTrue(0 < dataPool.getContent().available());
        assertEquals(name, dataPool.getName());
        assertEquals(mimeType, dataPool.getMimeType());
        assertEquals(size, dataPool.getSize());
    }

    @Test
    void testIndexAlreadyRead() throws Exception {
        String originalIndex = random();
        String copyIndex = random();

        InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("test.hex");
        DataPool.registerData(originalIndex, new RawData(inputStream));

        CopyDataPoolContentToIndex fixture = new CopyDataPoolContentToIndex();
        fixture.setRegisteredIndex(originalIndex);
        fixture.setCopyIndex(copyIndex);

        DataPool dataPool = new DataPool();
        dataPool.setIndex(originalIndex);
        dataPool.getContent().readAllBytes();

        assertThrows(InvalidDatasetException.class, () -> fixture.manipulate(null, null));
    }

    @Test
    void testValidate() {
        CopyDataPoolContentToIndex fixture = new CopyDataPoolContentToIndex();
        fixture.setRegisteredIndex(random());
        assertThrows(InvalidApi.class, fixture::validate, "Required fields are missing");
    }

}
