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

import com.webxells.dis.api.error.DisException;
import com.webxells.dis.base.trigger.OnJobEnd;
import com.webxells.dis.base.trigger.OnJobEndConfig;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JobEndNotifierTest {
    private boolean success;

    @Test
    void test() throws DisException, InterruptedException {
        success = false;
        OnJobEndConfig config1 = new OnJobEndConfig();
        config1.setJobName("test");
        OnJobEnd trigger = new OnJobEnd(config1);
        trigger.awaitAction(() -> success = true);
        final JobEndNotifierConfig config = new JobEndNotifierConfig();
        config.setName("test");
        JobEndNotifier fixture = new JobEndNotifier(config);
        assertFalse(success);
        fixture.write(null);
        assertFalse(success);
        fixture.start();
        assertFalse(success);
        fixture.end();
        Thread.sleep(600);
        assertTrue(success);
        trigger.abort();
    }

}