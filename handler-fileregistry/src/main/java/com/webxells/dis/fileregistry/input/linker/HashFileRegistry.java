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
package com.webxells.dis.fileregistry.input.linker;

import com.webxells.dis.api.MappingPortrayal;
import com.webxells.dis.api.config.MappingConfiguration;
import com.webxells.dis.api.config.MappingPart;
import com.webxells.dis.api.config.MappingPoint;
import com.webxells.dis.api.config.description.Default;
import com.webxells.dis.api.config.description.Description;
import com.webxells.dis.api.config.description.Required;
import com.webxells.dis.api.error.DisException;
import com.webxells.dis.api.error.InputOutputError;
import com.webxells.dis.api.input.linker.JoinLinker;
import com.webxells.dis.base.SimpleDatasetPiece;
import com.webxells.dis.base.config.SimpleMappingConfiguration;
import com.webxells.dis.base.config.SimpleMappingPart;
import com.webxells.dis.base.config.SimpleMappingPoint;
import com.webxells.dis.fileregistry.internal.Murmur3HashFileRegistry;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Description("Hashing mapping parts of hashFields as index - index file is looked up or otherwise retrieved by source")
public class HashFileRegistry extends Murmur3HashFileRegistry implements JoinLinker {
    public static class Source {
        @Required
        public JoinLinker linker;
        @Required
        @Description("Where to look up value after linker is called")
        public MappingPortrayal portrayal;
    }

    @Description("where value should be stored")
    @Required
    protected MappingPortrayal target;
    @Description("How to look up value if index file does not exist")
    @Required
    protected Source source;
    @Description("Raise error if source failed")
    @Default("false")
    protected boolean strict;

    @Override
    public int getData(final MappingConfiguration from) throws InputOutputError {
        final MappingConfiguration strippedConfiguration = stripConfiguration(from);
        return callFileRegistry(createDataHash(strippedConfiguration), strippedConfiguration, from);
    }

    protected int callFileRegistry(final String dataHash, final MappingConfiguration strippedConfiguration, final MappingConfiguration from) throws InputOutputError {
        final int result = readData(createFile(dataHash), strippedConfiguration);
        from.getByPortrayal(target).ifPresent(
                a -> strippedConfiguration.getByPortrayal(source.portrayal).ifPresent(
                        b -> b.value().ifPresent(c -> a.getDataset().collect(b.getDataset().getContent()))));
        return result;
    }

    public void setStrict(final boolean strict) {
        this.strict = strict;
    }

    protected int readData(final File file, final MappingConfiguration configuration) throws InputOutputError {
        if (validFile(file)) {
            return readByFile(file, configuration);
        }
        final int result = readBySource(configuration);
        final Optional<MappingPart> part = configuration.getByPortrayal(source.portrayal);
        if (part.isPresent()) {
            writeToFile(file, part.get());
        }
        return result;
    }

    protected int readBySource(final MappingConfiguration configuration) throws InputOutputError {
        try {
            source.linker.start();
            final int result = source.linker.getData(configuration);
            source.linker.end();
            return result > 0 ? 1 : 0;
        } catch (final DisException e) {
            throw new InputOutputError("Could not read by source linker", e);
        }
    }

    private void writeToFile(final File file, final MappingPart part) throws InputOutputError {
        if (part.value().isPresent() && part.getDataset().getContent().get(0).value().isPresent()) {
            try {
                Files.write(file.toPath(), part.getDataset().getContent().get(0).value().get().getBytes(),
                        StandardOpenOption.CREATE_NEW);
            } catch (final IOException e) {
                throw new InputOutputError("Could not write to file: ".concat(file.getAbsolutePath()), e);
            }
        }
    }

    private int readByFile(final File file, final MappingConfiguration configuration) throws InputOutputError {
        try {
            final String content = new String(Files.readAllBytes(file.toPath()));
            configuration.getByPortrayal(source.portrayal).ifPresent(
                    a -> a.getDataset().collect(new SimpleDatasetPiece(content)));
            return 1;
        } catch (final IOException e) {
            throw new InputOutputError("Could not read from file: ".concat(file.getAbsolutePath()), e);
        }
    }

    private boolean validFile(final File file) throws InputOutputError {
        if (file.exists()) {
            if (file.isDirectory() || !file.canRead()) {
                throw new InputOutputError("File not a readable file: ".concat(file.getAbsolutePath()));
            }
            return true;
        }
        return false;
    }

    protected MappingConfiguration stripConfiguration(final MappingConfiguration from) {
        final MappingPoint sourcePoint = new SimpleMappingPoint(source.portrayal.getReference(),
                source.portrayal.getPath());
        final SimpleMappingConfiguration result = new SimpleMappingConfiguration();
        result.setParts(List.of(new SimpleMappingPart(result, sourcePoint, sourcePoint)));
        result.setParts(stripConfigurationParts(from, result));
        return result;
    }

    private List<MappingPart> stripConfigurationParts(final MappingConfiguration from,
                                                  final SimpleMappingConfiguration result) {
        final List<MappingPart> list = new ArrayList<>();
        for (final MappingPortrayal hashField : hashFields) {
            final Optional<MappingPart> a = from.getByPortrayal(hashField);
            if (a.isPresent()) {
                if (strict && a.get().value().isEmpty() && a.get().getSubData().isEmpty()) {
                    throw new StrictnessFailed();
                }
                final SimpleMappingPart simpleMappingPart = new SimpleMappingPart(result, a.get().getInput(),
                        a.get().getOutput()) {{
                    getDataset().collect(a.get().getDataset().getContent());
                    setSubData(a.get().getSubData());
                    setRefinements(a.get().getRefinements());
                }};
                list.add(simpleMappingPart);
            } else if (strict){
                throw new StrictnessFailed();
            }
        }
        return list;
    }

    @Override
    public String getInputName() {
        return source.linker.getInputName();
    }

    @Override
    public void start() throws DisException {    }

    @Override
    public void end() throws DisException {    }

    @Override
    public String getType() {
        return HashFileRegistry.class.getName();
    }

    public void setTarget(final MappingPortrayal target) {
        this.target = target;
    }

    public void setSource(final Source source) {
        this.source = source;
    }
}