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
package com.webxells.dis.base.trigger.group;

import com.webxells.dis.api.config.MappingConfiguration;
import com.webxells.dis.api.config.OutputConfig;
import com.webxells.dis.api.error.DisException;
import com.webxells.dis.api.output.Output;
import com.webxells.dis.base.SimpleDatasetPiece;
import com.webxells.dis.base.config.SimpleMappingConfiguration;
import com.webxells.dis.base.config.SimpleMappingPart;
import com.webxells.dis.base.config.SimpleMappingPoint;
import com.webxells.dis.boot.ServiceManager;
import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

class UpdateSender {
    interface DisRunnableWithErrors {
        void run() throws DisException;
    }
    private static final DateTimeFormatter DEFAULT_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");

    private final String reference;
    private final String group;
    private final OutputConfig outputConfig;

    private Output<?> output;
    private DateTimeFormatter formatter = DEFAULT_FORMATTER;


    public UpdateSender(final OutputConfig outputConfig, final String group) {
        this.outputConfig = outputConfig;
        if (null == outputConfig) {
            throw new IllegalArgumentException("outputConfig is null");
        }
        this.group = group;
        reference = outputConfig.getName();
    }

    public void send(final List<Child> orderedChildren) {
        if (orderedChildren.isEmpty()) {
            throw new IllegalArgumentException("orderedChildren is empty");
        }
        final SimpleMappingConfiguration mappingConfiguration = new SimpleMappingConfiguration();
        addToConfiguration(mappingConfiguration, "name", group);
        String status = "STARTED";
        final LocalDateTime start = Optional.ofNullable(orderedChildren.getFirst().getStart())
                .orElseGet(LocalDateTime::now);
        final LocalDateTime end = orderedChildren.getLast().getEnd();
        addToConfiguration(mappingConfiguration, "start", formatter.format(start));
        final SimpleMappingPart childrenPart = createPart(mappingConfiguration, "children");
        final boolean errorFound = addChildren(childrenPart, orderedChildren);
        if (errorFound) {
            status = "ERROR";
        }
        if (null != end) {
            status = "FINISHED".concat("ERROR".equals(status) ? " (ERROR)" : "");
            addToConfiguration(mappingConfiguration, "end", formatter.format(end));
        }
        addToConfiguration(mappingConfiguration, "status", status);
        turnErrorsToUnchecked(() -> output.write(mappingConfiguration));
    }

    private boolean addChildren(final SimpleMappingPart childrenPart, final List<Child> orderedChildren) {
        boolean errorFound = false;
        final MappingConfiguration parent = childrenPart.getConfiguration();
        for (final Child current : orderedChildren) {
            final SimpleMappingConfiguration childMapping = new SimpleMappingConfiguration();
            childMapping.setParent(parent);
            childrenPart.addSubData(childMapping);
            errorFound = errorFound || fillConfig(current, childMapping);
        }
        return errorFound;
    }

    private boolean fillConfig(final Child current, final SimpleMappingConfiguration childMapping) {
        addToConfiguration(childMapping, "name", current.getName());
        addToConfiguration(childMapping,"status", current.getStatus().name());
        Optional.ofNullable(current.getStart())
                .ifPresent(start ->
                        addToConfiguration(childMapping, "start", formatter.format(start)));
        Optional.ofNullable(current.getEnd())
                .ifPresent(end ->
                        addToConfiguration(childMapping, "end", formatter.format(end)));
        final Throwable throwable = current.getError();
        if (null != throwable) {
            addToConfiguration(childMapping, "error",
                    String.format("%s: %s", throwable.getClass().getSimpleName(), throwable.getMessage()));
            addToConfiguration(childMapping, "error-trace", getErrorTrace(throwable));
            return true;
        }
        return false;
    }

    private String getErrorTrace(final Throwable throwable) {
        final ByteArrayOutputStream errorTraces = new ByteArrayOutputStream();
        final PrintWriter errorTraceWriter = new PrintWriter(errorTraces);
        throwable.printStackTrace(errorTraceWriter);
        errorTraceWriter.flush();
        return errorTraces.toString();
    }

    private void addToConfiguration(final SimpleMappingConfiguration mappingConfiguration, final String name,
                                    final String content) {
        final SimpleMappingPart part = createPart(mappingConfiguration, name);
        part.getDataset().collect(new SimpleDatasetPiece(content));
    }

    private SimpleMappingPart createPart(final SimpleMappingConfiguration mappingConfiguration, final String name) {
        final SimpleMappingPart part = new SimpleMappingPart(mappingConfiguration,
                new SimpleMappingPoint(), new SimpleMappingPoint(reference, name));
        mappingConfiguration.addPart(part);
        return part;
    }


    public void close() {
        turnErrorsToUnchecked(output::end);
    }

    private Output<?> startOutput(final OutputConfig outputConfig) {
        final Output<?> result = ServiceManager.loadByConfig(outputConfig);
        turnErrorsToUnchecked(result::start);
        return result;
    }

    private void turnErrorsToUnchecked(final DisRunnableWithErrors toDo) {
        try {
            toDo.run();
        } catch (final DisException e) {
            throw new RuntimeException("Could not fulfill Dis operation", e);
        }
    }

    public void clear() {
        output = startOutput(outputConfig);
    }

    public void setDateFormat(final String format) {
        this.formatter = DateTimeFormatter.ofPattern(format);
    }

}