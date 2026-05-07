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

class RangeDecimalTest extends SimpleTestCase {

    @Test
    void test() throws InvalidApi, InputOutputError {
        RangeDecimalConfig config = new RangeDecimalConfig();
        assertThrows(InvalidApi.class, () -> new RangeDecimal(config).validate());
        config.setName(random());
        config.setStep("0");
        assertThrows(InvalidApi.class, () -> new RangeDecimal(config).validate());
        config.setStart("5");
        config.setEnd("4");
        config.setStep("1");
        assertThrows(InvalidApi.class, () -> new RangeDecimal(config).validate());
        config.setStart("4");
        config.setEnd("5");
        config.setStep("-1");
        assertThrows(InvalidApi.class, () -> new RangeDecimal(config).validate());
        config.setStart("1286282.311982946");
        config.setEnd("7286282.7118345576");
        config.setStep("556.1252344");
        config.setName(random());
        config.validate();
        MappingConfiguration configuration = newConfiguration()
                .addPart(ConfigurationBuilder.newPart(RANDOM))
                .setFollowingInputResource(config.getName())
                .addPart(ConfigurationBuilder.newPart(RANDOM))
                .addPart(ConfigurationBuilder.newPart(RANDOM))
                .build();
        RangeDecimal fixture = new RangeDecimal(config);
        fixture.start();
        int rounds = 0;
        while (fixture.hasNext()) {
            configuration.clear();
            rounds++;
            assertEquals(2, fixture.read(configuration));
        }
        fixture.end();
        assertEquals(10789, rounds);
        assertEquals("7285761.340690146", configuration.parts().get(1).value().get());

    }

}