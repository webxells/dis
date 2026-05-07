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
package com.webxells.dis.config.json;

import com.google.gson.JsonSyntaxException;
import com.webxells.dis.api.MappingOperation;
import com.webxells.dis.api.MappingPortrayal;
import com.webxells.dis.api.config.DisConfig;
import com.webxells.dis.api.config.InputConfig;
import com.webxells.dis.api.config.JobConfig;
import com.webxells.dis.api.config.MappingConfiguration;
import com.webxells.dis.api.config.MappingPart;
import com.webxells.dis.api.config.MappingPoint;
import com.webxells.dis.api.config.OutputConfig;
import com.webxells.dis.api.config.TriggerConfig;
import com.webxells.dis.api.validator.Validator;
import com.webxells.dis.base.SimpleMappingPortrayal;
import com.webxells.dis.base.config.SimpleMappingPoint;
import com.webxells.dis.base.config.StableMappingPart;
import com.webxells.dis.boot.ConfigurationMapping;
import com.webxells.dis.boot.KnownTypeMapping;
import com.webxells.dis.config.json.intern.SourceMapping;
import com.webxells.dis.test.cases.SimpleTestCase;
import com.webxells.dis.test.example.input.NestedTestClass;
import com.webxells.dis.test.example.input.NestedTestClassA;
import com.webxells.dis.test.example.input.NestedTestClassB;
import com.webxells.dis.test.example.input.TestInputConfig;
import com.webxells.dis.test.example.mapping.TestMappingPart;
import com.webxells.dis.test.example.mapping.TestMappingPoint;
import com.webxells.dis.test.example.output.TestOutputConfig;
import com.webxells.dis.test.example.trigger.TestTriggerConfig;
import com.webxells.dis.test.example.validator.TestValidator;
import com.webxells.dis.test.example.workflow.TestWorkflow;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import test.defaultinterface.Default;
import test.defaultinterface.DefaultB;
import test.defaultinterface.DefaultParent;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

class JsonConfigTest extends SimpleTestCase {

    @BeforeAll
    static void setUp() {
        System.setProperty("dison-source-mapping-key", "test");
        KnownTypeMapping.add(MappingPortrayal.class, SimpleMappingPortrayal.class.getName());
        KnownTypeMapping.add(MappingPoint.class, SimpleMappingPoint.class.getName());
        KnownTypeMapping.add(MappingPart.class, StableMappingPart.class.getName());
    }

    @BeforeEach
    void resetTypeMapping() {
        KnownTypeMapping.force(Default.class, null);
    }

    @Test
    void testConfigurationMapping() {
        String location1 = random();
        String location2 = random();
        JsonConfig fixture = new JsonConfig(String.format("""
                {
                    "system": {
                     "type": "com.webxells.dis.test.example.workflow.TestSupplier"
                    },
                    "configurations": [
                     {
                       "name": "config-1",
                       "type": "com.webxells.dis.base.config.SimpleJobConfig",
                       "trigger": [],
                       "input": {
                         "name": "input-1",
                         "type": "com.webxells.dis.test.example.input.TestInput",
                         "%s": "%s"
                       },
                       "output": [],
                       "mapping": {
                         "parts": [
                           {
                             "input": {
                               "reference": "input-1",
                               "path": "some.thing>over.there",
                               "type": "com.webxells.dis.test.example.mapping.TestMappingPoint",
                               "test": "someWeirdValue"
                             },
                             "output": {
                               "reference": "output-1",
                               "path": "path.to.hell"
                             },
                             "operations": [
                               {
                                 "type": "com.webxells.dis.test.example.validator.TestValidator",
                                 "%1$s": "%s"
                               }
                             ]
                           }
                         ]
                       }
                     }
                    ]
                }
                """, SourceMapping.getMappingKey(), location1, location2));
        assertEquals(location1, ConfigurationMapping.getLocation(fixture.getConfigurations().get(0).getInput()));
        assertEquals(location2, ConfigurationMapping.getLocation(fixture.getConfigurations().get(0).getMappings().parts().get(0).getOperations().get(0)));
    }

    @Test
    void testConfigWithListOfMapOfList() throws IOException {
        JsonConfig fixture = getConfigByFilename("config_list-map-list");
        if (fixture.configurations.get(0).getMappings().parts().get(0).getOperations().get(0) instanceof TestValidator validator) {
            assertEquals(List.of(Map.of("test", List.of("val1", "val2"))), validator.getComplicated());
            return;
        }
        fail();
    }

    @Test
    void getConfigWithTypeNotImplementingApiShouldFail() {
        assertThrows(JsonSyntaxException.class, () -> getConfigByFilename("config_simple_wrong_type_trigger"));
        assertThrows(JsonSyntaxException.class, () -> getConfigByFilename("config_simple_wrong_type_input"));
        assertThrows(JsonSyntaxException.class, () -> getConfigByFilename("config_simple_wrong_type_output"));
    }

    @Test
    void getConfigWithoutTypeShouldFail() {
        assertThrows(JsonSyntaxException.class, () -> getConfigByFilename("config_simple_no_type_trigger"));
        assertThrows(JsonSyntaxException.class, () -> getConfigByFilename("config_simple_no_type_input"));
        assertThrows(JsonSyntaxException.class, () -> getConfigByFilename("config_simple_no_type_output"));
    }

    @Test
    void getConfigWithInvalidTypeShouldFail() {
        assertThrows(JsonSyntaxException.class, () -> getConfigByFilename("config_simple_invalid_type_trigger"));
        assertThrows(JsonSyntaxException.class, () -> getConfigByFilename("config_simple_invalid_type_output"));
        assertThrows(JsonSyntaxException.class, () -> getConfigByFilename("config_simple_invalid_type_input"));
    }

    @Test
    void testSetKnowTypeMapping() throws IOException {
        KnownTypeMapping.force(Default.class, DefaultB.class.getName());
        JsonConfig config = getConfigByFilename("config_set_default_known_type_mapping");

        MappingOperation defaultImplementation = config.getConfigurations().getFirst()
                .getMappings().parts().getFirst()
                .getOperations().getFirst();
        assertInstanceOf(DefaultParent.class, defaultImplementation);

        assertEquals(DefaultB.class.getName(), ((DefaultParent) defaultImplementation).getDefaultInterface());
    }

    @Test
    void testValidatingResults() {
        assertDoesNotThrow(() -> getConfigByFilename("config_valid"));

        assertThrows(JsonSyntaxException.class, () -> getConfigByFilename("config_invalid"));
    }

    @Test
    void getConfigWithNestedClasses() throws IOException {
        JsonConfig fixture = getConfigByFilename("config_nested_classes");
        TestInputConfig inputConfig = (TestInputConfig) fixture.getConfigurations().get(0).getInput();
        List<NestedTestClass> nestedImplementation = inputConfig.getNestedImplementations();
        assertEquals(3, nestedImplementation.size());
        assertTrue(nestedImplementation.get(0) instanceof NestedTestClassA);
        assertTrue(nestedImplementation.get(1) instanceof NestedTestClassA);
        assertTrue(nestedImplementation.get(2) instanceof NestedTestClassB);

        TestWorkflow testWorkflow = fixture.<TestWorkflow> getSystemSupplier().get(fixture);
        assertFalse(testWorkflow.isSomeBoolean());
        assertNull(testWorkflow.getSomeString());
    }

    @Test
    void getConfigWithMaps() throws IOException {
        JsonConfig fixture = getConfigByFilename("config_simple_map");
        TestInputConfig inputConfig = (TestInputConfig) fixture.getConfigurations().get(0).getInput();

        Map<String, String> actualStringMap = inputConfig.getStringMap();
        Map<String, NestedTestClass> actualStringObjectMap = inputConfig.getStringObjectMap();
        Map<String, Integer> actualStringIntMap = inputConfig.getStringIntMap();
        Map<String, Boolean> actualStringBooleanMap = inputConfig.getStringBooleanMap();
        Map<String, List<String>> actualStringListStringMap = inputConfig.getStringListStringMap();
        Map<String, List<NestedTestClass>> actualStringListObjectMap = inputConfig.getStringListObjectMap();


        assertTrue(actualStringMap.containsKey("testa"));
        assertTrue(actualStringMap.containsKey("testb"));
        assertEquals("testa-value", actualStringMap.get("testa"));
        assertEquals("testb-value", actualStringMap.get("testb"));

        assertTrue(actualStringObjectMap.containsKey("testa"));
        assertTrue(actualStringObjectMap.containsKey("testb"));
        assertTrue(actualStringObjectMap.get("testa") instanceof NestedTestClassA);
        assertTrue(actualStringObjectMap.get("testb") instanceof NestedTestClassB);

        assertTrue(actualStringIntMap.containsKey("testa"));
        assertTrue(actualStringIntMap.containsKey("testb"));
        assertEquals(12, actualStringIntMap.get("testa"));
        assertEquals(5, actualStringIntMap.get("testb"));

        assertTrue(actualStringBooleanMap.containsKey("testa"));
        assertTrue(actualStringBooleanMap.containsKey("testb"));
        assertTrue(actualStringBooleanMap.containsKey("testc"));
        assertTrue(actualStringBooleanMap.get("testa"));
        assertFalse(actualStringBooleanMap.get("testb"));
        assertNull(actualStringBooleanMap.get("testc"));

        assertTrue(actualStringListStringMap.containsKey("testa"));
        assertTrue(actualStringListStringMap.containsKey("testb"));
        List<String> testa = actualStringListStringMap.get("testa");
        assertEquals(2, testa.size());
        assertEquals("a", testa.get(0));
        assertEquals("b", testa.get(1));
        List<String> testb = actualStringListStringMap.get("testb");
        assertEquals(2, testb.size());
        assertEquals("c", testb.get(0));
        assertEquals("d", testb.get(1));

        assertTrue(actualStringObjectMap.containsKey("testa"));
        assertTrue(actualStringObjectMap.containsKey("testb"));
        List<NestedTestClass> test2a = actualStringListObjectMap.get("testa");
        assertEquals(2, test2a.size());
        assertTrue(test2a.get(0) instanceof NestedTestClassB);
        assertTrue(test2a.get(1) instanceof NestedTestClassA);
        List<String> test2aNestedStringList = ((NestedTestClassA) test2a.get(1)).getStringList();
        assertEquals(2, test2aNestedStringList.size());
        assertEquals("a", test2aNestedStringList.get(0));
        assertEquals("b", test2aNestedStringList.get(1));
        List<NestedTestClass> test2b = actualStringListObjectMap.get("testb");
        assertEquals(2, test2b.size());
        assertTrue(test2b.get(0) instanceof NestedTestClassA);
        assertTrue(test2b.get(1) instanceof NestedTestClassB);

        TestWorkflow testWorkflow = fixture.<TestWorkflow> getSystemSupplier().get(fixture);
        assertFalse(testWorkflow.isSomeBoolean());
        assertEquals("ert", testWorkflow.getSomeString());
    }

    @Test
    void getConfigSimple() throws IOException {
        DisConfig fixture = getConfigByFilename("config_simple");
        List<JobConfig> actual = fixture.getConfigurations();
        assertEquals(1, actual.size());
        JobConfig actualDisConfig = actual.get(0);
        assertEquals("config-1", actualDisConfig.getName());

        MappingConfiguration actualMappings = actualDisConfig.getMappings();
        assertEquals(1, actualMappings.size());
        MappingPart actualMappingConfig = actualMappings.parts().get(0);

        assertEquals(TestMappingPart.class.getName(), actualMappingConfig.getClass().getName());
        assertEquals("someWeirdValue", ((TestMappingPart) actualMappingConfig).getTest());
        assertEquals(TestMappingPoint.class.getName(), actualMappingConfig.getInput().getClass().getName());
        assertEquals("someWeirdValue", ((TestMappingPoint) actualMappingConfig.getInput()).getTest());

        MappingPoint actualInputMappingPoint = actualMappingConfig.getInput();
        assertTrue(actualInputMappingPoint instanceof SimpleMappingPoint);
        assertEquals("some.thing>over.there", actualInputMappingPoint.getPath());
        assertEquals("input-1", actualInputMappingPoint.getReference());
        MappingPoint actualOutputMappingPoint = actualMappingConfig.getOutput();
        assertTrue(actualOutputMappingPoint instanceof SimpleMappingPoint);
        Assertions.assertNotSame(actualOutputMappingPoint, actualInputMappingPoint);
        assertEquals("path.to.hell", actualOutputMappingPoint.getPath());
        assertEquals("output-1", actualOutputMappingPoint.getReference());
        List<MappingOperation> operations = actualMappingConfig.getOperations();
        assertEquals(1, operations.size());
        MappingOperation mappingOperation = operations.get(0);
        assertTrue(mappingOperation instanceof TestValidator);
        assertEquals("validator", ((TestValidator) mappingOperation).getParameter());
        assertEquals(Validator.ErrorStrategy.ERROR, actualMappingConfig.getValidatorErrorStrategy());
        assertEquals(MappingPart.MultiToSingleSelectStrategy.LAST, actualMappingConfig.getMultiToSingleSelectStrategy());

        List<TriggerConfig> actualTriggers = actualDisConfig.getTriggers();
        assertEquals(1, actualTriggers.size());
        TriggerConfig actualTriggerConfig = actualTriggers.get(0);
        assertTrue(actualTriggerConfig instanceof TestTriggerConfig);
        assertEquals("testValue", ((TestTriggerConfig) actualTriggerConfig).getTestField());

        InputConfig actualInputConfig = actualDisConfig.getInput();
        assertEquals("input-1", actualInputConfig.getName());
        assertTrue(actualInputConfig instanceof TestInputConfig);
        assertEquals("testInputValue", ((TestInputConfig) actualInputConfig).getTestInputField());

        List<OutputConfig> actualOutputs = actualDisConfig.getOutputs();
        assertEquals(1, actualOutputs.size());
        OutputConfig actualOutputConfig = actualOutputs.get(0);
        assertEquals("output-1", actualOutputConfig.getName());
        assertTrue(actualOutputConfig instanceof TestOutputConfig);
        assertEquals("testOutputValue", ((TestOutputConfig) actualOutputConfig).getTestOutputField());
        assertTrue(((TestOutputConfig) actualOutputConfig).getMappingPortrayal() instanceof SimpleMappingPortrayal);

        List<MappingConfiguration> subData = actualDisConfig.getMappings().parts().get(0).getSubData();
        assertNotNull(subData);
        assertEquals(1, subData.size());
        MappingPart subPart = subData.get(0).parts().get(0);
        assertEquals("input-1-sub", subPart.getInput().getReference());
        assertEquals("some.thing[new]>over.there", subPart.getInput().getPath());
        assertEquals("output-1-sub", subPart.getOutput().getReference());
        assertEquals("path.to.frozen.hell", subPart.getOutput().getPath());

        TestWorkflow testWorkflow = fixture.<TestWorkflow> getSystemSupplier().get(fixture);
        assertTrue(testWorkflow.isSomeBoolean());
        assertEquals("abc", testWorkflow.getSomeString());
    }

    private JsonConfig getConfigByFilename(String filename) throws IOException {
        try (InputStream in = JsonConfig.class.getClassLoader().getResourceAsStream(String.format("%s.json", filename))){
            if (null == in) {
                Assertions.fail("config not found: ".concat(filename));
            }
            return new JsonConfig(new String(in.readAllBytes()));
        }
    }
}