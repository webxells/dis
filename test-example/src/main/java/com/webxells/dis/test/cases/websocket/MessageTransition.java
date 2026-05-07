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
package com.webxells.dis.test.cases.websocket;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class MessageTransition {
    private static final Queue<String> RESPONSE_QUEUE =  new ConcurrentLinkedQueue<>();
    private static final Queue<String> COMMAND_QUEUE =  new ConcurrentLinkedQueue<>();
    private static final List<String> LOGGED_REQUESTS =  Collections.synchronizedList(new ArrayList<>());

    public void newRequest(final String request) {
        LOGGED_REQUESTS.add(request);
    }

    public void newCommand(final String  command) {
        COMMAND_QUEUE.add(command);
    }

    public String getNextCommand() {
        return COMMAND_QUEUE.poll();
    }

    public String getNextResponse() {
        return RESPONSE_QUEUE.poll();
    }

    public void clear() {
        RESPONSE_QUEUE.clear();
        LOGGED_REQUESTS.clear();
        COMMAND_QUEUE.clear();
    }

    public List<String> getLoggedRequests() {
        return LOGGED_REQUESTS;
    }

    public boolean hasACommand() {
        return  0 < COMMAND_QUEUE.size();
    }

    public void newResponse(final String response) {
        RESPONSE_QUEUE.add(response);
    }
}