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

import com.webxells.dis.api.MappingPortrayal;
import com.webxells.dis.api.config.MappingConfiguration;
import com.webxells.dis.api.error.InputOutputError;
import com.webxells.dis.api.error.InvalidApi;
import com.webxells.dis.api.output.Output;
import com.webxells.dis.base.SimpleMappingPortrayal;
import com.webxells.dis.base.config.SimpleJobConfig;
import com.webxells.dis.base.config.SimpleMappingPoint;
import com.webxells.dis.base.output.DevNull;
import com.webxells.dis.base.validator.Succeed;
import com.webxells.dis.event.EventManager;
import com.webxells.dis.test.cases.ConfigurationBuilder;
import com.webxells.dis.test.cases.SimpleTestCase;
import com.webxells.dis.test.cases.TestWorkflowSecurity;
import com.webxells.dis.test.example.input.TestInputConfig;
import com.webxells.dis.workflow.service.ServiceJob;
import com.webxells.dis.workflow.service.event.DatasetSkipped;
import com.webxells.dis.workflow.service.event.DatasetSkippedByValidator;
import com.webxells.dis.workflow.service.event.InvalidDataset;
import com.webxells.dis.workflow.service.event.JobEnded;
import com.webxells.dis.workflow.service.event.JobError;
import com.webxells.dis.workflow.service.event.JobStarted;
import com.webxells.dis.workflow.service.event.NewDataset;
import com.webxells.dis.workflow.service.event.OutputWritten;
import com.webxells.dis.workflow.service.meta.output.CountingFilteredOutput;
import com.webxells.dis.workflow.service.meta.output.CountingFilteredOutputConfig;
import java.util.List;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LastRunTest extends SimpleTestCase {

    @Test
    void test() throws InputOutputError, InvalidApi {
        setUpWorkflowSecurity();
        String outputName1 = random();
        Output<?> output1 = Mockito.mock(Output.class);
        Mockito.when(output1.getName()).thenReturn(outputName1);
        String outputName2 = random();
        Output<?> output2 = Mockito.mock(Output.class);
        Mockito.when(output2.getName()).thenReturn(outputName2);
        LastRunConfig config = new LastRunConfig();
        config.setName(random());
        config.setConfigName(config.getName());
        MappingConfiguration mappingConfiguration = newConfiguration()
                .addPart(ConfigurationBuilder.newPart(ConfigurationBuilder.PartBuilder.PartBuilderState.RANDOM)
                        .setInput(new SimpleMappingPoint(config.getName(), "datasets"))
                )
                .addPart(ConfigurationBuilder.newPart(ConfigurationBuilder.PartBuilder.PartBuilderState.RANDOM)
                        .setInput(new SimpleMappingPoint(config.getName(), "skipped"))
                )
                .addPart(ConfigurationBuilder.newPart(ConfigurationBuilder.PartBuilder.PartBuilderState.RANDOM)
                        .setInput(new SimpleMappingPoint(config.getName(), "invalid"))
                )
                .addPart(ConfigurationBuilder.newPart(ConfigurationBuilder.PartBuilder.PartBuilderState.RANDOM)
                        .setInput(new SimpleMappingPoint(config.getName(), "output " + outputName1))
                )
                .addPart(ConfigurationBuilder.newPart(ConfigurationBuilder.PartBuilder.PartBuilderState.RANDOM)
                        .setInput(new SimpleMappingPoint(config.getName(), "output " + outputName2))
                )
                .addPart(ConfigurationBuilder.newPart(ConfigurationBuilder.PartBuilder.PartBuilderState.RANDOM)
                        .setInput(new SimpleMappingPoint(config.getName(), "output unknown"))
                )
                .addPart(ConfigurationBuilder.newPart(ConfigurationBuilder.PartBuilder.PartBuilderState.RANDOM)
                        .setInput(new SimpleMappingPoint(config.getName(), "errors"))
                )
                .addPart(ConfigurationBuilder.newPart(ConfigurationBuilder.PartBuilder.PartBuilderState.RANDOM)
                        .setInput(new SimpleMappingPoint(config.getName(), "start date"))
                )
                .addPart(ConfigurationBuilder.newPart(ConfigurationBuilder.PartBuilder.PartBuilderState.RANDOM)
                        .setInput(new SimpleMappingPoint(config.getName(), "end date"))
                )
                .addPart(ConfigurationBuilder.newPart(ConfigurationBuilder.PartBuilder.PartBuilderState.RANDOM)
                        .setInput(new SimpleMappingPoint(config.getName(), "start date yyyy-MM-dd"))
                )
                .addPart(ConfigurationBuilder.newPart(ConfigurationBuilder.PartBuilder.PartBuilderState.RANDOM)
                        .setInput(new SimpleMappingPoint(config.getName(), "end date yyyy-MM-dd"))
                )
                .addPart(ConfigurationBuilder.newPart(ConfigurationBuilder.PartBuilder.PartBuilderState.RANDOM)
                        .setInput(new SimpleMappingPoint(config.getName(), "duration seconds"))
                )
                .addPart(ConfigurationBuilder.newPart(ConfigurationBuilder.PartBuilder.PartBuilderState.RANDOM)
                        .setInput(new SimpleMappingPoint(config.getName(), "duration NaNos"))
                )
                .addPart(ConfigurationBuilder.newPart(ConfigurationBuilder.PartBuilder.PartBuilderState.RANDOM)
                        .setInput(new SimpleMappingPoint(config.getName(), "duration unknown"))
                )
                .build();
        SimpleJobConfig jobConfig = new SimpleJobConfig(config.getName());
        jobConfig.setTrigger(List.of());
        jobConfig.setInput(new TestInputConfig());
        jobConfig.setOutput(List.of());
        jobConfig.setMapping(mappingConfiguration);
        ServiceJob serviceJob = Mockito.mock(ServiceJob.class);
        TestWorkflowSecurity.setJobs(config.getName(), serviceJob);
        EventManager.ContextProxy event = EventManager.instance().new ContextProxy(serviceJob);

        LastRun fixture = new LastRun(config);
        fixture.validate();

        CountingFilteredOutputConfig outputConfig = new CountingFilteredOutputConfig();
        outputConfig.setName(outputName1);
        outputConfig.setCountingFilterEntries(List.of(
                new CountingFilteredOutputConfig.CountingFilterEntry() {{
                    aliasName = outputName1;
                    output = new DevNull.Configuration();
                    validator = new Succeed();
                    portrayal = new SimpleMappingPortrayal(MappingPortrayal.Source.OUTPUT, "test", "test");
                }}
        ));
        MappingConfiguration outputMapping = newConfiguration()
                .addPart(ConfigurationBuilder.newPart()
                        .setOutput(new SimpleMappingPoint("test", "test"))
                )
                .build();
        outputMapping.setJobConfig(jobConfig);
        CountingFilteredOutput output = new CountingFilteredOutput(outputConfig);


        event.trigger(new JobStarted(serviceJob));
        int[] ranges = {randomMax(666), randomMax(666), randomMax(666), randomMax(666), randomMax(666), randomMax(666)};
        IntStream.range(0, ranges[0]).forEach(a -> event.trigger(new NewDataset(a)));
        IntStream.range(0, ranges[1]).forEach(a -> event.trigger(new DatasetSkipped(a)));
        IntStream.range(0, ranges[2]).forEach(a -> event.trigger(new InvalidDataset(a, mappingConfiguration)));
        IntStream.range(0, ranges[3]).forEach(a -> event.trigger(new DatasetSkippedByValidator()));
        IntStream.range(0, ranges[4]).forEach(a -> {
            try {
                output.write(outputMapping);
            } catch (InputOutputError e) {
                throw new RuntimeException(e);
            }
        });
        IntStream.range(0, ranges[5]).forEach(a -> event.trigger(new OutputWritten(a, mappingConfiguration,
                output2)));
        Throwable throwable = new RuntimeException(random(), new RuntimeException(random()));
        event.trigger(new JobError(serviceJob, throwable));
        event.trigger(new JobEnded(serviceJob));

        fixture.start();
        assertTrue(fixture.hasNext());
        fixture.read(mappingConfiguration);
        assertFalse(fixture.hasNext());
        fixture.end();

        assertEquals(String.valueOf(ranges[0]), mappingConfiguration.parts().get(0).value().get());
        assertEquals(String.valueOf(ranges[1] + ranges[3]), mappingConfiguration.parts().get(1).value().get());
        assertEquals(String.valueOf(ranges[2] - ranges[3]), mappingConfiguration.parts().get(2).value().get());
        assertEquals(String.valueOf(ranges[4]), mappingConfiguration.parts().get(3).value().get());
        assertEquals(String.valueOf(ranges[5]), mappingConfiguration.parts().get(4).value().get());
        assertTrue(mappingConfiguration.parts().get(5).value().isEmpty());
        assertTrue(mappingConfiguration.parts().get(6).value().get().startsWith(String.format("Error on %s%nStackTrace: %s: %s",
                serviceJob.toString(), throwable.getClass().getName(), throwable.getMessage())));
        assertTrue(mappingConfiguration.parts().get(7).value().get()
                .matches("^\\d\\d\\d\\d-\\d\\d-\\d\\dT\\d\\d:\\d\\d:\\d\\d\\.\\d+$"));
        assertTrue(mappingConfiguration.parts().get(8).value().get()
                .matches("^\\d\\d\\d\\d-\\d\\d-\\d\\dT\\d\\d:\\d\\d:\\d\\d\\.\\d+$"));
        assertTrue(mappingConfiguration.parts().get(9).value().get().matches("^\\d\\d\\d\\d-\\d\\d-\\d\\d$"));
        assertTrue(mappingConfiguration.parts().get(10).value().get().matches("^\\d\\d\\d\\d-\\d\\d-\\d\\d$"));
        assertEquals("0",//String.valueOf(last.getStart().until(last.getEnd(), ChronoUnit.NANOS)),
                mappingConfiguration.parts().get(11).value().get());
        assertTrue(mappingConfiguration.parts().get(12).value().get().matches("^\\d{4,}$"));
        assertTrue(mappingConfiguration.parts().get(13).value().isEmpty());
    }

}