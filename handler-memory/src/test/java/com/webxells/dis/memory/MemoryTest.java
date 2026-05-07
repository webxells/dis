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
package com.webxells.dis.memory;

import com.webxells.dis.api.config.MappingConfiguration;
import com.webxells.dis.base.SimpleDatasetPiece;
import com.webxells.dis.base.config.SimpleMappingConfiguration;
import com.webxells.dis.base.config.StableMappingPart;
import com.webxells.dis.base.config.SimpleMappingPoint;
import com.webxells.dis.test.cases.ConfigurationBuilder;
import java.util.List;

import com.webxells.dis.test.cases.SimpleTestCase;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MemoryTest extends SimpleTestCase {

    @Test
    void testPopping() {
        String random = random();
        String random2 = random();
        MappingConfiguration configuration = newConfiguration()
                .addPart(ConfigurationBuilder.newPart()
                        .setInput(new SimpleMappingPoint("test", "test"))
                        .setOutput(new SimpleMappingPoint("test", "test"))
                        .withContent(random)
                )
                .build();

        MemoryOutputConfig memoryOutputConfig = new MemoryOutputConfig();
        memoryOutputConfig.setName("test");
        MemoryOutput memoryOutput = new MemoryOutput(memoryOutputConfig);
        memoryOutput.write(configuration);

        configuration.clear();
        assertTrue(configuration.parts().get(0).value().isEmpty());

        MemoryInputConfig memoryInputConfig = new MemoryInputConfig();
        memoryInputConfig.setPopInsteadOfRead(true);
        memoryInputConfig.setName("test");
        MemoryInput memoryInput = new MemoryInput(memoryInputConfig);

        assertEquals(1, memoryInput.read(configuration));
        assertEquals(random, configuration.parts().get(0).value().get());

        configuration.clear();
        assertTrue(configuration.parts().get(0).value().isEmpty());
        assertEquals(0, memoryInput.read(configuration));

        configuration.parts().get(0).getDataset().collect(new SimpleDatasetPiece(random2));
        memoryOutput.write(configuration);

        configuration.clear();
        assertEquals(1, memoryInput.read(configuration));
    }

    @Test
    void test() {
        String key1 = random();
        String key2 = random();
        String key3 = random();
        String name1 = random();
        String name2 = random();
        String name3 = random();
        SimpleDatasetPiece ds1_1 = new SimpleDatasetPiece(random());
        SimpleDatasetPiece ds1_2 = new SimpleDatasetPiece(random());
        SimpleDatasetPiece ds1_3 = new SimpleDatasetPiece(random());
        SimpleDatasetPiece ds1_4 = new SimpleDatasetPiece(random());
        SimpleDatasetPiece ds2_1 = new SimpleDatasetPiece(random());
        SimpleDatasetPiece ds2_2 = new SimpleDatasetPiece(random());
        SimpleDatasetPiece ds2_3 = new SimpleDatasetPiece(random());
        SimpleDatasetPiece ds2_4 = new SimpleDatasetPiece(random());
        SimpleDatasetPiece ds3_4 = new SimpleDatasetPiece(random());
        SimpleMappingConfiguration mappingConfiguration = new SimpleMappingConfiguration();
        mappingConfiguration.setParts(List.of(
                new StableMappingPart(mappingConfiguration, new SimpleMappingPoint(name1, key1),
                        new SimpleMappingPoint(name1, key1)),
                new StableMappingPart(mappingConfiguration, new SimpleMappingPoint(random("random input"), key1),
                        new SimpleMappingPoint(random("random output"), key1)),
                new StableMappingPart(mappingConfiguration, new SimpleMappingPoint(name1, key2),
                        new SimpleMappingPoint(name1, key2)),
                new StableMappingPart(mappingConfiguration, new SimpleMappingPoint(name2, key3),
                        new SimpleMappingPoint(name2, key3)),
                new StableMappingPart(mappingConfiguration, new SimpleMappingPoint(name3, key3),
                        new SimpleMappingPoint(name3, key3))
        ));
        MemoryOutputConfig outputConfig1 = new MemoryOutputConfig();
        outputConfig1.setName(name1);
        MemoryOutputConfig outputConfig2 = new MemoryOutputConfig();
        outputConfig2.setName(name2);
        MemoryOutputConfig outputConfig3 = new MemoryOutputConfig();
        outputConfig3.setName(name3);
        outputConfig3.setOverwrite(true);
        MemoryOutput output1 = new MemoryOutput(outputConfig1);
        MemoryOutput output2 = new MemoryOutput(outputConfig2);
        MemoryOutput output3 = new MemoryOutput(outputConfig3);
        output1.start();
        output2.start();
        output3.start();
        mappingConfiguration.parts().get(0).getDataset().collect(ds1_1);
        mappingConfiguration.parts().get(2).getDataset().collect(ds1_2);
        mappingConfiguration.parts().get(3).getDataset().collect(ds1_3);
        mappingConfiguration.parts().get(3).getDataset().collect(ds1_3);
        mappingConfiguration.parts().get(4).getDataset().collect(ds1_4);
        output1.write(mappingConfiguration);
        output2.write(mappingConfiguration);
        output3.write(mappingConfiguration);
        mappingConfiguration.clear();
        mappingConfiguration.parts().get(0).getDataset().collect(ds2_1);
        mappingConfiguration.parts().get(2).getDataset().collect(ds2_2);
        mappingConfiguration.parts().get(3).getDataset().collect(ds2_3);
        mappingConfiguration.parts().get(4).getDataset().collect(ds2_4);
        output1.write(mappingConfiguration);
        output2.write(mappingConfiguration);
        output3.write(mappingConfiguration);
        output1.end();
        output2.end();
        output3.end();

        //Now read output
        MemoryInputConfig inputConfig1 = new MemoryInputConfig();
        inputConfig1.setName(name1);
        MemoryInputConfig inputConfig2 = new MemoryInputConfig();
        inputConfig2.setName(name2);
        MemoryInputConfig inputConfig3 = new MemoryInputConfig();
        inputConfig3.setName(name3);
        inputConfig3.setPersistent(true);
        MemoryInput input1 = new MemoryInput(inputConfig1);
        MemoryInput input2 = new MemoryInput(inputConfig2);
        MemoryInput input3 = new MemoryInput(inputConfig3);
        input1.start();
        input2.start();
        input3.start();
        mappingConfiguration.clear();
        assertTrue(input1.hasNext());
        int read1_1 = input1.read(mappingConfiguration);
        assertTrue(input1.hasNext());
        int read1_2 = input1.read(mappingConfiguration);
        assertFalse(input1.hasNext());
        assertTrue(input2.hasNext());
        int read2_1 = input2.read(mappingConfiguration);
        assertTrue(input2.hasNext());
        int read2_2 = input2.read(mappingConfiguration);
        assertFalse(input2.hasNext());
        assertTrue(input3.hasNext());
        int read3_1 = input3.read(mappingConfiguration);
        assertTrue(input3.hasNext());
        int read3_2 = input3.read(mappingConfiguration);
        assertFalse(input3.hasNext());
        assertEquals(0, input1.read(mappingConfiguration));
        assertEquals(0, input2.read(mappingConfiguration));
        assertEquals(0, input3.read(mappingConfiguration));
        input1.start();
        input2.start();
        input3.start();
        assertFalse(input1.hasNext());
        assertFalse(input2.hasNext());
        assertTrue(input3.hasNext());
        int read3_3 = input3.read(mappingConfiguration);
        assertTrue(input3.hasNext());
        int read3_4 = input3.read(mappingConfiguration);
        assertFalse(input3.hasNext());
        input1.end();
        input2.end();
        input3.end();
        assertEquals(2, read1_1);
        assertEquals(2, read1_2);
        assertEquals(2, read2_1);
        assertEquals(1, read2_2);
        assertEquals(1, read3_1);
        assertEquals(1, read3_2);
        assertEquals(1, read3_3);
        assertEquals(1, read3_4);

        mappingConfiguration.clear();
        mappingConfiguration.parts().get(4).getDataset().collect(ds3_4);

        output3.start();
        output3.write(mappingConfiguration);
        output3.end();

        mappingConfiguration.clear();

        input3.start();
        int read4_2 = input3.read(mappingConfiguration);
        input3.end();
        assertEquals(1, read4_2);
        assertEquals(ds3_4.value().get(), mappingConfiguration.parts().get(4).value().get());

    }

}