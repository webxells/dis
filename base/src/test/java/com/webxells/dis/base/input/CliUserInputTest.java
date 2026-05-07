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
import com.webxells.dis.base.config.SimpleMappingPoint;
import com.webxells.dis.test.cases.ConfigurationBuilder;
import com.webxells.dis.test.cases.SimpleTestCase;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

class CliUserInputTest extends SimpleTestCase {
    private static final InputStream originalIn = System.in;
    private static final PrintStream originalOut = System.out;

    @AfterAll
    static void setup() {
        System.setIn(originalIn);
        System.setOut(originalOut);
    }

    @Test
    void test() throws InputOutputError {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        String userInput = String.format("%s%n%s%n%s", random(), random(), random());
        System.setIn(new ByteArrayInputStream(userInput.getBytes()));
        System.setOut(new PrintStream(out));
        CliUserInputConfig config = new CliUserInputConfig();
        config.setName(random());
        config.setDefaultValue(random());
        config.setMaxLinesToFetch(2);
        config.setPrompt(random());
        config.setTimeoutSeconds(5);
        String expectedUser = userInput.substring(0, userInput.indexOf(config.getEndOfLine(), userInput.indexOf(config.getEndOfLine())+ 1));
        MappingConfiguration mappingConfiguration = newConfiguration()
                .addPart(ConfigurationBuilder.newPart()
                        .setInput(new SimpleMappingPoint(config.getName(), "test1"))
                ).addPart(ConfigurationBuilder.newPart()
                        .setInput(new SimpleMappingPoint(config.getName(), "test2"))
                )
                .build();

        CliUserInput fixture = new CliUserInput(config);
        assertTrue(fixture.hasNext());
        assertEquals(2, fixture.read(mappingConfiguration));
        assertFalse(fixture.hasNext());
        assertEquals(config.getPrompt().get().concat(": "), out.toString());
        assertEquals(expectedUser, mappingConfiguration.parts().get(0).value().get());
        assertEquals(expectedUser, mappingConfiguration.parts().get(1).value().get());
    }

    @Test
    void testTimeout() throws InputOutputError {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        String userInput = String.format("%s%n%s%n%s", random(), random(), random());
        System.setIn(originalIn);
        System.setOut(new PrintStream(out));
        CliUserInputConfig config = new CliUserInputConfig();
        config.setName(random());
        config.setDefaultValue(random());
        config.setTimeoutSeconds(2);
        MappingConfiguration mappingConfiguration = newConfiguration()
                .addPart(ConfigurationBuilder.newPart()
                        .setInput(new SimpleMappingPoint(config.getName(), "test1"))
                ).addPart(ConfigurationBuilder.newPart()
                        .setInput(new SimpleMappingPoint(config.getName(), "test2"))
                )
                .build();

        CliUserInput fixture = new CliUserInput(config);
        long startTime = System.currentTimeMillis();
        assertEquals(2, fixture.read(mappingConfiguration));
        long usedTime = System.currentTimeMillis() - startTime;
        assertTrue(usedTime >= 2000, "not >= 2000: " + usedTime);
        // Over Maven this will fail => >10s
        // assertTrue(usedTime < 000, "not < 3000: " + usedTime);
        assertEquals("", out.toString());
        assertEquals(config.getDefaultValue(), mappingConfiguration.parts().get(0).value().get());
        assertEquals(config.getDefaultValue(), mappingConfiguration.parts().get(1).value().get());
    }
}