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
package com.webxells.dis.rest.input;

import com.webxells.dis.api.error.DisException;
import com.webxells.dis.rest.Rest;
import com.webxells.dis.test.cases.SimpleTestCase;
import com.webxells.dis.test.example.input.TestInput;
import com.webxells.dis.test.example.input.TestReceiverInputConfig;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class PassConfigurationToRestTest extends SimpleTestCase {

    @Test
    void test() throws DisException {
        PassConfigurationToRestConfig config = new PassConfigurationToRestConfig();
        Rest rest = Mockito.mock(Rest.class);
        config.setChild(new TestReceiverInputConfig() {{
            setReceiver(rest);
        }});
        TestInput.setData(List.of(Map.of()));
        PassConfigurationToRest fixture = new PassConfigurationToRest(config);
        fixture.start();
        Assertions.assertFalse(fixture.hasNext());
        fixture.read(newConfiguration().build());
        fixture.end();
    }

}