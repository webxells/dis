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
package com.webxells.dis.localfile;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class FileNameStore {
    private static final Map<String, String> STORE = new HashMap<>();

    public static String getPath(final String index) {
        return STORE.get(index);
    }

    public static void setPath(final String index, final String path) {
        STORE.put(index, path);
    }

    public static String getFileName(final String index) {
        final String path = getPath(index);
        if (null != path && path.lastIndexOf(File.separator) < path.length() - 1) {
            return path.substring(path.lastIndexOf(File.separator) + 1);
        }
        return null;
    }

    public static String getFileNameWithoutType(final String index) {
        final String fileName = getFileName(index);
        if (null != fileName && fileName.lastIndexOf('.') > 0) {
            return fileName.substring(0, fileName.lastIndexOf('.'));
        }
        return null;
    }

    public static String getFileType(final String index) {
        final String fileName = getFileName(index);
        if (null != fileName && fileName.lastIndexOf('.') < fileName.length()) {
            return fileName.substring(fileName.lastIndexOf('.') + 1);
        }
        return null;
    }

    /*
     * @todo: refactor to in fact parsing :D
     */
    public static String parseFileName(final String fileNameStoreIndex, String fileName) {
        if (!STORE.containsKey(fileNameStoreIndex)) {
            return fileName;
        }
        if (fileName.contains("$TYPE")) {
            fileName = fileName.replace("$TYPE",
                    Optional.ofNullable(getFileType(fileNameStoreIndex)).orElse(""));
        }
        if (fileName.contains("$FILE_LONG")) {
            fileName = fileName.replace("$FILE_LONG",
                    Optional.ofNullable(getFileName(fileNameStoreIndex)).orElse(""));
        }
        if (fileName.contains("$FILE")) {
            fileName = fileName.replace("$FILE",
                    Optional.ofNullable(getFileNameWithoutType(fileNameStoreIndex)).orElse(""));
        }
        return fileName;
    }
}
