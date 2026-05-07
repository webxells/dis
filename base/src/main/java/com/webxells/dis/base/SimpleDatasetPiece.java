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

import com.webxells.dis.api.DatasetPiece;
import java.util.Objects;
import java.util.Optional;

public class SimpleDatasetPiece implements DatasetPiece {
    private String value;

    public SimpleDatasetPiece(final  String value) {
        this.value = value;
    }

    @Override
    public Optional<String> value() {
        return Optional.ofNullable(value);
    }

    @Override
    public void rewriteValue(final String value) {
        this.value = value;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof SimpleDatasetPiece)) return false;
        final SimpleDatasetPiece that = (SimpleDatasetPiece) o;
        return  Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return "DatasetPiece: " + value;
    }
}