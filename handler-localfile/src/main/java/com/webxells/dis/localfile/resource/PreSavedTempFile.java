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
package com.webxells.dis.localfile.resource;

import com.webxells.dis.api.Logger;
import com.webxells.dis.api.config.description.Default;
import com.webxells.dis.api.config.description.Description;
import com.webxells.dis.api.error.InputOutputError;
import com.webxells.dis.api.resource.Resource;
import com.webxells.dis.api.resource.SizeProvidingResource;
import com.webxells.dis.localfile.input.LocalPreSavedInputStream;
import com.webxells.dis.logging.LoggerProxyFactory;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Objects;
import java.util.Optional;

@Description("Saves received data in a temporary file")
public class PreSavedTempFile implements SizeProvidingResource {
    private record FakeResource(InputStream stream) implements Resource {
        @Override
        public OutputStream send() {
            throw new UnsupportedOperationException("makes no sense");
        }

        @Override
        public InputStream receive() {
            return stream;
        }
    }

    private static final Logger LOGGER = LoggerProxyFactory.logger(PreSavedTempFile.class);

    @Description("Actual Resource")
    private Resource receiver;
    @Description("Path to temporary directory")
    @Default("Temporary directory path of Java")
    private String locallyPreSaveDirectory = System.getProperty("java.io.tmpdir");

    private LocalPreSavedInputStream localPreSavedInputStream;

    public static LocalPreSavedInputStream createPreSavedInputStream(final InputStream inputStream, final String locallyPreSaveDirectory)
            throws InputOutputError {
        final FakeResource fake = new FakeResource(inputStream);
        final PreSavedTempFile self = new PreSavedTempFile();
        self.setReceiver(fake);
        self.setLocallyPreSaveDirectory(locallyPreSaveDirectory);
        return self.receive();
    }

    @Override
    public long getSize() {
        try {
            return Optional.ofNullable(getLocalInputStream())
                    .map(LocalPreSavedInputStream::getFile)
                    .map(File::length)
                    .orElse(-1L);
        } catch (final InputOutputError e) {
            LOGGER.error("InputStream could not fetch from receiver", e);
        }
        return -1;
    }

    @Override
    public OutputStream send() {
        throw new UnsupportedOperationException();
    }

    @Override
    public LocalPreSavedInputStream receive() throws InputOutputError {
        return getLocalInputStream();
    }

    public Resource getReceiver() {
        return receiver;
    }

    public void setReceiver(final Resource receiver) {
        this.receiver = receiver;
    }

    public void setLocallyPreSaveDirectory(final String locallyPreSaveDirectory) {
        this.locallyPreSaveDirectory = locallyPreSaveDirectory;
    }

    private LocalPreSavedInputStream getLocalInputStream() throws InputOutputError {
        if (Objects.isNull(localPreSavedInputStream) || !localPreSavedInputStream.isOpen()) {
            try {
                this.localPreSavedInputStream = new LocalPreSavedInputStream(receiver.receive(), locallyPreSaveDirectory);
            } catch (final IOException e) {
                throw new InputOutputError("Unable to save to tmp file", e);
            }
        }

        return localPreSavedInputStream;
    }
}