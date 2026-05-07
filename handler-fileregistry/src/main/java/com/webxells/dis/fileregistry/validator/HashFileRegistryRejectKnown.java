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
package com.webxells.dis.fileregistry.validator;

import com.webxells.dis.api.DatasetPiece;
import com.webxells.dis.api.Logger;
import com.webxells.dis.api.config.MappingPart;
import com.webxells.dis.api.config.description.Default;
import com.webxells.dis.api.config.description.Description;
import com.webxells.dis.api.validator.SingleCallForAllValuesValidator;
import com.webxells.dis.logging.LoggerProxyFactory;
import com.webxells.dis.fileregistry.internal.Murmur3HashFileRegistry;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.time.LocalDateTime;

@Description("Asserts that data is unknown of file registry")
public class HashFileRegistryRejectKnown extends Murmur3HashFileRegistry implements SingleCallForAllValuesValidator {
    private final static Logger LOGGER = LoggerProxyFactory.logger(HashFileRegistryRejectKnown.class);

    @Description("Skips validation of complete mapping configuration")
    @Default("false")
    private boolean skipVersionValidation;

    @Override
    public boolean validate(final DatasetPiece currentPiece, final MappingPart mappingPart) {
        final File file = createFile(createFileName(mappingPart));
        try {
            if (file.exists()) {
                return !skipVersionValidation && checkFileForVersion(file, mappingPart);
            }
            LOGGER.t("No hash file found");
            saveCurrentVersion(file, mappingPart);
        } catch (IOException e) {
            LOGGER.e("file error regarding file hash registry", e);
        }
        return true;
    }

    private boolean checkFileForVersion(final File file, final MappingPart mappingPart) throws IOException {
        final String hash = createDataHash(mappingPart.getConfiguration());
        final String lastEntry = parseLastLine(file);
        if (lastEntry.equals(hash)) {
            LOGGER.t("Hashes are equal - no update required");
            return false;
        }
        LOGGER.t("Hashes differ - update required");
        saveCurrentVersion(file, hash);
        return true;
    }

    private void saveCurrentVersion(final File file, final MappingPart mappingPart) throws IOException {
        saveCurrentVersion(file, createDataHash(mappingPart.getConfiguration()));
    }

    private void saveCurrentVersion(final File file, final String hash) throws IOException {
        final FileOutputStream outputStream = new FileOutputStream(file, true);
        outputStream.write(String.format("%s => %s%s", LocalDateTime.now(), hash, LINE_SEPARATOR).getBytes());
        outputStream.close();
    }

    private String parseLastLine(final File file) throws IOException {
        String lastLine = readLastLine(file);
        return lastLine.contains("=>") ? lastLine.substring(lastLine.indexOf("=>") + 3) : "";
    }

    private String readLastLine(final File file) throws IOException {
        RandomAccessFile reader = new RandomAccessFile(file, "r");
        StringBuilder lastLine = new StringBuilder();
        long i = file.length() - LINE_SEPARATOR.length();
        while (i > 0) {
            reader.seek(--i);
            char current = (char) reader.readByte();
            if (current == '\n' || current == '\r') {
                break;
            }
            lastLine.append(current);
        }
        reader.close();
        return lastLine.reverse().toString();
    }

    @Override
    public String getType() {
        return HashFileRegistryRejectKnown.class.getName();
    }

    public void setSkipVersionValidation(final boolean skipVersionValidation) {
        this.skipVersionValidation = skipVersionValidation;
    }
}