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
package com.webxells.dis.json.intern;

import com.webxells.dis.json.JsonType;
import java.util.List;

public class ComposingJsonPath {
    private final String path;
    private List<String> value;
    private final JsonType type;

    public ComposingJsonPath(final String path, final JsonType type) {
        this.path = path;
        this.type = type;
    }

    @Override
    public String toString() {
        return "ComposingJsonPath{" +
                "path='" + path + '\'' +
                ", type=" + type +
                '}';
    }

    public List<String> getValue() {
        return value;
    }

    public void setValue(final List<String> value) {
        this.value = value;
    }

    public String getPath() {
        return path;
    }

    public JsonType getType() {
        return type;
    }
}
