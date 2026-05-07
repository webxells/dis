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

import com.webxells.dis.api.config.description.Default;
import com.webxells.dis.api.config.description.Description;
import com.webxells.dis.api.config.description.Required;
import com.webxells.dis.api.error.InputOutputError;
import com.webxells.dis.api.error.InvalidApi;
import com.webxells.dis.api.resource.NameProvidingResource;
import com.webxells.dis.api.resource.SizeProvidingResource;
import com.webxells.dis.api.resource.TypeProvidingResource;
import com.webxells.dis.localfile.FileNameStore;
import com.webxells.dis.localfile.intern.WiseFileNameMap;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLConnection;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

@Description("Access of single local file")
public class LocalFile implements SizeProvidingResource, NameProvidingResource, TypeProvidingResource {
    static {{
        WiseFileNameMap.initialize();
    }}

    @Required
    @Description("Path of file")
    private String path;
    @Description("Adds file name to path")
    private String fileName;
    @Description("Writes file path into internal file storage")
    private String fileNameStoreIndex;
    @Default("false")
    @Description("Parses java time inside filename for sending")
    private boolean fileNameContainsDateFormat;
    @Description("Creates file with given name if it doesn't exist")
    @Default("false")
    private boolean createIfNotExists;
    @Description("Appends data to existing file")
    @Default("false")
    private boolean append;

    public LocalFile() { }

    public LocalFile(String path) {
        this.path = path;
    }

    @Override
    public void validate() throws InvalidApi {
        if (null == path) {
            throw new InvalidApi("path required (file)");
        }
        final File parsedPath = createPath();
        if (!createIfNotExists && !(parsedPath.exists() && parsedPath.isFile())) {
            throw new InvalidApi("Could not find file: ".concat(parsedPath.getAbsolutePath()));
        }
    }

    @Override
    public OutputStream send() throws InputOutputError{
        try {
            return new FileOutputStream(createPath(), append);
        } catch (final FileNotFoundException e) {
            throw new InputOutputError("Could not create file output stream", e);
        }
    }

    public File createPath() {
        final StringBuilder result = new StringBuilder(path);
        if (null != fileName) {
            if (result.length() - 1 != result.lastIndexOf(File.separator)) {
                result.append(File.separator);
            }
            result.append(null == fileNameStoreIndex ?
                    parseDate(fileName) : FileNameStore.parseFileName(fileNameStoreIndex, fileName));

        }
        return new File(result.toString());
    }

    private String parseDate(final String fileName) {
        if (fileNameContainsDateFormat) {
            return LocalDateTime.now().format(DateTimeFormatter.ofPattern(fileName));
        }
        return fileName;
    }

    @Override
    public InputStream receive() throws InputOutputError {
        try {
            return new FileInputStream(path);
        } catch (final FileNotFoundException e) {
            throw new InputOutputError("Could not create file resource stream", e);
        }
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }

    @Override
    public String getType() {
        return LocalFile.class.getName();
    }

    public void setFileName(final String fileName) {
        this.fileName = fileName;
    }

    public void setFileNameStoreIndex(final String fileNameStoreIndex) {
        this.fileNameStoreIndex = fileNameStoreIndex;
    }

    @Override
    public String toString() {
        return String.format("LocalFile[%s]", createPath());
    }

    public void setAppend(final boolean append) {
        this.append = append;
    }

    public void setFileNameContainsDateFormat(final boolean fileNameContainsDateFormat) {
        this.fileNameContainsDateFormat = fileNameContainsDateFormat;
    }

    @Override
    public long getSize() {
        return createPath().length();
    }

    @Override
    public String getResourceName() {
        return createPath().getName();
    }

    @Override
    public String getMimeType() {
        return Optional.ofNullable(
                Optional.ofNullable(fileName).orElse(getResourceName()))
                .map(URLConnection::guessContentTypeFromName)
                .orElse(null);
    }

    public void setCreateIfNotExists(final boolean createIfNotExists) {
        this.createIfNotExists = createIfNotExists;
    }
}