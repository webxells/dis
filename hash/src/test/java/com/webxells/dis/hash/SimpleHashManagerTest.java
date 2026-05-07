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
package com.webxells.dis.hash;

import com.webxells.dis.api.hash.Engine;
import com.webxells.dis.api.hash.Task;
import com.webxells.dis.base.SimpleDatasetPiece;
import com.webxells.dis.base.SimpleMappingPortrayal;
import com.webxells.dis.base.config.SimpleMappingConfiguration;
import com.webxells.dis.base.config.SimpleMappingPart;
import com.webxells.dis.base.config.SimpleMappingPoint;
import com.webxells.dis.hash.engine.Murmur3;
import com.webxells.dis.test.cases.ConfigurationBuilder;
import com.webxells.dis.test.cases.SimpleTestCase;
import java.io.IOException;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.when;

class SimpleHashManagerTest extends SimpleTestCase {

    @Test
    void testFile() throws IOException {
        Murmur3 murmur3 = new Murmur3();
        murmur3.setSeed(1234);
        assertEquals("de37f030b50d3880dbb362b264991bbe", murmur3.convert(getResourceFileStream("test1")));
        murmur3.setSeed(4321);
        assertEquals("add50cffaf7ccff0df8df8f10c0ea30c", murmur3.convert(getResourceFileStream("test1")));
        assertEquals("83c5385382b7dbbd704d0a1fb7515ed6", murmur3.convert(getResourceFileStream("test2")));
    }

    @Test
    void testSimple() {
        String in_p_1 = random("ip1");
        String in_p_2 = random("ip2");
        String in_p_3 = random("ip3");
        String in_p_4 = random("ip4");
        String in_p_5 = random("ip5");
        String in_p_6 = random("ip6");
        String in_p_7 = random("ip7");
        String in_p_8 = random("ip8");
        String in_p_9 = random("ip9");
        String in_p_10 = random("ip10");
        String in_r_1 = random("ir1");
        String in_r_2 = random("ir2");
        String in_r_3 = random("ir3");
        String in_r_4 = random("ir4");
        String in_r_5 = random("ir5");
        String in_r_6 = random("ir6");
        String in_r_7 = random("ir7");
        String in_r_8 = random("ir8");
        String in_r_9 = random("ir9");
        String in_r_10 = random("ir10");
        String value1 = random("1");
        String value2 = random("2");
        String value3 = random("3");
        String value4 = random("4");
        String value5 = random("5");
        String value6 = random("6");
        SimpleMappingConfiguration configuration = newConfiguration()
                .addPart(ConfigurationBuilder.newPart(ConfigurationBuilder.PartBuilder.PartBuilderState.RANDOM)
                        .setInput(new SimpleMappingPoint(in_r_1, in_p_1))
                        .setOutput(new SimpleMappingPoint(in_r_2, in_p_2))
                        .withContent(value1)
                        .addSubData(newConfiguration()
                                .addPart(ConfigurationBuilder.newPart(ConfigurationBuilder.PartBuilder.PartBuilderState.RANDOM)
                                        .setInput(new SimpleMappingPoint(in_r_3, in_p_3))
                                        .setOutput(new SimpleMappingPoint(in_r_4, in_p_4))
                                        .withContent(value2)
                                        .addSubData(newConfiguration()
                                                .addPart(ConfigurationBuilder.newPart(ConfigurationBuilder.PartBuilder.PartBuilderState.RANDOM)
                                                        .setInput(new SimpleMappingPoint(in_r_5, in_p_5))
                                                        .setOutput(new SimpleMappingPoint(in_r_6, in_p_6))
                                                        .withContent(value3))
                                                .build())
                                )
                                .build())
                )
                .addPart(ConfigurationBuilder.newPart(ConfigurationBuilder.PartBuilder.PartBuilderState.RANDOM)
                        .setInput(new SimpleMappingPoint(in_r_7, in_p_7))
                        .setOutput(new SimpleMappingPoint(in_r_8, in_p_8))
                        .withContent(value4)
                        .addSubData(newConfiguration()
                                .addPart(ConfigurationBuilder.newPart(ConfigurationBuilder.PartBuilder.PartBuilderState.RANDOM)
                                        .setInput(new SimpleMappingPoint(in_r_9, in_p_9))
                                        .setOutput(new SimpleMappingPoint(in_r_10, in_p_10))
                                        .withContent(value5, value6))
                                .build())
                )
                .build();

        Engine engine = Mockito.mock(Engine.class);
        SimpleHashManager fixture = new SimpleHashManager();
        fixture.setHashStrategy(HashStrategy.SIMPLE);
        fixture.setEngine(engine);
        final Task task = fixture.newTask();

        String randomString = random("some random string");
        String hashResponse1 = random("hash1");

        when(engine.convert(randomString)).thenReturn(hashResponse1);
        final String hash1 = task.hash(randomString);
        assertSame(hashResponse1, hash1);


        String hashResponse2 = random("hash2");
        String hashResponse3 = random("hash3");
        String hashResponse4 = random("hash4");
        String hashResponse5 = random("hash5");
        String hashResponse6 = random("hash6");
        String hashResponse7 = random("hash7");

        when(engine.convert(String.format("%s-%s-%s-%s-%ssubsub", in_p_5, in_r_5, in_p_6, in_r_6, value3))).thenReturn(hashResponse2);
        when(engine.convert(String.format("%ssub%s-%s-%s-%s-%ssub", hashResponse2, in_p_3, in_r_3, in_p_4, in_r_4, value2))).thenReturn(hashResponse3);
        when(engine.convert(String.format("%s-%ssub%1$s-%ssub", String.format("%s-%s-%s-%s", in_p_9, in_r_9, in_p_10, in_r_10), value5, value6))).thenReturn(hashResponse4);

        when(engine.convert(String.format("%s%s-%s-%s-%s-%s%s%s-%s-%s-%s-%s",
                hashResponse3, in_p_1, in_r_1, in_p_2, in_r_2, value1, hashResponse4, in_p_7, in_r_7, in_p_8, in_r_8, value4))).thenReturn(hashResponse5);


        final String hash2 = task.hash(configuration);
        assertSame(hashResponse5, hash2);


        when(engine.convert(String.format("%s-%s%1$s-%s", String.format("%s-%s-%s-%s", in_p_9, in_r_9, in_p_10, in_r_10), value5, value6))).thenReturn(hashResponse6);
        when(engine.convert(String.format("%s-%s%1$s-%s", String.format("%s-%s-%s-%s", in_p_7, in_r_7, in_p_8, in_r_8), value4, hashResponse6))).thenReturn(hashResponse7);

        final String hash3 = task.hash(configuration.parts().get(1));
        assertSame(hashResponse7, hash3);


        String hashResponse8 = random("hash8");
        when(engine.convert(String.format("%s-%s%1$s-%s%1$s-{{NULL}}", String.format("%s-%s-%s-%s", in_p_7, in_r_7, in_p_8, in_r_8), value4, hashResponse6))).thenReturn(hashResponse8);

        String in_p_11 = random("ip11");
        String in_r_11 = random("ir11");

        configuration.addPart(new SimpleMappingPart(configuration) {{ setInput(new SimpleMappingPoint(in_p_11, in_r_11));}});

        final String hash4 = task.hash(configuration.parts().get(1), List.of(new SimpleMappingPortrayal(in_p_11, in_r_11)));
        assertSame(hashResponse8, hash4);


        String hashResponse9 = random("hash8");
        String value7 = random("value7");

        when(engine.convert(String.format("%s-%s%1$s-%s%1$s-%s", String.format("%s-%s-%s-%s", in_p_7, in_r_7, in_p_8, in_r_8), value4, hashResponse6, value7))).thenReturn(hashResponse9);


        configuration.parts().get(2).getDataset().collect(new SimpleDatasetPiece(value7));
        final String hash5 = task.hash(configuration.parts().get(1), List.of(new SimpleMappingPortrayal(in_p_11, in_r_11)));
        assertSame(hashResponse9, hash5);
    }



}