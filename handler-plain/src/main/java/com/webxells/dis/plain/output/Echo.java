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
package com.webxells.dis.plain.output;

import com.webxells.dis.api.config.MappingConfiguration;
import com.webxells.dis.api.config.MappingPart;
import com.webxells.dis.api.error.DisException;
import com.webxells.dis.api.error.InputOutputError;
import com.webxells.dis.api.output.Output;
import com.webxells.dis.base.config.SimpleMappingConfiguration;
import com.webxells.dis.plain.output.parser.EchoParser;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class Echo implements Output<EchoConfig> {
    private final EchoConfig configuration;
    private final String template;

    private OutputStreamWriter outputStreamWriter;

    public static String parse(final EchoConfig config, final Map<String, String> values) {
        final EchoParser parser = new EchoParser(new SimpleMappingConfiguration(), config);
        parser.setStaticValues(values);
        return parser.parse(config.getTemplate());
    }

    public Echo(final EchoConfig configuration) {
        this.configuration = Objects.requireNonNull(configuration);
        template = configuration.getTemplate();
    }

    @Override
    public void write(final MappingConfiguration mappingConfiguration) throws InputOutputError {
        writeParts(mappingConfiguration);
    }

    private void writeParts(final MappingConfiguration parts) throws InputOutputError {
        try {
            outputStreamWriter.write(parseTemplate(parts));
            outputStreamWriter.flush();
        } catch (IOException e) {
            throw new InputOutputError("failed echoing to stream", e);
        }
    }


    @Override
    public String getName() {
        return configuration.getName();
    }

    private String parseTemplate(final MappingConfiguration parts) {
        if (null == template) {
            return joinValues(parts.partsByDestination(configuration.getName()));
        }
        final EchoParser parser = new EchoParser(parts, configuration);
        return parser.parse(template);
    }

    @Override
    public void start() throws DisException {
        outputStreamWriter = new OutputStreamWriter(configuration.getSender().send());
    }

    @Override
    public void end() throws DisException {
        if (null != outputStreamWriter) {
            try {
                outputStreamWriter.close();
            } catch (IOException e) {
                throw new InputOutputError("closing failed", e);
            }
        }
    }

    private String joinValues(final List<MappingPart> values) {
        return values.stream()
                .map(a -> a.value().orElse(""))
                .collect(Collectors.joining(configuration.getSeparator()));
    }

}