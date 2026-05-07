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
package com.webxells.dis.plain.input;

import com.webxells.dis.api.error.InputOutputError;
import com.webxells.dis.api.resource.Resource;
import com.webxells.dis.base.config.SimpleMappingConfiguration;
import com.webxells.dis.base.config.StableMappingPart;
import com.webxells.dis.base.config.SimpleMappingPoint;
import com.webxells.dis.test.cases.SimpleTestCase;
import java.io.ByteArrayInputStream;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ToStringTest extends SimpleTestCase {

    @Test
    void test() throws InputOutputError {
        String name = random("name");
        String content = random("some content");
        ByteArrayInputStream inputStream = new ByteArrayInputStream(content.getBytes());
        Resource resource = Mockito.mock(Resource.class);
        Mockito.when(resource.receive()).thenReturn(inputStream);
        SimpleMappingConfiguration mapping = new SimpleMappingConfiguration();
        mapping.setParts(List.of(
                new StableMappingPart(mapping, new SimpleMappingPoint(name, random()),
                        new SimpleMappingPoint(random(), random())),
                new StableMappingPart(mapping, new SimpleMappingPoint(random(), random()),
                        new SimpleMappingPoint(random(), random())),
                new StableMappingPart(mapping, new SimpleMappingPoint(name, random()),
                        new SimpleMappingPoint(random(), random()))
        ));
        ToStringConfiguration config = new ToStringConfiguration();
        config.setName(name);
        config.setReceiver(resource);
        ToString fixture = new ToString(config);
        assertFalse(fixture.hasNext());
        fixture.start();
        assertTrue(fixture.hasNext());
        assertEquals(2, fixture.read(mapping));
        assertEquals(content, mapping.parts().get(0).value().get());
        assertTrue(mapping.parts().get(1).value().isEmpty());
        assertEquals(content, mapping.parts().get(2).value().get());
        assertFalse(fixture.hasNext());
        fixture.end();
        fixture.start();
        assertTrue(fixture.hasNext());
    }

}
