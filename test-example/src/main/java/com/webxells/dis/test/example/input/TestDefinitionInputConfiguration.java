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

import com.webxells.dis.api.DatasetPiece;
import com.webxells.dis.api.config.InputConfig;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

public class TestDefinitionInputConfiguration implements InputConfig {
    public class EventLogger {
        private final Queue<String> storage = new LinkedList<>();

        public void log(final String event) {
            storage.add(event);
        }

        public String pop() {
            return storage.poll();
        }
    }

    private final EventLogger eventLogger = new EventLogger();
    private LinkedList<Map<String, String>> data;
    private String name;

    public void setName(final String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getType() {
        return TestDefinitionInput.class.getName();
    }

    public EventLogger logger() {
        return eventLogger;
    }

    public LinkedList<Map<String, String>> getData() {
        return data;
    }

    public void setData(final LinkedList<Map<String, String>> data) {
        this.data = data;
    }
}
