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

import com.webxells.dis.api.config.MappingConfiguration;
import com.webxells.dis.api.config.OutputConfig;
import com.webxells.dis.api.config.description.Alias;
import com.webxells.dis.api.config.description.Description;
import com.webxells.dis.api.config.description.Required;
import com.webxells.dis.api.error.DisException;
import com.webxells.dis.api.error.InputOutputError;
import com.webxells.dis.api.error.InvalidApi;
import com.webxells.dis.api.output.Output;
import com.webxells.dis.boot.ServiceManager;

public class SkipEmptyDataset implements Output<SkipEmptyDataset.Config> {
    @Description("Skips empty datasets for child Output")
    public static class Config implements OutputConfig {
        @Required
        @Alias("child")
        private OutputConfig output;

        @Override
        public void validate() throws InvalidApi {
            if (null == output) {
                throw new InvalidApi("output required");
            }
            output.validate();
        }

        @Override
        public String getName() {
            return null;
        }

        @Override
        public String getType() {
            return SkipEmptyDataset.class.getName();
        }

        public void setOutput(final OutputConfig output) {
            this.output = output;
        }
    }

    private final Output<?> output;

    public SkipEmptyDataset(final Config config) {
        output = ServiceManager.loadByConfig(config.output);
    }

    @Override
    public void write(final MappingConfiguration to) throws InputOutputError {
        if (to.partsByDestination(output.getName()).stream().anyMatch(a -> a.value().isPresent())) {
            output.write(to);
        }
    }

    @Override
    public String getName() {
        return output.getName();
    }

    @Override
    public void start() throws DisException {
        output.start();
    }

    @Override
    public void end() throws DisException {
        output.end();
    }
}