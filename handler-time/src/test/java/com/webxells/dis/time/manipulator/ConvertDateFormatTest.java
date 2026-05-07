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
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ConvertDateFormatTest extends SimpleTestCase {

    @Test
    void test() throws InvalidApi, InvalidDatasetException {
        final SimpleMappingConfiguration config = newConfiguration()
                .addPart(new ConfigurationBuilder.PartBuilder()
                        .withContent("2022:07:04 11:11:25"))
                .build();

        ConvertDateFormat fixture = new ConvertDateFormat();
        fixture.setFormat("yyyy:MM:dd HH:mm:ss");
        fixture.setConvertFormat("yyyy-MM-dd'T'HH:mm:ss");
        fixture.validate();

        fixture.manipulate(config.parts().get(0).getDataset().getContent().get(0), config.parts().get(0));

        assertEquals("2022-07-04T11:11:25", config.parts().get(0).value().get());
    }

    @Test
    void testWrongOriginalFormat() throws InvalidApi {
        final SimpleMappingConfiguration config = newConfiguration()
                .addPart(new ConfigurationBuilder.PartBuilder()
                        .withContent("2022:07:04 11:11:25"))
                .build();

        ConvertDateFormat fixture = new ConvertDateFormat();
        fixture.setFormat("yyyy-MM-dd'T'HH:mm:ss");
        fixture.setConvertFormat("yyyy:MM:dd HH:mm:ss");
        fixture.validate();

        assertThrows(InvalidDatasetException.class, () ->
                        fixture.manipulate(config.parts().get(0).getDataset().getContent().get(0), config.parts().get(0)),
                "Date 2022:07:04 11:11:25 could not be parsed with format yyyy-MM-dd'T'HH:mm:ss");
    }

}
