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
import java.io.InputStream;
import java.util.Optional;

public class RawData implements BinaryData {
    private final InputStream content;

    private String mimeType;
    private String name;
    private long size = -1;

    public RawData(final InputStream content) {
        this.content = content;
    }

    public RawData(final InputStream content, final String mimeType) {
        this(content);
        this.mimeType = mimeType;
    }

    @Override
    public InputStream getContent() {
        return content;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getMimeType() {
        return Optional.ofNullable(mimeType)
                .orElse(BinaryData.super.getMimeType());
    }

    @Override
    public long getSize() {
        return size;
    }

    public void setMimeType(final String mimeType) {
        this.mimeType = mimeType;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public void setSize(final long size) {
        this.size = size;
    }
}