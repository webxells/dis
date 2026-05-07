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

import com.webxells.dis.api.error.InvalidDatasetException;
import com.webxells.dis.base.SimpleDatasetPiece;
import com.webxells.dis.base.SimpleMappingPortrayal;
import com.webxells.dis.base.binary.DataPool;
import com.webxells.dis.base.config.SimpleMappingConfiguration;
import com.webxells.dis.base.config.SimpleMappingPart;
import com.webxells.dis.base.config.SimpleMappingPoint;import com.webxells.dis.test.cases.SimpleTestCase;
import com.webxells.dis.test.cases.ConfigurationBuilder;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class Hex2BinaryTest extends SimpleTestCase {
    private DataPool dataPool = new DataPool();

    @Test
    void test() throws IOException, InvalidDatasetException {
        runTest("test.hex", 78425);
        runTest("test2.hex", 41479);
    }

    void runTest(String file, int size) throws IOException, InvalidDatasetException {
        String index = random();
        dataPool.setIndex(index);
        Assertions.assertNull(dataPool.getContent());

        SimpleMappingConfiguration config = new SimpleMappingConfiguration();
        config.setParts(List.of(
                new SimpleMappingPart(config, new SimpleMappingPoint(random(), random()),
                        new SimpleMappingPoint(random(), random())) {{
                            getDataset().collect(new SimpleDatasetPiece(random("filename")));
                }}, new SimpleMappingPart(config, new SimpleMappingPoint(random(), random()),
                        new SimpleMappingPoint(random(), random())) {{
                    getDataset().collect(new SimpleDatasetPiece(random("type")));
                }}, new SimpleMappingPart(config, new SimpleMappingPoint(random(), random()),
                        new SimpleMappingPoint(random(), random())) {{
                    getDataset().collect(new SimpleDatasetPiece(new String(Hex2BinaryTest.class
                            .getClassLoader().getResourceAsStream(file).readAllBytes()).trim()));
                }}
        ));
        Hex2Binary fixture = new Hex2Binary();
        fixture.setIndex(index);
        fixture.setNamePortrayal(SimpleMappingPortrayal.source(config.parts().get(0)));
        fixture.setTypePortrayal(SimpleMappingPortrayal.source(config.parts().get(1)));

        fixture.manipulate(config.parts().get(2).getDataset().getContent().get(0), config.parts().get(2));

        Assertions.assertEquals(size, dataPool.getSize());
        Assertions.assertEquals(config.parts().get(0).value().get(), dataPool.getName());
        Assertions.assertEquals(config.parts().get(1).value().get(), dataPool.getMimeType());
        Assertions.assertTrue(Arrays.equals(dataPool.getContent().readAllBytes(), Hex2BinaryTest.class
                .getClassLoader().getResourceAsStream(file.concat(".jpg")).readAllBytes()));
    }

}