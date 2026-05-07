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

import com.webxells.dis.api.config.MappingConfiguration;
import com.webxells.dis.api.workflow.Job;
import com.webxells.dis.base.SimpleDatasetPiece;
import com.webxells.dis.base.config.SimpleDisConfig;
import com.webxells.dis.base.config.SimpleJobConfig;
import com.webxells.dis.boot.RawThreadEnvironment;
import com.webxells.dis.test.cases.ConfigurationBuilder;
import com.webxells.dis.test.cases.SimpleTestCase;
import com.webxells.dis.test.example.input.TestInput;
import com.webxells.dis.test.example.input.TestInputConfig;
import com.webxells.dis.test.example.output.TestOutput;
import com.webxells.dis.test.example.output.TestOutputConfig;
import com.webxells.dis.test.example.trigger.TestTriggerConfig;
import com.webxells.dis.workflow.service.config.ServiceJobConfig;
import com.webxells.dis.workflow.service.internal.Director;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

class DisAsServiceTest extends SimpleTestCase {

    @Test
    void testSimpleWorkflow() throws InterruptedException {
        DisAsService.registerServiceSecurity();
        AtomicBoolean triggerStart = new AtomicBoolean();
        AtomicReference<TestOutput> output = new AtomicReference<>();
        SimpleJobConfig jobConfig = new ServiceJobConfig("test", RestartPolicy.ERROR);
        SimpleJobConfig jobConfig2 = new ServiceJobConfig("test2", RestartPolicy.RESTART);
        SimpleDisConfig config = new SimpleDisConfig();
        jobConfig.setInput(new TestInputConfig("input"));
        jobConfig.setOutput(List.of(new TestOutputConfig("output") {{
            setSelfReference(output);
        }}));
        jobConfig.setTrigger(List.of(new TestTriggerConfig(triggerStart)));
        jobConfig.setMapping(newConfiguration()
                .addPart(ConfigurationBuilder.newPart()
                        .setInput("input", "field2")
                        .setOutput("output", "field1")
                )
                .build());
        jobConfig2.setInput(new TestInputConfig("input2"));
        jobConfig2.setMapping(newConfiguration()
                .addPart(ConfigurationBuilder.newPart()
                        .setInput("input2", "field2")
                        .setOutput("output", "field1")
                )
                .build());
        jobConfig2.setTrigger(List.of(new TestTriggerConfig()));
        config.addJobConfiguration(jobConfig);
        config.addJobConfiguration(jobConfig2);

        AtomicReference<ServiceJob> job1 = new  AtomicReference<>();
        AtomicReference<ServiceJob> job2 = new  AtomicReference<>();
        TestInput.setData(List.of(
                Map.of("field1", new SimpleDatasetPiece(random()),
                        "field2", new SimpleDatasetPiece(random())),
                Map.of("field1", new SimpleDatasetPiece(random()),
                        "field2", new SimpleDatasetPiece(random()))));

        ServiceSupplier serviceSupplier = new ServiceSupplier();
        serviceSupplier.setExposeJobStatus(true);
        serviceSupplier.setMonitoringInterval(100);
        serviceSupplier.setForcefullyStopJobsOnShutdown(true);
        serviceSupplier.setRuntimeEnvironment(RawThreadEnvironment.instance());
        serviceSupplier.setOpenEventContexts(Map.of("test", List.of("test2")));

        DisAsService fixture = serviceSupplier.get(config);
        assertEquals(linkedMapOf(
                "test",createStatusEntry(null, null, null, List.of()),
                "test2",createStatusEntry(null, null, null, List.of())
                ),
                fixture.getJobStatus());
        fixture.start();


        triggerStart.set(true);

        Thread.sleep(2000);

        assertSucceededFirstJob(fixture.getJobStatus());

        fixture.stop();

        Thread.sleep(1000);

        assertSucceededFirstJob(fixture.getJobStatus());

        final List<MappingConfiguration> outputAssigned = output.get().getAssignedMappingConfig();
        assertEquals(2, outputAssigned.size());
        assertEquals(TestInput.getAssignedMappingConfig(),
                outputAssigned);
    }

    private void assertSucceededFirstJob(final Map<String, Object> jobStatus) {
        assertEquals(createStatusEntry(Job.JobState.WAITING, null, null, List.of()), jobStatus.get("test2"));
        final Map<String, Object> firstJob = (Map<String, Object>) jobStatus.get("test");
        assertEquals(Job.JobState.SUCCESS, firstJob.get("state"));
        assertInstanceOf(LocalDateTime.class, firstJob.get("start"));
        assertInstanceOf(LocalDateTime.class, firstJob.get("end"));
        assertTrue(((LocalDateTime) firstJob.get("start")).isBefore(((LocalDateTime) firstJob.get("end"))));
    }

    private Map<String, Object> createStatusEntry(final Job.JobState status,
                                                  final Object start, final Object end,
                                                  final List<Map<String, Object>> history) {
        Map<String, Object> result = new HashMap<>();
        result.put("state", status);
        result.put("start", start);
        result.put("end", end);
        result.put("history", history);
        return result;

    }

}