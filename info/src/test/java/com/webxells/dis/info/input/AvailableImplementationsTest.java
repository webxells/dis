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
package com.webxells.dis.info.input;

import com.webxells.dis.api.MappingOperation;
import com.webxells.dis.api.MappingPortrayal;
import com.webxells.dis.api.RuntimeEnvironment;
import com.webxells.dis.api.config.DisConfigApi;
import com.webxells.dis.api.config.InputConfig;
import com.webxells.dis.api.config.MappingConfiguration;
import com.webxells.dis.api.config.MappingPoint;
import com.webxells.dis.api.config.OutputConfig;
import com.webxells.dis.api.config.TriggerConfig;
import com.webxells.dis.base.SimpleMappingPortrayal;
import com.webxells.dis.base.config.SimpleMappingConfiguration;
import com.webxells.dis.base.config.SimpleMappingPoint;
import com.webxells.dis.boot.KnownTypeMapping;
import com.webxells.dis.info.input.ModuleBuilder.EnumValue;
import com.webxells.dis.test.cases.ConfigurationBuilder;
import com.webxells.dis.test.cases.SimpleTestCase;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AvailableImplementationsTest extends SimpleTestCase {

    private static final List<MappingConfiguration> implementationData = new LinkedList<>();
    private static final List<MappingConfiguration> knownTypeMappingData = new LinkedList<>();

    private static final String INPUT_REF = "test";
    private static ModuleBuilder moduleBuilder;

    @BeforeAll
    static void setup() throws Exception {
        KnownTypeMapping.add(MappingPortrayal.class, SimpleMappingPortrayal.class.getName());
        KnownTypeMapping.add(MappingPoint.class, SimpleMappingPoint.class.getName());

        SimpleMappingConfiguration mappingConfiguration = newConfiguration()
                .addPart(ConfigurationBuilder.newPart()
                        .setInput(new SimpleMappingPoint(INPUT_REF, "implementations")))
                .addPart(ConfigurationBuilder.newPart()
                        .setInput(new SimpleMappingPoint(INPUT_REF, "knownTypeMapping")))
                .build();
        AvailableImplementationsConfig config = new AvailableImplementationsConfig();
        config.setName(INPUT_REF);
        config.setKnownMappingAsMap(true);
        AvailableImplementations fixture = new AvailableImplementations(config);
        fixture.start();
        assertTrue(fixture.hasNext());
        int count = fixture.read(mappingConfiguration);
        assertFalse(fixture.hasNext());
        fixture.end();

        implementationData.addAll(mappingConfiguration.parts().get(0).getSubData());
        knownTypeMappingData.addAll(mappingConfiguration.parts().get(1).getSubData());

        assertTrue(3000 < count);

        moduleBuilder = new ModuleBuilder(implementationData);

    }

    @Test
    void configClassesShouldNotExists() {
        moduleBuilder.notListed(InputConfig.class);
        moduleBuilder.notListed(OutputConfig.class);
        moduleBuilder.notListed(TriggerConfig.class);
    }

    @Test
    void test() {
        moduleBuilder.findModule("com.webxells.dis.info.input.TestDisClass", implementationData)
                .forApi("com.webxells.dis.api.input.Input")
                .respects(TestDisClass.TestRefinement.class)
                .withOption(ModuleBuilder.newOption("amount")
                        .type("int")
                        .defaultValue("1")
                        .requiredOr(List.of("status", "internalText")))
                .withOption(ModuleBuilder.newOption("numbers")
                        .required()
                        .multiple()
                        .alias("testNumbers")
                        .type("int"))
                .withOption(ModuleBuilder.newOption("childMapping")
                        .mappable()
                        .type("implementation")
                        .value("com.webxells.dis.info.input.TestDisClass"))
                .withOption(ModuleBuilder.newOption("status")
                        .type("enum")
                        .requiredIfPresent(List.of("amount"))
                        .values(List.of(new EnumValue("ON", "test on"),
                                new EnumValue("OFF", "test off"))))
                .withOption(ModuleBuilder.newOption("text")
                        .type("string")
                        .required()
                        .alias("description"))
                .assertExists();

        moduleBuilder.findModule("com.webxells.dis.base.input.Join", implementationData)
                .forApi("com.webxells.dis.api.input.Input")
                .withDescription("Joins multiple Input configurations by specific JoinLinker")
                .withOption(ModuleBuilder.newOption("name")
                        .required()
                        .description("Reference of this handler")
                        .type("string")
                )
                .withOption(ModuleBuilder.newOption("rootConfig")
                        .required()
                        .description("Configuration that child JoinLinkers depends")
                        .alias("root")
                        .type("implementation")
                        .value("com.webxells.dis.api.input.Input")
                )
                .withOption(ModuleBuilder.newOption("children")
                        .required()
                        .multiple()
                        .description("To link root with specific child")
                        .type("implementation")
                        .value("com.webxells.dis.api.input.linker.JoinLinker")
                )
                .assertExists();

        moduleBuilder.findModule("com.webxells.dis.base.output.DevNull", implementationData)
                .forApi("com.webxells.dis.api.output.Output")
                .withOption(ModuleBuilder.newOption("name")
                        .required()
                        .description("Reference of this handler")
                        .type("string")
                )
                .assertExists();

        moduleBuilder.findModule("com.webxells.dis.base.manipulator.ChangeCase", implementationData)
                .forApi("com.webxells.dis.api.MappingOperation","com.webxells.dis.api.manipulator.Manipulator")
                .withOption(ModuleBuilder.newOption("toType")
                        .type("enum")
                        .defaultValue("LOWER")
                        .values(List.of(new EnumValue("LOWER"), new EnumValue("UPPER")))
                )
                .assertExists();

        moduleBuilder.findModule("com.webxells.dis.base.validator.IsSet", implementationData)
                .forApi("com.webxells.dis.api.MappingOperation","com.webxells.dis.api.validator.Validator")
                .withOption(ModuleBuilder.newOption("not")
                        .required()
                        .description("Inverts the Validator result")
                        .defaultValue("false")
                        .type("boolean")
                )
                .withOption(ModuleBuilder.newOption("fail")
                        .description("Log message printed if Validator fails")
                        .defaultValue("None")
                        .type("string"))
                .withOption(ModuleBuilder.newOption("level")
                        .description("Log weight of message")
                        .defaultValue("DEBUG")
                        .type("enum")
                        .values(List.of(new EnumValue("FATAL"),new EnumValue("ERROR"),new EnumValue("WARN"),new EnumValue("INFO"),
                                new EnumValue("DEBUG"), new EnumValue("TRACE"), new EnumValue("OFF"))))
                .assertExists();

        moduleBuilder.findModule("com.webxells.dis.base.trigger.OnJobEnd", implementationData)
                .forApi("com.webxells.dis.api.trigger.Trigger")
                .withOption(ModuleBuilder.newOption("jobName")
                        .description("Name is defined in the JobEndNotifier of the previous config")
                        .type("string")
                )
                .withOption(ModuleBuilder.newOption("name")
                        .description("Reference of this handler")
                        .type("string")
                )
                .withOption(ModuleBuilder.newOption("sleepInterval")
                        .description("Pausing interval between Trigger validations")
                        .defaultValue("500")
                        .type("long")
                )
                .withOption(ModuleBuilder.newOption("runtimeEnvironment")
                        .type("implementation")
                        .value(RuntimeEnvironment.class.getName())
                )
                .assertExists();

        moduleBuilder.findModule("com.webxells.dis.base.SimpleMappingPortrayal", implementationData)
                .forApi("com.webxells.dis.api.MappingPortrayal")
                .withOption(ModuleBuilder.newOption("path")
                        .description("Path of mapping part")
                        .type("string")
                )
                .withOption(ModuleBuilder.newOption("reference")
                        .description("Reference of mapping part")
                        .type("string")
                )
                .withOption(ModuleBuilder.newOption("source")
                        .description("Source of mapping part")
                        .defaultValue("INPUT")
                        .type("enum")
                        .values(List.of(new EnumValue("INPUT"), new EnumValue("OUTPUT")))
                )
                .withOption(ModuleBuilder.newOption("required")
                        .description("Raises an error if mapping part was not found")
                        .defaultValue("false")
                        .type("boolean")
                )
                .assertExists();

        moduleBuilder.findModule("com.webxells.dis.base.config.SimpleMappingPart", implementationData)
                .forApi("com.webxells.dis.api.config.MappingPart")
                .withOption(ModuleBuilder.newOption("input")
                        .type("implementation")
                        .value("com.webxells.dis.api.config.MappingPoint")
                )
                .withOption(ModuleBuilder.newOption("output")
                        .type("implementation")
                        .value("com.webxells.dis.api.config.MappingPoint")
                )
                .withOption(ModuleBuilder.newOption("operations")
                        .type("implementation")
                        .multiple()
                        .value("com.webxells.dis.api.MappingOperation")
                )
                .withOption(ModuleBuilder.newOption("validatorErrorStrategy")
                        .description("Strategy to handle failing data validations")
                        .type("enum")
                        .values(convertToEnumValues(List.of("RESET_CONFIGURATION", "SKIP_FOLLOWING_OPERATIONS", "SKIP_DATASET_PIECE",
                                "SKIP_DATASET", "CONTINUE_NEXT_READ", "ERROR")))
                )
                .withOption(ModuleBuilder.newOption("multiToSingleSelectStrategy")
                        .description("Strategy to pick single value on multiple values present")
                        .defaultValue("FIRST")
                        .type("enum")
                        .values(List.of(new EnumValue("FIRST"), new EnumValue("LAST"), new EnumValue("ERROR")))
                )
                .withOption(ModuleBuilder.newOption("subData")
                        .description("Mapping configuration within this MappingPart")
                        .type("implementation")
                        .value("com.webxells.dis.api.config.MappingConfiguration")
                )
                .withOption(ModuleBuilder.newOption("refinements")
                        .description("Refinements are special flags that may alter normal workflow of several modules")
                        .type("implementation")
                        .multiple()
                        .value("com.webxells.dis.api.config.Refinement")
                )
                .assertExists();

        moduleBuilder.findModule("com.webxells.dis.base.manipulator.Mapping", implementationData)
                .forApi("com.webxells.dis.api.MappingOperation","com.webxells.dis.api.manipulator.Manipulator")
                .withOption(ModuleBuilder.newOption("valueMapping")
                        .required()
                        .mappable()
                        .type("string")
                )
                .withOption(ModuleBuilder.newOption("ignoreCase")
                        .description("Ignores case of mapping keys")
                        .defaultValue("false")
                        .type("boolean")
                )
                .withOption(ModuleBuilder.newOption("defaultValue")
                        .description("Default value, if no mapping for current value was found")
                        .type("string")
                )
                .assertExists();

        moduleBuilder.findModule("com.webxells.dis.rest.content.paginating.NumericParameterChange", implementationData)
                .forApi("com.webxells.dis.rest.content.paginating.NumericParameterChange")
                .withOption(ModuleBuilder.newOption("name")
                        .required()
                        .description("Parameter name")
                        .type("string")
                )
                .withOption(ModuleBuilder.newOption("startValue")
                        .required()
                        .description("Start value of the parameter")
                        .type("long")
                )
                .withOption(ModuleBuilder.newOption("increment")
                        .description("Increment value of the parameter")
                        .defaultValue("1")
                        .type("long")
                )
                .assertExists();

        moduleBuilder.findModule("com.webxells.dis.plain.MultiMapping", implementationData)
                .forApi("com.webxells.dis.plain.MultiMapping")
                .withOption(ModuleBuilder.newOption("template")
                        .description("Template for subData - required by sourceType MULTI_CONFIGURATION")
                        .type("string")
                )
                .withOption(ModuleBuilder.newOption("delimiter")
                        .description("Value to put between values - required by sourceType MULTI_VALUE")
                        .type("string")
                )
                .withOption(ModuleBuilder.newOption("before")
                        .description("Value to put before the generated string")
                        .type("string")
                )
                .withOption(ModuleBuilder.newOption("after")
                        .description("Value to put after the generated string")
                        .type("string")
                )
                .withOption(ModuleBuilder.newOption("sourceType")
                        .description("Type of this multi Mapping")
                        .defaultValue("MULTI_CONFIGURATION")
                        .type("enum")
                        .values(List.of(new EnumValue("MULTI_CONFIGURATION", "Parsing subData as new template"),
                                new EnumValue("MULTI_VALUE","Parsing subData as value list")))
                )
                .assertExists();
    }

    @Test
    void testContainsInnerClassNotDisConfigApi() {
        moduleBuilder.findModule("com.webxells.dis.rest.content.DatasetAlteringMethod", implementationData)
                .forApi("com.webxells.dis.rest.content.ContentStrategy")
                .withOption(ModuleBuilder.newOption("rules")
                        .required()
                        .multiple()
                        .type("implementation")
                        .value("com.webxells.dis.rest.content.DatasetAlteringMethod$Rule")
                )
                .assertExists();

        moduleBuilder.findModule("com.webxells.dis.rest.content.DatasetAlteringMethod$Rule", implementationData)
                .forApi("com.webxells.dis.rest.content.DatasetAlteringMethod$Rule")
                .withOption(ModuleBuilder.newOption("checkType")
                        .required()
                        .type("enum")
                        .values(List.of(new EnumValue("ISSET"), new EnumValue("EMPTY")))
                )
                .withOption(ModuleBuilder.newOption("resultMethod")
                        .required()
                        .type("enum")
                        .values(convertToEnumValues(List.of("GET", "POST", "PUT", "DELETE", "HEAD", "PATCH", "SEARCH")))
                )
                .withOption(ModuleBuilder.newOption("mappingPortrayal")
                        .required()
                        .type("implementation")
                        .value("com.webxells.dis.api.MappingPortrayal")
                )
                .assertExists();
    }

    @Test
    void testContainsOnlyRelevantModules() {
        assertTrue(implementationData.stream()
                .flatMap(id -> id.parts().get(1).getSubData().stream())
                .flatMap(sd -> sd.parts().stream())
                .filter(s -> "name".equals(s.getInput().getPath()))
                .map(p -> {
                    try {
                        return Class.forName(p.value().get());
                    } catch (ClassNotFoundException e) {
                        throw new RuntimeException(e);
                    }
                })
                .flatMap(cl -> getPotentialConfigClasses(cl).stream())
                .allMatch(cl -> !cl.isInterface() && !Modifier.isAbstract(cl.getModifiers()) &&
                            (DisConfigApi.class.isAssignableFrom(cl) || DisConfigApi.class.isAssignableFrom(cl.getNestHost()))));
    }

    @Test
    void testDataContainsMappingOperationWithModules() {
        final MappingConfiguration mappingOperationConfig = implementationData.stream()
                .filter(id -> MappingOperation.class.getName().equals(id.parts().getFirst().getDataset().getContent().getFirst().value().get()))
                .findFirst()
                .orElse(null);

        assertNotNull(mappingOperationConfig);
        assertFalse(mappingOperationConfig.parts().get(1).getSubData().getFirst().parts().isEmpty());
    }

    @Test
    void testKnownTypeMapping() {
        assertEquals(1, knownTypeMappingData.size());
        assertEquals(2, knownTypeMappingData.get(0).parts().size());
        moduleBuilder.assertPartHasInputPathAndContent(MappingPortrayal.class.getName(), SimpleMappingPortrayal.class.getName(),
                knownTypeMappingData.get(0).parts());
        moduleBuilder.assertPartHasInputPathAndContent(MappingPoint.class.getName(), SimpleMappingPoint.class.getName(),
                knownTypeMappingData.get(0).parts());
    }

    private Set<Class<?>> getPotentialConfigClasses(final Class<?> clazz) {
        return getAllTypeParameters(clazz).stream()
                .filter(tp -> !(tp instanceof ParameterizedType) && !(tp instanceof WildcardType))
                .filter(Objects::nonNull)
                .map(tp -> getClassByName(tp.getTypeName()))
                .collect(Collectors.toSet());
    }

    private Class<?> getClassByName(final String clazzName) {
        try {
            return Class.forName(clazzName);
        } catch (final ClassNotFoundException | NoClassDefFoundError e) {
            throw new RuntimeException("Could not load class " + clazzName, e);
        }
    }

    private Set<Type> getAllTypeParameters(final Class<?> clazz) {
        final Set<Type> typeParameters = new LinkedHashSet<>();

        typeParameters.addAll(getTypeParameters(clazz));
        typeParameters.addAll(getInterfaceTypeParameters(clazz));
        removeTypeVariableFromTypeParameters(typeParameters);
        return typeParameters;
    }

    private Set<Type> getTypeParameters(final Class<?> clazz) {
        return Stream.of(clazz.getTypeParameters())
                .flatMap(tp -> Stream.of(tp.getBounds()))
                .collect(Collectors.toSet());
    }

    private Set<Type> getInterfaceTypeParameters(final Class<?> clazz) {
        final Set<Type> typeParameters = new LinkedHashSet<>();

        for (Type interfaceType : clazz.getGenericInterfaces()) {
            if (interfaceType instanceof ParameterizedType) {
                typeParameters.addAll(Set.of(((ParameterizedType) interfaceType).getActualTypeArguments()));
            }
        }

        return typeParameters;
    }

    private Set<Type> getSuperclassParameters(final Class<?> clazz) {
        final Set<Type> parameterTypes = new LinkedHashSet<>();

        if (clazz.getGenericSuperclass() instanceof ParameterizedType) {
            parameterTypes.addAll(Set.of(((ParameterizedType) clazz.getGenericSuperclass()).getActualTypeArguments()));
        }
        return parameterTypes;
    }

    private void removeTypeVariableFromTypeParameters(final Set<Type> typeParameters) {
        typeParameters.removeAll(typeParameters.stream()
                .filter(tp -> tp instanceof TypeVariable)
                .collect(Collectors.toSet()));
    }


    private List<EnumValue> convertToEnumValues(List<String> values) {
        return values.stream().map(EnumValue::new).collect(Collectors.toList());
    }

}