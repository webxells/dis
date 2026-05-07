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
package com.webxells.dis.config.json.parsing.element;

import com.webxells.dis.config.json.parsing.DisonElement;
import com.webxells.dis.config.json.parsing.element.DisonPrimitiveReader;
import java.util.Optional;

public class DisonBooleanReader extends DisonPrimitiveReader<Boolean> {

    public DisonBooleanReader(final boolean content) {
        super(content);
    }

    protected static DisonBooleanReader parseStringValue(final String value) {
        if ("true".equalsIgnoreCase(value)) {
            return new DisonBooleanReader(true);
        }
        if ("false".equalsIgnoreCase(value)) {
            return new DisonBooleanReader(false);
        }
        return null;
    }

    @Override
    public DisonElement getType() {
        return DisonElement.BOOLEAN;
    }

    @Override
    public String writeJson() {
        return content ? "true" : "false";
    }

}