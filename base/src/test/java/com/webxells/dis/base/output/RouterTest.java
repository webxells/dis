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
package com.webxells.dis.base.output;

import com.webxells.dis.api.MappingPortrayal;
import com.webxells.dis.api.error.DisException;
import com.webxells.dis.api.resource.Resource;
import com.webxells.dis.api.validator.Validator;
import com.webxells.dis.base.SimpleDatasetPiece;
import com.webxells.dis.base.SimpleMappingPortrayal;
import com.webxells.dis.base.config.SimpleMappingConfiguration;
import com.webxells.dis.base.config.SimpleMappingPart;
import com.webxells.dis.base.config.SimpleMappingPoint;import com.webxells.dis.test.cases.SimpleTestCase;
import com.webxells.dis.test.cases.ConfigurationBuilder;
import com.webxells.dis.test.example.output.ResourceTestOutputConfig;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

class RouterTest extends SimpleTestCase {

    @Test
    void testStartAll() throws DisException, IOException {
        String name = random("name");
        Resource resource1 = mock(Resource.class);
        Resource resource2 = mock(Resource.class);
        Resource resource3 = mock(Resource.class);
        Resource elseResource = mock(Resource.class);
        OutputStream outputStream1 = mock(OutputStream.class);
        OutputStream outputStream2 = mock(OutputStream.class);
        OutputStream outputStream3 = mock(OutputStream.class);
        OutputStream elseOutputStream = mock(OutputStream.class);


        when(resource1.send())
                .thenReturn(outputStream1);
        when(resource2.send())
                .thenReturn(outputStream2);
        when(resource3.send())
                .thenReturn(outputStream3);
        when(elseResource.send())
                .thenReturn(elseOutputStream);

        final RouterConfig config = new RouterConfig();
        config.setChild(new ResourceTestOutputConfig());
        config.setName(name);
        config.setStartForAll(true);
        config.setNoneFound(elseResource);
        config.setEndPoints(List.of(
                new RouterConfig.RoutingEndPoint() {{
                    endPoint = resource1;
                }}, new RouterConfig.RoutingEndPoint() {{
                    endPoint = resource2;
                }}, new RouterConfig.RoutingEndPoint() {{
                    endPoint = resource3;
                }}
        ));
        Router fixture = new Router(config);
        fixture.start();
        fixture.end();

        verify(outputStream1, times(1)).flush();
        verify(outputStream2, times(1)).flush();
        verify(outputStream3, times(1)).flush();
        verify(elseOutputStream, times(1)).flush();
        verify(outputStream1, times(1)).close();
        verify(outputStream2, times(1)).close();
        verify(outputStream3, times(1)).close();
        verify(elseOutputStream, times(1)).close();
    }

    @Test
    void test() throws DisException, IOException {
        String name = random("name");
        Validator validator1 = mock(Validator.class);
        Validator validator2 = mock(Validator.class);
        Validator validator3 = mock(Validator.class);
        Resource resource1 = mock(Resource.class);
        Resource resource2 = mock(Resource.class);
        Resource resource3 = mock(Resource.class);
        Resource elseResource = mock(Resource.class);
        OutputStream outputStream1 = mock(OutputStream.class);
        OutputStream outputStream2 = mock(OutputStream.class);
        OutputStream outputStream3 = mock(OutputStream.class);
        OutputStream elseOutputStream = mock(OutputStream.class);
        when(validator1.validate(any(), any()))
                .thenReturn(false)
                .thenReturn(true)
                .thenReturn(true);
        when(resource1.send())
                .thenReturn(outputStream1);
        when(validator2.validate(any(), any()))
                .thenReturn(false)
                .thenReturn(false)
                .thenReturn(false);
        when(resource2.send())
                .thenReturn(outputStream2);
        when(validator3.validate(any(), any()))
                .thenReturn(false)
                .thenReturn(false)
                .thenReturn(true);
        when(resource3.send())
                .thenReturn(outputStream3);
        when(elseResource.send())
                .thenReturn(elseOutputStream);

        SimpleMappingConfiguration mappingConfiguration = new SimpleMappingConfiguration();
        mappingConfiguration.setParts(List.of(
                new SimpleMappingPart(mappingConfiguration, new SimpleMappingPoint(random(), random()),
                        new SimpleMappingPoint(name, random())) {{
                    getDataset().collect(new SimpleDatasetPiece(random("v1")));
                }}, new SimpleMappingPart(mappingConfiguration, new SimpleMappingPoint(random(), random()),
                        new SimpleMappingPoint(name, random())) {{
                    getDataset().collect(new SimpleDatasetPiece(random("v2")));
                }}, new SimpleMappingPart(mappingConfiguration, new SimpleMappingPoint(random(), random()),
                        new SimpleMappingPoint(random(), random())) {{
                    getDataset().collect(new SimpleDatasetPiece(random("some other")));
                }}
        ));
        MappingPortrayal portrayalForAll = new SimpleMappingPortrayal(
                mappingConfiguration.parts().get(1).getInput().getReference(),
                mappingConfiguration.parts().get(1).getInput().getPath());

        final RouterConfig config = new RouterConfig();
        config.setChild(new ResourceTestOutputConfig());
        config.setName(name);
        config.setNoneFound(elseResource);
        config.setEndPoints(List.of(
                new RouterConfig.RoutingEndPoint() {{
                    condition = validator1;
                    endPoint = resource1;
                    portrayal = portrayalForAll;
                }}, new RouterConfig.RoutingEndPoint() {{
                    condition = validator2;
                    endPoint = resource2;
                    portrayal = portrayalForAll;
                }}, new RouterConfig.RoutingEndPoint() {{
                    condition = validator3;
                    endPoint = resource3;
                    portrayal = portrayalForAll;
                }}
        ));
        Router fixture = new Router(config);
        fixture.start();
        fixture.write(mappingConfiguration);
        fixture.write(mappingConfiguration);
        fixture.write(mappingConfiguration);
        fixture.end();

        verify(outputStream1, times(2)).write(3);
        verify(outputStream3, times(1)).write(3);
        verify(elseOutputStream, times(1)).write(3);
        verify(outputStream1).close();
        verify(outputStream2).close();
        verify(outputStream3).close();
        verify(elseOutputStream).close();
        verifyNoMoreInteractions(outputStream1, outputStream2, outputStream3, elseOutputStream);
    }

}