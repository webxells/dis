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
package com.webxells.dis.hash.input;

import com.webxells.dis.api.config.MappingConfiguration;
import com.webxells.dis.api.error.InputOutputError;
import com.webxells.dis.base.config.SimpleMappingPoint;
import com.webxells.dis.hash.SimpleHashManager;
import com.webxells.dis.hash.engine.Murmur3;
import com.webxells.dis.test.TestResource;
import com.webxells.dis.test.cases.ConfigurationBuilder;
import com.webxells.dis.test.cases.SimpleTestCase;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HashTest extends SimpleTestCase {

    @Test
    void test() throws InputOutputError {
        HashConfig config = new HashConfig();
        SimpleHashManager manager = new SimpleHashManager();
        Murmur3 engine = new Murmur3();
        engine.setSeed(1234);
        manager.setEngine(engine);
        config.setManager(manager);
        config.setName(random());
        config.setReceiver(new TestResource("test1"));
        MappingConfiguration mappingConfiguration = newConfiguration()
                .addPart(ConfigurationBuilder.newPart()
                        .setInput(new SimpleMappingPoint(config.getName(), random()))
                )
                .build();
        Hash fixture = new Hash(config);
        fixture.start();
        assertTrue(fixture.hasNext());
        fixture.read(mappingConfiguration);
        assertEquals("de37f030b50d3880dbb362b264991bbe", mappingConfiguration.parts().get(0).value().get());
        assertFalse(fixture.hasNext());
    }
}