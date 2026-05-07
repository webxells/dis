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
package com.webxells.dis.config.json.parsing.plugins;

import com.webxells.dis.api.Logger;
import com.webxells.dis.config.json.parsing.DisonElementReader;
import com.webxells.dis.config.json.parsing.DisonJsonTransformer;
import com.webxells.dis.config.json.parsing.element.DisonMethodReader;
import com.webxells.dis.logging.LoggerProxyFactory;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Optional;

public class IncludeParser extends TemplateParser {
    public static class Manager implements DisonPluginManager {
        @Override
        public DisonPlugin isCompetent(final String disCommand) {
            if ("--dis-include".equals(disCommand)) {
                return new IncludeParser(false);
            }
            if ("--dis-include-as-children".equals(disCommand)) {
                return new IncludeParser(true);
            }
            return null;
        }
    }

    private static final Logger LOGGER = LoggerProxyFactory.logger(IncludeParser.class);
    private final boolean preventMultiArrays;

    public IncludeParser(final boolean preventMultiArrays) {
        this.preventMultiArrays = preventMultiArrays;
    }

    @Override
    public Optional<DisonElementReader> handle(final DisonElementReader disonElementReader, final DisonJsonTransformer disonJsonTransformer) {
        if (disonElementReader instanceof DisonMethodReader) {
            return handleMethodCall(disonElementReader, disonJsonTransformer);
        }
        disonJsonTransformer.plugInError("Unexpected dison type: ".concat(disonElementReader.getType().toString()));
        return Optional.empty();
    }

    @Override
    protected String parseName(final String name, final DisonJsonTransformer disonJsonTransformer) {
        final String rightFile = getRightFile(name);
        //unix only absolute path
        return rightFile.startsWith("/") ? rightFile :
                String.format("%s%s%s", disonJsonTransformer.getCurrentPath(), File.separator,
                        '/' == File.separatorChar ? rightFile : rightFile.replace('/', File.separatorChar));
    }

    @Override
    protected boolean isPreventMultiArray() {
        return preventMultiArrays;
    }

    @Override
    protected String calculateNextPath(final String name, final DisonJsonTransformer disonJsonTransformer) {
        return name.substring(0, name.lastIndexOf(File.separator));
    }

    @Override
    protected String readContent(final String name, final DisonJsonTransformer disonJsonTransformer) {
        final File file = new File(name);
        if (file.exists() && file.canRead() && file.isFile()) {
            try {
                return new String(Files.readAllBytes(file.toPath()));
            } catch (final IOException e) {
                LOGGER.e(e);
                disonJsonTransformer.plugInError("Could not include dison file: ".concat(name));
            }
        } else {
            disonJsonTransformer.plugInError("No readable file found: ".concat(name));
        }
        return null;
    }

    private String getRightFile(final String file) {
        return file.endsWith(".dison") || file.endsWith(".json") ? file : file.concat(".dison");
    }

}