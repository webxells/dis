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
import com.webxells.dis.api.error.InputOutputError;
import com.webxells.dis.api.error.InvalidApi;
import com.webxells.dis.base.resource.ProcessCall.RequiredSystem;
import com.webxells.dis.test.TestLogManager.TestLogger;
import com.webxells.dis.test.cases.SimpleTestCase;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ProcessCallTest extends SimpleTestCase {

    @Test
    @EnabledOnOs({OS.LINUX, OS.MAC, OS.FREEBSD, OS.OPENBSD, OS.SOLARIS})
    void testLinux() throws InvalidApi, InputOutputError, IOException {
        test("sleep 2; echo $ENVVAR", RequiredSystem.UNIX, RequiredSystem.WINDOWS);
    }

    @Test
    @EnabledOnOs({OS.WINDOWS})
    void testWindows() throws InvalidApi, InputOutputError, IOException {
        test("timeout /t 2 /nobreak && echo %ENVVAR%", RequiredSystem.WINDOWS, RequiredSystem.LINUX);
    }

    private void test(final String command, final RequiredSystem goesWith, final RequiredSystem goesNotWith) throws InvalidApi, InputOutputError, IOException {
        ProcessCall fixture  = new ProcessCall();
        String random = random();
        fixture.validate();
        fixture.setMaxWaitTime(-3);
        Assertions.assertThrows(InvalidApi.class, fixture::validate);
        fixture.setMaxWaitTime(3000);
        fixture.validate();
        fixture.setEnvironment(Map.of("ENVVAR", random));
        fixture.setRequiredSystem(goesNotWith);
        Assertions.assertThrows(InvalidApi.class, fixture::validate);
        fixture.setRequiredSystem(goesWith);
        fixture.validate();

        final OutputStream send = fixture.send();
        send.write(command.getBytes());
        long start = System.currentTimeMillis();
        send.flush();

        assertTrue(System.currentTimeMillis() - start >= 2000);
        final TestLogger logger = getLogger(ProcessCall.class);
        logger.assertEventWasFired(Logger.LogLevel.INFO, "Process output: " + random);
        logger.assertEventWasFired(Logger.LogLevel.DEBUG, "Process finished with return value: 0");
    }

}