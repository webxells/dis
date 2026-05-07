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
package com.webxells.dis.json.config;

import com.webxells.dis.api.MappingPortrayal;
import com.webxells.dis.api.error.DisException;
import com.webxells.dis.api.error.InvalidApi;
import com.webxells.dis.api.resource.Resource;
import com.webxells.dis.base.SimpleMappingPortrayal;
import com.webxells.dis.base.config.SimpleMappingConfiguration;
import com.webxells.dis.base.config.SimpleMappingPoint;
import com.webxells.dis.json.config.map.SubDataPerEntry;
import com.webxells.dis.json.input.JsonInput;
import com.webxells.dis.json.input.JsonInputConfig;
import com.webxells.dis.test.cases.ConfigurationBuilder;
import com.webxells.dis.test.cases.SimpleTestCase;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;

class SubDataPerEntryTest extends SimpleTestCase {

    @Test
    void testSubDataPerEntryInput() throws DisException {
        SimpleMappingConfiguration configuration = newConfiguration()
                .addPart(ConfigurationBuilder.newPart()
                        .setInput(new SimpleMappingPoint("test", "simple-key"))
                )
                .build();
        SubDataPerEntry subDataPerEntryPart = new SubDataPerEntry(configuration, new SimpleMappingPoint("test", "map1"),
                new SimpleMappingPoint());
        subDataPerEntryPart.addSubData(newConfiguration()
                .addPart(ConfigurationBuilder.newPart()
                        .setInput(new SimpleMappingPoint("test", "title"))
                )
                .addPart(ConfigurationBuilder.newPart()
                        .setInput(new SimpleMappingPoint("test", "value"))
                )
                .build());

        MappingPortrayal titlePortrayal = new SimpleMappingPortrayal("test", "title");
        MappingPortrayal valuePortrayal = new SimpleMappingPortrayal("test", "value");
        assertThrows(InvalidApi.class, subDataPerEntryPart::validate);
        subDataPerEntryPart.setPortrayalForTitle(titlePortrayal);
        subDataPerEntryPart.setPortrayalForValue(valuePortrayal);
        subDataPerEntryPart.validate();
        configuration.addPart(subDataPerEntryPart);

        JsonInputConfig inputConfig = new JsonInputConfig();
        inputConfig.setIterationPath("$");
        inputConfig.setLenient(true);
        inputConfig.setSingleObject(true);
        Resource receiver = Mockito.mock(Resource.class);
        Mockito.when(receiver.receive()).thenReturn(getResourceFileStream("mapfile.json"));
        inputConfig.setReceiver(receiver);
        inputConfig.setName("test");
        JsonInput fixture = new JsonInput(inputConfig);
        fixture.start();
        /*assertEquals(1, */fixture.read(configuration);//);
        fixture.end();

        assertEquals("simple-value", configuration.parts().get(0).value().get());
        assertFalse(configuration.parts().get(1).value().isPresent());
        assertEquals(3, configuration.parts().get(1).getSubData().size());
        assertEquals(2, configuration.parts().get(1).getSubData().get(0).parts().size());
        assertEquals("map-title1", configuration.parts().get(1).getSubData().get(0).getByPortrayal(titlePortrayal).get().value().get());
        assertEquals("map-value1", configuration.parts().get(1).getSubData().get(0).getByPortrayal(valuePortrayal).get().value().get());
        assertEquals(2, configuration.parts().get(1).getSubData().get(1).parts().size());
        assertEquals("map-title2", configuration.parts().get(1).getSubData().get(1).getByPortrayal(titlePortrayal).get().value().get());
        assertEquals("map-value2", configuration.parts().get(1).getSubData().get(1).getByPortrayal(valuePortrayal).get().value().get());
        assertEquals(2, configuration.parts().get(1).getSubData().get(2).parts().size());
        assertEquals("map-title3", configuration.parts().get(1).getSubData().get(2).getByPortrayal(titlePortrayal).get().value().get());
        assertEquals("map-value3", configuration.parts().get(1).getSubData().get(2).getByPortrayal(valuePortrayal).get().value().get());
    }

}