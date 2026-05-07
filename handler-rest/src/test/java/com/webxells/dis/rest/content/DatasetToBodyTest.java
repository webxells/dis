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

import com.webxells.dis.base.SimpleDatasetPiece;
import com.webxells.dis.base.SimpleMappingPortrayal;
import com.webxells.dis.base.config.SimpleMappingConfiguration;
import com.webxells.dis.base.config.SimpleMappingPart;
import com.webxells.dis.base.config.SimpleMappingPoint;
import com.webxells.dis.rest.execution.HttpMethod;
import com.webxells.dis.rest.execution.Request;
import com.webxells.dis.plain.output.EchoConfig;
import com.webxells.dis.test.cases.SimpleTestCase;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;


class DatasetToBodyTest extends SimpleTestCase {

    @Test
    void multiThreadTest() {
        IntStream.range(0,25)
                .parallel()
                .forEach(a -> test());
    }


    @Test
    void testAlias() {
        DatasetToBody fixture = new DatasetToBody();
        String name = random("name");
        String key1 = random();
        String key2 = random();
        String value1 = random("value1");
        String value2 = random("value2");
        String alias = random();
        SimpleMappingConfiguration mapping = new SimpleMappingConfiguration();
        mapping.setParts(List.of(
                new SimpleMappingPart(mapping,
                        new SimpleMappingPoint(random("other"), key1),
                        new SimpleMappingPoint(name, key1)) {{
                    getDataset().collect(new SimpleDatasetPiece(value1));
                }}, new SimpleMappingPart(mapping,
                        new SimpleMappingPoint(random("other"), key2),
                        new SimpleMappingPoint(name, key2)) {{
                    getDataset().collect(new SimpleDatasetPiece(value2));
                }}
        ));
        Request.Builder builder = Mockito.mock(Request.Builder.class);
        EchoConfig echoConfig = new EchoConfig();
        echoConfig.setTemplate(String.format("asd$%s ir$%s", alias, key2));
        fixture.setContent(mapping);
        fixture.setAliasFilter(Map.of(alias, new SimpleMappingPortrayal(){{
            setSource(Source.OUTPUT);
            setPath(key1);
            setReference(name);
        }}));
        fixture.setMappingFilter(List.of(new SimpleMappingPortrayal(){{
            setSource(Source.OUTPUT);
            setPath(key2);
            setReference(name);
        }}));
        fixture.setOutputConfig(echoConfig);
        fixture.parseInputRequest(HttpMethod.POST, builder, null);
        AtomicBoolean runAssertions = new AtomicBoolean();
        Mockito.verify(builder).setBodyPublisher(Mockito.argThat(a -> {
            try {
                assertEquals(String.format("asd%s ir%s", value1, value2),
                        new String(new String(a.get().readAllBytes())));
                runAssertions.set(true);
            } catch (IOException e) {
                fail();
            }
            return true;
        }));
        Assertions.assertTrue(runAssertions.get());
        Mockito.verifyNoMoreInteractions(builder);
    }

    @Test
    void test() {
        DatasetToBody fixture = new DatasetToBody();
        String name = random("name");
        String key1 = random();
        String value1 = random("value1");
        String key2 = random();
        String value2 = random("value2");
        SimpleMappingConfiguration mapping = new SimpleMappingConfiguration();
        mapping.setParts(List.of(
                new SimpleMappingPart(mapping,
                        new SimpleMappingPoint(random("other"), key1),
                        new SimpleMappingPoint(name, key1)) {{
                    getDataset().collect(new SimpleDatasetPiece(value1));
                }}, new SimpleMappingPart(mapping,
                        new SimpleMappingPoint(random("other"), key2),
                        new SimpleMappingPoint(random("other"), key2)) {{
                    getDataset().collect(new SimpleDatasetPiece("not my value"));
                }}, new SimpleMappingPart(mapping,
                        new SimpleMappingPoint(random("other"), key2),
                        new SimpleMappingPoint(name, key2)) {{
                    getDataset().collect(new SimpleDatasetPiece(value2));
                }}
        ));
        Request.Builder builder = Mockito.mock(Request.Builder.class);
        EchoConfig echoConfig = new EchoConfig();
        echoConfig.setTemplate(String.format("asd$%s ir\\$%s jd$%s jd$", key1, key1, key2));
        fixture.setContent(mapping);
        fixture.setReferenceMask(name);
        fixture.setOutputConfig(echoConfig);
        fixture.parseInputRequest(HttpMethod.POST, builder, null);
        AtomicBoolean runAssertions = new AtomicBoolean();
        Mockito.verify(builder).setBodyPublisher(Mockito.argThat(a ->  {
            try {
                assertEquals(String.format("asd%s ir$%s jd%s jd$", value1, key1, value2),
                        new String(new String(a.get().readAllBytes())));
                runAssertions.set(true);
            } catch (IOException e) {
                fail();
            }
            return true;
        }));
        Assertions.assertTrue(runAssertions.get());
        Mockito.verifyNoMoreInteractions(builder);
    }

}