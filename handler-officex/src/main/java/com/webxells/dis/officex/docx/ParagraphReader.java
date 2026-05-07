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
package com.webxells.dis.officex.docx;

import com.webxells.dis.api.config.MappingConfiguration;
import com.webxells.dis.api.error.InputOutputError;
import com.webxells.dis.base.SimpleDatasetPiece;
import com.webxells.dis.officex.Reader;
import com.webxells.dis.officex.docx.internal.DocumentReader;
import java.io.IOException;
import javax.xml.stream.XMLStreamException;

public class ParagraphReader extends Reader<ParagraphReaderConfig> {
    private final ParagraphReaderConfig config;

    private DocumentReader documentReader;

    public ParagraphReader(final ParagraphReaderConfig config) {
        super(config);
        this.config = config;
    }

    @Override
    public int read(final MappingConfiguration from) throws InputOutputError {
        try {
            final DocumentReader.Result current = documentReader.next();
            return setAndCount(from, "content", current.content()) +
                    setAndCount(from, "heading", current.heading()) +
                    setAndCount(from, "heading-weight", String.valueOf(current.headingWeight())) +
                    getAll(from, "heading-path")
                            .mapToInt(a ->
                                    current.headingPath().stream()
                                            .map(SimpleDatasetPiece::new)
                                            .mapToInt(b -> {
                                                a.getDataset().collect(b);
                                                return 1;
                                            }).sum())
                            .sum();
        } catch (final IOException | XMLStreamException e) {
            throw new InputOutputError("could not read from doc", e);
        }
    }

    private int setAndCount(final MappingConfiguration from, final String path, final String value) {
        if (null == value) {
            return 0;
        }
        return getAll(from, path)
                .mapToInt(a -> {
                    a.getDataset().collect(new SimpleDatasetPiece(value));
                    return 1;
                }).sum();
    }

    @Override
    public boolean hasNext() throws InputOutputError {
        try {
            return documentReader.hasNext();
        } catch (final XMLStreamException | IOException e) {
            throw new InputOutputError("Could not read from docX", e);
        }
    }

    @Override
    public void start() throws InputOutputError {
        super.start();
        try {
            documentReader = new DocumentReader(this, config.isMergeContentByHeading());
        } catch (final IOException | XMLStreamException e) {
            throw new InputOutputError("Could not read for paragraph", e);
        }
    }
}