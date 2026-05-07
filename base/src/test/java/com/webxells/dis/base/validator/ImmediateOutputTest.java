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
package com.webxells.dis.base.validator;

import com.webxells.dis.api.config.MappingConfiguration;
import com.webxells.dis.base.config.SimpleMappingPoint;
import com.webxells.dis.test.cases.ConfigurationBuilder;import com.webxells.dis.test.cases.SimpleTestCase;
import com.webxells.dis.test.cases.ConfigurationBuilder;
import com.webxells.dis.test.example.output.TestOutput;
import com.webxells.dis.test.example.output.TestOutputConfig;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ImmediateOutputTest extends SimpleTestCase {

    @Test
    void test() {
        TestOutput.clear();
        ImmediateOutput immediateOutput = new ImmediateOutput();
        TestOutputConfig outputConfig = new TestOutputConfig();
        outputConfig.setName(random());
        MappingConfiguration configuration = newConfiguration()
                .addPart(ConfigurationBuilder.newPart(ConfigurationBuilder.PartBuilder.PartBuilderState.RANDOM)
                        .withContent())
                .addPart(ConfigurationBuilder.newPart(ConfigurationBuilder.PartBuilder.PartBuilderState.RANDOM)
                        .withContent())
                .addPart(ConfigurationBuilder.newPart()
                        .setOutput(new SimpleMappingPoint(outputConfig.getName(), random()))
                        .withContent())
                .build();
        immediateOutput.setConfiguration(outputConfig);
        immediateOutput.validateCurrentPiece(null, configuration.parts().get(0));

        List<MappingConfiguration> allAssignedMappingConfig = TestOutput.getAllAssignedMappingConfig();
        assertEquals(1, allAssignedMappingConfig.size());
        assertSame(configuration, allAssignedMappingConfig.get(0));

        List<Integer> allHashes = TestOutput.getAllHashes();
        assertEquals(1, allHashes.size());
        assertEquals(configuration.parts().get(2).value().get().hashCode(), allHashes.get(0));

    }

}