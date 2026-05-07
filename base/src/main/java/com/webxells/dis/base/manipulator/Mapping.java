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
package com.webxells.dis.base.manipulator;

import com.webxells.dis.api.DatasetPiece;
import com.webxells.dis.api.config.MappingPart;
import com.webxells.dis.api.config.description.Default;
import com.webxells.dis.api.config.description.Description;
import com.webxells.dis.api.config.description.Required;
import com.webxells.dis.api.error.InvalidApi;
import com.webxells.dis.api.error.InvalidDatasetException;
import com.webxells.dis.api.manipulator.Manipulator;
import java.util.Map;
import java.util.Optional;

@Description("Converts given values to other ones")
public class Mapping implements Manipulator {
    @Required
    private Map<String, String> valueMapping;
    @Description("Ignores case of mapping keys")
    @Default("false")
    private boolean ignoreCase;
    @Description("Default value, if no mapping for current value was found")
    private String defaultValue;

    @Override
    public void validate() throws InvalidApi {
        if (null == valueMapping) {
            throw new InvalidApi("no mapping provided");
        }
    }

    @Override
    public void manipulate(final DatasetPiece currentPiece, final MappingPart mappingPart) throws InvalidDatasetException {
        currentPiece.value()
            .map(this::getMappedValue)
            .ifPresentOrElse(currentPiece::rewriteValue,
                    () -> Optional.ofNullable(defaultValue).ifPresent(currentPiece::rewriteValue));
        }

    private String getMappedValue(final String old) {
        if (!ignoreCase && valueMapping.containsKey(old)) {
            return valueMapping.get(old);
        } else if (ignoreCase) {
            return valueMapping.entrySet().stream()
                    .filter(a -> a.getKey().equalsIgnoreCase(old))
                    .map(Map.Entry::getValue)
                    .findAny().orElse(defaultValue);
        }
        return defaultValue;
    }

    public void setValueMapping(final Map<String, String> valueMapping) {
        this.valueMapping = valueMapping;
    }

    public void setIgnoreCase(final boolean ignoreCase) {
        this.ignoreCase = ignoreCase;
    }

    public void setDefaultValue(final String defaultValue) {
        this.defaultValue = defaultValue;
    }
}
