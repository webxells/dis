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
package com.webxells.dis.test.example.output;

import com.webxells.dis.api.MappingPortrayal;
import com.webxells.dis.api.config.OutputConfig;
import com.webxells.dis.api.config.description.Internal;
import com.webxells.dis.test.example.TemplateDeep;
import com.webxells.dis.test.example.TemplateNested;
import java.util.concurrent.atomic.AtomicReference;

public class TestOutputConfig implements OutputConfig {
    private final String type;
    private String name;
    private String testOutputField;
    private String templateField;
    private TemplateDeep templateDeepField;
    private String path;
    private TemplateNested nested;
    private MappingPortrayal mappingPortrayal;
    private AtomicReference<TestOutput> selfReference;

    public TestOutputConfig() {
        this(null);
    }

    public TestOutputConfig(String name) {
        this(name, TestOutput.class.getName());
    }

    public TestOutputConfig(String name, String type) {
        this.name = name;
        this.type = type;
    }

    @Override
    public String getName() {
        return name;
    }

    public String getPath() {
        return path;
    }

    public void setPath(final String path) {
        this.path = path;
    }

    public TemplateNested getNested() {
        return nested;
    }

    public void setNested(final TemplateNested nested) {
        this.nested = nested;
    }

    public void setName(final String name) {
        this.name = name;
    }

    @Override
    public String getType() {
        return type;
    }

    public String getTestOutputField() {
        return testOutputField;
    }

    public void setTestOutputField(final String testOutputField) {
        this.testOutputField = testOutputField;
    }

    public String getTemplateField() {
        return templateField;
    }

    public void setTemplateField(final String templateField) {
        this.templateField = templateField;
    }

    public TemplateDeep getTemplateDeepField() {
        return templateDeepField;
    }

    public void setTemplateDeepField(final TemplateDeep templateDeepField) {
        this.templateDeepField = templateDeepField;
    }

    public MappingPortrayal getMappingPortrayal() {
        return mappingPortrayal;
    }

    public void setMappingPortrayal(final MappingPortrayal mappingPortrayal) {
        this.mappingPortrayal = mappingPortrayal;
    }

    public AtomicReference<TestOutput> getSelfReference() {
        return selfReference;
    }

    @Internal
    public void setSelfReference(final AtomicReference<TestOutput> selfReference) {
        this.selfReference = selfReference;
    }
}