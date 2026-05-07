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
package com.webxells.dis.base.input;

import com.webxells.dis.api.MappingPortrayal;
import com.webxells.dis.api.error.InputOutputError;
import com.webxells.dis.api.resource.Resource;
import com.webxells.dis.base.SimpleDatasetPiece;
import com.webxells.dis.base.SimpleMappingPortrayal;
import com.webxells.dis.base.binary.DataPool;
import com.webxells.dis.base.config.SimpleMappingConfiguration;

import java.io.*;
import java.util.List;

import com.webxells.dis.base.config.SimpleMappingPart;
import com.webxells.dis.base.config.SimpleMappingPoint;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DataPoolInputTest {

    @Test
    void test() throws InputOutputError, IOException {
        Resource resource = Mockito.mock(Resource.class);
        InputStream stream = InputStream.nullInputStream();
        Mockito.when(resource.receive()).thenReturn(stream);
        DataPoolConfiguration config = new DataPoolConfiguration();
        config.setName(random());
        config.setReceiver(resource);
        config.setFileNamePortrayal(new SimpleMappingPortrayal());
        DataPoolInput fixture = new DataPoolInput(config);

        assertFalse(fixture.hasNext());
        fixture.start();
        assertTrue(fixture.hasNext());
        assertEquals(1, fixture.read(new SimpleMappingConfiguration()));
        DataPool dataPool = new DataPool();
        dataPool.setIndex(config.getName());
        assertSame(stream, dataPool.getContent());
        assertFalse(fixture.hasNext());
        assertEquals(0, fixture.read(new SimpleMappingConfiguration()));
    }

    @Test
    void testFileNameMapping() throws Exception {
        final String testFileName = "test.hex";
        final String reference = "reference";
        final String path = "path";

        Resource resource = Mockito.mock(Resource.class);
        Mockito.when(resource.receive()).thenReturn(this.getClass().getClassLoader().getResourceAsStream(testFileName));

        SimpleMappingConfiguration config = new SimpleMappingConfiguration();
        SimpleMappingPart simpleMappingPart = new SimpleMappingPart(config,
                new SimpleMappingPoint(reference,path), new SimpleMappingPoint());
        simpleMappingPart.getDataset().getContent().add(new SimpleDatasetPiece(testFileName));

        config.setParts(List.of(simpleMappingPart));

        DataPoolConfiguration dataPoolConfiguration = new DataPoolConfiguration();
        dataPoolConfiguration.setName("file-index");
        dataPoolConfiguration.setReceiver(resource);
        dataPoolConfiguration.setFileNamePortrayal(
                new SimpleMappingPortrayal(MappingPortrayal.Source.INPUT, reference, path));
        DataPoolInput fixture = new DataPoolInput(dataPoolConfiguration);

        fixture.start();
        fixture.read(config);

        DataPool dataPool = new DataPool();
        dataPool.setIndex(dataPoolConfiguration.getName());
        assertEquals(testFileName, dataPool.getName());
    }

    private String random() {
        return String.valueOf(Math.round(Math.random() * 8342));
    }

}
