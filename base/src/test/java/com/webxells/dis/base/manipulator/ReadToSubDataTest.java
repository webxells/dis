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
import com.webxells.dis.api.config.MappingConfiguration;
import com.webxells.dis.api.error.InvalidDatasetException;
import com.webxells.dis.base.SimpleDatasetPiece;
import com.webxells.dis.base.SimpleMappingPortrayal;
import com.webxells.dis.base.config.SimpleMappingPoint;
import com.webxells.dis.test.cases.ConfigurationBuilder;
import com.webxells.dis.test.cases.SimpleTestCase;
import com.webxells.dis.test.example.input.TestInput;
import com.webxells.dis.test.example.input.TestInputConfig;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ReadToSubDataTest extends SimpleTestCase {

    @Test
    void test() throws InvalidDatasetException {
        MappingConfiguration configuration = newConfiguration()
                .addPart(ConfigurationBuilder.newPart(ConfigurationBuilder.PartBuilder.PartBuilderState.RANDOM)
                        .withContent()
                )
                .addPart(ConfigurationBuilder.newPart(ConfigurationBuilder.PartBuilder.PartBuilderState.RANDOM)
                        .withContent()
                        .addSubData(newConfiguration()
                                .addPart(ConfigurationBuilder.newPart()
                                        .setInput(new SimpleMappingPoint("test","one"))
                                )
                                .addPart(ConfigurationBuilder.newPart()
                                        .setInput(new SimpleMappingPoint("test","two"))
                                )
                                .addPart(ConfigurationBuilder.newPart(ConfigurationBuilder.PartBuilder.PartBuilderState.RANDOM))
                                .build())
                )
                .build();
        ReadToSubData fixture = new ReadToSubData();
        fixture.setInputConfig(new TestInputConfig("test"));
        fixture.setReadOnly(List.of(SimpleMappingPortrayal.source(configuration.parts().get(0))));
        LinkedList<Map<String, DatasetPiece>> data = new LinkedList<>(List.of(Map.of("one", new SimpleDatasetPiece(random()),
                "two", new SimpleDatasetPiece(random())),
        Map.of("one", new SimpleDatasetPiece(random()),
                "two", new SimpleDatasetPiece(random())),
        Map.of("one", new SimpleDatasetPiece(random()),
                "two", new SimpleDatasetPiece(random()))));
        TestInput.setData(data);
        fixture.manipulate(null, configuration.parts().get(1));

        LinkedList<MappingConfiguration> subData = new LinkedList<>(configuration.parts().get(1).getSubData());
        assertEquals(3, subData.size());
        while (!data.isEmpty()) {
            Map<String, DatasetPiece> currentData = data.pop();
            MappingConfiguration currentConfiguration = subData.pop();
            assertEquals(3, currentConfiguration.parts().size());
            assertEquals(currentData.get("one").value().get(), currentConfiguration.parts().get(0).value().get());
            assertEquals(currentData.get("two").value().get(), currentConfiguration.parts().get(1).value().get());
        }
    }

}