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
package com.webxells.dis.base.input;

import com.webxells.dis.api.config.MappingConfiguration;
import com.webxells.dis.api.error.InputOutputError;
import com.webxells.dis.api.error.InvalidApi;
import com.webxells.dis.test.cases.ConfigurationBuilder;
import com.webxells.dis.test.cases.SimpleTestCase;
import org.junit.jupiter.api.Test;

import static com.webxells.dis.test.cases.ConfigurationBuilder.PartBuilder.PartBuilderState.RANDOM;
import static org.junit.jupiter.api.Assertions.*;

class RangeTest extends SimpleTestCase {
    @Test
    void test() throws InvalidApi, InputOutputError {
        RangeConfig config = new RangeConfig();
        assertThrows(InvalidApi.class, config::validate);
        config.setName(random());
        config.validate();
        config.setStep(0);
        assertThrows(InvalidApi.class, config::validate);
        config.setStart(5);
        config.setEnd(4);
        config.setStep(1);
        assertThrows(InvalidApi.class, config::validate);
        config.setStart(4);
        config.setEnd(5);
        config.setStep(-1);
        assertThrows(InvalidApi.class, config::validate);
        config.setStart(random(1));
        config.setEnd(config.getStart() + randomMax(100));
        config.setStep(1 + randomMax(10));
        config.validate();
        MappingConfiguration configuration = newConfiguration()
                .addPart(ConfigurationBuilder.newPart(RANDOM))
                .setFollowingInputResource(config.getName())
                .addPart(ConfigurationBuilder.newPart(RANDOM))
                .addPart(ConfigurationBuilder.newPart(RANDOM))
                .build();
        Range fixture = new Range(config);
        fixture.start();
        for (int i = config.getStart(); i <= config.getEnd(); i=i+ config.getStep()) {
            configuration.clear();
            assertTrue(fixture.hasNext());
            fixture.read(configuration);
            assertTrue(configuration.parts().get(0).value().isEmpty());
            assertEquals(String.valueOf(i), configuration.parts().get(1).value().get());
            assertEquals(String.valueOf(i), configuration.parts().get(2).value().get());
        }
        assertFalse(fixture.hasNext());
        fixture.end();
    }

}