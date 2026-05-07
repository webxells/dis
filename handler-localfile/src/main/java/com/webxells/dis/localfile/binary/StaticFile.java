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
package com.webxells.dis.localfile.binary;

import com.webxells.dis.api.BinaryData;
import com.webxells.dis.api.config.description.Description;
import com.webxells.dis.api.config.description.Required;
import com.webxells.dis.api.error.InvalidApi;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

@Description("Loads a single local file")
public class StaticFile implements BinaryData {
    private File file;

    @Override
    public void validate() throws InvalidApi {
        if (null == file || !file.isFile() || !file.canRead()) {
            throw new InvalidApi("path should point to valid readable file");
        }
    }

    @Override
    public String getType() {
        return StaticFile.class.getName();
    }

    @Description("Path to file")
    @Required
    public void setPath(final String path) {
        file = new File(path);
    }

    @Override
    public InputStream getContent() throws IOException {
        return new BufferedInputStream(new FileInputStream(file));
    }

    @Override
    public String getName() {
        return file.getName();
    }

    @Override
    public String getMimeType() {
        try {
            final String result = Files.probeContentType(file.toPath());
            if (null != result) {
                return result;
            }
        } catch (final IOException ignored) {}
        return "application/octet-stream";
    }

    @Override
    public long getSize() {
        return file.length();
    }
}