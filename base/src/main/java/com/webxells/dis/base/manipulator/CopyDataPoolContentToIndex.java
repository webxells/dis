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
import com.webxells.dis.api.config.MappingPart;
import com.webxells.dis.api.config.description.Description;
import com.webxells.dis.api.config.description.Required;
import com.webxells.dis.api.error.InvalidApi;
import com.webxells.dis.api.error.InvalidDatasetException;
import com.webxells.dis.api.manipulator.Manipulator;
import com.webxells.dis.base.binary.DataPool;
import com.webxells.dis.base.binary.RawData;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

@Description("Copies data from one data pool index to new one")
public class CopyDataPoolContentToIndex implements Manipulator {

    @Required
    @Description("Already holds data")
    private String registeredIndex;

    @Required
    @Description("New index that gets the same data as the originalIndex")
    private String copyIndex;

    @Override
    public void validate() throws InvalidApi {
        if (null == registeredIndex || null == copyIndex) {
            throw new InvalidApi("Required fields are missing");
        }
    }

    @Override
    public void manipulate(final DatasetPiece datasetPiece, final MappingPart mappingPart) throws InvalidDatasetException {
        final DataPool dataPool = new DataPool();
        dataPool.setIndex(registeredIndex);

        final byte[] content = readOriginalContent(dataPool);

        registerData(registeredIndex, dataPool, content);
        registerData(copyIndex, dataPool, content);
    }

    private void registerData(final String index, final DataPool dataPool, final byte[] content) {
        final RawData rawData = new RawData(new ByteArrayInputStream(content), dataPool.getMimeType());
        rawData.setName(dataPool.getName());
        rawData.setSize(dataPool.getSize());
        DataPool.registerData(index, rawData);
    }

    private byte[] readOriginalContent(final DataPool dataPool) throws InvalidDatasetException {
        InputStream inputStream;
        byte[] content;

        try {
            inputStream = dataPool.getContent();
            content = inputStream.readAllBytes();
        } catch (IOException e) {
            throw new InvalidDatasetException("Content of registered index could not be loaded", e);
        }

        if (content.length == 0) {
            throw new InvalidDatasetException("Content of registered index was already read");
        }

        return content;
    }

    public void setRegisteredIndex(final String registeredIndex) {
        this.registeredIndex = registeredIndex;
    }

    public void setCopyIndex(final String copyIndex) {
        this.copyIndex = copyIndex;
    }
}
