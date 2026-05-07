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
package com.webxells.dis.config.json.parsing.intern;

import com.webxells.dis.config.json.parsing.plugins.DisonPlugin;
import com.webxells.dis.config.json.parsing.plugins.DisonPluginManager;
import com.webxells.dis.config.json.parsing.plugins.ResilientPlugin;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class PluginFactory {
    private final List<DisonPluginManager> managers = new LinkedList<>();
    private final Map<String, DisonPlugin> resilientPlugins = new HashMap<>();

    public void register(final DisonPluginManager disonPluginManager) {
        managers.add(disonPluginManager);
    }

    public Optional<DisonPlugin> find(final String varName) {
        if (resilientPlugins.containsKey(varName)) {
            return Optional.of(resilientPlugins.get(varName));
        }
        for (final DisonPluginManager description : managers) {
            final DisonPlugin plugin = description.isCompetent(varName);
            if (null != plugin) {
                if (plugin instanceof ResilientPlugin) {
                    resilientPlugins.put(varName, plugin);
                }
                return Optional.of(plugin);
            }
        }
        return Optional.empty();
    }

    public void clear() {
        resilientPlugins.clear();
    }
}