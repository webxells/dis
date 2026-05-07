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

import com.webxells.dis.config.json.parsing.DisonElementReader;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

public abstract class DisonPrimitiveReader<T> implements DisonElementReader {
    protected final T content;

    protected DisonPrimitiveReader(final T content) {
        this.content = content;
    }

    public static DisonElementReader parseString(final String value) {
        return findFirst(List.of(
                () -> DisonNullReader.parseStringValue(value),
                () -> DisonBooleanReader.parseStringValue(value),
                () -> DisonLongReader.parseStringValue(value),
                () -> DisonDoubleReader.parseStringValue(value)))
                .orElseGet(() -> new DisonStringReader(value));
    }

    private static Optional<DisonElementReader> findFirst(final List<Supplier<DisonElementReader>> types) {
        return types.stream()
                .map(Supplier::get)
                .filter(Objects::nonNull)
                .findFirst();
    }

    public T read() {
        return content;
    }

    @Override
    public String describeSelf() {
        return String.format("%s (%s)", content, getType());
    }
}