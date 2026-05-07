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
package com.webxells.dis.test.example.input;

import com.webxells.dis.api.config.MappingConfiguration;
import com.webxells.dis.api.error.DisException;
import com.webxells.dis.api.error.InputOutputError;
import com.webxells.dis.api.input.Input;
import com.webxells.dis.api.resource.Resource;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

public class TestReceiverInput implements Input<TestReceiverInputConfig> {
    public final static List<InputStream> receivedStreams = new LinkedList<>();
    private final Resource receiver;
    private boolean empty;

    public static void clear() {
        receivedStreams.clear();
    }

    public TestReceiverInput(TestReceiverInputConfig config) {
        receiver = config.getReceiver();
    }

    @Override
    public int read(final MappingConfiguration from) throws InputOutputError {
        if (empty) {
            empty = false;
            return 0;
        }
        empty = true;
        Optional.ofNullable(receiver.receive())
                .ifPresent(receivedStreams::add);
        return 1;
    }

    @Override
    public boolean hasNext() throws InputOutputError {
        return false;
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public void start() throws DisException { }

    @Override
    public void end() throws DisException { }


}