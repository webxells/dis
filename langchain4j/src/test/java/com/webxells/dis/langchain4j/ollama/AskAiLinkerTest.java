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
package com.webxells.dis.langchain4j.ollama;

import com.webxells.dis.api.config.MappingConfiguration;
import com.webxells.dis.api.error.InputOutputError;
import com.webxells.dis.test.cases.ConfigurationBuilder;
import com.webxells.dis.test.cases.SimpleTestCase;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

class AskAiLinkerTest extends SimpleTestCase {

    @Test
    void test() throws InputOutputError {
        String name = random();
        String prompt = random();
        String prePrompt = random();
        AskAiLinker linker = new AskAiLinker();
        linker.setPrePrompt(prePrompt);
        linker.setConnection(new OllamaConnection() {{
            setMaxRetries(2);
            setPort(123);
            setTimeout(new Timeout(ChronoUnit.MICROS, 2));
            setUrl("http://localhost");

        }});
        linker.setModel(AskAiConfiguration.SupportedModel.LLAMA3_1);
        linker.setName(name);
        linker.setNumPredict(random(1));
        linker.setRepeatPenalty(random(0.3));
        linker.setSeed(random(1234));
        linker.setStop(List.of(random(), random()));
        linker.setTemperature(random(0.3));
        linker.setTopK(random(1));
        linker.setTopP(random(1));

        MappingConfiguration mappingConfiguration = newConfiguration()
                .addPart(ConfigurationBuilder.newPart()
                        .setInput(name, "token-input")
                ).addPart(ConfigurationBuilder.newPart()
                        .setInput(name, "token-output")
                ).addPart(ConfigurationBuilder.newPart()
                        .setInput(name, "answer")
                ).addPart(ConfigurationBuilder.newPart()
                        .setOutput(name, "input")
                        .withContent(prompt)
                )
                .build();

        AskAiTest.test(name, mappingConfiguration, a -> {
            assertEquals(3, a.messages().size());
            assertEquals("SystemMessage { text = \"You are a system bot that responses only very shortly.\" }",
                    a.messages().get(0).toString());
            assertEquals(String.format("UserMessage { name = null contents = [TextContent { text = \"%s\" }] }", prePrompt),
                    a.messages().get(1).toString());
            assertEquals(String.format("UserMessage { name = null contents = [TextContent { text = \"%s\" }] }", prompt),
                    a.messages().get(2).toString());
        }, () -> {
            try {
                assertEquals(3, linker.getData(mappingConfiguration));
            } catch (InputOutputError e) {
                fail("linker failed");
            }
        });
    }
}