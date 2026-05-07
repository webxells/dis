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

import com.webxells.dis.api.Logger;
import com.webxells.dis.api.config.DisConfig;
import com.webxells.dis.api.config.JobConfig;
import com.webxells.dis.api.error.InvalidApi;
import com.webxells.dis.api.workflow.DisSystem;
import com.webxells.dis.api.workflow.SystemSupplier;
import com.webxells.dis.logging.LoggerProxyFactory;
import java.util.List;
import java.util.Optional;

public class DisBuilder<T extends DisSystem> {
    private static final Logger LOGGER = LoggerProxyFactory.logger(DisBuilder.class);

    private DisConfig configuration;
    private List<JobConfig> jobConfigurations;
    private SystemSupplier<T> systemSupplier;
    private boolean validate;

    public T build() {
        Optional.ofNullable(jobConfigurations)
                .ifPresent(configuration::setConfigurations);
        Optional.ofNullable(systemSupplier)
                .ifPresent(configuration::setSystemSupplier);
        validateConfiguration();
        return configuration.<T> getSystemSupplier().get(configuration);
    }

    private void validateConfiguration() {
        if (validate) {
            try {
                configuration.validate();
            } catch (final InvalidApi e) {
                LOGGER.e("Validation of configuration failed", e);
                throw new RuntimeException("Dis building failed");
            }
        }
    }

    public DisBuilder<T> validate() {
        validate = true;
        return this;
    }

    public DisBuilder<T> setConfiguration(final DisConfig configuration) {
        this.configuration = configuration;
        return this;
    }

    public DisBuilder<T> overwriteJobConfigurations(final List<JobConfig> jobConfigurations) {
        this.jobConfigurations = jobConfigurations;
        return this;
    }

    public DisBuilder<T> overwriteSystem(final SystemSupplier<T> systemSupplier) {
        this.systemSupplier = systemSupplier;
        return this;
    }
}