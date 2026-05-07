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
package com.webxells.dis.workflow.service.meta.input;

import com.webxells.dis.api.config.MappingConfiguration;
import com.webxells.dis.api.error.InvalidApi;
import com.webxells.dis.api.input.Input;
import com.webxells.dis.base.SimpleDatasetPiece;
import com.webxells.dis.boot.WorkflowSecurity;
import com.webxells.dis.event.EventManager;
import com.webxells.dis.workflow.service.event.ChildOutputWritten;
import com.webxells.dis.workflow.service.event.DatasetSkipped;
import com.webxells.dis.workflow.service.event.DatasetSkippedByValidator;
import com.webxells.dis.workflow.service.event.InvalidDataset;
import com.webxells.dis.workflow.service.event.JobEnded;
import com.webxells.dis.workflow.service.event.JobError;
import com.webxells.dis.workflow.service.event.JobStarted;
import com.webxells.dis.workflow.service.event.LimitReached;
import com.webxells.dis.workflow.service.event.NewDataset;
import com.webxells.dis.workflow.service.event.OutputWritten;
import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class LastRun implements Input<LastRunConfig> {
    private final String name;
    private final String configName;
    private final Map<String, AtomicLong> outputsUsed = new HashMap<>();

    private boolean hasNext;
    private EventManager.ContextProxy eventManager;
    private long datasetCount;
    private long datasetSkipped;
    private long datasetInvalid;
    private long datasetSkippedByValidator;
    private boolean limitReached;
    private String error;
    private LocalDateTime start;
    private LocalDateTime end;

    public LastRun(final LastRunConfig config) {
        name = config.getName();
        configName = config.getConfigName();
    }

    @Override
    public void validate() throws InvalidApi {
        if (null == name || null == configName) {
            throw new InvalidApi("Required parameters are missing");
        }
        registerEventManager();
    }

    private void registerEvents() {
        eventManager.on(JobStarted.class, a -> {
            clear();
            start = LocalDateTime.now();
        });
        eventManager.on(DatasetSkipped.class, a -> datasetSkipped++);
        eventManager.on(NewDataset.class, a -> datasetCount++);
        eventManager.on(InvalidDataset.class, a -> datasetInvalid++);
        eventManager.on(DatasetSkippedByValidator.class, a-> datasetSkippedByValidator++);
        eventManager.on(LimitReached.class, a -> limitReached = true);
        eventManager.on(JobError.class, this::saveError);
        eventManager.on(JobEnded.class, a -> end = LocalDateTime.now());
        eventManager.on(OutputWritten.class, a -> outputsUsed.computeIfAbsent(a.getOutput().getName(),
                    b -> new AtomicLong()).incrementAndGet());
        eventManager.on(ChildOutputWritten.class, a -> outputsUsed.computeIfAbsent(a.getAlias(),
                b -> new AtomicLong()).incrementAndGet());
    }

    private void saveError(final JobError event) {
        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        final PrintWriter errorWriter = new PrintWriter(outputStream, true);
        event.getError().printStackTrace(errorWriter);
        error+= String.format("Error on %s%nStackTrace: %s%n%n",
                event.getJob().toString(),
                outputStream);
        errorWriter.close();
    }

    @Override
    public int read(final MappingConfiguration from) {
        registerEventManager();
        hasNext = false;
        final AtomicInteger counter = new AtomicInteger();
        from.partsBySource(name)
                .forEach(a -> Optional.ofNullable(a.getInput().getPath())
                        .map(this::getMetaData)
                        .map(SimpleDatasetPiece::new)
                        .ifPresent(b -> {
                            a.getDataset().collect(b);
                            counter.getAndIncrement();
                        }));
        return counter.get();
    }

    private void registerEventManager() {
        if (null == eventManager) {
            eventManager = EventManager.instance().new ContextProxy(WorkflowSecurity.instance().getJob(configName));
            registerEvents();
        }
    }

    private String getMetaData(final String index) {
        final Object raw = getRawMetaData(index);
        if (raw instanceof String) {
            return (String) raw;
        }
        if (null == raw) {
            return null;
        }
        return String.valueOf(raw);
    }

    private Object getRawMetaData(final String index) {
        Object result = getSimpleData(index);
        if (null == result && index.startsWith("output ") && index.length() > 7) {
            return outputsUsed.get(index.substring(7));
        }
        if (null == result && null != start && null != end && index.startsWith("duration ") && index.length() > 9) {
            try {
                return start.until(end, ChronoUnit.valueOf(index.substring(9).toUpperCase()));
            } catch (final IllegalArgumentException ignored) {
            }
        }
        if (null == result) {
            result = getDate(index, "start date", start);
        }
        if (null == result) {
            result = getDate(index, "end date", end);
        }
        return result;
    }

    private Object getSimpleData(final String index) {
        return switch (index) {
            case "datasets" -> datasetCount;
            case "skipped" -> datasetSkipped + datasetSkippedByValidator;
            case "invalid" -> datasetInvalid - datasetSkippedByValidator;
            case "limit" -> limitReached ? "true" : "false";
            case "errors" -> error;
            default -> null;
        };
    }

    private Object getDate(final String current, final String index, final LocalDateTime date) {
        final int length = index.length();
        if (null != date && current.startsWith(index)) {
            if (current.length() == length) {
                return date;
            }
            if (current.length() > length + 1) {
                return date.format(DateTimeFormatter.ofPattern(current.substring(length + 1)));
            }
        }
        return null;
    }

    @Override
    public boolean hasNext() {
        return hasNext;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void start() {
        hasNext = true;
    }

    @Override
    public void end() {
        clear();
    }

    private void clear() {
        datasetCount = 0;
        datasetSkipped = 0;
        datasetInvalid = 0;
        error = "";
        start = null;
        end = null;
        limitReached = false;
        outputsUsed.clear();
    }

}