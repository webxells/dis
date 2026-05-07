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
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

public class NoPriorityShortening implements Shortening{

    @Override
    public String shorten(final List<String> parts, final int finalPartLength, final int maxSize) {
        final int partLength = parts.size();
        int estimatedSize;
        final AtomicBoolean sizeIsChanging = new AtomicBoolean();
        int round = 0;

        do {
            round++;
            estimatedSize = 0;
            for (final String current : parts) {
                estimatedSize += Math.max(1, current.length() - round);
                sizeIsChanging.set(sizeIsChanging.get() || current.length() - round > 1);
            }
            if (estimatedSize + partLength + finalPartLength <= maxSize) {
                return createShortenedParts(round, parts);
            }
        } while (sizeIsChanging.getAndSet(false));

        if (estimatedSize + partLength + Math.max(1, finalPartLength - round) <= maxSize) {
            return createShortenedParts(round, parts);
        }
        return "";
    }

    private String createShortenedParts(final int round, final List<String> parts) {
        final String result = parts.stream()
                .map(a -> a.substring(0, Math.max(1, a.length() - round)))
                .collect(Collectors.joining("."));
        if (result.isBlank()) {
            return "";
        }
        return result.concat(".");
    }
}