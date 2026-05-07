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
import java.util.Optional;

public class Message extends VariableWithDefinition {
    private static final String NEW_LINE = System.lineSeparator();

    private final int limit;
    private final boolean intelligentTabs;
    private final boolean markLimitReached;

    public Message(final String definition) {
        super(definition);
        limit = defineInt("limit", -1);
        intelligentTabs = defineBoolean("intelligentTabs", true);
        markLimitReached = defineBoolean("markLimitReached", false);
    }

    @Override
    public String parse(final Event event) {
        return Optional.ofNullable(event.getMsg())
                .map(this::limit)
                .map(this::placeTabs)
                .orElse("");
    }

    private String placeTabs(final String message) {
        if (intelligentTabs) {
            return message.replace(NEW_LINE, NEW_LINE.concat("\t"));
        }
        return message;
    }

    private String limit(final String message) {
        if (limit > 0) {
            return message.substring(0, limit).concat(markLimitReached && limit < message.length() ? "[...]" : "");
        }
        return message;
    }
}