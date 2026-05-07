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

import com.webxells.dis.api.config.MappingConfiguration;
import com.webxells.dis.api.error.DisException;
import com.webxells.dis.base.config.SimpleMappingPoint;
import com.webxells.dis.test.cases.ConfigurationBuilder;
import com.webxells.dis.test.cases.SimpleTestCase;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StaticValuesTest extends SimpleTestCase {

    @Test
    void test() throws DisException {
        String key1 = random();
        String key2 = random();
        List<Map<String, List<String>>> values = List.of(
                Map.of(
                        key1, List.of(random(), random()),
                        key2, List.of(random(), random())),
                Map.of(
                        key1, List.of(random(), random()),
                        key2, List.of(random())));
        MappingConfiguration mappingConfig = newConfiguration()
                .addPart(ConfigurationBuilder.newPart()
                        .setInput(new SimpleMappingPoint("test", key2))
                )
                .addPart(ConfigurationBuilder.newPart()
                        .setInput(new SimpleMappingPoint("test", key1))
                )
                .addPart(ConfigurationBuilder.newPart()
                        .setInput(new SimpleMappingPoint("test", "key3"))
                )
                .addPart(ConfigurationBuilder.newPart()
                        .setInput(new SimpleMappingPoint("unknown", key1))
                )
                .build();
        StaticValues.Config config = new StaticValues.Config();
        config.setName("test");
        config.setValues(values);
        StaticValues fixture = new StaticValues(config);

        assertFalse(fixture.hasNext());
        fixture.start();
        assertTrue(fixture.hasNext());
        assertEquals(4, fixture.read(mappingConfig));
        assertEquals(2, mappingConfig.parts().get(1).getDataset().getContent().size());
        assertEquals(values.get(0).get(key1).get(0), mappingConfig.parts().get(1).getDataset().getContent().get(0).value().get());
        assertEquals(values.get(0).get(key1).get(1), mappingConfig.parts().get(1).getDataset().getContent().get(1).value().get());
        assertEquals(2, mappingConfig.parts().get(0).getDataset().getContent().size());
        assertEquals(values.get(0).get(key2).get(0), mappingConfig.parts().get(0).getDataset().getContent().get(0).value().get());
        assertEquals(values.get(0).get(key2).get(1), mappingConfig.parts().get(0).getDataset().getContent().get(1).value().get());
        assertEquals(0, mappingConfig.parts().get(2).getDataset().getContent().size());
        assertEquals(0, mappingConfig.parts().get(3).getDataset().getContent().size());

        mappingConfig.clear();
        assertTrue(fixture.hasNext());
        assertEquals(3, fixture.read(mappingConfig));
        assertEquals(2, mappingConfig.parts().get(1).getDataset().getContent().size());
        assertEquals(values.get(1).get(key1).get(0), mappingConfig.parts().get(1).getDataset().getContent().get(0).value().get());
        assertEquals(values.get(1).get(key1).get(1), mappingConfig.parts().get(1).getDataset().getContent().get(1).value().get());
        assertEquals(1, mappingConfig.parts().get(0).getDataset().getContent().size());
        assertEquals(values.get(1).get(key2).get(0), mappingConfig.parts().get(0).getDataset().getContent().get(0).value().get());
        assertEquals(0, mappingConfig.parts().get(2).getDataset().getContent().size());
        assertEquals(0, mappingConfig.parts().get(3).getDataset().getContent().size());

        assertFalse(fixture.hasNext());
        fixture.end();
        assertFalse(fixture.hasNext());
    }

}