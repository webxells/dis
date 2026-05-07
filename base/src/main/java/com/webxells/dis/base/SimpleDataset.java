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
package com.webxells.dis.base;

import com.webxells.dis.api.Dataset;
import com.webxells.dis.api.DatasetPiece;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class SimpleDataset implements Dataset {
    private final List<DatasetPiece> content = new ArrayList<>();

    @Override
    public void collect(final List<DatasetPiece> list) {
        content.addAll(list);
    }

    @Override
    public void collect(final DatasetPiece piece) {
        content.add(piece);
    }

    @Override
    public void collectCopy(final Dataset other) {
        other.getContent().forEach(a -> collect(new SimpleDatasetPiece(a.value().orElse(null))));
    }

    @Override
    public List<DatasetPiece> getContent() {
        return content;
    }

    @Override
    public void clear() {
        content.clear();
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final SimpleDataset that = (SimpleDataset) o;
        return content.equals(that.content);
    }

    @Override
    public int hashCode() {
        return Objects.hash(content);
    }

    @Override
    public String toString() {
        return content.stream()
                .map(a -> a.value().orElse("<null>"))
                .collect(Collectors.joining("; "));
    }
}