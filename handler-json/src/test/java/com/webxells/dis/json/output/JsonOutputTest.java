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
package com.webxells.dis.json.output;

import com.webxells.dis.api.config.MappingConfiguration;
import com.webxells.dis.api.error.DisException;
import com.webxells.dis.api.error.InvalidApi;
import com.webxells.dis.api.resource.Resource;
import com.webxells.dis.base.SimpleDatasetPiece;
import com.webxells.dis.base.config.SimpleMappingConfiguration;
import com.webxells.dis.base.config.SimpleMappingPoint;
import com.webxells.dis.base.config.StableMappingPart;
import com.webxells.dis.json.JsonType;
import com.webxells.dis.json.refinement.Nullable;
import com.webxells.dis.test.cases.ConfigurationBuilder;
import com.webxells.dis.test.cases.SimpleTestCase;
import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JsonOutputTest extends SimpleTestCase {

    @Test
    void testWriteFromRoot() throws DisException {
        String random1 = randomUnique();
        String random2 = randomUnique();
        String random3 = randomUnique();

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Resource sender = Mockito.mock(Resource.class);
        Mockito.when(sender.send()).thenReturn(outputStream);

        MappingConfiguration configuration = newConfiguration()
                .addPart(ConfigurationBuilder.newPart()
                        .setOutput(new SimpleMappingPoint("test", "$.sub"))
                        .addSubData(newConfiguration()
                                .addPart(ConfigurationBuilder.newPart()
                                        .setOutput(new SimpleMappingPoint("test", "$"))
                                        .withContent(random1)
                                ).build())
                        .addSubData(newConfiguration()
                                .addPart(ConfigurationBuilder.newPart()
                                        .setOutput(new SimpleMappingPoint("test", "$"))
                                        .withContent(random2)
                                ).build())
                        .addSubData(newConfiguration()
                                .addPart(ConfigurationBuilder.newPart()
                                        .setOutput(new SimpleMappingPoint("test", "$"))
                                        .withContent(random3)
                                ).build())
                )
                .addPart(ConfigurationBuilder.newPart()
                        .setOutput(new SimpleMappingPoint("test", "$.multiValue"))
                        .withContent(random1, random2, random3)
                ).build();
        JsonOutputConfig outputConfig = new JsonOutputConfig();
        assertThrows(InvalidApi.class, outputConfig::validate);
        outputConfig.setName("test");
        outputConfig.setSingleObject(true);
        outputConfig.setIterationPath("$");
        outputConfig.setTypeMapping(Map.of("$.multiValue", JsonType.LIST));
        outputConfig.validate();
        outputConfig.setSender(sender);
        JsonOutput fixture = new JsonOutput(outputConfig);
        fixture.start();
        fixture.write(configuration);
        fixture.end();
        assertEquals(String.format("{\"multiValue\":[\"%s\",\"%s\",\"%s\"],\"sub\":[\"%1$s\",\"%2$s\",\"%3$s\"]}",
                        random1, random2, random3), outputStream.toString());
    }

    @Test
    void testWithNullableRefinement() throws DisException {
        SimpleMappingConfiguration mappingConfiguration = newConfiguration()
                .addPart(ConfigurationBuilder.newPart(ConfigurationBuilder.PartBuilder.PartBuilderState.RANDOM)
                        .setOutput(new SimpleMappingPoint("test", "$.null1"))
                        .withNullContent()
                        .withRefinements(new Nullable())
                )
                .addPart(ConfigurationBuilder.newPart(ConfigurationBuilder.PartBuilder.PartBuilderState.RANDOM)
                        .setOutput(new SimpleMappingPoint("test", "$.null2"))
                        .withNullContent()
                )
                .build();

        testAssertResult(mappingConfiguration, "{\"null1\":null}");
    }

    @Test
    void testArrayOfMultipleMappingParts() throws DisException {
        SimpleMappingConfiguration mappingConfiguration = newConfiguration()
                .addPart(ConfigurationBuilder.newPart(ConfigurationBuilder.PartBuilder.PartBuilderState.RANDOM)
                        .setOutput(new SimpleMappingPoint("test", "$.array[0]"))
                        .withContent()
                )
                .addPart(ConfigurationBuilder.newPart(ConfigurationBuilder.PartBuilder.PartBuilderState.RANDOM)
                        .setOutput(new SimpleMappingPoint("test", "$.array[2]"))
                        .withContent()
                )
                .addPart(ConfigurationBuilder.newPart(ConfigurationBuilder.PartBuilder.PartBuilderState.RANDOM)
                        .setOutput(new SimpleMappingPoint("test", "$.lala"))
                        .withContent()
                )
                .addPart(ConfigurationBuilder.newPart(ConfigurationBuilder.PartBuilder.PartBuilderState.RANDOM)
                        .setOutput(new SimpleMappingPoint("test", "$.array[]"))
                        .withContent()
                )
                .build();

        testAssertResult(mappingConfiguration, String.format("{\"array\":[\"%s\",\"%s\",\"%s\"],\"lala\":\"%s\"}",
                mappingConfiguration.parts().get(0).value().get(),
                mappingConfiguration.parts().get(1).value().get(),
                mappingConfiguration.parts().get(3).value().get(),
                mappingConfiguration.parts().get(2).value().get()));
    }

    private void testAssertResult(final SimpleMappingConfiguration mappingConfiguration, final String result) throws DisException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Resource sender = Mockito.mock(Resource.class);
        Mockito.when(sender.send()).thenReturn(outputStream);

        JsonOutputConfig outputConfig = new JsonOutputConfig();
        outputConfig.setSingleObject(true);
        outputConfig.setName("test");
        outputConfig.setIterationPath("$");
        outputConfig.setSender(sender);
        JsonOutput fixture = new JsonOutput(outputConfig);
        fixture.start();
        fixture.write(mappingConfiguration);
        fixture.end();
        assertEquals(result, outputStream.toString());
    }

    @Test
    void testSingleObjectWithListAtEnd() throws DisException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Resource sender = Mockito.mock(Resource.class);
        Mockito.when(sender.send()).thenReturn(outputStream);

        String reference = random("output");
        SimpleMappingConfiguration subConfig1 = new SimpleMappingConfiguration();
        subConfig1.setParts(List.of(
                new StableMappingPart(subConfig1, new SimpleMappingPoint(reference, "$.e"),
                        new SimpleMappingPoint(reference, "$.e")) {{
                    getDataset().collect(new SimpleDatasetPiece("123"));
                }}
        ));
        SimpleMappingConfiguration subConfig2 = new SimpleMappingConfiguration();
        subConfig2.setParts(List.of(
                new StableMappingPart(subConfig2, new SimpleMappingPoint(reference, "$.e"),
                        new SimpleMappingPoint(reference, "$.e")) {{
                    getDataset().collect(new SimpleDatasetPiece("456"));
                }}
        ));
        SimpleMappingConfiguration mappingConfig = new SimpleMappingConfiguration();
        mappingConfig.setParts(List.of(
                new StableMappingPart(mappingConfig,
                        new SimpleMappingPoint(), new SimpleMappingPoint(reference, "$.a")) {{
                            setSubData(List.of(subConfig1, subConfig2));
                            getDataset().collect(new SimpleDatasetPiece(random()));
                }}
        ));
        JsonOutputConfig outputConfig = new JsonOutputConfig();
        assertThrows(InvalidApi.class, outputConfig::validate);
        outputConfig.setName(reference);
        outputConfig.setSingleObject(true);
        outputConfig.setIterationPath("$");
        outputConfig.validate();
        outputConfig.setSender(sender);
        JsonOutput fixture = new JsonOutput(outputConfig);
        fixture.start();
        fixture.write(mappingConfig);
        fixture.end();
        assertEquals(String.format("{\"a\":[{\"e\":\"123\"},{\"e\":\"456\"},\"%s\"]}",
                        mappingConfig.parts().get(0).value().get()),
                outputStream.toString());
    }


    @Test
    void testRootIterationPathAsSingleObjectWithoutStaticMapping() throws DisException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Resource sender = Mockito.mock(Resource.class);
        Mockito.when(sender.send()).thenReturn(outputStream);

        String reference = random("output");
        SimpleMappingConfiguration mappingConfig = new SimpleMappingConfiguration();
        mappingConfig.setParts(List.of(
                new StableMappingPart(mappingConfig,
                        new SimpleMappingPoint(), new SimpleMappingPoint(reference, "$.a")),
                new StableMappingPart(mappingConfig,
                        new SimpleMappingPoint(), new SimpleMappingPoint(reference, "$.b.c"))
        ));
        JsonOutputConfig outputConfig = new JsonOutputConfig();
        assertThrows(InvalidApi.class, outputConfig::validate);
        outputConfig.setName(reference);
        outputConfig.setIterationPath("$");
        outputConfig.setSingleObject(true);
        outputConfig.validate();
        outputConfig.setSender(sender);
        JsonOutput fixture = new JsonOutput(outputConfig);
        fixture.start();
        mappingConfig.parts().get(0).getDataset().collect(new SimpleDatasetPiece("123"));
        mappingConfig.parts().get(1).getDataset().collect(new SimpleDatasetPiece("true"));
        fixture.write(mappingConfig);
        assertEquals("{\"a\":\"123\",\"b\":{\"c\":\"true\"}}", outputStream.toString());
        mappingConfig.clear();
        mappingConfig.parts().get(0).getDataset().collect(new SimpleDatasetPiece("456.234"));
        mappingConfig.parts().get(1).getDataset().collect(new SimpleDatasetPiece("false"));
        fixture.write(mappingConfig);
        fixture.end();
        assertEquals("{\"a\":\"456.234\",\"b\":{\"c\":\"false\"}}", outputStream.toString().substring(28));
    }

    @Test
    void testSimple() throws DisException{
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Resource sender = Mockito.mock(Resource.class);
        Mockito.when(sender.send()).thenReturn(outputStream);

        SimpleMappingConfiguration mappingConfiguration = newConfiguration()
                .addPart(ConfigurationBuilder.newPart()
                        .setOutput(new SimpleMappingPoint("test", "$.simple"))
                        .withContent("simple")
                )
                .addPart(ConfigurationBuilder.newPart()
                        .setOutput(new SimpleMappingPoint("test", "$.simpleList"))
                        .withContent("simple", "list")
                )
                .addPart(ConfigurationBuilder.newPart()
                        .setOutput(new SimpleMappingPoint("test", "$.subDataSimple"))
                        .addSubData(newConfiguration()
                                .addPart(ConfigurationBuilder.newPart()
                                        .setOutput(new SimpleMappingPoint("test", "sub-simple"))
                                        .withContent("subData.sub1")
                                )
                                .addPart(ConfigurationBuilder.newPart()
                                        .setOutput(new SimpleMappingPoint("test", "sub-deep"))
                                        .withContent("simple in object array")
                                        .addSubData(newConfiguration()
                                                .addPart(ConfigurationBuilder.newPart()
                                                        .setOutput(new SimpleMappingPoint("test", "deep"))
                                                        .withContent("subData.sub-deep.deep")
                                                )
                                                .build())
                                        .addSubData(newConfiguration()
                                                .addPart(ConfigurationBuilder.newPart()
                                                        .setOutput(new SimpleMappingPoint("test", "deep"))
                                                        .withContent("subData.sub-deep.deep2")
                                                )
                                                .build())
                                )
                                .build())
                        .addSubData(newConfiguration()
                                .addPart(ConfigurationBuilder.newPart()
                                        .setOutput(new SimpleMappingPoint("test", "sub-simple"))
                                        .withContent("subData.sub2")
                                )
                                .addPart(ConfigurationBuilder.newPart()
                                        .setOutput(new SimpleMappingPoint("test", "sub-deep"))
                                )
                                .build())
                )
                .build();
        JsonOutputConfig outputConfig = new JsonOutputConfig();
        outputConfig.setNoValueStrategy(JsonOutputConfig.NoValueStrategy.WRITE_NULL);
        assertThrows(InvalidApi.class, outputConfig::validate);
        outputConfig.setName("test");
        outputConfig.setIterationPath("$");
        outputConfig.validate();
        outputConfig.setLenient(true);
        outputConfig.setSender(sender);
        outputConfig.setStaticMapping(Map.of(
                "$.simple-static", "simple-static",
                "$.some-array[2].bool", "0"
        ));
        outputConfig.setTypeMapping(Map.of(
                "$.simpleList", JsonType.LIST,
                "$.some-array[2].bool", JsonType.BOOLEAN
        ));
        JsonOutput fixture = new JsonOutput(outputConfig);
        fixture.start();
        fixture.write(mappingConfiguration);
        fixture.end();
        assertEquals("[{" +
                        "\"simple\":\"simple\"," +
                        "\"simple-static\":\"simple-static\"," +
                        "\"simpleList\":[" +
                            "\"simple\"," +
                            "\"list\"" +
                        "]," +
                        "\"some-array\":[" +
                            "null," +
                            "null," +
                            "{" +
                                "\"bool\":false" +
                            "}" +
                        "]," +
                        "\"subDataSimple\":[" +
                            "{" +
                                "\"sub-deep\":[" +
                                    "{" +
                                        "\"deep\":\"subData.sub-deep.deep\"" +
                                    "}," +
                                    "{" +
                                        "\"deep\":\"subData.sub-deep.deep2\"" +
                                    "}," +
                                    "\"simple in object array\"" +
                                "]," +
                                "\"sub-simple\":\"subData.sub1\"" +
                            "}," +
                            "{" +
                                "\"sub-deep\":null," +
                                "\"sub-simple\":\"subData.sub2\"" +
                            "}" +
                        "]" +
                "}]",
                outputStream.toString());

    }

    @Test
    void testRootIterationPathAsSingleObject() throws DisException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Resource sender = Mockito.mock(Resource.class);
        Mockito.when(sender.send()).thenReturn(outputStream);

        String reference = random("output");
        SimpleMappingConfiguration mappingConfig = new SimpleMappingConfiguration();
        mappingConfig.setParts(List.of(
                new StableMappingPart(mappingConfig,
                        new SimpleMappingPoint(), new SimpleMappingPoint(reference, "$.a")),
                new StableMappingPart(mappingConfig,
                        new SimpleMappingPoint(), new SimpleMappingPoint(reference, "$.b.c")),
                new StableMappingPart(mappingConfig,
                        new SimpleMappingPoint(), new SimpleMappingPoint(reference, "$.rawContent"))
        ));
        JsonOutputConfig outputConfig = new JsonOutputConfig();
        assertThrows(InvalidApi.class, outputConfig::validate);
        outputConfig.setName(reference);
        outputConfig.setIterationPath("$");
        outputConfig.setSingleObject(true);
        outputConfig.validate();
        outputConfig.setSender(sender);
        outputConfig.setStaticMapping(Map.of(
                "$.simple-string", "porower\nnewline",
                "$.g[2].a", "0"
        ));
        outputConfig.setTypeMapping(Map.of(
                "$.simple-string", JsonType.LIST,
                "$.rawContent", JsonType.RAW
        ));
        JsonOutput fixture = new JsonOutput(outputConfig);
        fixture.start();
        mappingConfig.parts().get(0).getDataset().collect(new SimpleDatasetPiece("123"));
        mappingConfig.parts().get(1).getDataset().collect(new SimpleDatasetPiece("true"));
        mappingConfig.parts().get(2).getDataset().collect(new SimpleDatasetPiece("[1,2,3,{\"test\":\"lalala\"}]]"));
        fixture.write(mappingConfig);
        assertEquals(
                "{\"a\":\"123\",\"b\":{\"c\":\"true\"},\"g\":[null,null,{\"a\":\"0\"}],\"rawContent\":[1,2,3,{\"test\":\"lalala\"}]]," +
                        "\"simple-string\":[\"porower\\nnewline\"]}" ,
                outputStream.toString());

        mappingConfig.clear();
        mappingConfig.parts().get(0).getDataset().collect(new SimpleDatasetPiece("456.234"));
        mappingConfig.parts().get(1).getDataset().collect(new SimpleDatasetPiece("false"));
        fixture.write(mappingConfig);
        fixture.end();
        assertEquals(
                "{\"a\":\"456.234\",\"b\":{\"c\":\"false\"},\"g\":[null,null,{\"a\":\"0\"}]," +
                        "\"simple-string\":[\"porower\\nnewline\"]}" ,
                outputStream.toString().substring(131));
    }

    @Test
    void testWithRootIterationPathWithStaticValues() throws DisException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Resource sender = Mockito.mock(Resource.class);
        Mockito.when(sender.send()).thenReturn(outputStream);

        String reference = random("output");
        SimpleMappingConfiguration mappingConfig = new SimpleMappingConfiguration();
        mappingConfig.setParts(List.of(
                new StableMappingPart(mappingConfig,
                        new SimpleMappingPoint(), new SimpleMappingPoint(reference, "$.a")),
                new StableMappingPart(mappingConfig,
                        new SimpleMappingPoint(), new SimpleMappingPoint(reference, "$.b.c"))
        ));
        JsonOutputConfig outputConfig = new JsonOutputConfig();
        outputConfig.setNoValueStrategy(JsonOutputConfig.NoValueStrategy.WRITE_NULL);
        assertThrows(InvalidApi.class, outputConfig::validate);
        outputConfig.setName(reference);
        outputConfig.setIterationPath("$");
        outputConfig.validate();
        outputConfig.setStaticMapping(Map.of(
                "$.simple-string", "porower"
        ));
        outputConfig.setSender(sender);
        JsonOutput fixture = new JsonOutput(outputConfig);
        fixture.start();
        mappingConfig.parts().get(0).getDataset().collect(new SimpleDatasetPiece("123"));
        mappingConfig.parts().get(1).getDataset().collect(new SimpleDatasetPiece("true"));
        fixture.write(mappingConfig);
        mappingConfig.clear();
        mappingConfig.parts().get(0).getDataset().collect(new SimpleDatasetPiece("456.234"));
        mappingConfig.parts().get(1).getDataset().collect(new SimpleDatasetPiece("false"));
        fixture.write(mappingConfig);
        mappingConfig.clear();
        fixture.write(mappingConfig);
        fixture.end();
        assertEquals(
                "[{\"a\":\"123\",\"b\":{\"c\":\"true\"},\"simple-string\":\"porower\"}," +
                        "{\"a\":\"456.234\",\"b\":{\"c\":\"false\"},\"simple-string\":\"porower\"}," +
                        "{\"a\":null,\"b\":{\"c\":null},\"simple-string\":\"porower\"}]",
                outputStream.toString());
    }

    @Test
    void testRootIterationPath() throws DisException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Resource sender = Mockito.mock(Resource.class);
        Mockito.when(sender.send()).thenReturn(outputStream);

        String reference = random("output");
        SimpleMappingConfiguration mappingConfig = new SimpleMappingConfiguration();
        mappingConfig.setParts(List.of(
                new StableMappingPart(mappingConfig,
                        new SimpleMappingPoint(), new SimpleMappingPoint(reference, "$.a")),
                new StableMappingPart(mappingConfig,
                        new SimpleMappingPoint(), new SimpleMappingPoint(reference, "$.b.c"))
        ));
        JsonOutputConfig outputConfig = new JsonOutputConfig();
        assertThrows(InvalidApi.class, outputConfig::validate);
        outputConfig.setName(reference);
        outputConfig.setIterationPath("$");
        outputConfig.validate();
        outputConfig.setNoValueStrategy(JsonOutputConfig.NoValueStrategy.IGNORE);
        outputConfig.setSender(sender);
        JsonOutput fixture = new JsonOutput(outputConfig);
        fixture.start();
        mappingConfig.parts().get(0).getDataset().collect(new SimpleDatasetPiece("123"));
        mappingConfig.parts().get(1).getDataset().collect(new SimpleDatasetPiece("true"));
        fixture.write(mappingConfig);
        mappingConfig.clear();
        mappingConfig.parts().get(0).getDataset().collect(new SimpleDatasetPiece("456.234"));
        mappingConfig.parts().get(1).getDataset().collect(new SimpleDatasetPiece("false"));
        fixture.write(mappingConfig);
        mappingConfig.clear();
        fixture.write(mappingConfig);
        fixture.end();
        assertEquals(
                "[{\"a\":\"123\",\"b\":{\"c\":\"true\"}},{\"a\":\"456.234\",\"b\":{\"c\":\"false\"}},{}]" ,
                outputStream.toString());
    }

    @Test
    void testWithMultiPath() throws DisException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Resource sender = Mockito.mock(Resource.class);
        Mockito.when(sender.send()).thenReturn(outputStream);

        String reference = random("output");
        SimpleMappingConfiguration mappingConfig = newConfiguration()
                .addPart(ConfigurationBuilder.newPart()
                        .setOutput(new SimpleMappingPoint("not me", "$.n"))
                        .withContent("n")
                )
                .addPart(ConfigurationBuilder.newPart()
                        .setOutput(new SimpleMappingPoint(reference, "$.n"))
                        .withContent("n")
                )
                .addPart(ConfigurationBuilder.newPart()
                        .setOutput(new SimpleMappingPoint(reference, "$.deep"))
                        .addSubData(newConfiguration()
                                .addPart(ConfigurationBuilder.newPart()
                                        .setOutput(new SimpleMappingPoint(reference, "$.nn"))
                                        .withContent("deep.nn")
                                ).addPart(ConfigurationBuilder.newPart()
                                        .setOutput(new SimpleMappingPoint(reference, "$.deep"))
                                        .addSubData(newConfiguration()
                                                .addPart(ConfigurationBuilder.newPart()
                                                        .setOutput(new SimpleMappingPoint(reference, "$.deep"))
                                                        .withContent("deep.deep.deep")
                                                )
                                                .addPart(ConfigurationBuilder.newPart()
                                                        .setOutput(new SimpleMappingPoint("not me", "$.n"))
                                                        .withContent("wrong")
                                                )
                                                .build())
                                )
                                .build())
                        .addSubData(newConfiguration()
                                .addPart(ConfigurationBuilder.newPart()
                                        .setOutput(new SimpleMappingPoint(reference, "$.nn"))
                                        .withContent("deep.nn2")
                                ).addPart(ConfigurationBuilder.newPart()
                                        .setOutput(new SimpleMappingPoint(reference, "$.deep"))
                                        .addSubData(newConfiguration()
                                                .addPart(ConfigurationBuilder.newPart()
                                                        .setOutput(new SimpleMappingPoint(reference, "$.deep"))
                                                        .withContent("deep.deep.deep2")
                                                )
                                                .addPart(ConfigurationBuilder.newPart()
                                                        .setOutput(new SimpleMappingPoint("not me", "$.n"))
                                                        .withContent("wrong")
                                                )
                                                .build())
                                )
                                .build())
                )
                .build();
        JsonOutputConfig outputConfig = new JsonOutputConfig();
        assertThrows(InvalidApi.class, outputConfig::validate);
        outputConfig.setName(reference);
        outputConfig.setIterationPath("$.a");
        outputConfig.validate();
        outputConfig.setSubDataToReferenceRestricted(true);
        outputConfig.setTypeMapping(Map.of(
                "$.deep[].deep.true", JsonType.BOOLEAN,
                "$.deep[].deep", JsonType.OBJECT
        ));
        outputConfig.setStaticMapping(Map.of(
                "$.deep[].deep.true", "1"
        ));
        outputConfig.setSender(sender);
        JsonOutput fixture = new JsonOutput(outputConfig);
        fixture.start();
        fixture.write(mappingConfig);
        fixture.end();
        assertEquals(
                String.format("{\"a\":[{\"deep\":[{\"deep\":{\"deep\":\"%s\"},\"nn\":\"%s\"}," +
                        "{\"deep\":{\"deep\":\"%s\"},\"nn\":\"%s\"},{\"deep\":{\"true\":true}}],\"n\":\"%s\"}]}",
                        mappingConfig.parts().get(2).getSubData().get(0).parts().get(1).getSubData().get(0).parts().get(0).value().get(),
                        mappingConfig.parts().get(2).getSubData().get(0).parts().get(0).value().get(),
                        mappingConfig.parts().get(2).getSubData().get(1).parts().get(1).getSubData().get(0).parts().get(0).value().get(),
                        mappingConfig.parts().get(2).getSubData().get(1).parts().get(0).value().get(),
                        mappingConfig.parts().get(0).value().get()),
                outputStream.toString());
    }

    @Test
    void test() throws DisException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Resource sender = Mockito.mock(Resource.class);
        Mockito.when(sender.send()).thenReturn(outputStream);

        String reference = random("output");
        SimpleMappingConfiguration mappingConfig = newConfiguration()
                .addPart(ConfigurationBuilder.newPart()
                        .setOutput(new SimpleMappingPoint(reference, "$.n1"))
                )
                .addPart(ConfigurationBuilder.newPart()
                        .setOutput(new SimpleMappingPoint(reference, "$.deep.b1"))
                )
                .addPart(ConfigurationBuilder.newPart()
                        .setOutput(new SimpleMappingPoint(reference, "$.deep.s1"))
                )
                .addPart(ConfigurationBuilder.newPart()
                        .setOutput(new SimpleMappingPoint(reference, "$.first"))
                )
                .addPart(ConfigurationBuilder.newPart()
                        .setOutput(new SimpleMappingPoint("moep", "$.first"))
                        .withContent("first")
                )
                .build();
        JsonOutputConfig outputConfig = new JsonOutputConfig();
        outputConfig.setNoValueStrategy(JsonOutputConfig.NoValueStrategy.WRITE_NULL);
        assertThrows(InvalidApi.class, outputConfig::validate);
        outputConfig.setName(reference);
        outputConfig.setIterationPath("$.a.b.c");
        outputConfig.validate();
        outputConfig.setStaticMapping(Map.of(
                "$.simple-string", "porower",
                "$.e.d[0].f", "fosald",
                "$.e.d[1].a", "0"
        ));
        outputConfig.setTypeMapping(Map.of(
                "$.n1", JsonType.DECIMAL,
                "$.deep.b1", JsonType.BOOLEAN,
                "$.e.d[1].a", JsonType.BOOLEAN
        ));
        outputConfig.setSender(sender);
        JsonOutput fixture = new JsonOutput(outputConfig);
        fixture.start();
        mappingConfig.parts().get(0).getDataset().collect(new SimpleDatasetPiece("123"));
        mappingConfig.parts().get(1).getDataset().collect(new SimpleDatasetPiece("true"));
        mappingConfig.parts().get(2).getDataset().collect(new SimpleDatasetPiece("fduväö"));
        fixture.write(mappingConfig);
        mappingConfig.clear();
        mappingConfig.parts().get(0).getDataset().collect(new SimpleDatasetPiece("456.234"));
        mappingConfig.parts().get(1).getDataset().collect(new SimpleDatasetPiece("false"));
        mappingConfig.parts().get(2).getDataset().collect(new SimpleDatasetPiece("wertz"));
        mappingConfig.parts().get(3).getDataset().collect(new SimpleDatasetPiece("only in second"));
        fixture.write(mappingConfig);
        mappingConfig.clear();
        fixture.write(mappingConfig);
        fixture.end();
        assertEquals(
                "{\"a\":{\"b\":{\"c\":[" +
                            "{\"deep\":{\"b1\":true,\"s1\":\"fduväö\"},\"e\":{\"d\":[{\"f\":\"fosald\"},{\"a\":false}]},\"first\":null,\"n1\":123,\"simple-string\":\"porower\"}," +
                            "{\"deep\":{\"b1\":false,\"s1\":\"wertz\"},\"e\":{\"d\":[{\"f\":\"fosald\"},{\"a\":false}]},\"first\":\"only in second\",\"n1\":456.234,\"simple-string\":\"porower\"}," +
                            "{\"deep\":{\"b1\":false,\"s1\":null},\"e\":{\"d\":[{\"f\":\"fosald\"},{\"a\":false}]},\"first\":null,\"n1\":null,\"simple-string\":\"porower\"}" +
                        "]}}}" ,
                outputStream.toString());
    }

}