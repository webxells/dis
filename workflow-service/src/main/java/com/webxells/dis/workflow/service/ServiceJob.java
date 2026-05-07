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
package com.webxells.dis.workflow.service;

import com.webxells.dis.api.DatasetPiece;
import com.webxells.dis.api.Logger;
import com.webxells.dis.api.MappingOperation;
import com.webxells.dis.api.config.ConfigurableByType;
import com.webxells.dis.api.config.InputConfig;
import com.webxells.dis.api.config.JobConfig;
import com.webxells.dis.api.config.MappingConfiguration;
import com.webxells.dis.api.config.MappingPart;
import com.webxells.dis.api.config.OutputConfig;
import com.webxells.dis.api.error.DisException;
import com.webxells.dis.api.error.InputOutputError;
import com.webxells.dis.api.error.InvalidDatasetException;
import com.webxells.dis.api.input.Input;
import com.webxells.dis.api.input.SkippableInput;
import com.webxells.dis.api.manipulator.Manipulator;
import com.webxells.dis.api.manipulator.SingleCallForAllValuesManipulator;
import com.webxells.dis.api.output.Output;
import com.webxells.dis.api.trigger.Trigger;
import com.webxells.dis.api.validator.SingleCallForAllValuesValidator;
import com.webxells.dis.api.validator.Validator;
import com.webxells.dis.api.workflow.Job;
import com.webxells.dis.base.SimpleDatasetPiece;
import com.webxells.dis.boot.ConfigurationMapping;
import com.webxells.dis.boot.ServiceManager;
import com.webxells.dis.event.EventManager;
import com.webxells.dis.logging.LoggerProxyFactory;
import com.webxells.dis.workflow.service.config.ServiceJobConfig;
import com.webxells.dis.workflow.service.event.*;
import com.webxells.dis.workflow.service.refinement.SubDataFirst;
import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

public class ServiceJob implements Job {
    private static final Logger LOGGER = LoggerProxyFactory.logger(ServiceJob.class);

    private final JobConfig config;
    private Thread threadRunning;
    private final List<Trigger<?>> triggers;
    private final Input<? extends InputConfig> input;
    private final List<Output<? extends OutputConfig>> outputs;
    private final MappingConfiguration mappingConfigs;
    private final long skipDatasets;
    private final long limitDatasets;
    private final boolean skipInputReadsOnSkip;
    private final EventManager.ContextProxy eventManager = EventManager.instance().new ContextProxy(this);

    private volatile JobState current = JobState.INITIALIZED;
    private long currentDataset;
    private LocalDateTime lastStart;

    public ServiceJob(final JobConfig config) {
        this.config = config;
        this.input = createInstance(config.getInput());
        this.outputs = createInstances(config.getOutputs());
        this.triggers = createInstances(config.getTriggers());
        this.mappingConfigs = config.getMappings();
        if (config instanceof ServiceJobConfig serviceJobConfig) {
            skipDatasets = serviceJobConfig.getDatasetSkipAmount();
            limitDatasets = serviceJobConfig.getDatasetLimitAmount();
            skipInputReadsOnSkip = serviceJobConfig.isSkipInputReads();
        } else {
            skipDatasets = 0;
            limitDatasets = -1;
            skipInputReadsOnSkip = true;
        }
    }

    private <T, R extends ConfigurableByType> T createInstance(final R configuration) {
        final T instance = ServiceManager.loadByConfig(configuration);
        Optional.ofNullable(ConfigurationMapping.getLocation(configuration))
                .ifPresent(location -> ConfigurationMapping.setLocation(instance, location));
        return instance;
    }

    private <T, R extends ConfigurableByType> List<T> createInstances(final List<R> configurations) {
        if (null == configurations) {
            return List.of();
        }
        //noinspection unchecked
        return configurations.stream()
                .map(configuration -> (T) createInstance(configuration))
                .toList();
    }

    public String getJobName() {
        return config.getName();
    }

    @Override
    public String toString() {
        final String location = ConfigurationMapping.getLocation(this);
        return String.format("ServiceJobConfig[%s]: %s", config.getName(), Optional.ofNullable(location)
                .orElseGet(() -> String.format("t:%d, i:%d, o:%d, m:%d", triggers.size(), 1, outputs.size(),mappingConfigs.size())));
    }

    public JobState state() {
        return current;
    }

    @Override
    public void start() {
        threadRunning = Thread.currentThread();
        try {
            runImport();
        } catch (final Throwable e) {
            setJobState(JobState.ERROR);
            eventManager.trigger(new JobError(this, e));
            LOGGER.e("Something failed", e);
            triggers.forEach(Trigger::abort);
        }
    }

    public Optional<Thread> getThreadRunning() {
        return Optional.ofNullable(threadRunning);
    }

    public void stop() {
        if (null != threadRunning && threadRunning.isAlive()) {
            LOGGER.d(String.format("Aborting thread: %s", threadRunning.getName()));
            threadRunning.interrupt();
        }
        setJobState(JobState.STOPPED);
    }

    public Optional<LocalDateTime> getLastStart() {
        return Optional.ofNullable(lastStart);
    }

    private synchronized void runImport() throws InterruptedException, InvalidDatasetException {
        if (JobState.WAITING.equals(current)) {
            setJobState(JobState.RUNNING);
            currentDataset = 0;
            lastStart = LocalDateTime.now();
            eventManager.trigger(new JobStarted(this));
            startInputOutput();
            while (hasNext(input)) {
                if (limitReached()) {
                    eventManager.trigger(new LimitReached(this, limitDatasets));
                    LOGGER.i("Limit reached - ending job");
                    break;
                }
                LOGGER.debug(String.format("New dataset: %s", ++currentDataset));
                writeNextDataset();
            }
            endInputOutput();
            eventManager.trigger(new JobEnded(this));
            LOGGER.info("Import finished");
            setJobState(JobState.WAITING);
        }
    }

    private boolean limitReached() {
        return limitDatasets > -1 && (currentDataset - skipDatasets) >= limitDatasets;
    }

    private boolean hasNext(final Input<? extends InputConfig> input) throws InterruptedException {
        try {
            return input.hasNext();
        } catch (final InputOutputError e) {
            LOGGER.e("ServiceJobConfig hasNext failed", e);
            throw new InterruptedException();
        }
    }

    private void endInputOutput() throws InterruptedException {
        try {
            input.end();
            for (Output<? extends OutputConfig> output : outputs) {
                output.end();
            }
        } catch (final DisException e) {
            eventManager.trigger(new JobError(this, e));
            LOGGER.e("ServiceJobConfig essentials shutdown failed", e);
            throw new InterruptedException();
        }
    }

    private void startInputOutput() throws InterruptedException {
        try {
            input.start();
            eventManager.trigger(new InputInitialized(input));
            for (Output<? extends OutputConfig> output : outputs) {
                output.start();
                eventManager.trigger(new OutputInitialized(output));
            }
        } catch (final DisException e) {
            eventManager.trigger(new JobError(this, e));
            LOGGER.e("ServiceJobConfig essentials startup failed", e);
            throw new InterruptedException();
        }
    }

    private void setJobState(final JobState jobState) {
        eventManager.trigger(new JobStatusChanged(this, current, jobState));
        current = jobState;
        LOGGER.d(String.format("jobState changed to %s (%s)", jobState, this));
        if (JobState.RUNNING != jobState) {
            threadRunning = null;
        }
    }

    private void writeNextDataset() throws InterruptedException, InvalidDatasetException {
        mappingConfigs.clear();
        try {
            eventManager.trigger(new NewDataset(currentDataset));
            final boolean skipped = skipped();
            readDataset(skipped);
            if (skipped) {
                eventManager.trigger(new DatasetSkipped(currentDataset));
                LOGGER.debug("Dataset skipped");
                return;
            }
            if (!analyzeDataset(mappingConfigs)) {
                for (Output<? extends OutputConfig> output : outputs) {
                    output.write(mappingConfigs);
                    eventManager.trigger(new OutputWritten(currentDataset, mappingConfigs, output));
                }
            } else {
                eventManager.trigger(new InvalidDataset(currentDataset, mappingConfigs));
            }
        } catch (final InputOutputError e) {
            eventManager.trigger(new JobError(this, e));
            LOGGER.e("ServiceJobConfig write failed", e);
            throw new InterruptedException();
        }
    }

    private void readDataset(final boolean skipped) throws InputOutputError {
        if (input instanceof SkippableInput) {
            ((SkippableInput<?>) input).read(mappingConfigs, skipInputReadsOnSkip && skipped);
        } else {
            input.read(mappingConfigs);
        }
        eventManager.trigger(new InputRead(currentDataset, mappingConfigs));
    }

    private boolean skipped() {
        return skipDatasets > 0 && currentDataset <= skipDatasets;
    }

    /**
     * @return true if dataset should be skipped
     * @throws InvalidDatasetException if error while validating or manipulating occurs
     */
    private boolean analyzeDataset(final MappingConfiguration mappingConfigs) throws InvalidDatasetException {
        partLoop: for (MappingPart part : mappingConfigs.parts()) {
            if (part.hasRefinement(SubDataFirst.class) && analyzeSubData(part)) {
                return true;
            }
            if (null != part.getOperations()) {
                operationLoop:
                for (MappingOperation operation : part.getOperations()) {
                    if (operation instanceof Manipulator) {
                        if (callManipulator((Manipulator) operation, part)) {
                            return true;
                        }
                    } else if (operation instanceof Validator) {
                        switch (callValidator((Validator) operation, part)) {
                            case SKIP:
                                continue partLoop;
                            case SKIP_OPERATIONS:
                                break operationLoop;
                            case CONTINUE:
                                break;
                            case BREAK_CURRENT:
                                return false;
                            case BREAK_ALL:
                                LOGGER.d(String.format("Validator (%s) failed for dataset", ConfigurationMapping.getFormatedLocation(operation)));
                                return true;
                        }
                    }
                }
            }
            if (!part.hasRefinement(SubDataFirst.class) && analyzeSubData(part)) {
                return true;
            }
        }
        return false;
    }
    private boolean analyzeSubData(final MappingPart part) throws InvalidDatasetException {
        for (final MappingConfiguration subDatum : part.getSubData()) {
            if (analyzeDataset(subDatum)) {
                return true;
            }
        }
        return false;
    }

    /*
    @todo: create test cases
     */

    private boolean callManipulator(final Manipulator operation, final MappingPart part) {
        final List<DatasetPiece> content = new LinkedList<>(part.getDataset().getContent());
        boolean addToPart = false;
        for (DatasetPiece datasetPiece : content.isEmpty() ? List.of(new SimpleDatasetPiece(null)) : content) {
            try {
                operation.manipulate(datasetPiece, part);
            } catch (final InvalidDatasetException e) {
                LOGGER.e("Invalid dataset while manipulation detected", e);
                return true;
            }
            if (content.isEmpty() && datasetPiece.value().isPresent()) {
                addToPart = true;
                content.add(datasetPiece);
            }
            if (operation instanceof SingleCallForAllValuesManipulator) {
                break;
            }
        }
        if (addToPart) {
            part.getDataset().clear();
            part.getDataset().collect(content);
        }
        return false;
    }

    /**
     * @param validator
     * @param part
     * @return true if dataset should be skipped
     * @throws InvalidDatasetException if error while validating occurs
     */
    private ValidatorState callValidator(final Validator validator, final MappingPart part) throws InvalidDatasetException {
        final List<DatasetPiece> content = new LinkedList<>(part.getDataset().getContent());
        final Iterator<DatasetPiece> iterator = (content.isEmpty() ?
                new LinkedList<DatasetPiece>(List.of(new SimpleDatasetPiece(null))) :
                content).iterator();
        while (iterator.hasNext()) {
            final DatasetPiece current = iterator.next();
            if (!validator.validate(current, part)) {
                logFail(validator);
                switch (part.getValidatorErrorStrategy()) {
                    case RESET_CONFIGURATION:
                        LOGGER.d("reset current config");
                        part.getConfiguration().clear();
                        return ValidatorState.BREAK_CURRENT;
                    case SKIP_DATASET:
                        part.getDataset().clear();
                        return ValidatorState.SKIP;
                    case SKIP_FOLLOWING_OPERATIONS:
                        return ValidatorState.SKIP_OPERATIONS;
                    case CONTINUE_NEXT_READ:
                        return ValidatorState.BREAK_ALL;
                    case ERROR:
                        throw new InvalidDatasetException("Validation failed: ".concat(ConfigurationMapping.getFormatedLocation(validator)));
                    case SKIP_DATASET_PIECE:
                        iterator.remove();
                }
            }
            if (validator instanceof SingleCallForAllValuesValidator) {
                break;
            }
        }
        if (Validator.ErrorStrategy.SKIP_DATASET_PIECE == part.getValidatorErrorStrategy()) {
            part.getDataset().clear();
            part.getDataset().collect(content);
        }
        return ValidatorState.CONTINUE;
    }

    private void logFail(final Validator validator) {
        validator.getFailMessage()
                .ifPresent(a -> LOGGER.log(validator.getFailLevel(), a));
    }

    public List<Trigger<?>> getTriggers() {
        return triggers;
    }

    public List<Output<? extends OutputConfig>> getOutputs() {
        return outputs;
    }

    public void ready() {
        setJobState(JobState.WAITING);
    }

}