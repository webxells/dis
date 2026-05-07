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
import com.webxells.dis.api.error.InvalidApi;
import com.webxells.dis.base.SimpleMappingPortrayal;
import com.webxells.dis.test.cases.ConfigurationBuilder;
import com.webxells.dis.test.cases.SimpleTestCase;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CopyFromOtherPartsTest extends SimpleTestCase {

    @Test
    void testWithoutSelf() throws InvalidApi {
        test(3, new CopyFromOtherParts());
    }

    @Test
    void testWithSelf() throws InvalidApi {
        CopyFromOtherParts fixture = new CopyFromOtherParts();
        fixture.setKeepSelf(true);
        test(0, fixture);
    }

    void test(int start, final CopyFromOtherParts fixture) throws InvalidApi {
        List<String> values = List.of(
                random(), random(), random(), random(), random(), random(),
                random(), random(), random(), random(), random(), random());
        MappingConfiguration configuration = newConfiguration()
                .addPart(ConfigurationBuilder.newPart(ConfigurationBuilder.PartBuilder.PartBuilderState.RANDOM)
                        .withContent(List.of(values.get(0), values.get(1), values.get(2)))
                )
                .addPart(ConfigurationBuilder.newPart(ConfigurationBuilder.PartBuilder.PartBuilderState.RANDOM)
                        .withContent(List.of(values.get(3), values.get(4), values.get(5)))
                )
                .addPart(ConfigurationBuilder.newPart(ConfigurationBuilder.PartBuilder.PartBuilderState.RANDOM)
                        .withContent(List.of(values.get(6), values.get(7), values.get(8)))
                )
                .addPart(ConfigurationBuilder.newPart(ConfigurationBuilder.PartBuilder.PartBuilderState.RANDOM)
                        .withContent(List.of(values.get(9), values.get(10), values.get(11)))
                )
                .addPart(ConfigurationBuilder.newPart(ConfigurationBuilder.PartBuilder.PartBuilderState.RANDOM)
                        .withContent(List.of(random(), random(), random()))
                )
                .build();
        fixture.setOthers(List.of(
                SimpleMappingPortrayal.source(configuration.parts().get(1)),
                SimpleMappingPortrayal.source(configuration.parts().get(2)),
                SimpleMappingPortrayal.source(configuration.parts().get(3))
        ));

        fixture.validate();

        fixture.manipulate(null, configuration.parts().get(0));

        assertEquals(12 - start, configuration.parts().get(0).getDataset().getContent().size());

        for (int i = start; i < 12; i++) {
            assertEquals(values.get(i),
                    configuration.parts().get(0).getDataset().getContent().get(i - start).value().get());
        }

    }

}