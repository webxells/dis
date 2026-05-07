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
package com.webxells.dis.rest.content;

import com.webxells.dis.api.config.MappingConfiguration;
import com.webxells.dis.base.SimpleMappingPortrayal;
import com.webxells.dis.base.config.SimpleMappingPoint;
import com.webxells.dis.rest.execution.Headers;
import com.webxells.dis.rest.execution.Request;
import com.webxells.dis.rest.execution.Response;
import com.webxells.dis.test.cases.ConfigurationBuilder;
import com.webxells.dis.test.cases.SimpleTestCase;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class HeaderTest extends SimpleTestCase {
    @Test
    void testOutput() throws IOException {
        String value = random();
        String name = random();

        Headers headers = Mockito.mock(Headers.class);
        Response response = Mockito.mock(Response.class);
        when(response.getHeaders()).thenReturn(headers);
        when(headers.get(name)).thenReturn(List.of(value));

        Header fixture = new Header();
        fixture.setName(name);

        try (InputStream result = fixture.parseOutputRequest(response)) {
            assertEquals(value, new String(result.readAllBytes()));
        }
    }

    @Test
    void testInput() throws IOException {
        String ref = random();
        String path = random();
        String value = random();
        String name = random();
        MappingConfiguration mappingConfiguration = newConfiguration()
                .addPart(ConfigurationBuilder.newPart()
                        .setInput(new SimpleMappingPoint(ref, path))
                        .withContent(value)
                )
                .build();

        Request.Builder builder = Mockito.mock(Request.Builder.class);

        Header fixture = new Header();
        fixture.setName(name);
        fixture.setValue(new SimpleMappingPortrayal(ref, path));
        fixture.setContent(mappingConfiguration);

        fixture.parseInputRequest(null, builder, null);

        verify(builder).header(name, value);

    }

}