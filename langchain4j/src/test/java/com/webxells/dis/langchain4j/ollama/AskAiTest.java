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
import com.webxells.dis.test.cases.ConfigurationBuilder;
import com.webxells.dis.test.cases.SimpleTestCase;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.model.output.TokenUsage;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.function.Consumer;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

class AskAiTest extends SimpleTestCase {

    @Test
    void test() {
        AskAiConfiguration config = new AskAiConfiguration();
        config.setConnection(new OllamaConnection() {{
            setMaxRetries(2);
            setPort(123);
            setTimeout(new Timeout(ChronoUnit.MICROS, 2));
            setUrl("http://localhost");

        }});
        config.setMaxRoundsAsking(1);
        config.setModel(AskAiConfiguration.SupportedModel.LLAMA3_1);
        config.setName(random());
        config.setNumPredict(random(1));
        config.setPrompt(random());
        config.setRepeatPenalty(random(0.3));
        config.setSeed(random(1234));
        config.setStop(List.of(random(), random()));
        config.setTemperature(random(0.3));
        config.setTopK(random(1));
        config.setTopP(random(1));
        MappingConfiguration mappingConfiguration = newConfiguration()
                .addPart(ConfigurationBuilder.newPart()
                        .setInput(config.getName(), "token-input")
                ).addPart(ConfigurationBuilder.newPart()
                        .setInput(config.getName(), "token-output")
                ).addPart(ConfigurationBuilder.newPart()
                        .setInput(config.getName(), "answer")
                )
                .build();

        test(config.getName(), mappingConfiguration, a -> {
            assertEquals(2, a.messages().size());
            assertEquals("SystemMessage { text = \"You are a system bot that responses only very shortly.\" }",
                    a.messages().get(0).toString());
            assertEquals(config.getPrompt().get(0).toString(),
                    a.messages().get(1).toString());
        }, () -> {
            AskAi askAi = new AskAi(config);
            askAi.start();
            assertEquals(config.getName(), askAi.getName());
            assertTrue(askAi.hasNext());
            askAi.read(mappingConfiguration);
            assertFalse(askAi.hasNext());
            askAi.end();
        });
    }

    static void test( String name, MappingConfiguration mappingConfiguration,
               Consumer<ChatRequest> assertChatRequest, Runnable executeTestCommand) {
        int inputToken = random(1);
        int outputToken = random(1);
        String answer = random();

        try (MockedStatic<OllamaChatModel> chatModel =
                     mockStatic(OllamaChatModel.class)) {
            OllamaChatModel.OllamaChatModelBuilder builder = mock(OllamaChatModel.OllamaChatModelBuilder.class);
            OllamaChatModel modelMock = mock(OllamaChatModel.class);
            ChatResponse responseMock = mock(ChatResponse.class);
            AiMessage messageMock = mock(AiMessage.class);

            chatModel.when(OllamaChatModel::builder)
                    .thenReturn(builder);

            when(builder.modelName(any()))
                    .thenReturn(builder);
            when(builder.build()).thenReturn(modelMock);

            when(modelMock.chat(argThat((ChatRequest a) -> {
                assertChatRequest.accept(a);
                return true;
            }))).thenReturn(responseMock);

            when(responseMock.tokenUsage()).thenReturn(new TokenUsage(inputToken, outputToken));

            when(responseMock.aiMessage()).thenReturn(messageMock);
            when(messageMock.text()).thenReturn(answer);

            executeTestCommand.run();
        }

        assertEquals(String.valueOf(inputToken), mappingConfiguration.parts().get(0).value().get());
        assertEquals(String.valueOf(outputToken), mappingConfiguration.parts().get(1).value().get());
        assertEquals(answer, mappingConfiguration.parts().get(2).value().get());
    }
}