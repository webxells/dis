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
import com.webxells.dis.api.error.DisException;
import com.webxells.dis.api.error.InvalidApi;
import com.webxells.dis.api.workflow.Job.JobState;
import com.webxells.dis.test.cases.ConfigurationBuilder;
import com.webxells.dis.test.cases.SimpleTestCase;
import com.webxells.dis.workflow.service.ServiceJob;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class JobStatusTest extends SimpleTestCase {

    @Test
    void test() throws DisException {
        JobStatus.registerJobs(null);
        String reference = random();
        String name1 = random();
        String name2 = random();
        String name3 = random();
        LocalDateTime date1 = LocalDateTime.now();
        LocalDateTime date2 = LocalDateTime.now().minusDays(1);
        List<ServiceJob> serviceJobs = List.of(
                mock(ServiceJob.class),
                mock(ServiceJob.class),
                mock(ServiceJob.class)
        );
        when(serviceJobs.get(0).getJobName())
                .thenReturn(name1);
        when(serviceJobs.get(1).getJobName())
                .thenReturn(name2);
        when(serviceJobs.get(2).getJobName())
                .thenReturn(name3);
        when(serviceJobs.get(0).state())
                .thenReturn(JobState.WAITING);
        when(serviceJobs.get(1).state())
                .thenReturn(JobState.RUNNING);
        when(serviceJobs.get(2).state())
                .thenReturn(JobState.ERROR);
        when(serviceJobs.get(0).getLastStart())
                .thenReturn(Optional.of(date1));
        when(serviceJobs.get(1).getLastStart())
                .thenReturn(Optional.of(date2));
        when(serviceJobs.get(2).getLastStart())
                .thenReturn(Optional.empty());
        JobStatus.Config config = new JobStatus.Config();
        config.setName(reference);
        MappingConfiguration mappingConfiguration = newConfiguration()
                .addPart(ConfigurationBuilder.newPart()
                        .setInput(reference, "ID")
                ).addPart(ConfigurationBuilder.newPart()
                        .setInput(reference, "NAME")
                ).addPart(ConfigurationBuilder.newPart()
                        .setInput(reference, "STATE")
                ).addPart(ConfigurationBuilder.newPart()
                        .setInput(reference, "LAST-START")
                ).addPart(ConfigurationBuilder.newPart()
                        .setInput(reference, random())
                )
                .addPart(ConfigurationBuilder.newPart(ConfigurationBuilder.PartBuilder.PartBuilderState.RANDOM))
                .build();
        JobStatus fixture = new JobStatus(config);

        assertThrows(InvalidApi.class, fixture::start);
        JobStatus.registerJobs(serviceJobs);
        fixture.start();

        assertTrue(fixture::hasNext);
        assertEquals(4, fixture.read(mappingConfiguration));

        assertEquals("0", mappingConfiguration.parts().get(0).value().get());
        assertEquals(name1, mappingConfiguration.parts().get(1).value().get());
        assertEquals("WAITING", mappingConfiguration.parts().get(2).value().get());
        assertEquals(date1.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME), mappingConfiguration.parts().get(3).value().get());

        mappingConfiguration.clear();
        assertTrue(fixture::hasNext);
        assertEquals(4, fixture.read(mappingConfiguration));

        assertEquals("1", mappingConfiguration.parts().get(0).value().get());
        assertEquals(name2, mappingConfiguration.parts().get(1).value().get());
        assertEquals("RUNNING", mappingConfiguration.parts().get(2).value().get());
        assertEquals(date2.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME), mappingConfiguration.parts().get(3).value().get());

        mappingConfiguration.clear();
        assertTrue(fixture::hasNext);
        assertEquals(4, fixture.read(mappingConfiguration));

        assertEquals("2", mappingConfiguration.parts().get(0).value().get());
        assertEquals(name3, mappingConfiguration.parts().get(1).value().get());
        assertEquals("ERROR", mappingConfiguration.parts().get(2).value().get());
        assertEquals("n/a", mappingConfiguration.parts().get(3).value().get());

        assertFalse(fixture::hasNext);
        fixture.end();
        assertFalse(fixture::hasNext);

    }

}