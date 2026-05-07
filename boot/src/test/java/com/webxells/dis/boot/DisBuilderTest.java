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
package com.webxells.dis.boot;

import com.webxells.dis.api.config.DisConfig;
import com.webxells.dis.api.config.JobConfig;
import com.webxells.dis.api.workflow.DisSystem;
import com.webxells.dis.api.workflow.SystemSupplier;
import com.webxells.dis.test.cases.SimpleTestCase;
import com.webxells.dis.test.example.TestDisConfiguration;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class DisBuilderTest extends SimpleTestCase {
    static class TestSystem implements DisSystem {
        @Override
        public void start() {

        }

        @Override
        public void stop() {

        }

        @Override
        public void join() {

        }
    }

    static class TestSupplier implements SystemSupplier<TestSystem> {
        private List<JobConfig> configs;

        TestSupplier(final List<JobConfig> configs) {
            this.configs = configs;
        }

        @Override
        public TestSystem get(final DisConfig configuration) {
            Assertions.assertSame(configs, configuration.getConfigurations());
            Assertions.assertSame(this, configuration.getSystemSupplier());
            return new TestSystem();
        }
    }

    @Test
    void test() {
        DisConfig config = new TestDisConfiguration();
        config.setConfigurations(List.of());
        TestSupplier testSupplier = new TestSupplier(config.getConfigurations());
        config.setSystemSupplier(testSupplier);

        TestSystem fixture = new DisBuilder<TestSystem>()
                .setConfiguration(config)
                .validate()
                .build();
    }
}