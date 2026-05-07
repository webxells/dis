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
import com.webxells.dis.api.config.description.Description;
import com.webxells.dis.api.error.InputOutputError;
import com.webxells.dis.api.input.Input;
import com.webxells.dis.base.SimpleDatasetPiece;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Optional;

@Description("Prompts and reads cli user input as value")
public class CliUserInput implements Input<CliUserInputConfig> {
    private final int timeoutSeconds;
    private final String name;
    private final CliUserInputConfig config;
    private final int maxLinesToFetch;
    private boolean alreadyRead;


    public CliUserInput(final CliUserInputConfig config) {
        timeoutSeconds = config.getTimeoutSeconds();
        name = config.getName();
        maxLinesToFetch = Optional.of(config.getMaxLinesToFetch())
                .filter(a -> a > 0)
                .orElse(1);
        this.config = config;
    }

    @Override
    public int read(final MappingConfiguration from) throws InputOutputError {
        alreadyRead = true;
        try {
            final String input = fetchInput();
            return from.partsBySource(name).stream()
                    .mapToInt(part -> {
                        part.getDataset().collect(new SimpleDatasetPiece(input));
                        return 1;})
                    .sum();
        } catch (final IOException | InterruptedException e) {
            throw new InputOutputError("Could not fetch user input", e);
        }
    }

    private String fetchInput() throws IOException, InterruptedException {
        printPrompt();
        final BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        final long start = getCurrentSec();
        int currentLine = 0;
        StringBuilder sb = null;
        linesLoop: while (currentLine++ < maxLinesToFetch) {
            while (!reader.ready()) {
                if (getCurrentSec() - start > timeoutSeconds) {
                    break linesLoop;
                }
                Thread.sleep(200);
            }
            if (null == sb) {
                sb = new StringBuilder();
            }
            if (1 < currentLine) {
                sb.append(config.getEndOfLine());
            }
            sb.append(reader.readLine());
        }
        return null == sb ? config.getDefaultValue() : sb.toString();
    }

    private void printPrompt() {
        config.getPrompt()
                .ifPresent(prompt -> System.out.printf("%s: ", prompt));
    }

    private long getCurrentSec() {
        return Math.round(System.currentTimeMillis() / 1000d);
    }

    @Override
    public boolean hasNext() {
        return !alreadyRead;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void start() {
        alreadyRead = false;
    }

    @Override
    public void end() { }
}