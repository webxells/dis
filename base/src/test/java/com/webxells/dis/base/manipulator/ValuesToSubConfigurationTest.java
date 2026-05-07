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
import com.webxells.dis.api.MappingPortrayal;
import com.webxells.dis.api.config.MappingConfiguration;
import com.webxells.dis.api.error.InvalidApi;
import com.webxells.dis.base.SimpleMappingPortrayal;
import com.webxells.dis.base.config.SimpleMappingConfiguration;
import com.webxells.dis.test.cases.ConfigurationBuilder;import com.webxells.dis.test.cases.SimpleTestCase;
import com.webxells.dis.test.cases.ConfigurationBuilder;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ValuesToSubConfigurationTest extends SimpleTestCase {

    @Test
    void testWithExistingSubDataAndContent() throws InvalidApi {
        SimpleMappingConfiguration config = newConfiguration()
                .addPart(ConfigurationBuilder.newPart(ConfigurationBuilder.PartBuilder.PartBuilderState.RANDOM)
                        .addSubData(newConfiguration()
                                .addPart(ConfigurationBuilder.newPart(ConfigurationBuilder.PartBuilder.PartBuilderState.RANDOM)
                                        .withContent(random())
                                )
                                .build())
                )
                .build();
        test(config, SimpleMappingPortrayal.destination(config.parts().get(0).getSubData().get(0).parts().get(0)));
    }

    @Test
    void testWithExistingSubDataButNoContent() throws InvalidApi {
        SimpleMappingConfiguration config = newConfiguration()
                .addPart(ConfigurationBuilder.newPart(ConfigurationBuilder.PartBuilder.PartBuilderState.RANDOM)
                        .addSubData(newConfiguration()
                                .addPart(ConfigurationBuilder.newPart(ConfigurationBuilder.PartBuilder.PartBuilderState.RANDOM))
                                .build())
                )
                .build();
        test(config, SimpleMappingPortrayal.destination(config.parts().get(0).getSubData().get(0).parts().get(0)));
    }

    @Test
    void testWithNoSubData() throws InvalidApi {
        SimpleMappingConfiguration config = newConfiguration()
                .addPart(ConfigurationBuilder.newPart(ConfigurationBuilder.PartBuilder.PartBuilderState.RANDOM))
                .build();
        test(config, new SimpleMappingPortrayal(random(), random()));
    }

    private void test(final SimpleMappingConfiguration config, final MappingPortrayal portrayal) throws InvalidApi {
        ValuesToSubConfiguration fixture = new ValuesToSubConfiguration();
        assertThrows(InvalidApi.class,fixture::validate);
        fixture.setDestinyPortrayal(portrayal);
        fixture.validate();
        List<DatasetPiece> content = List.of(
                SimpleTestCase.createDataSetPiece(), SimpleTestCase.createDataSetPiece(),
                SimpleTestCase.createDataSetPiece());
        config.parts().get(0).getDataset().collect(content);
        fixture.manipulate(null, config.parts().get(0));

        assertEquals(3, config.parts().get(0).getSubData().size());
        assertTrue(config.parts().get(0).value().isEmpty());

        for (int i = 0; i < config.parts().get(0).getSubData().size(); i++) {
            MappingConfiguration current = config.parts().get(0).getSubData().get(i);
            assertTrue(current.getByPortrayal(portrayal).isPresent());
            assertEquals(content.get(i).value().get(), current.getByPortrayal(portrayal).get().value().get());
        }

    }

}