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

import com.webxells.dis.api.Logger;
import com.webxells.dis.api.config.MappingConfiguration;
import com.webxells.dis.api.input.Input;
import com.webxells.dis.base.SimpleDatasetPiece;
import com.webxells.dis.base.SimpleMappingPortrayal;
import com.webxells.dis.logging.LoggerProxyFactory;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.request.ResponseFormat;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.model.output.TokenUsage;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public class AskAi implements Input<AskAiConfiguration> {
    private static final Logger LOGGER = LoggerProxyFactory.logger(AskAi.class);
    private static final SystemMessage SYSTEM_PROMPT =
            SystemMessage.systemMessage("You are a system bot that responses only very shortly.");

    private final AskAiConfiguration config;
    private final String name;
    private final int maxRoundsAsking;

    private OllamaChatModel ollama;
    private int current;

    public AskAi(final AskAiConfiguration config) {
        this.config = config;
        name = config.getName();
        maxRoundsAsking = config.getMaxRoundsAsking();
    }

    @Override
    public int read(final MappingConfiguration from) {
        if (maxRoundsAsking <= current++) {
            return 0;
        }
        final List<ChatMessage> messages = new ArrayList<>();
        messages.add(SYSTEM_PROMPT);
        messages.addAll(config.getPrompt());

        final ChatRequest request = ChatRequest.builder()
                        .messages(messages)
                .build();

        LOGGER.d("Asking ai: %s", request.toString());
        final ChatResponse response = ollama.chat(request);
        final TokenUsage tokenUsage = response.tokenUsage();
        LOGGER.logIfDebug(() -> String.format("Done - used token: %d input %d output",
                tokenUsage.inputTokenCount(), tokenUsage.outputTokenCount()));
        LOGGER.logIfTrace(() -> String.format("Reason: %s", response.finishReason()));
        return setTokenUsed(tokenUsage, from) +
                setAllByPortrayal(from, "answer", response.aiMessage().text());
    }

    private int setTokenUsed(final TokenUsage tokenUsage, final MappingConfiguration from) {
        return setAllByPortrayal(from, "token-input", tokenUsage.inputTokenCount()) +
                setAllByPortrayal(from, "token-output", tokenUsage.outputTokenCount());
    }

    private int setAllByPortrayal(final MappingConfiguration from, final String path, final int value) {
        return setAllByPortrayal(from, path, String.valueOf(value));
    }

    private int setAllByPortrayal(final MappingConfiguration from, final String path, final String value) {
        return from.getAllByPortrayal(new SimpleMappingPortrayal(name, path))
                .stream().mapToInt(a -> {
                    a.getDataset().collect(new SimpleDatasetPiece(value));
                    return 1;
                }).sum();
    }

    @Override
    public boolean hasNext() {
        return maxRoundsAsking > current;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void start() {
        ollama = createOllama();
    }

    private OllamaChatModel createOllama() {
        final OllamaChatModel.OllamaChatModelBuilder builder = OllamaChatModel.builder();
        setConnection(config.getConnection(), builder);
        setOptionalNumber(builder::topK, config.getTopK());
        setOptionalNumber(builder::topP, config.getTopP());
        setOptionalNumber(builder::temperature, config.getTemperature());
        setOptionalNumber(builder::seed, config.getSeed());
        setOptionalNumber(builder::numPredict, config.getNumPredict());
        setOptionalNumber(builder::repeatPenalty, config.getRepeatPenalty());
        builder.responseFormat(ResponseFormat.TEXT);
        Optional.ofNullable(config.getStop())
                .filter(a -> 0 < a.size())
                .ifPresent(builder::stop);
        return builder
                .modelName(config.getModel().asString())
                .build();
    }

    private void setOptionalNumber(final Consumer<Double> consumer, final double value) {
        if (0 < value) {
            consumer.accept(value);
        }
    }

    private void setOptionalNumber(final Consumer<Integer> consumer, final int value) {
        if (0 < value) {
            consumer.accept(value);
        }
    }

    private void setConnection(final OllamaConnection connection, final OllamaChatModel.OllamaChatModelBuilder builder) {
        builder.baseUrl(String.format("%s:%d", connection.getUrl(), connection.getPort()));
        Optional.ofNullable(connection.getTimeout())
                .ifPresent(a -> builder.timeout(a.duration()));
        if (0 < connection.getMaxRetries()) {
            builder.maxRetries(connection.getMaxRetries());
        }
    }

    @Override
    public void end() {
        if (null != ollama) {
            ollama = null;
        }
    }
}