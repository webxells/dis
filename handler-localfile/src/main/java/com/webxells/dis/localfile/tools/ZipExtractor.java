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
package com.webxells.dis.localfile.tools;

import com.webxells.dis.api.Logger;
import com.webxells.dis.api.error.InputOutputError;
import com.webxells.dis.logging.LoggerProxyFactory;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class ZipExtractor {
    private static final Logger LOGGER = LoggerProxyFactory.logger(ZipExtractor.class);

    private final Path extractedDir;

    public ZipExtractor(final Path tmpFileDir) throws InputOutputError {
        extractedDir = createTmpFile(tmpFileDir);
    }

    public Path extractFiles(final InputStream zipInput) throws IOException {
        return extractFiles(zipInput, Charset.defaultCharset());
    }

    public Path extractFiles(final InputStream zipInput, final Charset charset) throws IOException {
        LOGGER.d("Extracting files...");
        final ZipInputStream zipStream = new ZipInputStream(new BufferedInputStream(zipInput), charset);
        ZipEntry entry;
        while ((entry = zipStream.getNextEntry()) != null) {
            if (entry.isDirectory()) {
                final File newDir = extractedDir.resolve(entry.getName()).toFile();
                if (!newDir.exists() && newDir.mkdirs() && !newDir.exists()) {
                    LOGGER.w("Failed to create directory: " + newDir);
                }
                continue;
            }
            saveFile(entry, zipStream);
        }
        zipStream.close();
        return extractedDir;
    }

    private void saveFile(final ZipEntry entry, final ZipInputStream zipStream) throws IOException {
        final Path destination = extractedDir.resolve(entry.getName());
        final Path parent = destination.getParent();
        if (!Files.exists(parent)) {
            Files.createDirectories(parent);
        }
        LOGGER.t("Writing file: ".concat(entry.getName()));
        try (final FileOutputStream output = new FileOutputStream(destination.toFile())) {
            zipStream.transferTo(output);
        }
    }

    private Path createTmpFile(final Path tmpFileDir) throws InputOutputError {
        final Path tmpDir =
                tmpFileDir.resolve(String.format("%s_%d_%d", this.getClass().getSimpleName(),
                        System.identityHashCode(this),
                        System.nanoTime()));
        return Optional.of(tmpDir)
                .filter(a -> !Files.exists(a))
                .filter(a -> a.toFile().mkdir() || Files.exists(a))
                .orElseThrow(() -> new InputOutputError("Failed to create tmp directory: " + tmpDir));
    }
}