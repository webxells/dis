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
package com.webxells.dis.plain.manipulator;

import com.webxells.dis.api.DatasetPiece;
import com.webxells.dis.api.MappingPortrayal;
import com.webxells.dis.api.config.MappingConfiguration;
import com.webxells.dis.api.config.MappingPart;
import com.webxells.dis.api.config.description.Default;
import com.webxells.dis.api.config.description.Description;
import com.webxells.dis.api.config.description.Required;
import com.webxells.dis.api.manipulator.Manipulator;
import java.util.Optional;

@Description("Provides possibility of extending an old value with a new value")
public class ExtendValue implements Manipulator {
    @Description("Rule on how to extend old value with new one; also static values can be added here")
    @Default("$old$new")
    private String extendRule = "$old$new";
    @Required(xor = "valuePortrayal")
    @Description("Value that replaces '$new' in 'extendedRule'")
    private String value = "";
    @Required(xor = "value")
    @Description("Portrayal of mapping part that value replaces '$new' in 'extendedRule'")
    private MappingPortrayal valuePortrayal;
    @Override
    public void manipulate(final DatasetPiece datasetPiece, final MappingPart mappingPart) {
        datasetPiece.rewriteValue(replaceExtension(datasetPiece.value().orElse(""), mappingPart.getConfiguration()));
    }

    private String replaceExtension(final String oldValue, final MappingConfiguration configuration) {
        final StringBuilder result = new StringBuilder();
        final String value = getValue(configuration);
        for (int i = 0, m = extendRule.length(); i < m; i++) {
            if ('$' == extendRule.charAt(i) && i + 4 <= m) {
                final String directive = extendRule.substring(i, i + 4);
                if ("$old".equals(directive) || "$new".equals(directive)) {
                    result.append("$old".equals(directive) ? oldValue : value);
                    i+=3;
                    continue;
                }
            }
            result.append(extendRule.charAt(i));
        }
        return result.toString();
    }

    private String getValue(final MappingConfiguration configuration) {
        return Optional.ofNullable(valuePortrayal)
                .flatMap(a -> configuration.getByPortrayal(valuePortrayal))
                .flatMap(MappingPart::value)
                .orElse(value);

    }

    public void setExtendRule(final String extendRule) {
        this.extendRule = extendRule;
    }

    public void setValue(final String value) {
        this.value = value;
    }

    public void setValuePortrayal(final MappingPortrayal valuePortrayal) {
        this.valuePortrayal = valuePortrayal;
    }
}