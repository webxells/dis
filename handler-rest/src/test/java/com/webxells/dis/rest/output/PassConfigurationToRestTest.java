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
package com.webxells.dis.rest.output;

import com.webxells.dis.api.error.DisException;
import com.webxells.dis.api.resource.Resource;
import com.webxells.dis.test.cases.SimpleTestCase;
import com.webxells.dis.test.example.output.TestOutputConfig;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class PassConfigurationToRestTest extends SimpleTestCase {

    @Test
    void test() throws DisException {
        Resource resource = Mockito.mock(Resource.class);
        PassConfigurationToRestConfig config = new PassConfigurationToRestConfig();
        config.setChild(new TestOutputConfig());
        config.setSender(resource);
        PassConfigurationToRest passConfigurationToRest = new PassConfigurationToRest(config);
        passConfigurationToRest.start();
        passConfigurationToRest.write(newConfiguration().build());
        passConfigurationToRest.end();
    }

}