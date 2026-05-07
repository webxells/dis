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
import com.webxells.dis.base.SimpleDatasetPiece;
import com.webxells.dis.base.config.SimpleMappingPart;
import com.webxells.dis.base.config.SimpleMappingPoint;
import com.webxells.dis.base.trigger.InputTrigger;
import com.webxells.dis.base.trigger.InputTriggerConfiguration;
import com.webxells.dis.base.trigger.input.Reading;
import com.webxells.dis.test.cases.ConfigurationBuilder;
import com.webxells.dis.test.cases.SimpleTestCase;
import com.webxells.dis.test.example.input.TestInput;
import com.webxells.dis.test.example.input.TestInputConfig;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

class TriggerInputTest extends SimpleTestCase {

    @Test
    void test() throws InvalidApi, InterruptedException, InputOutputError {
        AtomicBoolean triggered = new AtomicBoolean();
        String data = random();
        TriggerInputConfig config = new TriggerInputConfig();
        config.setName(random());
        TriggerInput fixture  = new TriggerInput(config);
        TestInput.setData(List.of(Map.of("a", new SimpleDatasetPiece(data))));
        MappingConfiguration mappingConfiguration = newConfiguration()
                .addPart(ConfigurationBuilder.newPart()
                        .setInput("notest", "a"))
                .build();
        List<SimpleMappingPart> parts = List.of(
                new SimpleMappingPart(null,
                        new SimpleMappingPoint(config.getName(), "a"), new SimpleMappingPoint()));
        InputTriggerConfiguration triggerConfig = new InputTriggerConfiguration();
        triggerConfig.setInput(new TestInputConfig(config.getName()));
        triggerConfig.setName(config.getName());
        triggerConfig.setParts(parts);
        triggerConfig.setTriggerStrategy(new Reading());
        InputTrigger trigger = new InputTrigger(triggerConfig);
        trigger.awaitAction(() -> {
            try {
                fixture.start();
                fixture.validate();
                assertTrue(fixture.hasNext());
                assertEquals(1, fixture.read(mappingConfiguration));
                assertEquals(2, mappingConfiguration.parts().size());
                assertEquals(data, mappingConfiguration.parts().get(1).value().get());
                trigger.abort();
                triggered.set(true);
            } catch (InputOutputError | InvalidApi e) {
                fail(e.getMessage());
            }
        });
        Thread.sleep(500);
        assertTrue(triggered.get());
    }

}