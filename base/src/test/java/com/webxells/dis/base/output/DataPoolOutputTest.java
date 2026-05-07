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
import com.webxells.dis.base.binary.DataPool;
import com.webxells.dis.base.binary.RawData;
import com.webxells.dis.test.cases.SimpleTestCase;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;
import org.junit.jupiter.api.Test;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

class DataPoolOutputTest extends SimpleTestCase {

    @Test
    void test() throws InputOutputError, IOException {
        String name  = random();
        String content  = random();
        Resource sender = mock(Resource.class);
        OutputStream stream = mock(OutputStream.class);
        when(sender.send()).thenReturn(stream);
        DataPool.registerData(name, new RawData(new ByteArrayInputStream(content.getBytes())));
        DataPoolConfiguration config = new DataPoolConfiguration();
        config.setName(name);
        config.setSender(sender);
        DataPoolOutput fixture = new DataPoolOutput(config);
        fixture.start();
        fixture.write(null);
        fixture.end();
        verify(sender).send();
        verify(stream).write(eq(content.getBytes()), eq(0), eq(content.length()));
        verify(stream).flush();
        verify(stream).close();
        verifyNoMoreInteractions(sender, stream);
    }

}