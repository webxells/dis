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
import com.webxells.dis.api.error.InvalidDatasetException;
import com.webxells.dis.base.SimpleMappingPortrayal;
import com.webxells.dis.test.cases.SimpleTestCase;
import com.webxells.dis.test.cases.ConfigurationBuilder;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ResetPartTest extends SimpleTestCase {

    @Test
    void test() throws InvalidDatasetException {
        MappingConfiguration configuration = getRandomizedConfiguration();

        ResetPart fixture = new ResetPart();
        fixture.setMappingPortrayals(List.of(SimpleMappingPortrayal.source(configuration.parts().get(2)),
                SimpleMappingPortrayal.destination(configuration.parts().get(0))));

        fixture.manipulate(null, configuration.parts().get(1));

        Assertions.assertTrue(configuration.parts().get(0).value().isEmpty());
        Assertions.assertFalse(configuration.parts().get(1).value().isEmpty());
        Assertions.assertTrue(configuration.parts().get(2).value().isEmpty());
        Assertions.assertEquals(1, configuration.parts().get(2).getSubData().size());
        Assertions.assertTrue(configuration.parts().get(2).getSubData().get(0).parts().get(0).value().isEmpty());
        Assertions.assertTrue(configuration.parts().get(2).getSubData().get(0).parts().get(1).value().isEmpty());
    }


    @Test
    void testWithoutPortrayals() throws InvalidDatasetException {
        MappingConfiguration configuration = getRandomizedConfiguration();

        ResetPart fixture = new ResetPart();

        fixture.manipulate(null, configuration.parts().get(1));

        Assertions.assertFalse(configuration.parts().get(0).value().isEmpty());
        Assertions.assertTrue(configuration.parts().get(1).value().isEmpty());
        Assertions.assertFalse(configuration.parts().get(2).value().isEmpty());
    }

    private MappingConfiguration getRandomizedConfiguration() {
        return newConfiguration()
                .addPart(ConfigurationBuilder.newPart(ConfigurationBuilder.PartBuilder.PartBuilderState.RANDOM)
                        .withContent()
                        .withContent())
                .addPart(ConfigurationBuilder.newPart(ConfigurationBuilder.PartBuilder.PartBuilderState.RANDOM)
                        .withContent())
                .addPart(ConfigurationBuilder.newPart(ConfigurationBuilder.PartBuilder.PartBuilderState.RANDOM)
                        .withContent()
                        .addSubData(newConfiguration()
                                .addPart(ConfigurationBuilder.newPart(ConfigurationBuilder.PartBuilder.PartBuilderState.RANDOM)
                                        .withContent())
                                .addPart(ConfigurationBuilder.newPart(ConfigurationBuilder.PartBuilder.PartBuilderState.RANDOM)
                                        .withContent())
                                .build())
                        .addSubData(newConfiguration()
                                .addPart(ConfigurationBuilder.newPart(ConfigurationBuilder.PartBuilder.PartBuilderState.RANDOM)
                                        .withContent())
                                .addPart(ConfigurationBuilder.newPart(ConfigurationBuilder.PartBuilder.PartBuilderState.RANDOM)
                                        .withContent())
                                .build())
                )
                .build();
    }

}