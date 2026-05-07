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
package com.webxells.dis.base.output;

import com.webxells.dis.api.error.InputOutputError;
import com.webxells.dis.api.resource.Resource;
import java.io.IOException;
import java.io.OutputStream;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class SimpleOutputTest {

    @Test
    void test() throws InputOutputError, IOException {
        Resource sender = Mockito.mock(Resource.class);
        OutputStream stream = Mockito.mock(OutputStream.class);
        Mockito.when(sender.send()).thenReturn(stream);
        SimpleOutputConfig config = new SimpleOutputConfig();
        config.setSender(sender);
        SimpleOutput fixture = new SimpleOutput(config);
        fixture.write(null);
        Mockito.verify(stream).flush();
    }

}