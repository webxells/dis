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
package com.webxells.dis.base.config;

import com.webxells.dis.api.config.MappingPoint;
import com.webxells.dis.api.config.description.Description;
import com.webxells.dis.api.config.description.Required;

import java.util.Objects;
import java.util.Optional;

@Description("Pointer locating data in handler")
public class SimpleMappingPoint implements MappingPoint {
    @Required
    @Description("Name of corresponding input or output")
    private String reference;
    @Required
    @Description("Locates the value in the referenced input or output")
    private String path;

    public SimpleMappingPoint() {}

    public SimpleMappingPoint(final String reference, final String path) {
        this.reference = reference;
        this.path = path;
    }

    @Override
    public String toString() {
        return String.format("Point{%s %s}", beautifulToString(reference), beautifulToString(path));
    }

    private String beautifulToString(final String string) {
        return Optional.ofNullable(string).map(a -> String.format("'%s'", a)).orElse("null");
    }

    @Override
    public String getReference() {
        return reference;
    }

    @Override
    public String getPath() {
        return path;
    }

    public void setReference(final String reference) {
        this.reference = reference;
    }

    public void setPath(final String path) {
        this.path = path;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final SimpleMappingPoint that = (SimpleMappingPoint) o;
        return Objects.equals(reference, that.reference) &&
                Objects.equals(path, that.path);
    }

    @Override
    public int hashCode() {
        return Objects.hash(reference, path);
    }
}