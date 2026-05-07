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
import com.webxells.dis.api.validator.Validator;
import java.io.File;
import java.util.Optional;

@Description("Creates a directory")
public class MakeDir implements Validator {
    @Description("File to check - use $current to place current dataset piece")
    @Default("$current")
    private String path;
    private boolean createPath;

    @Override
    public boolean validate(final DatasetPiece currentPiece, final MappingPart mappingPart) {
        final File dir = new File(getPath(currentPiece));
        if (!dir.exists()) {
            if (createPath) {
                return dir.mkdirs();
            }
            return dir.mkdir();
        }
        return true;
    }

    private String getPath(final DatasetPiece currentPiece) {
        return Optional.ofNullable(path)
                .orElse("$current")
                .replaceAll("\\$current", currentPiece.value().orElse(""));
    }

    public void setPath(final String path) {
        this.path = path;
    }

    public void setCreatePath(final boolean createPath) {
        this.createPath = createPath;
    }
}