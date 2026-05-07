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
package com.webxells.dis.memory;

import com.webxells.dis.api.config.MappingConfiguration;
import com.webxells.dis.base.config.SimpleMappingPoint;
import com.webxells.dis.memory.registry.MemoryRegistry;
import com.webxells.dis.test.cases.ConfigurationBuilder;
import com.webxells.dis.test.cases.SimpleTestCase;
import java.util.Set;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class IndexedMemoryOutputTest extends SimpleTestCase {

    @Test
    void test() {
        String name = random();
        String key = random();
        String content1 = random();
        String content2 = random();
        MappingConfiguration mappingConfig = newConfiguration()
                .setFollowingOutputResource(name)
                .addPart(ConfigurationBuilder.newPart()
                        .setOutput(new SimpleMappingPoint(name, key))
                        .withContent(content1)
                )
                .addPart(ConfigurationBuilder.newPart(ConfigurationBuilder.PartBuilder.PartBuilderState.RANDOM)
                        .withContent(content2)
                )
                .build();
        IndexedMemoryOutputConfig config = new IndexedMemoryOutputConfig();
        config.setName(name);
        config.setIndexFields(Set.of(""));
        IndexedMemoryOutput fixture = new IndexedMemoryOutput(config);
        fixture.write(mappingConfig);
        MemoryRegistry registry = MemoryRegistry.getExistent(name);
        assertTrue(registry.hasNext());
    }

}