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
package com.webxells.dis.test.example.output;

import com.webxells.dis.api.config.MappingConfiguration;
import com.webxells.dis.api.error.InputOutputError;
import com.webxells.dis.api.output.Output;

public class IncrementTestOutput implements Output<TestOutputConfig> {
    public static int counter = 0;

    public static void reset() {
        counter = 0;
    }

    public IncrementTestOutput(TestOutputConfig config) {  }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public void start() {  }

    @Override
    public void end() { }

    @Override
    public void write(MappingConfiguration to) throws InputOutputError {
        counter++;
    }


}