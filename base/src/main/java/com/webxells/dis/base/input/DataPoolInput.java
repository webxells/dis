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
package com.webxells.dis.base.input;

import com.webxells.dis.api.BinaryData;
import com.webxells.dis.api.MappingPortrayal;
import com.webxells.dis.api.config.MappingConfiguration;
import com.webxells.dis.api.config.MappingPart;
import com.webxells.dis.api.error.InputOutputError;
import com.webxells.dis.api.input.Input;
import com.webxells.dis.api.resource.NameProvidingResource;
import com.webxells.dis.api.resource.Resource;
import com.webxells.dis.api.resource.SizeProvidingResource;
import com.webxells.dis.api.resource.TypeProvidingResource;
import com.webxells.dis.base.binary.DataPool;
import com.webxells.dis.base.binary.RawData;
import java.io.InputStream;
import java.util.Optional;

public class DataPoolInput implements Input<DataPoolConfiguration> {
    private final Resource receiver;
    private final String name;
    private final String fileType;
    private final String fileName;
    private final MappingPortrayal fileNamePortrayal;
    private final MappingPortrayal fileTypePortrayal;
    private boolean more;

    public DataPoolInput(final DataPoolConfiguration config) {
        receiver = config.getReceiver();
        name = config.getName();
        fileType = config.getFileType();
        fileName = config.getFileName();
        fileNamePortrayal = config.getFileNamePortrayal();
        fileTypePortrayal = config.getFileTypePortrayal();
    }

    @Override
    public int read(final MappingConfiguration from) throws InputOutputError {
        if (more) {
            Optional.ofNullable(receiver.receive())
                    .ifPresent(a -> DataPool.registerData(name, createRawData(a,
                            receiver instanceof SizeProvidingResource ?
                                    ((SizeProvidingResource) receiver).getSize() : null, from)));
            more = false;
            return 1;
        }
        return 0;
    }

    private BinaryData createRawData(final InputStream dataStream, final Long size, final MappingConfiguration from) {
        final RawData result = new RawData(dataStream, fileType);
        if (null != fileName) {
            result.setName(fileName);
        } else if (receiver instanceof NameProvidingResource) {
            result.setName(((NameProvidingResource) receiver).getResourceName());
        } else if (null != fileNamePortrayal) {
            result.setName(from.getByPortrayal(fileNamePortrayal).flatMap(MappingPart::value).orElse(null));
        }
        if (receiver instanceof TypeProvidingResource && null == fileType) {
            result.setMimeType(((TypeProvidingResource) receiver).getMimeType());
        } else if (null != fileTypePortrayal) {
            result.setMimeType(from.getByPortrayal(fileTypePortrayal)
                    .flatMap(MappingPart::value).orElse(null));
        }
        Optional.ofNullable(size).ifPresent(result::setSize);
        return result;
    }

    @Override
    public boolean hasNext() {
        return more;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void start() {
        more = true;
    }

    @Override
    public void end() { }
}