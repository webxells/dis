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
package com.webxells.dis.gis.manipulator;

import com.webxells.dis.api.config.MappingConfiguration;
import com.webxells.dis.api.error.InvalidDatasetException;
import com.webxells.dis.gis.PointDefinition;
import com.webxells.dis.test.cases.ConfigurationBuilder;
import com.webxells.dis.test.cases.SimpleTestCase;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CentroidTest extends SimpleTestCase {

    @Test
    void test() throws InvalidDatasetException {
        Centroid fixture = new Centroid();
        fixture.setDefinition(new PointDefinition());

        MappingConfiguration configuration = newConfiguration()
                .addPart(ConfigurationBuilder.newPart(ConfigurationBuilder.PartBuilder.PartBuilderState.RANDOM)
                        .withContent("14.4445 53.444 15.4445 52.444 14.1445 51.5555 13.1445 22.5555")
                )
                .addPart(ConfigurationBuilder.newPart(ConfigurationBuilder.PartBuilder.PartBuilderState.RANDOM)
                        .withContent("14.4445 53.444 15.4445 52.444 14.1445 51.5555")
                )
                .build();

        fixture.manipulate(configuration.parts().get(0).getDataset().getContent().get(0), configuration.parts().get(0));
        fixture.manipulate(configuration.parts().get(1).getDataset().getContent().get(0), configuration.parts().get(1));

        assertEquals("13.548231343283488 37.8019888059699", configuration.parts().get(0).value().get());
        assertEquals("14.677833333333396 52.48116666666674", configuration.parts().get(1).value().get());

    }

}