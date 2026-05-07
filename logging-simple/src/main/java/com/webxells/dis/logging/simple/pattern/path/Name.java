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
import com.webxells.dis.logging.simple.pattern.path.name.FirstFirstShortening;
import com.webxells.dis.logging.simple.pattern.path.name.NoPriorityShortening;
import com.webxells.dis.logging.simple.pattern.path.name.Shortening;
import java.util.LinkedList;
import java.util.List;

public class Name extends VariableWithDefinition {
    public enum ShorteningPattern {
        NO_PRIORITY(new NoPriorityShortening()),
        FIRST_FIRST(new FirstFirstShortening());

        private final Shortening shortening;

        ShorteningPattern(final Shortening shortening) {
            this.shortening = shortening;
        }

        String shorten(final List<String> name, final int finalPartLength, final int maxSize) {
            return shortening.shorten(name, finalPartLength, maxSize);
        }
    }

    public final int maxSize;
    public final int fixedSize;
    public final ShorteningPattern pattern;

    public Name(final String definition) {
        super(definition);
        pattern = defineEnum("pattern", ShorteningPattern.NO_PRIORITY);
        fixedSize = defineInt("fixedSize", 0);
        maxSize = 0 < fixedSize ? fixedSize : defineInt("maxSize", 0);
    }

    @Override
    public String parse(final Event event) {
        return shortening(event.getName());
    }

    private String shortening(String name) {
        if (maxSize > 0 && name.length() > maxSize) {
            final LinkedList<String> parts = split(name);
            if (parts.size() == 0) {
                throw new RuntimeException("unexpected logging name: " + name);
            }
            final String finalPart = parts.pollLast();
            final int finalPartLength = finalPart.length();
            final String partPath = pattern.shorten(parts, finalPartLength, maxSize);
            name = partPath.concat(
                    finalPart.substring(0, Math.min(finalPartLength, maxSize - partPath.length())));
        }
        if (fixedSize > 0) {
            return name.concat(" ".repeat(fixedSize - name.length()));
        }
        return name;
    }

    private LinkedList<String> split(final String name) {
        final LinkedList<String> result = new LinkedList<>();
        int start = 0;
        while (name.indexOf(".", start) > 0) {
            final int end = name.indexOf(".", start);
            result.add(name.substring(start, end));
            start = end + 1;
        }
        result.add(name.substring(start));
        return result;
    }
}