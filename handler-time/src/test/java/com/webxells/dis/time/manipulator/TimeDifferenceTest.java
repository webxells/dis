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
package com.webxells.dis.time.manipulator;

import com.webxells.dis.api.error.InvalidApi;
import com.webxells.dis.api.error.InvalidDatasetException;
import com.webxells.dis.base.config.SimpleMappingConfiguration;
import com.webxells.dis.test.cases.ConfigurationBuilder;
import com.webxells.dis.test.cases.SimpleTestCase;
import com.webxells.dis.time.StaticTimePortrayal;
import java.time.temporal.ChronoUnit;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TimeDifferenceTest extends SimpleTestCase {

    @Test
    void test() throws InvalidApi, InvalidDatasetException {
        TimeDifference fixture = new TimeDifference();
        assertThrows(InvalidApi.class, fixture::validate);

        final SimpleMappingConfiguration config = newConfiguration()
                .addPart(new ConfigurationBuilder.PartBuilder()
                        .withContent("2022-07-04T11:11:25"))
                .build();
        fixture.setComparativePoint(new StaticTimePortrayal("2022-07-04T11:12:25"));
        fixture.validate();

        fixture.manipulate(config.parts().get(0).getDataset().getContent().get(0), config.parts().get(0));
        assertEquals(1, config.parts().get(0).getDataset().getContent().size());
        assertEquals("60", config.parts().get(0).value().get());

        fixture.setUnit(ChronoUnit.MINUTES);
        fixture.setReferencePoint(new StaticTimePortrayal("2022-07-04T11:11:25"));
        fixture.manipulate(config.parts().get(0).getDataset().getContent().get(0), config.parts().get(0));
        assertEquals(1, config.parts().get(0).getDataset().getContent().size());
        assertEquals("1", config.parts().get(0).value().get());
    }

}