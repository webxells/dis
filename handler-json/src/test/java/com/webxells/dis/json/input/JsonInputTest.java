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
package com.webxells.dis.json.input;

import com.webxells.dis.api.error.DisException;
import com.webxells.dis.api.error.InputOutputError;
import com.webxells.dis.api.error.InvalidApi;
import com.webxells.dis.api.resource.Resource;
import com.webxells.dis.base.SimpleDatasetPiece;
import com.webxells.dis.base.config.SimpleMappingConfiguration;
import com.webxells.dis.base.config.SimpleMappingPoint;
import com.webxells.dis.base.config.StableMappingPart;
import com.webxells.dis.json.refinement.JsonType;
import com.webxells.dis.test.cases.ConfigurationBuilder;
import com.webxells.dis.test.cases.SimpleTestCase;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JsonInputTest extends SimpleTestCase {

    @Test
    void testJsonTypeArrayToMultipleDatasetPieces() throws DisException {
        SimpleMappingConfiguration mappingConfiguration = newConfiguration()
                .addPart(ConfigurationBuilder.newPart()
                        .setInput(new SimpleMappingPoint("test", "$.a"))
                        .withRefinements(new JsonType() {{
                            setJsonType(Type.ARRAY_TO_MULTIPLE_DATASET_PIECES);
                        }})
                )
                .build();

        JsonInputConfig inputConfig = new JsonInputConfig();
        inputConfig.setIterationPath("$");
        inputConfig.setSingleObject(true);
        inputConfig.setName("test");

        Resource retriever = Mockito.mock(Resource.class);
        Mockito.when(retriever.receive()).thenReturn(getFileInputStream("test-primitive-array.json"));
        inputConfig.setReceiver(retriever);

        JsonInput fixture = new JsonInput(inputConfig);
        fixture.start();
        assertTrue(fixture.hasNext());
        assertEquals(1, fixture.read(mappingConfiguration));
        assertEquals("1", mappingConfiguration.parts().get(0).getDataset().getContent().get(0).value().get());
        assertEquals("2", mappingConfiguration.parts().get(0).getDataset().getContent().get(1).value().get());
        assertEquals("3", mappingConfiguration.parts().get(0).getDataset().getContent().get(2).value().get());
        assertFalse(fixture.hasNext());
        fixture.end();
    }

    @Test
    void readSimpleRawJsonPart() throws DisException {
        SimpleMappingConfiguration mappingConfiguration = newConfiguration()
                .addPart(ConfigurationBuilder.newPart()
                        .setInput(new SimpleMappingPoint("test", "$.a"))
                )
                .build();

        JsonInputConfig inputConfig = new JsonInputConfig();
        inputConfig.setIterationPath("$");
        inputConfig.setReadTypes(Map.of("$.a", JsonInputConfig.ReadType.RAW));
        inputConfig.setSingleObject(true);
        inputConfig.setName("test");

        Resource retriever = Mockito.mock(Resource.class);
        Mockito.when(retriever.receive()).thenReturn(getFileInputStream("test-primitive-array.json"));
        inputConfig.setReceiver(retriever);

        JsonInput fixture = new JsonInput(inputConfig);
        fixture.start();
        assertTrue(fixture.hasNext());
        assertEquals(1, fixture.read(mappingConfiguration));
        assertEquals("[1,2,3]", mappingConfiguration.parts().get(0).value().get());
        assertFalse(fixture.hasNext());
        fixture.end();
    }

    @Test
    void readSimpleRawJsonPartBehindArrays() throws DisException {
        SimpleMappingConfiguration mappingConfiguration = newConfiguration()
                .addPart(ConfigurationBuilder.newPart()
                        .setInput(new SimpleMappingPoint("test", "$.d"))
                ).addPart(ConfigurationBuilder.newPart()
                        .setInput(new SimpleMappingPoint("test", "$.d"))
                )
                .build();

        JsonInputConfig inputConfig = new JsonInputConfig();
        inputConfig.setIterationPath("$.main");
        inputConfig.setReadTypes(Map.of("$.main[].d", JsonInputConfig.ReadType.RAW));
        inputConfig.setName("test");

        Resource retriever = Mockito.mock(Resource.class);
        Mockito.when(retriever.receive()).thenReturn(getFileInputStream("some-data.json"));
        inputConfig.setReceiver(retriever);

        JsonInput fixture = new JsonInput(inputConfig);
        fixture.start();
        assertTrue(fixture.hasNext());
        assertEquals(2, fixture.read(mappingConfiguration));
        assertEquals("[1,2,3,4]", mappingConfiguration.parts().get(0).value().get());
        assertEquals("[1,2,3,4]", mappingConfiguration.parts().get(1).value().get());
        fixture.end();
    }

    @Test

    void readComplexRawJsonPart() throws DisException {
        SimpleMappingConfiguration mappingConfiguration = newConfiguration()
                .addPart(ConfigurationBuilder.newPart()
                        .setInput(new SimpleMappingPoint("test", "$.a"))
                )
                .build();

        JsonInputConfig inputConfig = new JsonInputConfig();
        inputConfig.setIterationPath("$");
        inputConfig.setReadTypes(Map.of("$.a", JsonInputConfig.ReadType.RAW));
        inputConfig.setSingleObject(true);
        inputConfig.setName("test");

        Resource retriever = Mockito.mock(Resource.class);
        Mockito.when(retriever.receive()).thenReturn(getFileInputStream("test.json"));
        inputConfig.setReceiver(retriever);

        JsonInput fixture = new JsonInput(inputConfig);
        fixture.start();
        assertTrue(fixture.hasNext());
        assertEquals(1, fixture.read(mappingConfiguration));
        assertEquals("{\"b\":{\"c\":[{\"n1\":123,\"b1\":true,\"s1\":\"fd\\\\\\\"iväö\"},{\"n1\":456.234,\"b1\":false,\"s1\":\"wertz\",\"first\":\"only in second\"},{},{}],\"d\":[{\"f\":\"fosald\"},{},{\"a\":\"jknsff\"}]}}",
                mappingConfiguration.parts().get(0).value().get());
        assertFalse(fixture.hasNext());
        fixture.end();
    }

    @Test
    void readPrimitiveArray() throws DisException {
        SimpleMappingConfiguration mappingConfiguration = newConfiguration()
                .addPart(ConfigurationBuilder.newPart()
                        .setInput(new SimpleMappingPoint("test", "$.a"))
                        .addSubData(newConfiguration()
                                .addPart(ConfigurationBuilder.newPart()
                                        .setInput(new SimpleMappingPoint("test", "$"))
                                )
                                .build())
                )
                .build();

        final JsonInputConfig inputConfig = new JsonInputConfig();
        inputConfig.setIterationPath("$");
        inputConfig.setSingleObject(true);
        inputConfig.setName("test");
        Resource retriever = Mockito.mock(Resource.class);
        Mockito.when(retriever.receive()).thenReturn(getFileInputStream("test-primitive-array.json"));
        inputConfig.setReceiver(retriever);

        JsonInput fixture = new JsonInput(inputConfig);
        fixture.start();
        assertTrue(fixture.hasNext());
        assertEquals(3, fixture.read(mappingConfiguration));
        assertEquals("1", mappingConfiguration.parts().get(0).getSubData().get(0).parts().get(0).value().get());
        assertEquals("2", mappingConfiguration.parts().get(0).getSubData().get(1).parts().get(0).value().get());
        assertEquals("3", mappingConfiguration.parts().get(0).getSubData().get(2).parts().get(0).value().get());
        assertFalse(fixture.hasNext());
        fixture.end();
    }

    @Test
    void readRealLifeExampleStackOverflow() throws DisException {
        SimpleMappingConfiguration mappingConfiguration = newConfiguration()
                .addPart(ConfigurationBuilder.newPart()
                        .setInput(new SimpleMappingPoint("test", "$.next_url"))
                )
                .addPart(ConfigurationBuilder.newPart()
                        .setInput(new SimpleMappingPoint("test", "$.rows"))
                        .addSubData(newConfiguration()
                                .addPart(ConfigurationBuilder.newPart()
                                        .setInput(new SimpleMappingPoint("test", "$[0]"))
                                )
                                .addPart(ConfigurationBuilder.newPart()
                                        .setInput(new SimpleMappingPoint("test", "$[1]"))
                                )
                                .build())
                )
                .build();

        final JsonInputConfig inputConfig = new JsonInputConfig();
        inputConfig.setIterationPath("$");
        inputConfig.setName("test");
        inputConfig.setLenient(true);
        inputConfig.setSingleObject(true);
        Resource retriever = Mockito.mock(Resource.class);
        Mockito.when(retriever.receive()).thenReturn(getFileInputStream("real-life-example1-stackoverflow.json"));
        inputConfig.setReceiver(retriever);

        JsonInput fixture = new JsonInput(inputConfig);
        fixture.start();
        assertTrue(fixture.hasNext());
        assertEquals(2001, fixture.read(mappingConfiguration));
        assertEquals("https://db.offeneregister.de/openregister/Names.json?_size=max&_search=Spedition&_next=602179", mappingConfiguration.parts().get(0).value().get());
        assertFalse(fixture.hasNext());
        fixture.end();
    }

    @Test
    void readRootArrayInSubData() throws DisException {
        SimpleMappingConfiguration mappingConfiguration = newConfiguration()
                .addPart(ConfigurationBuilder.newPart()
                        .setInput(new SimpleMappingPoint("test", "$.a"))
                        .addSubData(newConfiguration()
                                .addPart(ConfigurationBuilder.newPart()
                                        .setInput(new SimpleMappingPoint("test", "$[1]"))
                                )
                                .addPart(ConfigurationBuilder.newPart()
                                        .setInput(new SimpleMappingPoint("test", "$[0]"))
                                )
                                .build())
                )
                .addPart(ConfigurationBuilder.newPart()
                        .setInput(new SimpleMappingPoint("test", "$.next"))
                )
                .addPart(ConfigurationBuilder.newPart()
                        .setInput(new SimpleMappingPoint("test", "$.next_url"))
                )
                .build();

        final JsonInputConfig inputConfig = new JsonInputConfig();
        inputConfig.setIterationPath("$");
        inputConfig.setName("test");
        inputConfig.setSingleObject(true);
        Resource retriever = Mockito.mock(Resource.class);
        Mockito.when(retriever.receive()).thenReturn(getFileInputStream("test-multiarray.json"));
        inputConfig.setReceiver(retriever);

        JsonInput fixture = new JsonInput(inputConfig);
        fixture.start();
        assertTrue(fixture.hasNext());
        assertEquals(6, fixture.read(mappingConfiguration));
        assertEquals("1", mappingConfiguration.parts().get(0).getSubData().get(0).parts().get(0).value().get());
        assertEquals("0", mappingConfiguration.parts().get(0).getSubData().get(0).parts().get(1).value().get());
        assertEquals("2", mappingConfiguration.parts().get(0).getSubData().get(1).parts().get(0).value().get());
        assertEquals("1", mappingConfiguration.parts().get(0).getSubData().get(1).parts().get(1).value().get());
        assertEquals("602179", mappingConfiguration.parts().get(1).value().get());
        assertEquals("https://db.offeneregister.de/openregister/Names.json?_size=max&_search=Spedition&_next=602179", mappingConfiguration.parts().get(2).value().get());
        assertFalse(fixture.hasNext());
        fixture.end();
    }

    @Test
    void readRootArray() throws DisException {
        SimpleMappingConfiguration mappingConfiguration = newConfiguration()
                .addPart(ConfigurationBuilder.newPart()
                        .setInput(new SimpleMappingPoint("test", "$[1]"))
                )
                .addPart(ConfigurationBuilder.newPart()
                        .setInput(new SimpleMappingPoint("test", "$[0]"))
                )
                .addPart(ConfigurationBuilder.newPart()
                        .setInput(new SimpleMappingPoint("test", "$[0]"))
                )
                .build();

        final JsonInputConfig inputConfig = new JsonInputConfig();
        inputConfig.setIterationPath("$.a");
        inputConfig.setName("test");
        Resource retriever = Mockito.mock(Resource.class);
        Mockito.when(retriever.receive()).thenReturn(getFileInputStream("test-multiarray.json"));
        inputConfig.setReceiver(retriever);

        JsonInput fixture = new JsonInput(inputConfig);
        fixture.start();
        assertTrue(fixture.hasNext());
        assertEquals(3, fixture.read(mappingConfiguration));
        assertEquals("1", mappingConfiguration.parts().get(0).value().get());
        assertEquals("0", mappingConfiguration.parts().get(1).value().get());
        assertEquals("0", mappingConfiguration.parts().get(2).value().get());
        assertTrue(fixture.hasNext());
        mappingConfiguration.clear();
        assertEquals(3, fixture.read(mappingConfiguration));
        assertEquals("2", mappingConfiguration.parts().get(0).value().get());
        assertEquals("1", mappingConfiguration.parts().get(1).value().get());
        assertEquals("1", mappingConfiguration.parts().get(2).value().get());
        assertFalse(fixture.hasNext());
        fixture.end();
    }

    @Test
    void readMultiArray() throws DisException {
        SimpleMappingConfiguration mappingConfiguration = newConfiguration()
                .addPart(ConfigurationBuilder.newPart()
                        .setInput(new SimpleMappingPoint("test", "$.a[0][1]"))
                )
                .addPart(ConfigurationBuilder.newPart()
                        .setInput(new SimpleMappingPoint("test", "$.a[1][0]"))
                )
                .build();

        final JsonInputConfig inputConfig = new JsonInputConfig();
        inputConfig.setIterationPath("$");
        inputConfig.setSingleObject(true);
        inputConfig.setName("test");
        Resource retriever = Mockito.mock(Resource.class);
        Mockito.when(retriever.receive()).thenReturn(getFileInputStream("test-multiarray.json"));
        inputConfig.setReceiver(retriever);

        JsonInput fixture = new JsonInput(inputConfig);
        fixture.start();
        assertTrue(fixture.hasNext());
        assertEquals(2, fixture.read(mappingConfiguration));
        assertEquals("1", mappingConfiguration.parts().get(0).value().get());
        assertEquals("1", mappingConfiguration.parts().get(1).value().get());
        assertFalse(fixture.hasNext());
        fixture.end();
    }

    @Test
    void readWithObjectPathButSomethingIsNull() throws DisException {
        SimpleMappingConfiguration mappingConfiguration = newConfiguration()
                .addPart(ConfigurationBuilder.newPart()
                        .setInput(new SimpleMappingPoint("test", "$.b.d"))
                )
                .build();

        final JsonInputConfig inputConfig = new JsonInputConfig();
        inputConfig.setIterationPath("$.a");
        Resource retriever = Mockito.mock(Resource.class);
        Mockito.when(retriever.receive()).thenReturn(getFileInputStream("test-broken-path.json"));
        inputConfig.setReceiver(retriever);
        inputConfig.setUnreachablePathStrategy(JsonInputConfig.UnreachablePathStrategy.IGNORE);
        inputConfig.setName("test");

        JsonInput fixture = new JsonInput(inputConfig);
        fixture.start();
        assertTrue(fixture.hasNext());
        assertEquals(0, fixture.read(mappingConfiguration));
        assertTrue(mappingConfiguration.parts().get(0).value().isEmpty());
        assertFalse(fixture.hasNext());
        fixture.end();
    }

    @Test
    void readMultiWithMoreThan10Values() throws DisException {
        SimpleMappingConfiguration sub = new SimpleMappingConfiguration();
        sub.setParts(List.of(
                new StableMappingPart(sub, new SimpleMappingPoint("ref", "$.value"),
                        new SimpleMappingPoint())
        ));
        SimpleMappingConfiguration mappingConfiguration = new SimpleMappingConfiguration();
        mappingConfiguration.setParts(List.of(
                new StableMappingPart(mappingConfiguration, new SimpleMappingPoint("ref", "$.b"),
                        new SimpleMappingPoint()) {{
                            addSubData(sub);
                }}
        ));

        final JsonInputConfig inputConfig = new JsonInputConfig();
        inputConfig.setIterationPath("$.a");
        Resource retriever = Mockito.mock(Resource.class);
        Mockito.when(retriever.receive()).thenReturn(getFileInputStream("test-more-than-10.json"));
        inputConfig.setReceiver(retriever);
        inputConfig.setSingleObject(true);
        inputConfig.setName("ref");

        JsonInput fixture = new JsonInput(inputConfig);
        fixture.start();
        assertTrue(fixture.hasNext());
        assertEquals(12, fixture.read(mappingConfiguration));
        for (int i = 0; i < 12; i++) {
            assertEquals(String.valueOf(i),
                    mappingConfiguration.parts().get(0).getSubData().get(i).parts().get(0).value().get());
        }

        assertFalse(fixture.hasNext());
        fixture.end();
    }

    @Test
    void readWithMultiNoValues() throws DisException {
        String reference = random("reference");
        SimpleMappingConfiguration sub2 = new SimpleMappingConfiguration();
        sub2.setParts(List.of(
                new StableMappingPart(sub2, new SimpleMappingPoint(reference, "$.end"),
                        new SimpleMappingPoint())
        ));
        SimpleMappingConfiguration sub1 = new SimpleMappingConfiguration();
        sub1.setParts(List.of(
                new StableMappingPart(sub1, new SimpleMappingPoint(reference, "$.multi"),
                        new SimpleMappingPoint()) {{
                    setSubData(List.of(sub2));
                }}
        ));
        SimpleMappingConfiguration mappingConfiguration = new SimpleMappingConfiguration();
        mappingConfiguration.setParts(List.of(
                new StableMappingPart(mappingConfiguration, new SimpleMappingPoint(reference, "$.multi"),
                        new SimpleMappingPoint()) {{
                    setSubData(List.of(sub1));
                }}
        ));

        final JsonInputConfig inputConfig = new JsonInputConfig();
        assertThrows(InvalidApi.class, inputConfig::validate);
        inputConfig.setIterationPath("$.a");
        inputConfig.setSingleObject(true);
        inputConfig.setUnreachablePathStrategy(JsonInputConfig.UnreachablePathStrategy.NULL);
        assertThrows(InvalidApi.class, inputConfig::validate);
        Resource retriever = Mockito.mock(Resource.class);
        Mockito.when(retriever.receive()).thenReturn(getFileInputStream("test-multi-no-values.json"));
        inputConfig.setReceiver(retriever);
        inputConfig.setName(reference);
        assertDoesNotThrow(inputConfig::validate);

        JsonInput fixture = new JsonInput(inputConfig);
        fixture.start();
        assertTrue(fixture.hasNext());
        assertEquals(4, fixture.read(mappingConfiguration));
        assertEquals("11",
                mappingConfiguration.parts().get(0).getSubData().get(0).parts().get(0).getSubData().get(0).parts().get(0).value().get());
        assertEquals("12",
                mappingConfiguration.parts().get(0).getSubData().get(0).parts().get(0).getSubData().get(1).parts().get(0).value().get());

        assertEquals(1, mappingConfiguration.parts().get(0).getSubData().get(1).parts().get(0).getSubData().size());
        assertTrue(mappingConfiguration.parts().get(0).getSubData().get(1).parts().get(0).getSubData().get(0).parts().get(0).value().isEmpty());

        assertEquals("31",
                mappingConfiguration.parts().get(0).getSubData().get(2).parts().get(0).getSubData().get(0).parts().get(0).value().get());
        assertEquals("32",
                mappingConfiguration.parts().get(0).getSubData().get(2).parts().get(0).getSubData().get(1).parts().get(0).value().get());

        assertFalse(fixture.hasNext());
        fixture.end();
    }

    @Test
    void readWithMultiValues() throws DisException {
        String reference = random("reference");
        SimpleMappingConfiguration sub2 = new SimpleMappingConfiguration();
        sub2.setParts(List.of(
                new StableMappingPart(sub2, new SimpleMappingPoint(reference, "$.end"),
                        new SimpleMappingPoint())
        ));
        SimpleMappingConfiguration sub1 = new SimpleMappingConfiguration();
        sub1.setParts(List.of(
                new StableMappingPart(sub1, new SimpleMappingPoint(reference, "$.n2"),
                        new SimpleMappingPoint()),
                new StableMappingPart(sub1, new SimpleMappingPoint(reference, "$.multi"),
                        new SimpleMappingPoint()) {{
                    setSubData(List.of(sub2));
                }}
        ));
        SimpleMappingConfiguration mappingConfiguration = new SimpleMappingConfiguration();
        mappingConfiguration.setParts(List.of(
                new StableMappingPart(mappingConfiguration, new SimpleMappingPoint(reference, "$.n1"),
                        new SimpleMappingPoint()),
                new StableMappingPart(mappingConfiguration, new SimpleMappingPoint(reference, "$.multi"),
                        new SimpleMappingPoint()) {{
                    setSubData(List.of(sub1));
                }}
        ));

        final JsonInputConfig inputConfig = new JsonInputConfig();
        assertThrows(InvalidApi.class, inputConfig::validate);
        inputConfig.setIterationPath("$.a");
        inputConfig.setSingleObject(true);
        inputConfig.setUnreachablePathStrategy(JsonInputConfig.UnreachablePathStrategy.FAIL);
        assertThrows(InvalidApi.class, inputConfig::validate);
        Resource retriever = Mockito.mock(Resource.class);
        Mockito.when(retriever.receive()).thenReturn(getFileInputStream("test-multi.json"));
        inputConfig.setReceiver(retriever);
        inputConfig.setName(reference);
        assertDoesNotThrow(inputConfig::validate);

        JsonInput fixture = new JsonInput(inputConfig);
        fixture.start();
        assertTrue(fixture.hasNext());
        assertEquals(13, fixture.read(mappingConfiguration));
        assertEquals("single", mappingConfiguration.parts().get(0).value().get());
        assertEquals("1single-sub",
                mappingConfiguration.parts().get(1).getSubData().get(0).parts().get(0).value().get());
        assertEquals("11",
                mappingConfiguration.parts().get(1).getSubData().get(0).parts().get(1).getSubData().get(0).parts().get(0).value().get());
        assertEquals("12",
                mappingConfiguration.parts().get(1).getSubData().get(0).parts().get(1).getSubData().get(1).parts().get(0).value().get());
        assertEquals("13",
                mappingConfiguration.parts().get(1).getSubData().get(0).parts().get(1).getSubData().get(2).parts().get(0).value().get());

        assertEquals("2single-sub",
                mappingConfiguration.parts().get(1).getSubData().get(1).parts().get(0).value().get());
        assertEquals("21",
                mappingConfiguration.parts().get(1).getSubData().get(1).parts().get(1).getSubData().get(0).parts().get(0).value().get());
        assertEquals("22",
                mappingConfiguration.parts().get(1).getSubData().get(1).parts().get(1).getSubData().get(1).parts().get(0).value().get());
        assertEquals("23",
                mappingConfiguration.parts().get(1).getSubData().get(1).parts().get(1).getSubData().get(2).parts().get(0).value().get());

        assertEquals("3single-sub",
                mappingConfiguration.parts().get(1).getSubData().get(2).parts().get(0).value().get());
        assertEquals("31",
                mappingConfiguration.parts().get(1).getSubData().get(2).parts().get(1).getSubData().get(0).parts().get(0).value().get());
        assertEquals("32",
                mappingConfiguration.parts().get(1).getSubData().get(2).parts().get(1).getSubData().get(1).parts().get(0).value().get());
        assertEquals("33",
                mappingConfiguration.parts().get(1).getSubData().get(2).parts().get(1).getSubData().get(2).parts().get(0).value().get());


        assertFalse(fixture.hasNext());
        fixture.end();
    }

    @Test
    void readFromSingleObjectIterationPath() throws DisException {
        String reference = random("reference");
        String value1 = random("value1");
        String value2 = random("value2");
        SimpleMappingConfiguration mappingConfiguration = new SimpleMappingConfiguration();
        mappingConfiguration.setParts(List.of(
                new StableMappingPart(mappingConfiguration, new SimpleMappingPoint(reference, "$.n1"),
                        new SimpleMappingPoint()) {{
                            getDataset().collect(new SimpleDatasetPiece(value1));
                }},
                new StableMappingPart(mappingConfiguration, new SimpleMappingPoint(reference, "$.b.c"),
                        new SimpleMappingPoint()){{
                    getDataset().collect(new SimpleDatasetPiece(value2));
                }}
        ));

        final JsonInputConfig inputConfig = new JsonInputConfig();
        assertThrows(InvalidApi.class, inputConfig::validate);
        inputConfig.setIterationPath("$.a");
        inputConfig.setSingleObject(true);
        Resource retriever = Mockito.mock(Resource.class);
        Mockito.when(retriever.receive()).thenReturn(new ByteArrayInputStream(String.format("{\"a\":{\"n1\":\"%s\"," +
                "\"b\":{\"c\":\"%s\"}}}", value1, value2).getBytes()));
        inputConfig.setReceiver(retriever);
        inputConfig.setName(reference);

        JsonInput fixture = new JsonInput(inputConfig);
        fixture.start();
        assertTrue(fixture.hasNext());
        assertEquals(2, fixture.read(mappingConfiguration));
        assertEquals(value1, mappingConfiguration.parts().get(0).value().get());
        assertEquals(value2, mappingConfiguration.parts().get(1).value().get());
        assertFalse(fixture.hasNext());
        fixture.end();
    }

    @Test
    void testWithEmptyArrayAsIterationPath() throws DisException {
        String reference = random("reference");
        SimpleMappingConfiguration mappingConfiguration = new SimpleMappingConfiguration();

        final JsonInputConfig inputConfig = new JsonInputConfig();
        inputConfig.setIterationPath("$");
        Resource receiver = Mockito.mock(Resource.class);
        Mockito.when(receiver.receive()).thenReturn(new ByteArrayInputStream("[ ]".getBytes()));
        inputConfig.setReceiver(receiver);
        inputConfig.setName(reference);
        JsonInput fixture = new JsonInput(inputConfig);
        fixture.start();
        assertFalse(fixture.hasNext());
        assertEquals(0, fixture.read(mappingConfiguration));
        fixture.end();
    }

    @Test
    void failCausePointToNoScalarValue() throws DisException {
        fail("{\"a\":[{\"n1\":{}}]}", "$.a", false);
    }

    @Test
    void failCauseIterationPathIsNoArray() throws DisException {
        fail("{\"a\":{}}", "$.a", true);
    }

    @Test
    void failCauseIterationPathIsNoSimpleJsonPath() throws DisException {
        fail("{\"a\":[]}", ".a", true);
    }

    @Test
    void test() throws DisException {
        String reference = random("reference");
        SimpleMappingConfiguration mappingConfiguration = new SimpleMappingConfiguration();
        mappingConfiguration.setParts(List.of(
                new StableMappingPart(mappingConfiguration, new SimpleMappingPoint(reference, "$.n1"),
                        new SimpleMappingPoint()),
                new StableMappingPart(mappingConfiguration, new SimpleMappingPoint(reference, "$.b1"),
                        new SimpleMappingPoint()),
                new StableMappingPart(mappingConfiguration, new SimpleMappingPoint(reference, "$.s1"),
                        new SimpleMappingPoint()),
                new StableMappingPart(mappingConfiguration, new SimpleMappingPoint(reference, "$.first"),
                        new SimpleMappingPoint()),
                new StableMappingPart(mappingConfiguration, new SimpleMappingPoint("something else", "$.b1"),
                        new SimpleMappingPoint())
        ));

        final JsonInputConfig inputConfig = new JsonInputConfig();
        assertThrows(InvalidApi.class, inputConfig::validate);
        inputConfig.setIterationPath("$.a.b.c");
        inputConfig.setUnreachablePathStrategy(JsonInputConfig.UnreachablePathStrategy.EMPTY);
        assertThrows(InvalidApi.class, inputConfig::validate);
        Resource retriever = Mockito.mock(Resource.class);
        Mockito.when(retriever.receive()).thenReturn(getFileInputStream("test.json"));
        inputConfig.setReceiver(retriever);
        inputConfig.setName(reference);
        assertDoesNotThrow(inputConfig::validate);

        JsonInput fixture = new JsonInput(inputConfig);
        fixture.start();
        assertTrue(fixture.hasNext());
        assertEquals(3, fixture.read(mappingConfiguration));
        assertEquals("123", mappingConfiguration.parts().get(0).value().get());
        assertEquals("true", mappingConfiguration.parts().get(1).value().get());
        assertEquals("fd\\\\\"iväö", mappingConfiguration.parts().get(2).value().get());
        assertEquals("", mappingConfiguration.parts().get(3).value().get());
        assertTrue(mappingConfiguration.parts().get(4).value().isEmpty());
        mappingConfiguration.clear();
        assertTrue(fixture.hasNext());
        assertEquals(4, fixture.read(mappingConfiguration));
        assertEquals("456.234", mappingConfiguration.parts().get(0).value().get());
        assertEquals("false", mappingConfiguration.parts().get(1).value().get());
        assertEquals("wertz", mappingConfiguration.parts().get(2).value().get());
        assertEquals("only in second", mappingConfiguration.parts().get(3).value().get());
        assertTrue(mappingConfiguration.parts().get(4).value().isEmpty());
        mappingConfiguration.clear();
        assertTrue(fixture.hasNext());
        inputConfig.setUnreachablePathStrategy(JsonInputConfig.UnreachablePathStrategy.NULL);
        assertEquals(0, fixture.read(mappingConfiguration));
        assertTrue(mappingConfiguration.parts().get(0).value().isEmpty());
        assertTrue(mappingConfiguration.parts().get(1).value().isEmpty());
        assertTrue(mappingConfiguration.parts().get(2).value().isEmpty());
        assertTrue(mappingConfiguration.parts().get(3).value().isEmpty());
        assertTrue(mappingConfiguration.parts().get(4).value().isEmpty());
        assertTrue(fixture.hasNext());
        inputConfig.setUnreachablePathStrategy(JsonInputConfig.UnreachablePathStrategy.FAIL);
        assertThrows(InputOutputError.class, () -> fixture.read(mappingConfiguration));
        assertFalse(fixture.hasNext());
        fixture.end();
    }

    private void fail(String insert, String iterationPath, boolean bootFail) throws DisException {
        InputStream input = new ByteArrayInputStream(insert.getBytes());
        SimpleMappingConfiguration mappingConfiguration = new SimpleMappingConfiguration();
        mappingConfiguration.setParts(List.of(
                new StableMappingPart(mappingConfiguration, new SimpleMappingPoint("test", "$.n1"),
                        new SimpleMappingPoint())
        ));

        final JsonInputConfig inputConfig = new JsonInputConfig();
        assertThrows(InvalidApi.class, inputConfig::validate);
        inputConfig.setIterationPath(iterationPath);
        inputConfig.setUnreachablePathStrategy(JsonInputConfig.UnreachablePathStrategy.EMPTY);
        assertThrows(InvalidApi.class, inputConfig::validate);
        Resource retriever = Mockito.mock(Resource.class);
        Mockito.when(retriever.receive()).thenReturn(input);
        inputConfig.setReceiver(retriever);
        inputConfig.setName("test");
        assertDoesNotThrow(inputConfig::validate);

        JsonInput fixture = new JsonInput(inputConfig);
        if (bootFail) {
            assertThrows(Exception.class, fixture::start);
            assertFalse(fixture.hasNext());
        } else {
            fixture.start();
            assertTrue(fixture.hasNext());
            assertThrows(InputOutputError.class, () -> fixture.read(mappingConfiguration));
        }
    }

    private InputStream getFileInputStream(final String file) {
        return Thread.currentThread().getContextClassLoader().getResourceAsStream(file);
    }


}