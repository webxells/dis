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
package com.webxells.dis.base.input.linker;

import com.webxells.dis.api.config.MappingConfiguration;
import com.webxells.dis.api.error.InputOutputError;
import com.webxells.dis.base.SimpleDatasetPiece;
import com.webxells.dis.base.config.SimpleMappingPoint;
import com.webxells.dis.test.cases.ConfigurationBuilder;
import com.webxells.dis.test.cases.SimpleTestCase;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SaveLastTest extends SimpleTestCase {

    @Test
    void test() throws InputOutputError {
        String value11 = random();
        String value12 = random();
        String value121 = random();
        String value21 = random();
        String value22 = random();
        String value221 = random();
        String value31 = random();
        String value32 = random();
        String value321 = random();
        MappingConfiguration configuration = newConfiguration()
                .addPart(ConfigurationBuilder.newPart()
                        .setInput(new SimpleMappingPoint("test", "first"))
                        .withContent(value11)
                )
                .addPart(ConfigurationBuilder.newPart()
                        .setInput(new SimpleMappingPoint("moep", "second"))
                        .withContent(value12)
                        .addSubData(newConfiguration()
                                .addPart(new ConfigurationBuilder.PartBuilder()
                                    .setInput(new SimpleMappingPoint("moep", "second-sub"))
                                    .withContent(value121))
                                .build())
                )
                .addPart(ConfigurationBuilder.newPart()
                        .setInput(new SimpleMappingPoint("last-1:test", "first"))
                )
                .addPart(ConfigurationBuilder.newPart()
                        .setInput(new SimpleMappingPoint("last-1:moep", "second"))
                )
                .addPart(ConfigurationBuilder.newPart()
                        .setInput(new SimpleMappingPoint("last-2:test", "first"))
                )
                .addPart(ConfigurationBuilder.newPart()
                        .setInput(new SimpleMappingPoint("last-2:moep", "second"))
                )
                .build();
        SaveLast fixture = new SaveLast();
        fixture.setAmount(2);

        assertEquals(0, fixture.getData(configuration));
        for (int i = 2; i < 6; i++) {
            assertTrue(configuration.parts().get(i).getDataset().getContent().isEmpty());
        }

        configuration.clear();
        configuration.parts().get(0).getDataset().collect(new SimpleDatasetPiece(value21));
        configuration.parts().get(1).getDataset().collect(new SimpleDatasetPiece(value22));
        configuration.parts().get(1).getSubData().get(0).parts().get(0).getDataset().collect(new SimpleDatasetPiece(value221));
        assertEquals(2 ,fixture.getData(configuration));
        for (int i = 4; i < 6; i++) {
            assertTrue(configuration.parts().get(i).getDataset().getContent().isEmpty());
        }
        assertEquals(value11, configuration.parts().get(2).value().get());
        assertEquals(value12, configuration.parts().get(3).value().get());
        assertEquals(value121, configuration.parts().get(3).getSubData().get(0).parts().get(0).value().get());

        configuration.clear();
        configuration.parts().get(0).getDataset().collect(new SimpleDatasetPiece(value31));
        configuration.parts().get(1).getDataset().collect(new SimpleDatasetPiece(value32));
        configuration.parts().get(1).getSubData().get(0).parts().get(0).getDataset().collect(new SimpleDatasetPiece(value321));

        assertEquals(4, fixture.getData(configuration));
        assertEquals(value21, configuration.parts().get(2).value().get());
        assertEquals(value22, configuration.parts().get(3).value().get());
        assertEquals(value221, configuration.parts().get(3).getSubData().get(0).parts().get(0).value().get());
        assertEquals(value11, configuration.parts().get(4).value().get());
        assertEquals(value12, configuration.parts().get(5).value().get());
        assertEquals(value121, configuration.parts().get(5).getSubData().get(0).parts().get(0).value().get());

        configuration.clear();
        configuration.parts().get(0).getDataset().collect(new SimpleDatasetPiece(value31));
        configuration.parts().get(1).getDataset().collect(new SimpleDatasetPiece(value32));
        configuration.parts().get(1).getSubData().get(0).parts().get(0).getDataset().collect(new SimpleDatasetPiece(value321));

        assertEquals(4, fixture.getData(configuration));
        assertEquals(value31, configuration.parts().get(2).value().get());
        assertEquals(value32, configuration.parts().get(3).value().get());
        assertEquals(value321, configuration.parts().get(3).getSubData().get(0).parts().get(0).value().get());
        assertEquals(value21, configuration.parts().get(4).value().get());
        assertEquals(value22, configuration.parts().get(5).value().get());
        assertEquals(value221, configuration.parts().get(5).getSubData().get(0).parts().get(0).value().get());


    }

}