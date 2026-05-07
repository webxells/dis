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
package com.webxells.dis.resource.sftp;

import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;
import com.webxells.dis.api.Logger;
import com.webxells.dis.api.config.description.Default;
import com.webxells.dis.api.config.description.Description;
import com.webxells.dis.api.error.InputOutputError;
import com.webxells.dis.logging.LoggerProxyFactory;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

@Description("Like SftpResource but saves files in temp directory")
public class LocallyStoredSftpResource extends SftpResource {
    private static final Logger LOGGER = LoggerProxyFactory.logger(LocallyStoredSftpResource.class);

    private final Map<LocallyStoredSftpResource, File> forDeletion = new HashMap<>();

    private LinkedList<File> files;
    @Description("Path to temp directory")
    @Default("Temporary directory of the operating system")
    private String localStoreDirectory = System.getProperty("java.io.tmpdir");


    @Override
    public RefreshResult refresh() {
        deleteFiles();
        if (null == files) {
            return RefreshResult.UNKNOWN;
        }
        if (files.isEmpty()) {
            return RefreshResult.NONE;
        }
        files.pop();
        return files.isEmpty() ? RefreshResult.NONE : RefreshResult.MORE;
    }

    @Override
    public void reset() {
        try {
            markLeftFilesForDeletion();
            deleteFiles();
            loadFileList();
            loadFiles();
            disconnect();
        } catch (final JSchException | SftpException e) {
            throw new RuntimeException("Loading files failed", e);
        }
    }

    private synchronized void markLeftFilesForDeletion() {
        if (null != files) {
            files.forEach(b -> forDeletion.put(this, b));
            files = null;
        }


    }

    private void loadFiles() {
        files = new LinkedList<>();
        filesLeft.stream()
                .parallel()
                .peek(a -> LOGGER.d("Loading next file: ".concat(a)))
                .map(this::toSftpInputStream)
                .forEach(this::copyToTempDirectory);
    }

    private void copyToTempDirectory(final InputStream inputStream) {
        try {
            final File temp = createTempFile(inputStream);
            files.add(temp);
            final OutputStream tempOutputStream = new BufferedOutputStream(new FileOutputStream(temp));
            inputStream.transferTo(tempOutputStream);
            LOGGER.d("Saved to: ".concat(temp.getAbsolutePath()));
            inputStream.close();
            tempOutputStream.close();
            temp.deleteOnExit();
        } catch (final IOException e) {
            throw new RuntimeException("Error while loading", e);
        }
    }

    private File createTempFile(final InputStream inputStream) {
        return new File(localStoreDirectory, String.format("sftp_resource_%s_%s_%s",
                System.identityHashCode(this), System.identityHashCode(inputStream),
                System.currentTimeMillis()));
    }

    private InputStream toSftpInputStream(final String s) {
            try {
                return getChannel().get(s, null, 0L);
            } catch (final JSchException | SftpException e) {
                throw new RuntimeException("Error while fetching", e);
            }
    }

    @Override
    public InputStream receive() throws InputOutputError {
        final File next = files.getFirst();
        forDeletion.put(this, next);
        try {
            return new BufferedInputStream(new FileInputStream(next));
        } catch (final FileNotFoundException e) {
            throw new InputOutputError("Could not fetch next file", e);
        }
    }

    private synchronized void deleteFiles() {
        forDeletion.forEach((a, b) -> b.delete());
        forDeletion.clear();
    }

    @Override
    public String getType() {
        return LocallyStoredSftpResource.class.getName();
    }

    public void setLocalStoreDirectory(final String localStoreDirectory) {
        this.localStoreDirectory = localStoreDirectory;
    }
}