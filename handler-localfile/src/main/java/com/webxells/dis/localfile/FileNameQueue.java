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
package com.webxells.dis.localfile;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

public class FileNameQueue {
    private static final Map<String, Queue<String>> REGISTRY = new HashMap<>();

    public static synchronized void add(final String instance, final String file) {
        REGISTRY.computeIfAbsent(instance, a -> new LinkedList<>()).add(file);
    }

    public static String getNext(final String index) {
        if (REGISTRY.containsKey(index) && REGISTRY.get(index).size() > 0) {
            return REGISTRY.get(index).poll();
        }
        return null;
    }
}
