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
package com.webxells.dis.officex.xlsx;

import com.webxells.dis.api.config.MappingConfiguration;
import com.webxells.dis.api.error.DisException;
import com.webxells.dis.base.config.SimpleMappingPoint;
import com.webxells.dis.test.TestResource;
import com.webxells.dis.test.cases.ConfigurationBuilder;
import com.webxells.dis.test.cases.SimpleTestCase;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class XssfMultiSheetInputTest extends SimpleTestCase {

    @Test
    void test() throws DisException {
        XssfMultiSheetInputConfig config = new XssfMultiSheetInputConfig();
        config.setName("test");
        config.setCellAccessType(OfficeXssfReaderConfig.CellAccessType.VALUE_OF_FIRST_LINE);
        config.setReceiver(new TestResource("multisheets.xlsx"));
        MappingConfiguration mappingConfiguration = newConfiguration()
                .addPart(ConfigurationBuilder.newPart()
                        .setInput(new SimpleMappingPoint("test", "a")))
                .addPart(ConfigurationBuilder.newPart()
                        .setInput(new SimpleMappingPoint("test", "b")))
                .addPart(ConfigurationBuilder.newPart()
                        .setInput(new SimpleMappingPoint("test", "c")))
                .build();
        XssfMultiSheetInput fixture = new XssfMultiSheetInput(config);
        fixture.start();
        
        for (int sheet = 1; sheet < 4; sheet++) {
            for (int row = 1; row < 6; row++) {
                assertTrue(fixture.hasNext());
                fixture.read(mappingConfiguration);
                for (int cell = 1; cell < 4; cell++) {
                    assertEquals(1, mappingConfiguration.parts().get(cell - 1).getDataset().getContent().size());
                    assertEquals(String.format("%d%d%d",  cell, sheet, row), mappingConfiguration.parts().get(cell - 1).value().get());
                }
                mappingConfiguration.clear();
            }
        }
        assertFalse(fixture.hasNext());
        fixture.end();

    }

}