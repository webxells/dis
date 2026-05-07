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
package com.webxells.dis.base.trigger;

import com.webxells.dis.api.config.MappingConfiguration;
import com.webxells.dis.api.error.InvalidApi;
import com.webxells.dis.base.SimpleDatasetPiece;
import com.webxells.dis.base.config.SimpleMappingPart;
import com.webxells.dis.base.config.SimpleMappingPoint;
import com.webxells.dis.base.trigger.input.Reading;
import com.webxells.dis.test.cases.ConfigurationBuilder;
import com.webxells.dis.test.cases.SimpleTestCase;
import com.webxells.dis.test.example.input.TestInput;
import com.webxells.dis.test.example.input.TestInputConfig;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InputTriggerTest extends SimpleTestCase {

    @AfterEach
    void clearTestInput() {
        TestInput.setData(List.of());
    }

    @Test
    void testReading() throws InvalidApi, InterruptedException {
        final AtomicInteger triggered = new AtomicInteger();
        Reading reading = new Reading();
        reading.setDataCount(3);
        TestInput.setData(List.of(Map.of("a", new SimpleDatasetPiece(random()))));
        MappingConfiguration mappingConfiguration = newConfiguration()
                .addPart(ConfigurationBuilder.newPart()
                        .setInput("notest", "a"))
                .build();
        List<SimpleMappingPart> parts = List.of(
                new SimpleMappingPart(null, new SimpleMappingPoint("test", "a"), new SimpleMappingPoint()));
        InputTriggerConfiguration configuration = new InputTriggerConfiguration();
        configuration.setInput(new TestInputConfig("test"));
        configuration.setSleepInterval(100);
        configuration.setParts(parts);
        configuration.setTriggerStrategy(reading);
        configuration.validate();
        InputTrigger trigger = new InputTrigger(configuration);
        assertFalse(trigger.isRunning());
        trigger.awaitAction(triggered::getAndIncrement);
        Thread.sleep(500);
        assertEquals(0, triggered.get());
        reading.setDataCount(1);
        TestInput.setData(List.of(Map.of("a", new SimpleDatasetPiece(random()))));
        Thread.sleep(500);
        assertEquals(1, triggered.get());
        trigger.abort();
    }


    @Test
    void testHasAnything() throws InvalidApi, InterruptedException {
        final AtomicBoolean triggered = new AtomicBoolean();
        TestInput.setData(List.of(Map.of("a", new SimpleDatasetPiece(random()))));
        MappingConfiguration mappingConfiguration = newConfiguration()
                .addPart(ConfigurationBuilder.newPart()
                        .setInput("notest", "a"))
                .build();

        List<SimpleMappingPart> parts = List.of(
                new SimpleMappingPart(null, new SimpleMappingPoint("test", "a"), new SimpleMappingPoint()));
        InputTriggerConfiguration configuration = new InputTriggerConfiguration();
        configuration.setInput(new TestInputConfig("test"));
        assertThrows(InvalidApi.class, configuration::validate);
        configuration.setParts(List.of());
        configuration.setSleepInterval(100);
        assertThrows(InvalidApi.class, configuration::validate);
        configuration.setParts(parts);
        configuration.validate();
        InputTrigger trigger = new InputTrigger(configuration);
        assertFalse(trigger.isRunning());
        trigger.awaitAction(() -> triggered.set(true));
        Thread.sleep(500);
        assertTrue(triggered.get());
        trigger.abort();
    }

}