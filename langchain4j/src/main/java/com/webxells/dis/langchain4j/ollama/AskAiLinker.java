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
import com.webxells.dis.api.config.MappingPart;
import com.webxells.dis.api.error.InputOutputError;
import com.webxells.dis.api.input.linker.JoinLinker;
import dev.langchain4j.data.message.UserMessage;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AskAiLinker implements JoinLinker {
    private String name;
    private String prePrompt;

    private OllamaConnection connection;
    private AskAiConfiguration.SupportedModel model;
    private int topK;
    private int topP;
    private double repeatPenalty;
    private int seed;
    private int numPredict;
    private double temperature;
    private List<String> stop;

    @Override
    public int getData(final MappingConfiguration from) throws InputOutputError {
        final AskAi askAi = createAskAi(createPrompt(from.partsByDestination(name)));
        askAi.start();
        final int result = askAi.read(from);
        askAi.end();
        return result;
    }

    private AskAi createAskAi(List<UserMessage> prompts) {
        AskAiConfiguration askAiConfiguration = new AskAiConfiguration();
        askAiConfiguration.setName(name);
        askAiConfiguration.setPrompts(prompts);
        askAiConfiguration.setModel(model);
        askAiConfiguration.setTopK(topK);
        askAiConfiguration.setTopP(topP);
        askAiConfiguration.setRepeatPenalty(repeatPenalty);
        askAiConfiguration.setSeed(seed);
        askAiConfiguration.setNumPredict(numPredict);
        askAiConfiguration.setTemperature(temperature);
        askAiConfiguration.setStop(stop);
        askAiConfiguration.setConnection(connection);
        return new AskAi(askAiConfiguration);
    }

    private List<UserMessage> createPrompt(final List<MappingPart> mappingParts) {
        final List<UserMessage> result = new ArrayList<>();
        Optional.ofNullable(prePrompt)
                .ifPresent(prompt -> result.add(new UserMessage(prompt)));
        for (final MappingPart current : mappingParts) {
            current.value().ifPresent(value -> result.add(new UserMessage(value)));
        }
        return result;
    }

    @Override
    public String getInputName() {
        return name;
    }

    @Override
    public void start() { }

    @Override
    public void end() { }

    public void setName(final String name) {
        this.name = name;
    }

    public void setConnection(final OllamaConnection connection) {
        this.connection = connection;
    }

    public void setModel(final AskAiConfiguration.SupportedModel model) {
        this.model = model;
    }

    public void setTopK(final int topK) {
        this.topK = topK;
    }

    public void setTopP(final int topP) {
        this.topP = topP;
    }

    public void setRepeatPenalty(final double repeatPenalty) {
        this.repeatPenalty = repeatPenalty;
    }

    public void setSeed(final int seed) {
        this.seed = seed;
    }

    public void setNumPredict(final int numPredict) {
        this.numPredict = numPredict;
    }

    public void setTemperature(final double temperature) {
        this.temperature = temperature;
    }

    public void setStop(final List<String> stop) {
        this.stop = stop;
    }

    public void setPrePrompt(final String prePrompt) {
        this.prePrompt = prePrompt;
    }
}