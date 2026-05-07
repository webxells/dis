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
package com.webxells.dis.localfile.validator;

import com.webxells.dis.api.DatasetPiece;
import com.webxells.dis.api.config.MappingPart;
import com.webxells.dis.api.config.description.Default;
import com.webxells.dis.api.config.description.Description;
import com.webxells.dis.base.validator.SimpleValidator;
import java.io.File;
import java.util.Optional;

@Description("Checks if a local file exists")
public class FileExists extends SimpleValidator {
    @Description("File to check - use $current to place current dataset piece")
    @Default("$current")
    private String path;
    @Description("Asserts that file is really a file")
    private boolean assertIsFile;
    @Description("Asserts that file is a directory")
    private boolean assertIsDirectory;

    @Override
    protected boolean validateCurrentPiece(final DatasetPiece currentPiece, final MappingPart mappingPart) {
        final File file = new File(getPath(currentPiece));
        return file.exists() && (!assertIsFile || file.isFile()) &&
                (!assertIsDirectory || file.isDirectory());
    }

    private String getPath(final DatasetPiece currentPiece) {
        return Optional.ofNullable(path)
                .orElse("$current")
                .replaceAll("\\$current", currentPiece.value().orElse(""));
    }

    public void setPath(final String path) {
        this.path = path;
    }

    public void setAssertIsFile(final boolean assertIsFile) {
        this.assertIsFile = assertIsFile;
    }

    public void setAssertIsDirectory(final boolean assertIsDirectory) {
        this.assertIsDirectory = assertIsDirectory;
    }
}