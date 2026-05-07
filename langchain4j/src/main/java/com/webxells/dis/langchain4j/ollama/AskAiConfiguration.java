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

import com.webxells.dis.api.config.InputConfig;
import com.webxells.dis.api.config.description.Required;
import dev.langchain4j.data.message.UserMessage;
import java.util.List;

public class AskAiConfiguration implements InputConfig {
    public enum SupportedModel {
        LLAMA3_1("llama3.1"), LLAMA3_2("llama3.2"), LLAMA3_3("llama3.3"),
        QWQ("qwq");

        private final String string;

        SupportedModel(final String string) {
            this.string = string;
        }

        public String asString() {
            return string;
        }
    }

    @Required
    private String name;
    @Required
    private OllamaConnection connection;
    @Required
    private String prompt;

    private SupportedModel model;
    private int topK;
    private int topP;
    private double repeatPenalty;
    private int seed;
    private int numPredict;
    private double temperature;
    private List<String> stop;
    private int maxRoundsAsking = 1;
    private List<UserMessage> prompts;

    @Override
    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public OllamaConnection getConnection() {
        return connection;
    }

    public void setConnection(final OllamaConnection connection) {
        this.connection = connection;
    }

    public List<UserMessage> getPrompt() {
        if (prompts == null) {
            return List.of(new UserMessage(prompt));
        }
        return prompts;
    }

    public void setPrompt(final String prompt) {
        this.prompt = prompt;
    }

    void setPrompts(final List<UserMessage> prompts) {
        this.prompts = prompts;
    }

    public SupportedModel getModel() {
        return model;
    }

    public void setModel(final SupportedModel model) {
        this.model = model;
    }

    public int getTopK() {
        return topK;
    }

    public void setTopK(final int topK) {
        this.topK = topK;
    }

    public int getTopP() {
        return topP;
    }

    public void setTopP(final int topP) {
        this.topP = topP;
    }

    public double getRepeatPenalty() {
        return repeatPenalty;
    }

    public void setRepeatPenalty(final double repeatPenalty) {
        this.repeatPenalty = repeatPenalty;
    }

    public int getSeed() {
        return seed;
    }

    public void setSeed(final int seed) {
        this.seed = seed;
    }

    public int getNumPredict() {
        return numPredict;
    }

    public void setNumPredict(final int numPredict) {
        this.numPredict = numPredict;
    }

    public double getTemperature() {
        return temperature;
    }

    public void setTemperature(final double temperature) {
        this.temperature = temperature;
    }

    public List<String> getStop() {
        return stop;
    }

    public void setStop(final List<String> stop) {
        this.stop = stop;
    }

    public int getMaxRoundsAsking() {
        return maxRoundsAsking;
    }

    public void setMaxRoundsAsking(final int maxRoundsAsking) {
        this.maxRoundsAsking = maxRoundsAsking;
    }
}