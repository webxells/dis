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

import com.webxells.dis.api.config.MappingConfiguration;
import com.webxells.dis.api.error.DisException;
import com.webxells.dis.api.error.InvalidApi;
import com.webxells.dis.base.SimpleDatasetPiece;
import com.webxells.dis.base.output.SkipEmptyDataset.Config;
import com.webxells.dis.test.cases.ConfigurationBuilder;import com.webxells.dis.test.cases.SimpleTestCase;
import com.webxells.dis.test.cases.ConfigurationBuilder;
import com.webxells.dis.test.example.output.TestOutput;
import com.webxells.dis.test.example.output.TestOutputConfig;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static com.webxells.dis.test.cases.ConfigurationBuilder.PartBuilder.PartBuilderState.RANDOM;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SkipEmptyDatasetTest extends SimpleTestCase {

    @BeforeAll
    static void setUp() {
        TestOutput.getAllHashes().clear();
    }

    @Test
    void test() throws DisException {
        String name = randomUnique();
        String value = randomUnique();
        MappingConfiguration config = newConfiguration()
                .addPart(ConfigurationBuilder.newPart(RANDOM)
                        .setOutputReference(randomUnique())
                        .withContent()
                )
                .setFollowingOutputResource(name)
                .addPart(ConfigurationBuilder.newPart(RANDOM))
                .addPart(ConfigurationBuilder.newPart(RANDOM))
                .addPart(ConfigurationBuilder.newPart(RANDOM))
                .addPart(ConfigurationBuilder.newPart(RANDOM))
                .build();
        Config fixtureConfig = new Config();
        assertThrows(InvalidApi.class, fixtureConfig::validate);
        fixtureConfig.setOutput(new TestOutputConfig(name));
        fixtureConfig.validate();
        SkipEmptyDataset fixture = new SkipEmptyDataset(fixtureConfig);
        fixture.start();

        fixture.write(config);
        assertEquals(0, TestOutput.getAllHashes().size());

        config.parts().get(1).getDataset().collect(new SimpleDatasetPiece(value));
        fixture.write(config);
        assertEquals(1, TestOutput.getAllHashes().size());
        assertEquals(value.hashCode(), TestOutput.getAllHashes().get(0));

        config.parts().get(1).getDataset().clear();
        fixture.write(config);
        assertEquals(1, TestOutput.getAllHashes().size());
        assertEquals(value.hashCode(), TestOutput.getAllHashes().get(0));

        fixture.end();


    }

}