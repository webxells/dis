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
package com.webxells.dis.logging.simple.appender.file;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.GZIPOutputStream;

public abstract class CompressingAbandonedFileRotation extends NoRotation {
    public boolean compressAbandonedFiles;

    protected void compressFile(final String path) throws IOException {
        compressFile(path, path.concat(".gz"));
    }
    protected void compressFile(final String sourcePath, final String destinyPath) throws IOException {
        if (compressAbandonedFiles) {
            final File sourceFile = new File(sourcePath);
            try (final FileInputStream source = new FileInputStream(sourceFile);
                 final GZIPOutputStream destiny = new GZIPOutputStream(new FileOutputStream(destinyPath))) {
                source.transferTo(destiny);
            }
            if (!sourceFile.delete()) {
                throw new IOException("Could not delete file: ".concat(sourcePath));
            }
        }
    }

}