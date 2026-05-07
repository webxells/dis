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

import com.webxells.dis.api.DatasetPiece;
import com.webxells.dis.api.config.MappingConfiguration;
import com.webxells.dis.base.config.RandomInputMappingPart;
import com.webxells.dis.base.config.SimpleMappingPoint;
import com.webxells.dis.test.cases.ConfigurationBuilder;
import com.webxells.dis.test.cases.SimpleTestCase;
import java.util.LinkedList;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RandomTest extends SimpleTestCase {

    @Test
    void test() {
        RandomConfig config = new RandomConfig();
        config.setMax(randomMax(50000));
        config.setMin(config.getMax() - 50000);
        config.setValueSize(randomMax(10) + 1);
        config.setName(random());
        RandomInputMappingPart randomInputMappingPart = new RandomInputMappingPart(null);
        randomInputMappingPart.setInput(new SimpleMappingPoint(config.getName(), random()));
        randomInputMappingPart.setMin(config.getMax() + 200);
        randomInputMappingPart.setMax(config.getMax() + 400);
        randomInputMappingPart.setSecureGeneration(true);
        randomInputMappingPart.setValueSize(config.getValueSize() + 3);
        Random fixture = new Random(config);
        MappingConfiguration mappingConfiguration = newConfiguration()
                .addPart(new ConfigurationBuilder.PartBuilder()
                        .setInput(new SimpleMappingPoint(config.getName(), random()))
                )
                .addPart(new ConfigurationBuilder.PartBuilder()
                        .setInput(new SimpleMappingPoint(config.getName(), random()))
                )
                .addPart(randomInputMappingPart)
                .addPart(new ConfigurationBuilder.PartBuilder())
                .addPart(new ConfigurationBuilder.PartBuilder(ConfigurationBuilder.PartBuilder.PartBuilderState.RANDOM))
                .build();
        fixture.start();

        assertTrue(fixture.hasNext());
        assertEquals(3, fixture.read(mappingConfiguration));
        assertFalse(fixture.hasNext());

        assertEquals(config.getValueSize(), mappingConfiguration.parts().get(0).getDataset().getContent().size());
        assertEquals(config.getValueSize(), mappingConfiguration.parts().get(1).getDataset().getContent().size());
        assertEquals(config.getValueSize() + 3, mappingConfiguration.parts().get(2).getDataset().getContent().size());
        assertEquals(0, mappingConfiguration.parts().get(3).getDataset().getContent().size());
        assertEquals(0, mappingConfiguration.parts().get(4).getDataset().getContent().size());

        List<Long> values = new LinkedList<>();
        addToList(values, mappingConfiguration.parts().get(0).getDataset().getContent());
        addToList(values, mappingConfiguration.parts().get(1).getDataset().getContent());
        values.forEach(a -> {
            // System.out.println(a + " => " + config.getMax() + " : " + config.getMin());
            assertTrue(a <= config.getMax() && a >= config.getMin());
        });
        values.clear();
        addToList(values, mappingConfiguration.parts().get(2).getDataset().getContent());
        values.forEach(a -> assertTrue(a <= randomInputMappingPart.getMax() && a >= randomInputMappingPart.getMin()));
    }

    private void addToList(final List<Long> values, final List<DatasetPiece> content) {
        content.stream()
                .map(a -> Long.parseLong(a.value().get()))
                .forEach(values::add);
    }

}