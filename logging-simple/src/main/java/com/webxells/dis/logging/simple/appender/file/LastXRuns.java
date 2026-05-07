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
import java.io.IOException;

public class LastXRuns extends CompressingAbandonedFileRotation {
    public int x = 5;

    @Override
    public void start(final String path) {
        if (!handleFile(path, 0)) {

        }
        super.start(path);
    }

    private boolean handleFile(final String path, final int i) {
        final File current = getFile(path, i);
        if (current.isFile() && i < x) {
            if (i == x - 1) {
                return current.delete();
            } else {
                return handleFile(path, i + 1) && rename(current, path, i);
            }
        }
        return true;
    }

    private boolean rename(final File current, final String path, final int i) {
        if (compressAbandonedFiles && i == 0) {
            try {
                compressFile(current.getAbsolutePath(), createXName(path, i + 1).getAbsolutePath());
                return true;
            } catch (final IOException e) {
                throw new RuntimeException("Could not compress file", e);
            }
        } else {
            return current.renameTo(createXName(path, i + 1));
        }
    }

    private File getFile(final String path, final int i) {
        return i == 0 ? new File(path) : createXName(path, i);
    }

    private File createXName(final String path, final int i) {
        int pos = path.lastIndexOf('.');
        if (pos < 0) {
            pos = path.length();
        }
        return new File(String.format("%s-%d%s%s", path.substring(0, pos), i, path.substring(pos), compressAbandonedFiles ? ".gz" : ""));
    }
}