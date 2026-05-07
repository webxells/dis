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
import com.webxells.dis.test.cases.SimpleTestCase;
import com.webxells.dis.test.cases.ConfigurationBuilder;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DevNullTest extends SimpleTestCase {

    @Test
    void test() {
        DevNull.Configuration configuration = new DevNull.Configuration();
        configuration.setName(random());
        DevNull fixture = new DevNull(configuration);
        fixture.start();
        fixture.write(null);
        fixture.start();
        fixture.write(null);
        fixture.write(null);
        fixture.end();
        fixture.write(null);
        fixture.end();
        assertEquals(configuration.getName(), fixture.getName());
    }

}