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
package com.webxells.dis.rest.input.linker;

import com.webxells.dis.api.error.InputOutputError;
import com.webxells.dis.api.error.InvalidApi;
import com.webxells.dis.api.resource.Resource;
import com.webxells.dis.base.SimpleDatasetPiece;
import com.webxells.dis.base.config.SimpleMappingConfiguration;
import com.webxells.dis.base.config.SimpleMappingPart;
import com.webxells.dis.base.config.SimpleMappingPoint;
import com.webxells.dis.rest.Rest;
import com.webxells.dis.rest.content.AlteringContentStrategy;
import com.webxells.dis.test.cases.SimpleTestCase;
import com.webxells.dis.test.example.input.TestInput;
import com.webxells.dis.test.example.input.TestRestInputConfig;
import com.webxells.dis.test.example.resource.TestParentResource;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

class LazyCallTest extends SimpleTestCase {

    @Test
    void testWithNoReceiverShouldFail() {
        LazyCall fixture = new LazyCall();
        fixture.setInputConfig(new TestRestInputConfig());
        Assertions.assertThrows(InvalidApi.class, fixture::validate);
    }

    @Test
    void testNoContentStrategy() throws InputOutputError, InvalidApi {
        SimpleMappingConfiguration config = new SimpleMappingConfiguration();
        config.setParts(List.of(
                new SimpleMappingPart(config,
                        new SimpleMappingPoint("root", "test1"),
                        new SimpleMappingPoint("root", "test1"))
        ));
        LazyCall fixture = new LazyCall();
        TestRestInputConfig testRestInputConfig = new TestRestInputConfig();
        Rest resourceMock = mock(Rest.class);
        testRestInputConfig.setReceiver(resourceMock);
        fixture.setInputConfig(testRestInputConfig);
        TestInput.setData(List.of(Map.of()));
        fixture.validate();
        verifyNoMoreInteractions(resourceMock);
        assertEquals(0, fixture.getData(config));

    }

    @Test
    void testSimple() throws InputOutputError, InvalidApi {
        Rest resourceMock = mock(Rest.class);
        AlteringContentStrategy contentStrategyMock = mock(AlteringContentStrategy.class);
        when(resourceMock.getRequestContentStrategy()).thenReturn(List.of(contentStrategyMock));
        test(new LazyCall(), resourceMock, contentStrategyMock);
    }

    @Test
    void testWithLookForNestedRestResource() throws InputOutputError, InvalidApi {
        final LazyCall fixture = new LazyCall();
        fixture.setLookForNestedRestResource(true);
        TestParentResource parentResource = new TestParentResource();
        TestParentResource childResource = new TestParentResource();
        Rest resourceMock = mock(Rest.class);
        parentResource.setReceiver(childResource);
        childResource.setReceiver(resourceMock);
        AlteringContentStrategy contentStrategyMock = mock(AlteringContentStrategy.class);
        when(resourceMock.getRequestContentStrategy()).thenReturn(List.of(contentStrategyMock));
        test(fixture, parentResource, contentStrategyMock);
    }

    private void test(final LazyCall fixture, final Resource resource,
                      final AlteringContentStrategy contentStrategyMock) throws InvalidApi, InputOutputError {
        String rootKey1 = random("key1");
        String rootKey1value1 = random("key1value1");
        String inputKey = random("inputKey");
        String inputKeyValue = random("inputKeyValue");
        String linkKey = random("linkKey");
        String linkKeyValue = random("linkKeyValue");
        TestRestInputConfig testRestInputConfig = new TestRestInputConfig();
        testRestInputConfig.setName("link");
        testRestInputConfig.setReceiver(resource);
        SimpleMappingConfiguration config = new SimpleMappingConfiguration();
        config.setParts(List.of(
                new SimpleMappingPart(config,
                        new SimpleMappingPoint("root", rootKey1),
                        new SimpleMappingPoint("root", rootKey1)) {{
                    getDataset().collect(new SimpleDatasetPiece(rootKey1value1));
                }},
                new SimpleMappingPart(config,
                        new SimpleMappingPoint("root", linkKey),
                        new SimpleMappingPoint("root", linkKey)) {{
                    getDataset().collect(new SimpleDatasetPiece(linkKeyValue));
                }},
                new SimpleMappingPart(config,
                        new SimpleMappingPoint("link", inputKey),
                        new SimpleMappingPoint("link", inputKey))
        ));
        TestInput.setData(List.of(
                Map.of(inputKey, new SimpleDatasetPiece(inputKeyValue)),
                Map.of(inputKey, new SimpleDatasetPiece(random("not read")))));
        fixture.setInputConfig(testRestInputConfig);
        fixture.validate();
        fixture.start();
        assertEquals(1, fixture.getData(config));
        assertEquals(inputKeyValue, config.parts().get(2).getDataset().getContent().get(0).value().get());
        verify(contentStrategyMock).setContent(same(config));
        fixture.end();
    }


}