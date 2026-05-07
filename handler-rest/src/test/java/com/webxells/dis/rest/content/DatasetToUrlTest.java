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
import com.webxells.dis.rest.execution.Request;
import com.webxells.dis.plain.output.EchoConfig;
import com.webxells.dis.test.cases.SimpleTestCase;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;


class DatasetToUrlTest extends SimpleTestCase {

    @Test
    void testWithPortrayals() {
        String value1 = random("value1");
        String value2 = random("value2");
        String reference1 = random();
        String reference2 = random();
        DatasetToUrl fixture = new DatasetToUrl();
        fixture.setMappingFilter(List.of(
                new SimpleMappingPortrayal() {{
                    setSource(Source.OUTPUT);
                    setReference(reference1);
                    setPath("key1");
                }},
                new SimpleMappingPortrayal() {{
                    setSource(Source.OUTPUT);
                    setReference(reference2);
                    setPath("key2");
                }}
        ));
        SimpleMappingConfiguration mappingConfig = new SimpleMappingConfiguration();
        mappingConfig.setParts(List.of(
                new SimpleMappingPart(mappingConfig, new SimpleMappingPoint(random(), random()),
                        new SimpleMappingPoint(reference1, "key1")) {{
                    getDataset().collect(new SimpleDatasetPiece(value1));
                }}, new SimpleMappingPart(mappingConfig, new SimpleMappingPoint(random(), random()),
                        new SimpleMappingPoint(reference2, "key2")) {{
                    getDataset().collect(new SimpleDatasetPiece(value2));
                }}
        ));
        fixture.setContent(mappingConfig);
        fixture.setOutputConfig(new EchoConfig() {{
            setTemplate("http://test.de?key1=$key1&key2=$key2");
        }});
        Request.Builder builder = Mockito.mock(Request.Builder.class);
        fixture.parseInputRequest(null, builder, null);
        Mockito.verify(builder).uri(ArgumentMatchers.eq(URI.create(String.format("http://test.de?key1=%s&key2=%s",
                URLEncoder.encode(value1, StandardCharsets.UTF_8), URLEncoder.encode(value2, StandardCharsets.UTF_8)))));
    }

    @Test
    void test() {
        DatasetToUrl fixture = new DatasetToUrl();
        fixture.setSkipReferenceVerification(true);
        String value1 = random("value1");
        String value2 = random("value2");
        SimpleMappingConfiguration mappingConfig = new SimpleMappingConfiguration();
        mappingConfig.setParts(List.of(
                new SimpleMappingPart(mappingConfig, new SimpleMappingPoint(random(), random()),
                        new SimpleMappingPoint(random(), "key1")) {{
                    getDataset().collect(new SimpleDatasetPiece(value1));
                }}, new SimpleMappingPart(mappingConfig, new SimpleMappingPoint(random(), random()),
                        new SimpleMappingPoint(random(), "key2")) {{
                    getDataset().collect(new SimpleDatasetPiece(value2));
                }}
        ));
        fixture.setContent(mappingConfig);
        fixture.setOutputConfig(new EchoConfig() {{
            setTemplate("http://test.de?key1=$key1&key2=$key2");
        }});
        Request.Builder builder = Mockito.mock(Request.Builder.class);
        fixture.parseInputRequest(null, builder, null);
        Mockito.verify(builder).uri(ArgumentMatchers.eq(URI.create(String.format("http://test.de?key1=%s&key2=%s",
                URLEncoder.encode(value1, StandardCharsets.UTF_8), URLEncoder.encode(value2, StandardCharsets.UTF_8)))));
    }

}