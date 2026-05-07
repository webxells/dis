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
package com.webxells.dis.base.manipulator;

import com.webxells.dis.api.DatasetPiece;
import com.webxells.dis.api.MappingPortrayal;
import com.webxells.dis.api.config.MappingPart;
import com.webxells.dis.api.config.description.Description;
import com.webxells.dis.api.config.description.Required;
import com.webxells.dis.api.error.InvalidApi;
import com.webxells.dis.api.error.InvalidDatasetException;
import com.webxells.dis.api.manipulator.Manipulator;
import com.webxells.dis.base.binary.DataPool;
import com.webxells.dis.base.binary.RawData;
import java.io.ByteArrayInputStream;
import java.io.InputStream;

@Description("Converts a hex file into binary and puts it into the data pool")
public class Hex2Binary implements Manipulator {
    @Description("MappingPart with the file name")
    private MappingPortrayal namePortrayal;
    @Description("MappingPart with the file type")
    private MappingPortrayal typePortrayal;
    @Required
    @Description("Identifier for the data pool")
    private String index;

    @Override
    public void validate() throws InvalidApi {
        if (null == index) {
            throw new InvalidApi("field index required");
        }
    }

    @Override
    public void manipulate(final DatasetPiece currentPiece, final MappingPart mappingPart) throws InvalidDatasetException {
        final String fileType = getByConfig(mappingPart, typePortrayal);
        final String fileName = getByConfig(mappingPart, namePortrayal);
        final int length = currentPiece.value().orElse("").length();
        final RawData rawData = new RawData(createBinaryStream(currentPiece, length), fileType);
        rawData.setName(fileName);
        rawData.setSize(length / 2);
        DataPool.registerData(index, rawData);
        currentPiece.rewriteValue(null);
    }

    public void setNamePortrayal(final MappingPortrayal namePortrayal) {
        this.namePortrayal = namePortrayal;
    }

    public void setTypePortrayal(final MappingPortrayal typePortrayal) {
        this.typePortrayal = typePortrayal;
    }

    public void setIndex(final String index) {
        this.index = index;
    }

    private String getByConfig(final MappingPart mappingPart, final MappingPortrayal portrayal) {
        return null == portrayal ? null : mappingPart.getConfiguration().getByPortrayal(portrayal)
                .flatMap(MappingPart::value)
                .orElse(null);
    }

    private InputStream createBinaryStream(final DatasetPiece currentPiece, final int length) throws InvalidDatasetException {
        final String content = currentPiece.value().orElse(null);
        if (null == content || length % 2 > 0) {
            throw new InvalidDatasetException("Empty or invalid  hex");
        }
        final byte[] result = new byte[length / 2];
        for (int i = length; i > 0;) {
            result[i / 2 - 1] = (byte) (toByte(content.charAt(--i)) | (toByte(content.charAt(--i)) << 4));
        }
        return new ByteArrayInputStream(result);
    }

    private int toByte(final char character) throws InvalidDatasetException {
        if (isNoHexChar(character)) {
            throw new InvalidDatasetException("Invalid character: ".concat(String.valueOf(character)));
        }
        return Character.digit(character, 16);
    }

    private boolean isNoHexChar(final char character) {
        return character < 48 ||
                (character > 57 && character < 65) ||
                (character > 70 && character < 97) ||
                character > 102;
    }

}
