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
import com.webxells.dis.config.json.parsing.DisonElementReader;
import com.webxells.dis.config.json.parsing.DisonJsonTransformer;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

public class DisonObjectReader implements DisonElementReader {
    private final Map<String, DisonElementReader> content;
    private final DisonJsonTransformer disonJsonTransformer;

    public DisonObjectReader(final Map<String, DisonElementReader> content, final DisonJsonTransformer disonJsonTransformer) {
        this.content = content;
        this.disonJsonTransformer = disonJsonTransformer;
    }

    @Override
    public DisonElement getType() {
        return DisonElement.OBJECT;
    }

    @Override
    public String writeJson() {
        disonJsonTransformer.pushStage(DisonJsonTransformer.JsonElementStage.OBJECT);
        final String result = String.format("{%s}", content.entrySet().stream()
                .map(a -> {
                    final String key = a.getKey();
                    disonJsonTransformer.pushStage(DisonJsonTransformer.JsonElementStage.ASSIGNMENT);
                    final String keyValueResult = String.format("\"%s\":%s", key, a.getValue().writeJson());
                    disonJsonTransformer.removeStage();
                    return keyValueResult;
                })
                .collect(Collectors.joining(",")));
        disonJsonTransformer.removeStage();
        return result;
    }

    public Map<String, DisonElementReader> getAsMap() {
        return Collections.unmodifiableMap(content);
    }
}