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
package com.webxells.dis.localfile.resource;

import com.webxells.dis.api.config.description.Description;
import com.webxells.dis.api.config.description.Required;
import com.webxells.dis.api.error.InputOutputError;
import com.webxells.dis.api.resource.MultiResource;
import com.webxells.dis.api.resource.NameProvidingResource;
import com.webxells.dis.api.resource.SizeProvidingResource;
import com.webxells.dis.api.resource.TypeProvidingResource;
import com.webxells.dis.localfile.FileNameQueue;
import com.webxells.dis.localfile.FileNameStore;
import com.webxells.dis.localfile.intern.WiseFileNameMap;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLConnection;
import java.util.Optional;

@Description("Loads current file from LocalFileEvent trigger")
public class FileEventListenerResource implements MultiResource, SizeProvidingResource, NameProvidingResource, TypeProvidingResource {
    static {{
        WiseFileNameMap.initialize();
    }}

    @Description("Index of LocalFileEvent trigger")
    @Required
    private String index;
    private File currentFile;

    @Override
    public OutputStream send() {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public InputStream receive() throws InputOutputError {
        try {
            return null == currentFile ? null : new FileInputStream(currentFile);
        } catch (final FileNotFoundException e) {
            throw new InputOutputError("Could not read from file", e);
        }
    }

    @Override
    public RefreshResult refresh() {
        String currentStringFile = FileNameQueue.getNext(index);
        FileNameStore.setPath(index, currentStringFile);
        if (null == currentStringFile) {
            currentFile = null;
            return RefreshResult.NONE;
        }
        currentFile = new File(currentStringFile);
        return RefreshResult.MORE;
    }

    @Override
    public void reset() {
        refresh();
    }

    @Override
    public long getSize() {
        return Optional.ofNullable(currentFile)
                .map(File::length)
                .orElse(-1L);
    }

    public void setIndex(final String index) {
        this.index = index;
    }

    @Override
    public String getResourceName() {
        return Optional.ofNullable(currentFile)
                .map(File::getName)
                .orElse(null);
    }

    @Override
    public String getMimeType() {
        if (null == currentFile) {
            return null;
        }
        return Optional.ofNullable(getResourceName())
                .map(URLConnection::guessContentTypeFromName)
                .orElse("application/octet-stream");
    }
}