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

import com.webxells.dis.api.config.MappingConfiguration;
import com.webxells.dis.api.config.MappingPart;

import com.webxells.dis.api.config.Refinement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Assertions;

import static org.junit.jupiter.api.Assertions.*;

public class ModuleBuilder {

    public static class EnumValue {

        public String value;
        public String description;

        public EnumValue(String value) {
            this.value = value;
        }

        public EnumValue(String value, String description) {
            this(value);
            this.description = description;
        }

    }

    public static class OptionBuilder {

        private String option;
        private Boolean required;
        private List<Object> requiredOr;
        private List<Object> requiredXor;
        private List<Object> requiredIfPresent;
        private String alias;
        private String defaultValue;
        private String type;
        private String description;
        private Boolean multiple;
        private Boolean mappable;

        private Object value;
        private List<EnumValue> values;

        public OptionBuilder(final String option) {
            this.option = option;
        }

        public OptionBuilder required() {
            required = true;
            return this;
        }

        public OptionBuilder requiredOr(List<Object> requiredOr) {
            this.requiredOr = requiredOr;
            this.required = true;
            return this;
        }

        public OptionBuilder requiredXor(List<Object> requiredXor) {
            this.requiredXor = requiredXor;
            this.required = true;
            return this;
        }

        public OptionBuilder requiredIfPresent(List<Object> requiredIfPresent) {
            this.requiredIfPresent = requiredIfPresent;
            this.required = true;
            return this;
        }

        public OptionBuilder multiple() {
            multiple = true;
            return this;
        }

        public OptionBuilder mappable() {
            mappable = true;
            return this;
        }

        public OptionBuilder alias(String alias) {
            this.alias = alias;
            return this;
        }

        public OptionBuilder defaultValue(String defaultValue) {
            this.defaultValue = defaultValue;
            return this;
        }

        public OptionBuilder description(String description) {
            this.description = description;
            return this;
        }

        public OptionBuilder type(String type) {
            this.type = type;
            return this;
        }

        public OptionBuilder value(Object value) {
            this.value = value;
            return this;
        }

        public OptionBuilder values(List<EnumValue> values) {
            this.values = values;
            return this;
        }
    }

    private List<MappingConfiguration> interfaceData;

    private String moduleName;
    private String[] apis;
    private String description;
    private final List<OptionBuilder> options = new ArrayList<>();
    private List<Class<? extends Refinement>> refinements = new ArrayList<>();

    public ModuleBuilder(final List<MappingConfiguration> interfaceData) {
        this.interfaceData = interfaceData;
    }

    public static OptionBuilder newOption(String option) {
        return new OptionBuilder(option);
    }

    public ModuleBuilder findModule(String moduleName, List<MappingConfiguration> configData) {
        this.moduleName = moduleName;
        interfaceData = configData;
        apis = null;
        description = null;
        options.clear();
        refinements.clear();
        return this;
    }

    public ModuleBuilder forApi(String... apis) {
        this.apis = apis;
        return this;
    }

    public ModuleBuilder withDescription(String description) {
        this.description = description;
        return this;
    }

    public ModuleBuilder withOption(OptionBuilder optionBuilder) {
        options.add(optionBuilder);
        return this;
    }

    public void assertExists() {
        List<MappingConfiguration> configData = new LinkedList<>();
        Arrays.stream(apis)
                .map(api -> getConfigsByClassName(api, interfaceData))
                .forEach(configData::addAll);

        assertFalse(configData.isEmpty());
        configData.forEach(this::assertModuleExists);
    }

    private void assertModuleExists(MappingConfiguration configuration) {
        final List<MappingConfiguration> moduleData = getConfigsByClassName(moduleName,
                getFirstPartByInputPath("modules", configuration.parts()).getSubData());
        assertFalse(moduleData.isEmpty());

        if (null != description) {
            moduleData.forEach(md -> {
                MappingPart part = getFirstPartByInputPath("description", md.parts());
                assertNotNull(part);
                assertEquals(description, part.value().get());
            });
        }

        if (0 < refinements.size()) {
            moduleData.forEach(md -> {
                MappingPart part = getFirstPartByInputPath("refinements", md.parts());
                assertNotNull(part);
                List<String> values = part.getDataset().getContent().stream()
                        .map(a -> a.value().orElseGet(Assertions::fail))
                        .toList();
                refinements.stream()
                        .map(Class::getName)
                        .filter(o -> !values.contains(o))
                        .findAny()
                        .ifPresent(a -> fail());
            });
        }

        moduleData.forEach(md -> {
            List<MappingConfiguration> optionData = getFirstPartByInputPath("options", md.parts()).getSubData();
            assertEquals(options.size(), optionData.size());

            for (OptionBuilder option : options) {
                MappingConfiguration optionConfig = optionData.stream()
                        .filter(od -> option.option.equals(getFirstPartByInputPath("name", od.parts()).value().get()))
                        .findFirst().orElse(null);

                if (null != optionConfig) {
                    assertOptionsExists(optionConfig.parts(), option);
                } else {
                    fail();
                }
            }
        });

    }

    private void assertOptionsExists(List<MappingPart> optionParts, OptionBuilder optionBuilder) {
        assertPartHasValue(optionBuilder.option, getFirstPartByInputPath("name", optionParts));
        assertPartHasValue(optionBuilder.type, getFirstPartByInputPath("type", optionParts));
        assertPartHasValue(optionBuilder.multiple, getFirstPartByInputPath("multiple", optionParts));
        assertPartHasValue(optionBuilder.mappable, getFirstPartByInputPath("mappable", optionParts));
        assertPartHasValue(optionBuilder.description, getFirstPartByInputPath("description", optionParts));
        assertPartHasValue(optionBuilder.alias, getFirstPartByInputPath("alias", optionParts));
        assertPartHasValue(optionBuilder.defaultValue, getFirstPartByInputPath("default", optionParts));
        assertRequired(optionParts, optionBuilder);

        switch (optionBuilder.type) {
            case "enum":
                assertEnumValues(getFirstPartByInputPath("values", optionParts), optionBuilder);
                break;
            case "implementation":
                assertPartHasValue(optionBuilder.value, getFirstPartByInputPath("ofType", optionParts));
                break;
        }
    }

    private void assertRequired(List<MappingPart> optionParts, OptionBuilder optionBuilder) {
        assertPartHasValue(optionBuilder.required, getFirstPartByInputPath("required", optionParts));

        MappingPart requiredConditionPart = getFirstPartByInputPath("requiredCondition", optionParts);
        if (null != requiredConditionPart) {
            assertEquals(1, requiredConditionPart.getSubData().size());
            List<MappingPart> subDataParts = requiredConditionPart.getSubData().get(0).parts();
            if (null != optionBuilder.requiredOr) {
                assertPartHasValues(optionBuilder.requiredOr,
                        getFirstPartByInputPath("or", subDataParts));
            }
            if (null != optionBuilder.requiredXor) {
                assertPartHasValues(optionBuilder.requiredXor,
                        getFirstPartByInputPath("xor", subDataParts));
            }
            if (null != optionBuilder.requiredIfPresent) {
                assertPartHasValues(optionBuilder.requiredIfPresent,
                        getFirstPartByInputPath("ifPresent", subDataParts));
            }
        } else {
            assertNull(optionBuilder.requiredOr);
            assertNull(optionBuilder.requiredXor);
            assertNull(optionBuilder.requiredIfPresent);
        }
    }

    private void assertEnumValues(final MappingPart valuePart, final OptionBuilder optionBuilder) {
        List<MappingConfiguration> valueConfigs = valuePart.getSubData();
        assertEquals(optionBuilder.values.size(), valueConfigs.size());

        for (EnumValue enumValue : optionBuilder.values) {
            MappingConfiguration valueConfig = getConfigHasPartWithContent(enumValue.value, valueConfigs);
            assertNotNull(valueConfig);

            MappingPart descriptionPart = getFirstPartByInputPath("description", valueConfig.parts());
            if (null != enumValue.description) {
                assertNotNull(descriptionPart);
                assertEquals(enumValue.description, descriptionPart.getDataset().getContent().get(0).value().get());
            } else if (null != descriptionPart) {
                fail(String.format("No description for enum value %s expected", enumValue.value));
            }
        }

    }

    public void assertPartHasInputPathAndContent(String path, String content, List<MappingPart> parts) {
        MappingPart part = getFirstPartByInputPath(path, parts);
        assertNotNull(part);
        assertTrue(partHasValue(part, content));
    }

    private MappingPart getFirstPartByInputPath(String path, List<MappingPart> parts) {
        return parts.stream().filter(part -> null != part.getInput() && null != path
                && path.equals(part.getInput().getPath())).findFirst().orElse(null);
    }

    private MappingPart getPartWithContent(String content, List<MappingPart> parts) {
        return parts.stream().filter(part -> null != part.getDataset() && null != content
                        && part.getDataset().getContent().stream().anyMatch(c -> content.equals(c.value().get())))
                .findFirst().orElse(null);
    }

    private boolean partHasValue(MappingPart part, Object value) {
        return null != part
                && null != part.getDataset()
                && part.getDataset().getContent().stream().anyMatch(dp -> dp.value().get().equals(value.toString()));
    }

    private void assertPartHasValue(Object expected, MappingPart part) {
        if (null != part && null != part.getDataset()) {
            assertEquals(expected.toString(), part.value().get());
        } else {
            assertNull(expected);
        }
    }

    private void assertPartHasValues(List<Object> expected, MappingPart part) {
        if (null != part && null != part.getDataset()) {
            List<Object> actual = part.getDataset().getContent().stream()
                    .map(d -> d.value().get())
                    .collect(Collectors.toList());

            assertEquals(expected.size(), actual.size());
            for (int i = 0; i < expected.size(); i++) {
                assertEquals(expected.get(i), actual.get(i));
            }
        } else {
            assertTrue(expected.isEmpty());
        }
    }

    private List<MappingConfiguration> getConfigsByClassName(String className, List<MappingConfiguration> configurations) {
        return configurations.stream().filter(c -> className.equals(getFirstPartByInputPath("name", c.parts())
                    .value().orElse("")))
                .toList();
    }

    private MappingConfiguration getConfigHasPartWithContent(String content, List<MappingConfiguration> configurations) {
        return configurations.stream().filter(c -> null != getPartWithContent(content, c.parts())).findFirst().get();
    }

    public void notListed(final Class<?> clazz) {
        if (!getConfigsByClassName(clazz.getName(), interfaceData).isEmpty()) {
            fail(clazz.getName() + " is not expected to be present");
        }
    }

    @SafeVarargs
    public final ModuleBuilder respects(final Class<? extends Refinement>... refinementClasses) {
        refinements.addAll(Arrays.asList(refinementClasses));
        return this;
    }
}