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
import com.webxells.dis.base.SimpleMappingPortrayal;import com.webxells.dis.test.cases.SimpleTestCase;
import com.webxells.dis.test.cases.ConfigurationBuilder;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CopyFromSubDataTest extends SimpleTestCase {

    @Test
    void test() throws InvalidDatasetException {
        CopyFromSubData fixture = new CopyFromSubData();
        String text = random();
        String text2 = random();
        String text3 = random();
        MappingConfiguration configuration = newConfiguration()
                .addPart(ConfigurationBuilder.newPart(ConfigurationBuilder.PartBuilder.PartBuilderState.RANDOM)
                        .withContent()
                )
                .addPart(ConfigurationBuilder.newPart(ConfigurationBuilder.PartBuilder.PartBuilderState.RANDOM)
                        .addSubData(newConfiguration()
                                .addPart(ConfigurationBuilder.newPart(ConfigurationBuilder.PartBuilder.PartBuilderState.RANDOM)
                                        .withContent(text, text2)
                                )
                                .build())
                        .addSameSubData(newConfiguration()
                                .addPart(ConfigurationBuilder.newPart(ConfigurationBuilder.PartBuilder.PartBuilderState.RANDOM)
                                        .withContent(text3)
                                )
                                .build())
                )
                .build();

        fixture.setRoot(SimpleMappingPortrayal.destination(configuration.parts().get(1)));
        fixture.setSub(SimpleMappingPortrayal.source(configuration.parts().get(1).getSubData().get(0).parts().get(0)));
        fixture.setAppend(false);

        fixture.manipulate(null, configuration.parts().get(0));

        assertEquals(text, configuration.parts().get(0).value().get());
        assertEquals(text2, configuration.parts().get(0).getDataset().getContent().get(1).value().get());
        assertEquals(text3, configuration.parts().get(0).getDataset().getContent().get(2).value().get());
    }

}