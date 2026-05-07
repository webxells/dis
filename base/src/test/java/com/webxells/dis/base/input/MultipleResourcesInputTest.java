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

import com.webxells.dis.api.error.DisException;
import com.webxells.dis.api.resource.MultiResource;
import com.webxells.dis.test.example.input.TestReceiverInput;
import com.webxells.dis.test.example.input.TestReceiverInputConfig;
import com.webxells.dis.test.example.resource.TestMultipleResource;
import java.io.InputStream;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MultipleResourcesInputTest {

    @BeforeEach
    void clear() {
        TestReceiverInput.clear();
    }

    @Test
    void testStopRefreshing() throws DisException {
        MultipleResourcesInputConfig config = new MultipleResourcesInputConfig();
        final TestReceiverInputConfig childConfig = new TestReceiverInputConfig();
        final List<InputStream> streams = List.of(
                Mockito.mock(InputStream.class, "1"),
                Mockito.mock(InputStream.class, "2"),
                Mockito.mock(InputStream.class, "3"),
                Mockito.mock(InputStream.class, "4")
        );
        MultiResource multipleResource =
                new TestMultipleResource(streams);
        childConfig.setReceiver(multipleResource);
        config.setInput(childConfig);
        MultipleResourcesInput fixture = new MultipleResourcesInput(config);
        assertTrue(TestReceiverInput.receivedStreams.isEmpty());
        fixture.start();
        fixture.read(null);
        assertEquals(1, TestReceiverInput.receivedStreams.size());
        assertSame(streams.get(0) , TestReceiverInput.receivedStreams.get(0));
        new MultipleResourcesInput.StopRefreshing().validate(null, null);
        fixture.read(null);
        assertEquals(1, TestReceiverInput.receivedStreams.size());
    }

    @Test
    void test() throws DisException {
        MultipleResourcesInputConfig config = new MultipleResourcesInputConfig();
        final TestReceiverInputConfig childConfig = new TestReceiverInputConfig();
        final List<InputStream> streams = List.of(
                Mockito.mock(InputStream.class, "1"),
                Mockito.mock(InputStream.class, "2"),
                Mockito.mock(InputStream.class, "3"),
                Mockito.mock(InputStream.class, "4")
        );
        MultiResource multipleResource =
                new TestMultipleResource(streams);
        childConfig.setReceiver(multipleResource);
        config.setInput(childConfig);
        MultipleResourcesInput fixture = new MultipleResourcesInput(config);
        assertTrue(TestReceiverInput.receivedStreams.isEmpty());
        fixture.start();
        for (int i = 0; i < 4; i++) {
            fixture.read(null);
            assertEquals(i + 1, TestReceiverInput.receivedStreams.size());
            assertSame(streams.get(i) , TestReceiverInput.receivedStreams.get(i));
        }
        fixture.read(null);
        assertEquals(4, TestReceiverInput.receivedStreams.size());
    }

}