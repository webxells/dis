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

import com.webxells.dis.api.MappingPortrayal;
import com.webxells.dis.api.error.InvalidApi;
import com.webxells.dis.base.config.SimpleMappingConfiguration;
import com.webxells.dis.base.config.SimpleMappingPoint;
import com.webxells.dis.test.cases.ConfigurationBuilder;import com.webxells.dis.test.cases.SimpleTestCase;
import com.webxells.dis.test.cases.ConfigurationBuilder;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CopyFromParentConfigurationByValueAsPathTest extends SimpleTestCase {

    @Test
    void test() throws InvalidApi {
        String destinyReference = random();
        String destinyPath = random();
        SimpleMappingConfiguration configuration = newConfiguration()
                .addPart(ConfigurationBuilder.newPart()
                        .setOutput(new SimpleMappingPoint(destinyReference, destinyPath))
                        .withContent())
                .addPart(ConfigurationBuilder.newPart(ConfigurationBuilder.PartBuilder.PartBuilderState.RANDOM)
                        .addSubData(newConfiguration()
                                .addPart(ConfigurationBuilder.newPart(ConfigurationBuilder.PartBuilder.PartBuilderState.RANDOM)
                                        .withContent(destinyPath)
                                )
                                .build())
                )
                .build();

        CopyFromParentConfigurationByValueAsPath fixture = new CopyFromParentConfigurationByValueAsPath();
        fixture.setParentSource(MappingPortrayal.Source.OUTPUT);
        fixture.setParentReference(destinyReference);

        fixture.validate();
        fixture.manipulate(null, configuration.parts().get(1).getSubData().get(0).parts().get(0));

        assertEquals(configuration.parts().get(0).value().get(),
                configuration.parts().get(1).getSubData().get(0).parts().get(0).value().get());
    }

    @Test
    void testWithoutParentReferenceShouldUseOwn() throws InvalidApi {
        String destinyReference = random();
        String destinyPath = random();
        SimpleMappingConfiguration configuration = newConfiguration()
                .addPart(ConfigurationBuilder.newPart()
                        .setOutput(new SimpleMappingPoint(destinyReference, destinyPath))
                        .withContent())
                .addPart(ConfigurationBuilder.newPart(ConfigurationBuilder.PartBuilder.PartBuilderState.RANDOM)
                        .addSubData(newConfiguration()
                                .addPart(ConfigurationBuilder.newPart(ConfigurationBuilder.PartBuilder.PartBuilderState.RANDOM)
                                        .setOutput(new SimpleMappingPoint(destinyReference, random()))
                                        .withContent(destinyPath)
                                )
                                .build())
                )
                .build();

        CopyFromParentConfigurationByValueAsPath fixture = new CopyFromParentConfigurationByValueAsPath();
        fixture.setParentSource(MappingPortrayal.Source.OUTPUT);

        fixture.validate();
        fixture.manipulate(null, configuration.parts().get(1).getSubData().get(0).parts().get(0));

        assertEquals(configuration.parts().get(0).value().get(),
                configuration.parts().get(1).getSubData().get(0).parts().get(0).value().get());
    }

}