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
package com.webxells.dis.logging.simple.pattern.path.name;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class FirstFirstShortening implements Shortening{

    @Override
    public String shorten(final List<String> parts, final int finalPartLength, final int maxSize) {
        final StringBuilder result = new StringBuilder();
        final int bigSize = parts.stream().mapToInt(String::length).sum() +
                parts.size() + finalPartLength;
        final AtomicInteger saved = new AtomicInteger();

        for (final String part : parts) {
            final int current = bigSize - saved.get();
            if (current <= maxSize) {
                result.append(part);
                result.append(".");
            } else {
                final int partLength = part.length();
                if (partLength > 0) {
                    final int newLength = Math.max(1, maxSize - current);
                    result.append(part, 0, newLength);
                    result.append(".");
                    saved.addAndGet(partLength - newLength);
                }
            }
        }
        final int resultLength = result.length();
        return resultLength == 0 || resultLength > maxSize ?
                "" : result.toString();
    }

}