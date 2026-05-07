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

import com.webxells.dis.api.error.InputOutputError;
import com.webxells.dis.api.error.InvalidApi;
import com.webxells.dis.api.resource.MultiResource;
import com.webxells.dis.api.resource.NameProvidingResource;
import com.webxells.dis.api.resource.ParentResource;
import com.webxells.dis.api.resource.Resource;
import com.webxells.dis.api.resource.SizeProvidingResource;
import com.webxells.dis.api.resource.TypeProvidingResource;
import com.webxells.dis.localfile.tools.ZipExtractor;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class ZipHandling implements ParentResource, MultiResource, SizeProvidingResource, NameProvidingResource, TypeProvidingResource {
    private Resource zipReceiver;
    private MultipleLocalFiles multipleLocalFiles;
    private Path tmpFileDir = Path.of(System.getProperty("java.io.tmpdir"));
    private Path extractedDir;
    private List<String> zipFilterPaths = List.of("**");
    private int maxZipDepth = 9999;
    private Charset charset = Charset.defaultCharset();
    private String fileNameStoreIndex;

    @Override
    public void validate() throws InvalidApi {
        if (null == zipReceiver) {
            throw new InvalidApi("zipReceiver is null");
        }
        if (!(Files.isDirectory(tmpFileDir) && Files.isWritable(tmpFileDir))) {
            throw new InvalidApi("Invalid tmpDirectory: " + tmpFileDir.toAbsolutePath());
        }
    }

    @Override
    public RefreshResult refresh() {
        assertExtractedSilently();
        return multipleLocalFiles.refresh();
    }

    @Override
    public void reset() {
        multipleLocalFiles = null;
    }

    @Override
    public OutputStream send() throws InputOutputError {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public List<Resource> getChildren() {
        return List.of(zipReceiver);
    }

    @Override
    public InputStream receive() throws InputOutputError {
        try {
            assertExtracted();
        } catch (final IOException | InvalidApi e) {
            throw new InputOutputError("Could not extract files", e);
        }
        return multipleLocalFiles.receive();
    }

    @Override
    public String getResourceName() {
        assertExtractedSilently();
        return multipleLocalFiles.getResourceName();
    }

    @Override
    public long getSize() {
        assertExtractedSilently();
        return multipleLocalFiles.getSize();
    }

    @Override
    public String getMimeType() {
        assertExtractedSilently();
        return multipleLocalFiles.getMimeType();
    }

    public void setTmpFileDir(final String tmpFileDir) {
        this.tmpFileDir = Path.of(tmpFileDir);
    }

    public void setZipReceiver(final Resource zipReceiver) {
        this.zipReceiver = zipReceiver;
    }

    public void setZipFilterPaths(final List<String> zipFilterPaths) {
        this.zipFilterPaths = zipFilterPaths;
    }

    public void setFileNameStoreIndex(final String fileNameStoreIndex) {
        this.fileNameStoreIndex = fileNameStoreIndex;
    }

    public void setMaxZipDepth(final int maxZipDepth) {
        this.maxZipDepth = maxZipDepth;
    }

    public void setCharset(final String charset) {
        this.charset = Charset.forName(charset);
    }

    private void assertExtractedSilently() {
        try {
            assertExtracted();
        } catch (final InputOutputError | IOException | InvalidApi e) {
            throw new RuntimeException("Could not extract files", e);
        }
    }

    private void assertExtracted() throws InputOutputError, IOException, InvalidApi {
        if (null == multipleLocalFiles) {
            final ZipExtractor zipExtractor = new ZipExtractor(tmpFileDir);
            extractedDir = zipExtractor.extractFiles(zipReceiver.receive(), charset);
            createMultipleLocalFiles();
        }
    }

    private void createMultipleLocalFiles() throws InvalidApi {
        final MultipleLocalFiles result = new MultipleLocalFiles();
        result.setGlobbing(true);
        result.setFileNameStoreIndex(fileNameStoreIndex);
        result.setGlobbingMaxDepth(maxZipDepth);
        result.setPaths(zipFilterPaths.stream()
                .map(a -> String.format("%s%s%s", extractedDir.toAbsolutePath(), File.separator, a))
                .toList());
        result.validate();
        multipleLocalFiles = result;
    }
}