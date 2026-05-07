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
package com.webxells.dis.officex;

import com.webxells.dis.api.Logger;
import com.webxells.dis.api.config.MappingConfiguration;
import com.webxells.dis.api.config.MappingPart;
import com.webxells.dis.api.error.InputOutputError;
import com.webxells.dis.api.error.InvalidApi;
import com.webxells.dis.api.input.Input;
import com.webxells.dis.api.resource.Resource;
import com.webxells.dis.base.SimpleMappingPortrayal;
import com.webxells.dis.localfile.tools.ZipExtractor;
import com.webxells.dis.logging.LoggerProxyFactory;
import com.webxells.dis.officex.internal.FileContext;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;

public abstract class Reader<T extends ReaderConfig> implements Input<T> {
    private static final Logger LOGGER = LoggerProxyFactory.logger(Reader.class);

    private final String name;
    private final Resource receiver;
    private final Path tmpFileDir;

    private Path extractedDir;
    private FileContext fileContext;

    public Reader(final ReaderConfig config) {
        receiver = config.getReceiver();
        name = config.getName();
        tmpFileDir = Path.of(config.getTmpDirectory());
    }

    @Override
    public void validate() throws InvalidApi {
        if (null == receiver || null == name) {
            throw new InvalidApi("missing required fields");
        }
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void start() throws InputOutputError {
        try {
            final ZipExtractor zipExtractor = new ZipExtractor(tmpFileDir);
            extractedDir = zipExtractor.extractFiles(receiver.receive());
            fileContext = new FileContext(extractedDir);
        } catch (final IOException | XMLStreamException e) {
            throw new InputOutputError("Failed to extract files", e);
        }
    }

    @Override
    public void end() throws InputOutputError {
        LOGGER.trace("Removing tmpFileDir: " + extractedDir);
        try (final Stream<Path> paths = Files.walk(extractedDir)) {
            paths
                    .sorted(Comparator.reverseOrder())
                    .forEach(a -> a.toFile().delete());
        } catch (final IOException e) {
            throw new InputOutputError("Failed to delete tmp file: " + tmpFileDir, e);
        }
    }

    public XMLEventReader getXmlReader(final String type) throws IOException, XMLStreamException {
        return fileContext.getXmlReader(type);
    }

    public List<XMLEventReader> getXmlReaders(final String type) throws XMLStreamException, IOException {
        return fileContext.getXmlReaders(type);
    }

    protected Stream<MappingPart> getAll(final MappingConfiguration from, final String path) {
        return from.getAllByPortrayal(new SimpleMappingPortrayal(getName(), path)).stream();
    }
}