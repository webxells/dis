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
package com.webxells.dis.base.output;

import com.webxells.dis.api.error.InputOutputError;
import com.webxells.dis.api.validator.Validator;
import com.webxells.dis.base.SimpleMappingPortrayal;
import com.webxells.dis.base.config.SimpleMappingConfiguration;
import com.webxells.dis.base.config.SimpleMappingPart;
import com.webxells.dis.base.config.SimpleMappingPoint;
import com.webxells.dis.base.output.FilteredOutputConfig.FilterEntry;
import com.webxells.dis.test.example.output.IncrementTestOutput;
import com.webxells.dis.test.example.output.TestOutputConfig;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.mockito.ArgumentMatchers.any;

class FilteredOutputTest {


    @Test
    void test() throws InputOutputError {
        IncrementTestOutput.reset();
        Validator validator1 = Mockito.mock(Validator.class);
        Mockito.when(validator1.validate(any(), any()))
                .thenReturn(true)
                .thenReturn(false)
                .thenReturn(false);
        Validator validator2 = Mockito.mock(Validator.class);
        Mockito.when(validator2.validate(any(), any()))
                .thenReturn(false)
                .thenReturn(true)
                .thenReturn(false);
        SimpleMappingConfiguration mappingConfiguration = new SimpleMappingConfiguration();
        mappingConfiguration.setParts(List.of(
                new SimpleMappingPart(mappingConfiguration, new SimpleMappingPoint("test", "path"),
                        new SimpleMappingPoint("test", "path"))
        ));
        FilteredOutputConfig config = new FilteredOutputConfig();
        config.setFilterEntries(List.of(
                new FilterEntry() {{
                    output = new TestOutputConfig("test", IncrementTestOutput.class.getName());
                    validator = validator1;
                    portrayal = new SimpleMappingPortrayal("test", "path");
                }}, new FilterEntry() {{
                    output = new TestOutputConfig("test", IncrementTestOutput.class.getName());
                    validator = validator2;
                    portrayal = new SimpleMappingPortrayal("test", "path");
                }},
                new FilterEntry() {{
                    output = new TestOutputConfig("test", IncrementTestOutput.class.getName());
                }}
        ));
        FilteredOutput fixture = new FilteredOutput(config);
        fixture.write(mappingConfiguration);
        fixture.write(mappingConfiguration);
        fixture.write(mappingConfiguration);
        Assertions.assertEquals(5, IncrementTestOutput.counter);
    }
}