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
package com.webxells.dis.workflow.service.validator;

import com.webxells.dis.base.SimpleDatasetPiece;
import com.webxells.dis.base.config.SimpleJobConfig;
import com.webxells.dis.base.config.SimpleMappingConfiguration;
import com.webxells.dis.base.config.SimpleMappingPart;
import com.webxells.dis.base.validator.IsSet;
import com.webxells.dis.event.EventManager;
import com.webxells.dis.test.cases.SimpleTestCase;
import com.webxells.dis.test.cases.TestWorkflowSecurity;
import com.webxells.dis.test.example.input.TestInputConfig;
import com.webxells.dis.workflow.service.ServiceJob;
import com.webxells.dis.workflow.service.event.DatasetSkippedByValidator;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SkipDatasetValidatorTest extends SimpleTestCase {

    private final AtomicBoolean result = new AtomicBoolean(false);

    @Test
    void testSkip() {
        final SkipDatasetValidator fixture = new SkipDatasetValidator();
        fixture.setChild(new IsSet());

        assertFalse(test(fixture));
        assertTrue(result.get());
    }

    @Test
    void testNoSkip() {
        IsSet isSet = new IsSet();
        isSet.setNot(true);

        final SkipDatasetValidator fixture = new SkipDatasetValidator();
        fixture.setChild(isSet);

        assertTrue(test(fixture));
        assertFalse(result.get());
    }

    private boolean test(final SkipDatasetValidator fixture) {
        setUpWorkflowSecurity();
        final String jobName = "skip_test";
        SimpleMappingConfiguration mappingConfiguration = new SimpleMappingConfiguration();

        SimpleJobConfig jobConfig = new SimpleJobConfig(jobName);
        jobConfig.setTrigger(List.of());
        jobConfig.setInput(new TestInputConfig());
        jobConfig.setOutput(List.of());
        jobConfig.setMapping(mappingConfiguration);

        ServiceJob serviceJob1 = Mockito.mock(ServiceJob.class);
        TestWorkflowSecurity.setJobs(jobName, serviceJob1);

        EventManager.ContextProxy eventManager = EventManager.instance().new ContextProxy(serviceJob1);
        eventManager.on(DatasetSkippedByValidator.class, a -> result.set(true));

        return fixture.validate(new SimpleDatasetPiece(null), new SimpleMappingPart(mappingConfiguration));
    }

}