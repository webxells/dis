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
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class DisonArrayReader implements DisonElementReader {
    private final List<DisonElementReader> content;
    private final DisonJsonTransformer disonJsonTransformer;

    public DisonArrayReader(final List<DisonElementReader> content, final DisonJsonTransformer disonJsonTransformer) {
        this.content = content;
        this.disonJsonTransformer = disonJsonTransformer;
    }

    @Override
    public DisonElement getType() {
        return DisonElement.ARRAY;
    }

    @Override
    public String writeJson() {
        disonJsonTransformer.pushStage(DisonJsonTransformer.JsonElementStage.ARRAY);
        final String result = String.format("[%s]", content.stream()
                .map(DisonElementReader::writeJson)
                .filter(Objects::nonNull)
                .collect(Collectors.joining(",")));
        disonJsonTransformer.removeStage();
        return result;
    }

    public List<DisonElementReader> getContent() {
        return content;
    }
}