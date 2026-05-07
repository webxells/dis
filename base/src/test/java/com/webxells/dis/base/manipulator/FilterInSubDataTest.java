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

import com.webxells.dis.api.config.MappingConfiguration;
import com.webxells.dis.base.SimpleDatasetPiece;
import com.webxells.dis.base.SimpleMappingPortrayal;
import com.webxells.dis.base.config.SimpleMappingConfiguration;
import com.webxells.dis.base.config.SimpleMappingPoint;
import com.webxells.dis.test.cases.ConfigurationBuilder;
import com.webxells.dis.test.cases.SimpleTestCase;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FilterInSubDataTest extends SimpleTestCase {

    @Test
    void testWithStaticValue() {
        SimpleMappingConfiguration subSubConfig = newConfiguration()
                .addPart(ConfigurationBuilder.newPart(ConfigurationBuilder.PartBuilder.PartBuilderState.RANDOM)
                        .withContent("value1")
                ).addPart(ConfigurationBuilder.newPart(ConfigurationBuilder.PartBuilder.PartBuilderState.RANDOM)
                        .withContent("value2")
                ).addPart(ConfigurationBuilder.newPart(ConfigurationBuilder.PartBuilder.PartBuilderState.RANDOM)
                        .withContent("value3")
                )
                .build();
        SimpleMappingConfiguration subConfig = newConfiguration()
                .addPart(ConfigurationBuilder.newPart(ConfigurationBuilder.PartBuilder.PartBuilderState.RANDOM)
                        .addSubData(subSubConfig)
                )
                .build();

        assertConfiguration(subConfig,  false, "value2");
    }

    @Test
    void testWithOneValue() {
        SimpleMappingConfiguration subSubConfig = newConfiguration()
                .addPart(ConfigurationBuilder.newPart(ConfigurationBuilder.PartBuilder.PartBuilderState.RANDOM)
                        .withContent("value1")
                ).addPart(ConfigurationBuilder.newPart(ConfigurationBuilder.PartBuilder.PartBuilderState.RANDOM)
                        .withContent("value2")
                ).addPart(ConfigurationBuilder.newPart(ConfigurationBuilder.PartBuilder.PartBuilderState.RANDOM)
                        .withContent("value3")
                )
                .build();
        SimpleMappingConfiguration subConfig = newConfiguration()
                .addPart(ConfigurationBuilder.newPart(ConfigurationBuilder.PartBuilder.PartBuilderState.RANDOM)
                        .addSubData(subSubConfig)
                )
                .build();

        assertConfiguration(subConfig,  false);
    }

    @Test
    void testWithMultipleSubData() {
        SimpleMappingConfiguration subSubConfig1 = newConfiguration()
                .addPart(ConfigurationBuilder.newPart()
                        .setInput(new SimpleMappingPoint("test", "test1"))
                        .withContent("value1")
                ).addPart(ConfigurationBuilder.newPart()
                        .setInput(new SimpleMappingPoint("test", "test2"))
                        .withContent("value2")
                )
                .build();
        SimpleMappingConfiguration subSubConfig2 = newConfiguration()
                .addPart(ConfigurationBuilder.newPart()
                        .setInput(new SimpleMappingPoint("test", "test1"))
                        .withContent("value1")
                ).addPart(ConfigurationBuilder.newPart()
                        .setInput(new SimpleMappingPoint("test", "test2"))
                        .withContent("value3")
                )
                .build();


        SimpleMappingConfiguration subConfig = newConfiguration()
                .addPart(ConfigurationBuilder.newPart(ConfigurationBuilder.PartBuilder.PartBuilderState.RANDOM)
                        .addSubData(subSubConfig1)
                        .addSubData(subSubConfig2)
                )
                .build();

        assertConfiguration(subConfig,  true);
    }
    private void assertConfiguration(MappingConfiguration subConfig, boolean findALl) {
        assertConfiguration(subConfig, findALl, null);
    }

    private void assertConfiguration(MappingConfiguration subConfig, boolean findALl, String value) {
        MappingConfiguration subSubConfig1 = subConfig.parts().get(0).getSubData().get(0);
        MappingConfiguration subSubConfig2 = subConfig.parts().get(0).getSubData().size() > 1
                ? subConfig.parts().get(0).getSubData().get(1) : null;

        SimpleMappingConfiguration config  = newConfiguration()
                .addPart(ConfigurationBuilder.newPart(ConfigurationBuilder.PartBuilder.PartBuilderState.RANDOM)
                        .addSubData(subConfig)
                        .withContent(findALl ? subSubConfig1.parts().get(0).value().get() : subSubConfig1.parts().get(1).value().get())
                )
                .build();
        FilterInSubData fixture = new FilterInSubData();
        fixture.setFindAll(findALl);
        fixture.setValue(value);
        fixture.setRoot(List.of(
                SimpleMappingPortrayal.source(config.parts().get(0)),
                SimpleMappingPortrayal.source(subConfig.parts().get(0))
        ));

        if (findALl) {
            fixture.setFilter(SimpleMappingPortrayal.source(subSubConfig1.parts().get(0)));
            fixture.setResult(SimpleMappingPortrayal.source(subSubConfig1.parts().get(1)));
        } else {
            fixture.setFilter(SimpleMappingPortrayal.source(subSubConfig1.parts().get(1)));
            fixture.setResult(SimpleMappingPortrayal.source(subSubConfig1.parts().get(2)));
        }

        fixture.manipulate(null == value ? config.parts().get(0).getDataset().getContent().get(0) : null, config.parts().get(0));

        if (null != subSubConfig2) {
            assertEquals(subSubConfig1.parts().get(1).getDataset().getContent().get(0), config.parts().get(0).getDataset().getContent().get(0));
            assertEquals(subSubConfig2.parts().get(1).getDataset().getContent().get(0), config.parts().get(0).getDataset().getContent().get(1));
        } else {
            assertEquals(subSubConfig1.parts().get(2).getDataset().getContent(), config.parts().get(0).getDataset().getContent());
        }

        config.parts().get(0).getDataset().getContent().clear();
        if (null == value) {
            config.parts().get(0).getDataset().collect(new SimpleDatasetPiece(random("otherValue")));
        } else {
            fixture.setValue("otherValue");
        }
        fixture.manipulate(null == value ? config.parts().get(0).getDataset().getContent().get(0) : null, config.parts().get(0));

        assertTrue(config.parts().get(0).value().isEmpty());
    }
}