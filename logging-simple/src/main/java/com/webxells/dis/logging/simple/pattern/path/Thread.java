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
package com.webxells.dis.logging.simple.pattern.path;

import com.webxells.dis.logging.simple.Event;

public class Thread extends VariableWithDefinition {
    private final int fixedSize;
    private final boolean centered;

    public Thread(final String definition) {
        super(definition);
        fixedSize = defineInt("fixedSize", 0);
        centered = defineBoolean("centered", false);
    }

    @Override
    public String parse(final Event event) {
        final String name = event.getThread().getName();
        if (0 < fixedSize) {
            if (name.length() > fixedSize) {
                return ".".concat(name.substring(1 + name.length() - fixedSize));
            } else {
                return bloatToFixedSize(name, fixedSize, centered);
            }
        }
        return name;
    }

    private String bloatToFixedSize(final String string, final int fixedSize, final boolean centered) {
        if (centered) {
            final String space = " ".repeat((fixedSize - string.length()) / 2);
            final String result = String.format("%s%s%s", space, string, space);
            return result.length() == fixedSize ? result : result.concat(" ");
        }
        return string.concat(" ".repeat(fixedSize - string.length()));
    }
}