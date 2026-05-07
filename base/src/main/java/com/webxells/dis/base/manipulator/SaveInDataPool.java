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

import com.webxells.dis.api.BinaryData;
import com.webxells.dis.api.DatasetPiece;
import com.webxells.dis.api.Logger;
import com.webxells.dis.api.config.MappingPart;
import com.webxells.dis.api.config.description.Default;
import com.webxells.dis.api.config.description.Description;
import com.webxells.dis.api.config.description.Required;
import com.webxells.dis.api.error.InvalidApi;
import com.webxells.dis.api.error.InvalidDatasetException;
import com.webxells.dis.api.manipulator.Manipulator;
import com.webxells.dis.api.validator.Validator;
import com.webxells.dis.base.binary.DataPool;
import com.webxells.dis.base.binary.RawData;
import com.webxells.dis.logging.LoggerProxyFactory;
import java.io.ByteArrayInputStream;
import java.util.Optional;

@Description("Saves current DatasetContent into DataPool")
public class SaveInDataPool implements Validator, Manipulator {
    public enum DoubletStrategy {
        @Description("Raises error") ERROR,
        @Description("Keeps old entry") IGNORE,
        @Description("Replace old entry with current") OVERWRITE
    }

    private static final Logger LOGGER = LoggerProxyFactory.logger(SaveInDataPool.class);

    @Description("Index to use")
    @Required
    private String index;
    @Description("What to do if index already taken")
    @Default("OVERWRITE")
    private DoubletStrategy doubletStrategy = DoubletStrategy.OVERWRITE;
    @Description("name for data")
    private String name;
    @Description("type for data")
    private String mimeType;
    @Description("resets DatasetContent after transformation (cut for copy)")
    private boolean resetPart;

    @Override
    public void validate() throws InvalidApi {
        if (null == index) {
            throw new InvalidApi("index is required");
        }
    }

    @Override
    public void manipulate(final DatasetPiece datasetPiece, final MappingPart mappingPart) throws InvalidDatasetException {
        saveData(datasetPiece);
    }

    @Override
    public boolean validate(final DatasetPiece datasetPiece, final MappingPart mappingPart) {
        try {
            saveData(datasetPiece);
        } catch (final InvalidDatasetException e) {
            LOGGER.error(e);
            return false;
        }
        return true;
    }

    private void saveData(final DatasetPiece datasetPiece) throws InvalidDatasetException {
        if (datasetPiece.value().isPresent()) {
            final String content = datasetPiece.value().get();
            if (resetPart) {
                datasetPiece.rewriteValue(null);
            }
            if (DataPool.isPresent(index)) {
                switch (doubletStrategy) {
                    case ERROR:
                        throw new InvalidDatasetException("Index already used: " + index);
                    case IGNORE:
                        return;
                }
            }
            DataPool.registerData(index, createData(content));
        } else {
            DataPool.remove(index);
        }
    }

    private BinaryData createData(final String content) {
        final RawData result = new RawData(new ByteArrayInputStream(content.getBytes()));
        Optional.ofNullable(name)
                .ifPresent(result::setName);
        Optional.ofNullable(mimeType)
                .ifPresent(result::setMimeType);
        result.setSize(content.length());
        return result;
    }

    public void setIndex(final String index) {
        this.index = index;
    }

    public void setDoubletStrategy(final DoubletStrategy doubletStrategy) {
        this.doubletStrategy = doubletStrategy;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public void setMimeType(final String mimeType) {
        this.mimeType = mimeType;
    }

    public void setResetPart(final boolean resetPart) {
        this.resetPart = resetPart;
    }
}