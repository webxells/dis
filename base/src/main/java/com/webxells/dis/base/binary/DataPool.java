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
package com.webxells.dis.base.binary;

import com.webxells.dis.api.BinaryData;
import com.webxells.dis.api.error.InvalidApi;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class DataPool implements BinaryData {
    private static final Map<String, BinaryData> REGISTRY = new ConcurrentHashMap<>();
    private String index;

    public static void registerData(final String index, final BinaryData data) {
        REGISTRY.put(index, data);
    }

    public static boolean isPresent(final String index) {
        return REGISTRY.containsKey(index) && null != REGISTRY.get(index);
    }

    public static void remove(final String index) {
        REGISTRY.remove(index);
    }

    public DataPool() {}

    public DataPool(final String index) {
        setIndex(index);
    }

    @Override
    public void validate() throws InvalidApi {
        if (Objects.isNull(index)) {
            throw new InvalidApi("index is required");
        }
    }

    public void setIndex(final String index) {
        this.index = index;
    }

    @Override
    public InputStream getContent() throws IOException {
        if (REGISTRY.containsKey(index)) {
            return REGISTRY.get(index).getContent();
        }
        return null;
    }

    @Override
    public String getName() {
        if (REGISTRY.containsKey(index)) {
            return REGISTRY.get(index).getName();
        }
        return null;
    }

    @Override
    public String getMimeType() {
        if (REGISTRY.containsKey(index)) {
            return REGISTRY.get(index).getMimeType();
        }
        return null;
    }

    @Override
    public long getSize() {
        if (REGISTRY.containsKey(index)) {
            return REGISTRY.get(index).getSize();
        }
        return -1;
    }

}