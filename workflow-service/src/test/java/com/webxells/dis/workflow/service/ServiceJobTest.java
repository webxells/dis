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
import com.webxells.dis.api.config.MappingConfiguration;
import com.webxells.dis.api.config.MappingPart;
import com.webxells.dis.api.manipulator.Manipulator;
import com.webxells.dis.api.validator.Validator;
import com.webxells.dis.api.validator.Validator.ErrorStrategy;
import com.webxells.dis.api.workflow.Job.JobState;
import com.webxells.dis.base.SimpleDatasetPiece;
import com.webxells.dis.base.config.SimpleMappingConfiguration;
import com.webxells.dis.base.config.SimpleMappingPoint;
import com.webxells.dis.base.config.StableMappingPart;
import com.webxells.dis.test.cases.ConfigurationBuilder;
import com.webxells.dis.test.cases.SimpleTestCase;
import com.webxells.dis.test.example.input.TestInput;
import com.webxells.dis.test.example.input.TestInputConfig;
import com.webxells.dis.test.example.output.TestOutput;
import com.webxells.dis.test.example.output.TestOutputConfig;
import com.webxells.dis.test.example.trigger.TestTrigger;
import com.webxells.dis.test.example.trigger.TestTriggerConfig;
import com.webxells.dis.workflow.service.config.ServiceJobConfig;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ServiceJobTest extends SimpleTestCase {
    private LinkedList<Map<String, DatasetPiece>> content;
    private TestTriggerConfig triggerConfig;
    private TestTriggerConfig trigger2Config;
    private TestInputConfig inputConfig;
    private TestOutputConfig outputConfig;
    private TestOutputConfig output2Config;
    private SimpleMappingConfiguration mappingConfig;
    private SimpleMappingConfiguration subMappingConfig1;
    private SimpleMappingConfiguration subMappingConfig2;
    private StableMappingPart mappingPart1;
    private StableMappingPart mappingPart2;
    private StableMappingPart subMappingConfig1Part;
    private StableMappingPart subMappingConfig2Part;
    private SimpleDatasetPiece content1;
    private SimpleDatasetPiece content2;
    private String inputName;
    private String output1Name;
    private String output2Name;
    private String mapping1InputPath;
    private String mapping1OutputPath;
    private String mapping2InputPath;
    private String mapping2OutputPath;
    private boolean validated;
    private boolean manipulated;
    private final Validator falseValidator = new Validator() {
        @Override
        public boolean validate(final DatasetPiece currentPiece, final MappingPart mappingPart) {
            return false;
        }

        @Override
        public String getType() {
            return null;
        }
    };

    @BeforeEach
    void setUpFields() {
        inputName = random();
        output1Name = random();
        output2Name = random();
        mapping1InputPath = random();
        mapping1OutputPath = random();
        mapping2InputPath = random();
        mapping2OutputPath = random();
        validated = false;
        manipulated = false;
        content1 = new SimpleDatasetPiece(random());
        content2 = new SimpleDatasetPiece(random());
        content = new LinkedList<>();
        content.add(Map.of(
                mapping1InputPath, content1,
                mapping2InputPath, content2));
        triggerConfig = new TestTriggerConfig();
        trigger2Config = new TestTriggerConfig();
        inputConfig = new TestInputConfig(inputName);
        TestInput.setData(content);
        outputConfig = new TestOutputConfig(output1Name);
        output2Config = new TestOutputConfig(output2Name);
        mappingConfig = new SimpleMappingConfiguration();
        mappingPart1 = new StableMappingPart(mappingConfig);
        mappingPart1.setInput(new SimpleMappingPoint(inputName, mapping1InputPath));
        mappingPart1.setOutput(new SimpleMappingPoint(output1Name, mapping1OutputPath));
        mappingPart1.getDataset().collect(List.of(content1));
        mappingPart2 = new StableMappingPart(mappingConfig);
        mappingPart2.getDataset().collect(List.of(content2));
        mappingPart2.setInput(new SimpleMappingPoint(inputName, mapping2InputPath));
        mappingPart2.setOutput(new SimpleMappingPoint(output2Name, mapping2OutputPath));
        mappingConfig.setParts(List.of(mappingPart1, mappingPart2));
        subMappingConfig1 = new SimpleMappingConfiguration();
        subMappingConfig1Part = new StableMappingPart(subMappingConfig1,
                new SimpleMappingPoint(inputName, random("subPart1i")),
                new SimpleMappingPoint(output1Name, random("subPart1o"))) {{
            getDataset().collect(new SimpleDatasetPiece(random("subPart1V")));
        }};
        subMappingConfig1.setParts(List.of(subMappingConfig1Part));
        subMappingConfig2 = new SimpleMappingConfiguration();
        subMappingConfig2Part = new StableMappingPart(subMappingConfig2,
                new SimpleMappingPoint(inputName, random("subPart2i")),
                new SimpleMappingPoint(output2Name, random("subPart2o"))) {{
            getDataset().collect(new SimpleDatasetPiece(random("subPart2V")));
        }};
        subMappingConfig2.setParts(List.of(subMappingConfig2Part));
        mappingPart1.addSubData(List.of(subMappingConfig1));
        mappingPart2.addSubData(List.of(subMappingConfig2));
    }

    @Test
    void testSkipLimit() throws InterruptedException {
        TestInput.setData(List.of(
                Map.of("round", new SimpleDatasetPiece("1")),
                Map.of("round", new SimpleDatasetPiece("2")),
                Map.of("round", new SimpleDatasetPiece("3")),
                Map.of("round", new SimpleDatasetPiece("4")),
                Map.of("round", new SimpleDatasetPiece("5"))
        ));
        MappingConfiguration mapping = newConfiguration()
                .addPart(ConfigurationBuilder.newPart()
                        .setInput(createMappingPoint(inputName, "round"))
                        .setOutput(createMappingPoint(output1Name, "round"))
                )
                .build();
        ServiceJob fixture = new ServiceJob(new ServiceJobConfig() {{
            setName("test");
            setMapping(mapping);
            setDatasetSkipAmount(2);
            setDatasetLimitAmount(2);
            setOutput(List.of(outputConfig));
            setInput(inputConfig);
            setTrigger(List.of(triggerConfig));
        }});

        fixture.getTriggers().get(0).awaitAction(fixture::start);
        fixture.ready();
        ((TestTrigger) fixture.getTriggers().get(0)).execute();

        Thread.sleep(500);
        assertEquals(List.of("3".hashCode(), "4".hashCode()), ((TestOutput) fixture.getOutputs().get(0)).getHashes());

    }

    @Test
    void testSubDataValidatorsFailing() throws InterruptedException {
        subMappingConfig2Part.setOperations(List.of(falseValidator));
        ServiceJob fixture = new ServiceJob(new ServiceJobConfig() {{
            setName("test");
            setMapping(mappingConfig);
            setOutput(List.of(outputConfig, output2Config));
            setInput(inputConfig);
            setTrigger(List.of(triggerConfig));
        }});
        fixture.getTriggers().get(0).awaitAction(fixture::start);
        fixture.ready();
        ((TestTrigger) fixture.getTriggers().get(0)).execute();

        Thread.sleep(400);
        assertEquals(JobState.WAITING, fixture.state());
        assertTrue(((TestOutput) fixture.getOutputs().get(0)).getHashes().isEmpty());
        assertTrue(((TestOutput) fixture.getOutputs().get(1)).getHashes().isEmpty());
    }

    @Test
    void testSubDataValidatorsFailingWithErrorStrategy() throws InterruptedException {
        subMappingConfig2Part.setOperations(List.of(falseValidator));
        subMappingConfig2Part.setValidatorErrorStrategy(ErrorStrategy.ERROR);
        ServiceJob fixture = new ServiceJob(new ServiceJobConfig() {{
            setName("test");
            setMapping(mappingConfig);
            setOutput(List.of(outputConfig, output2Config));
            setInput(inputConfig);
            setTrigger(List.of(triggerConfig));
        }});
        fixture.getTriggers().get(0).awaitAction(fixture::start);
        fixture.ready();
        ((TestTrigger) fixture.getTriggers().get(0)).execute();

        Thread.sleep(400);
        assertEquals(JobState.ERROR, fixture.state());
        assertTrue(((TestOutput) fixture.getOutputs().get(0)).getHashes().isEmpty());
        assertTrue(((TestOutput) fixture.getOutputs().get(1)).getHashes().isEmpty());
    }

    @Test
    void testSubDataValidatorsFailingWithSkipDatasetPiece() throws InterruptedException {
        subMappingConfig2Part.setOperations(List.of(falseValidator));
        subMappingConfig2Part.setValidatorErrorStrategy(ErrorStrategy.SKIP_DATASET);
        ServiceJob fixture = new ServiceJob(new ServiceJobConfig() {{
            setName("test");
            setMapping(mappingConfig);
            setOutput(List.of(outputConfig, output2Config));
            setInput(inputConfig);
            setTrigger(List.of(triggerConfig));
        }});
        fixture.getTriggers().get(0).awaitAction(fixture::start);
        fixture.ready();
        ((TestTrigger) fixture.getTriggers().get(0)).execute();

        Thread.sleep(400);
        assertEquals(List.of(content1.value().get().hashCode()), ((TestOutput) fixture.getOutputs().get(0)).getHashes());
        assertEquals(List.of(content2.value().get().hashCode()), ((TestOutput) fixture.getOutputs().get(1)).getHashes());
    }

    @Test
    void testValidatorsFailingWithError() throws InterruptedException {
        mappingPart1.setOperations(List.of(falseValidator));
        mappingPart1.setValidatorErrorStrategy(ErrorStrategy.ERROR);
        ServiceJob fixture = new ServiceJob(new ServiceJobConfig() {{
            setName("test");
            setMapping(mappingConfig);
            setOutput(List.of(outputConfig, output2Config));
            setInput(inputConfig);
            setTrigger(List.of(triggerConfig));
        }});
        fixture.getTriggers().get(0).awaitAction(fixture::start);
        fixture.ready();

        ((TestTrigger) fixture.getTriggers().get(0)).execute();

        Thread.sleep(700);
        assertEquals(JobState.ERROR, fixture.state());
        assertTrue(((TestOutput) fixture.getOutputs().get(0)).getHashes().isEmpty());
    }

    @Test
    void testValidatorsFailingWithSkipDatasetPiece() throws InterruptedException {
        mappingPart1.setOperations(List.of(falseValidator));
        mappingPart1.setValidatorErrorStrategy(ErrorStrategy.SKIP_DATASET_PIECE);
        ServiceJob fixture = new ServiceJob(new ServiceJobConfig() {{
            setName("test");
            setMapping(mappingConfig);
            setOutput(List.of(outputConfig, output2Config));
            setInput(inputConfig);
            setTrigger(List.of(triggerConfig));
        }});
        fixture.getTriggers().get(0).awaitAction(fixture::start);
        fixture.ready();
        ((TestTrigger) fixture.getTriggers().get(0)).execute();
        assertTrue(((TestOutput) fixture.getOutputs().get(0)).getHashes().isEmpty());

        Thread.sleep(400);
        assertEquals(List.of(0), ((TestOutput) fixture.getOutputs().get(0)).getHashes());
        assertEquals(List.of(content2.value().get().hashCode()), ((TestOutput) fixture.getOutputs().get(1)).getHashes());
    }

    @Test
    void testValidatorsFailingWithSkipDataset() throws InterruptedException {
        mappingPart1.setOperations(List.of(falseValidator));
        mappingPart1.setValidatorErrorStrategy(ErrorStrategy.CONTINUE_NEXT_READ);
        ServiceJob fixture = new ServiceJob(new ServiceJobConfig() {{
            setName("test");
            setMapping(mappingConfig);
            setOutput(List.of(outputConfig, output2Config));
            setInput(inputConfig);
            setTrigger(List.of(triggerConfig));
        }});
        fixture.getTriggers().get(0).awaitAction(fixture::start);
        fixture.ready();

        assertTrue(((TestOutput) fixture.getOutputs().get(0)).getHashes().isEmpty());

        Thread.sleep(400);
        assertEquals(Collections.emptyList(), ((TestOutput) fixture.getOutputs().get(0)).getHashes());
    }

    @Test
    void testStart() throws InterruptedException {
        inputConfig.setShouldWaitForSomeTestAssertions(true);
        mappingPart1.setOperations(List.of(new Validator() {
            @Override
            public boolean validate(final DatasetPiece currentPiece, final MappingPart mappingPart) {
                validated = true;
                return true;
            }

            @Override
            public String getType() {
                return null;
            }
        }));
        mappingPart2.setOperations(List.of(new Manipulator() {
            @Override
            public void manipulate(final DatasetPiece currentPiece, final MappingPart mappingPart) {
                manipulated = true;
            }

            @Override
            public String getType() {
                return null;
            }
        }));
        allFalse(TestInput.isEnd(), TestInput.isStart());

        ServiceJob fixture = new ServiceJob(new ServiceJobConfig() {{
            setName("test");
            setMapping(mappingConfig);
            setOutput(List.of(outputConfig, output2Config));
            setInput(inputConfig);
            setTrigger(List.of(triggerConfig, trigger2Config));
        }});
        assertEquals(JobState.INITIALIZED, fixture.state());
        allFalse(TestInput.isEnd(), TestInput.isStart(), ((TestOutput) fixture.getOutputs().get(0)).isStartRun(),
                ((TestOutput) fixture.getOutputs().get(0)).isEndRun(),
                ((TestOutput) fixture.getOutputs().get(1)).isStartRun(),
                ((TestOutput) fixture.getOutputs().get(0)).isEndRun(),
                fixture.getTriggers().get(0).isRunning(), fixture.getTriggers().get(0).isRunning());
        fixture.getTriggers().get(0).awaitAction(fixture::start);
        fixture.getTriggers().get(1).awaitAction(fixture::start);
        fixture.ready();
        assertEquals(JobState.WAITING, fixture.state());
        allFalse(validated, manipulated, TestInput.isEnd(), TestInput.isStart(),
                ((TestOutput) fixture.getOutputs().get(0)).isStartRun(),
                ((TestOutput) fixture.getOutputs().get(0)).isEndRun(),
                ((TestOutput) fixture.getOutputs().get(1)).isStartRun(),
                ((TestOutput) fixture.getOutputs().get(1)).isEndRun());
        allTrue(fixture.getTriggers().get(0).isRunning());
        assertTrue(((TestOutput) fixture.getOutputs().get(0)).getHashes().isEmpty());
        ((TestTrigger) fixture.getTriggers().get(0)).execute();
        Thread.sleep(400);

        assertEquals(JobState.RUNNING, fixture.state());
        Thread.sleep(1000);
        assertEquals(JobState.WAITING, fixture.state());
        allTrue(validated, manipulated, TestInput.isEnd(), TestInput.isStart(), ((TestOutput) fixture.getOutputs().get(0)).isStartRun(), ((TestOutput) fixture.getOutputs().get(0)).isEndRun(),
                ((TestOutput) fixture.getOutputs().get(1)).isStartRun(), ((TestOutput) fixture.getOutputs().get(1)).isEndRun(), fixture.getTriggers().get(0).isRunning(),
                fixture.getTriggers().get(1).isRunning());
        assertEquals(List.of(content1.value().get().hashCode()), ((TestOutput) fixture.getOutputs().get(0)).getHashes());
        assertEquals(List.of(content2.value().get().hashCode()), ((TestOutput) fixture.getOutputs().get(1)).getHashes());
        fixture.stop();
        assertEquals(JobState.STOPPED, fixture.state());
    }

    @Test
    void runningImportWithExceptionShouldStopOnErrorState() throws InterruptedException {
        inputConfig.setShouldTriggerException(true);
        ServiceJob fixture = new ServiceJob(new ServiceJobConfig() {{
            setName("test");
            setMapping(mappingConfig);
            setOutput(List.of(outputConfig, output2Config));
            setInput(inputConfig);
            setTrigger(List.of(triggerConfig));
        }});
        fixture.getTriggers().get(0).awaitAction(fixture::start);
        fixture.ready();

        assertTrue(((TestOutput) fixture.getOutputs().get(0)).getHashes().isEmpty());
        ((TestTrigger) fixture.getTriggers().get(0)).execute();
        Thread.sleep(400);
        assertEquals(JobState.ERROR, fixture.state());
        Thread.sleep(500);
        assertEquals(JobState.ERROR, fixture.state());
    }

}