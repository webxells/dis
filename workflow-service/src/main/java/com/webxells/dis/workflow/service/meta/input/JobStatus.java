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

import com.webxells.dis.api.config.InputConfig;
import com.webxells.dis.api.config.MappingConfiguration;
import com.webxells.dis.api.config.MappingPart;
import com.webxells.dis.api.error.DisException;
import com.webxells.dis.api.error.InvalidApi;
import com.webxells.dis.api.input.Input;
import com.webxells.dis.base.SimpleDatasetPiece;
import com.webxells.dis.workflow.service.ServiceJob;
import java.time.format.DateTimeFormatter;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

public class JobStatus implements Input<JobStatus.Config> {
    public static class Config implements InputConfig {
        private String name;

        @Override
        public String getName() {
            return name;
        }

        @Override
        public String getType() {
            return JobStatus.class.getName();
        }

        public void setName(final String name) {
            this.name = name;
        }
    }

    private static List<ServiceJob> serviceJobs;

    private final String name;
    private Iterator<ServiceJob> currentJobs;
    private int currentIndex;

    public static void registerJobs(final List<ServiceJob> serviceJobs) {
        JobStatus.serviceJobs = serviceJobs;
    }

    public JobStatus(final Config config) {
        name = config.name;
    }

    @Override
    public int read(final MappingConfiguration from) {
        final ServiceJob current = currentJobs.next();
        return from.partsBySource(name).stream()
                .mapToInt(a -> setJobInfo(current, a))
                .sum();
    }

    private int setJobInfo(final ServiceJob current, final MappingPart part) {
        return Optional.ofNullable(switch (part.getInput().getPath()) {
            case "ID"-> String.valueOf(currentIndex++);
            case "NAME"-> current.getJobName();
            case "STATE" -> current.state().name();
            case "LAST-START" -> current.getLastStart()
                    .map(a -> a.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                    .orElse("n/a");
            default -> null;
        })
            .map(a -> {
                part.getDataset().collect(new SimpleDatasetPiece(a));
                return 1;
            })
            .orElse(0);
    }

    @Override
    public boolean hasNext() {
        return currentJobs != null && currentJobs.hasNext();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void start() throws DisException {
        if (null == serviceJobs) {
            throw new InvalidApi("monitoring exposure not allowed");
        }
        currentJobs = serviceJobs.iterator();
        currentIndex = 0;
    }

    @Override
    public void end() {
        currentJobs = null;
    }
}