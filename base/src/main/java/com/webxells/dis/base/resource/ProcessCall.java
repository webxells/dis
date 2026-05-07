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
package com.webxells.dis.base.resource;

import com.webxells.dis.api.Logger;
import com.webxells.dis.api.config.description.Default;
import com.webxells.dis.api.config.description.Description;
import com.webxells.dis.api.error.InputOutputError;
import com.webxells.dis.api.error.InvalidApi;
import com.webxells.dis.api.resource.Resource;
import com.webxells.dis.logging.LoggerProxyFactory;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Description("Transforms output to a system process call")
public class ProcessCall implements Resource {
    public enum RequiredSystem {
        LINUX(List.of("linux")), MACOS(List.of("mac", "darwin")),
        UNIX(List.of("linux", "freebsd", "unix", "sunos", "solaris")),
        WINDOWS(List.of("windows")),;

        private static final String OS = System.getProperty("os.name").toLowerCase(Locale.ROOT);

        private final List<String> lookFor;

        RequiredSystem(final List<String> lookFor) {
            this.lookFor = lookFor;
        }

        private boolean foundAsOs() {
            return lookFor.stream().anyMatch(OS::startsWith);
        }
    }

    public class CallBuilder extends OutputStream {
        private final ByteArrayOutputStream command = new ByteArrayOutputStream();
        private boolean flushable = true;

        @Override
        public void write(final int b) throws IOException {
            command.write(b);
        }

        @Override
        public void write(final byte[] b, final int off, final int len) throws IOException {
            command.write(b, off, len);
        }

        @Override
        public void flush() throws IOException {
            if (flushable) {
                callCommand(command.toString(StandardCharsets.UTF_8));
                flushable = false;
            }
        }
    }

    private static final Logger LOGGER = LoggerProxyFactory.logger(ProcessCall.class);

    private RequiredSystem requiredSystem;
    private Map<String, String> environment;
    @Description("Max milliseconds waiting to process to be finished")
    @Default("4000")
    private int maxWaitTime = 4000;

    @Override
    public void validate() throws InvalidApi {
        if (1 > maxWaitTime) {
            throw new InvalidApi("max wait time must be a positive integer");
        }
        if (null != requiredSystem && !requiredSystem.foundAsOs()) {
            throw new InvalidApi("Required system " + requiredSystem + " not found! Current: " + RequiredSystem.OS);
        }
    }

    @Override
    public OutputStream send() throws InputOutputError {
        return new CallBuilder();
    }

    @Override
    public InputStream receive() {
        throw new UnsupportedOperationException();
    }

    public void setRequiredSystem(final RequiredSystem requiredSystem) {
        this.requiredSystem = requiredSystem;
    }

    public void setEnvironment(final Map<String, String> environment) {
        this.environment = environment;
    }

    public void setMaxWaitTime(final int maxWaitTime) {
        this.maxWaitTime = maxWaitTime;
    }

    private void callCommand(final String command) throws IOException {
        final ProcessBuilder processBuilder = createProcessBuilder(command);
        Optional.ofNullable(environment)
                        .ifPresent(a -> processBuilder.environment().putAll(a));
        processBuilder.redirectErrorStream(true);
        LOGGER.d("Calling process: %s", command);
        monitorProcess(processBuilder.start());
    }

    private ProcessBuilder createProcessBuilder(final String command) {
        if (RequiredSystem.WINDOWS.foundAsOs()) {
            return new ProcessBuilder("cmd", "/c", command);
        }
        return new ProcessBuilder("sh", "-c", command);
    }

    private void monitorProcess(final Process process) throws IOException {
        final long start = System.currentTimeMillis();
        LOGGER.t("Process started: %d", process.pid());
        final BufferedReader inputReader = process.inputReader(StandardCharsets.UTF_8);
        while (process.isAlive()) {
            if (maxWaitTime < System.currentTimeMillis() - start) {
                process.destroyForcibly();
                throw new IOException("maxWait time exceeded");
            }
        }
        output(inputReader, process.exitValue());
    }

    private void output(final BufferedReader inputReader, final int exitValue) throws IOException {
        Optional.of(inputReader.lines()
                        .collect(Collectors.joining(System.lineSeparator())))
                .filter(a -> !a.isBlank())
                .ifPresent(a -> LOGGER.i("Process output: %s", a));
        LOGGER.d("Process finished with return value: %d", exitValue);
        inputReader.close();
    }
}