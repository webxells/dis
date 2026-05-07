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
package com.webxells.dis.plain.output;

import com.webxells.dis.api.config.OutputConfig;
import com.webxells.dis.api.config.description.Default;
import com.webxells.dis.api.config.description.Description;
import com.webxells.dis.api.config.description.Required;
import com.webxells.dis.api.resource.Resource;
import com.webxells.dis.plain.MultiMapping;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Description("Template engine")
public class EchoConfig implements OutputConfig {
    public enum ValueSource {
        INPUT, OUTPUT, INPUT_OUTPUT, OUTPUT_INPUT, BOTH
    }
    @Description("Reference for this handler")
    private String name;
    @Description("Embeds variable into this string")
    @Required(xor = "separator")
    private String template;
    @Description("Defines separator to join values with")
    @Required(xor = "template")
    @Default("Blank string")
    private String separator = "";
    @Description("Evaluates variable names as template")
    @Default("false")
    private boolean evaluateVarNames;
    @Description("Hide variables not found")
    @Default("false")
    private boolean hideUnknownVariables;
    @Description("Mapping points of this source will be considered as variable names")
    @Default("OUTPUT")
    private ValueSource valueSource = ValueSource.OUTPUT;
    @Description("Variables that will be treated as multi mapping")
    private Map<String, MultiMapping> multiMapping = Map.of();
    @Required
    @Description("Sends data to specified destination")
    private Resource sender;
    @Description("Defines characters to be escaped by the escapeChar")
    private List<Character> charsToEscape;
    @Default("\\")
    private char escapeChar = '\\';

    public void setName(final String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getType() {
        return Echo.class.getName();
    }

    public String getTemplate() {
        return template;
    }

    public void setTemplate(final String template) {
        this.template = template;
    }

    public Resource getSender() {
        return sender;
    }

    public void setSender(final Resource sender) {
        this.sender = sender;
    }

    public String getSeparator() {
        return separator;
    }

    public void setSeparator(final String separator) {
        this.separator = Objects.requireNonNull(separator);
    }

    public Map<String, MultiMapping> getMultiMapping() {
        return multiMapping;
    }

    public void setMultiMapping(final Map<String, MultiMapping> multiMapping) {
        this.multiMapping = multiMapping;
    }

    public ValueSource getValueSource() {
        return valueSource;
    }

    public void setValueSource(final ValueSource valueSource) {
        this.valueSource = valueSource;
    }

    public List<Character> getCharsToEscape() {
        return charsToEscape;
    }

    public void setCharsToEscape(final List<Character> charsToEscape) {
        this.charsToEscape = charsToEscape;
    }

    public EchoConfig cloneWithDifferentName(final String name) {
        return new EchoConfig() {{
            setName(name);
            setMultiMapping(multiMapping);
            setTemplate(template);
            setSender(sender);
            setSeparator(separator);
            setCharsToEscape(charsToEscape);
        }};
    }

    public char getEscapeChar() {
        return escapeChar;
    }

    public void setEscapeChar(final char escapeChar) {
        this.escapeChar = escapeChar;
    }

    public boolean isEvaluateVarNames() {
        return evaluateVarNames;
    }

    public void setEvaluateVarNames(final boolean evaluateVarNames) {
        this.evaluateVarNames = evaluateVarNames;
    }

    public boolean isHideUnknownVariables() {
        return hideUnknownVariables;
    }

    public void setHideUnknownVariables(final boolean hideUnknownVariables) {
        this.hideUnknownVariables = hideUnknownVariables;
    }
}