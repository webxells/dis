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
package com.webxells.dis.workflow.service.config;

import com.webxells.dis.base.config.SimpleJobConfig;
import com.webxells.dis.workflow.service.RestartPolicy;

public class ServiceJobConfig extends SimpleJobConfig {
    private RestartPolicy restartPolicy = RestartPolicy.ERROR;
    private long datasetSkipAmount;
    private long datasetLimitAmount = -1;
    private boolean skipInputReads = true;

    public ServiceJobConfig() {
        super();
    }

    public ServiceJobConfig(final String name) {
        super(name);
    }
    public ServiceJobConfig(final String name, final RestartPolicy restartPolicy) {
        super(name);
        this.restartPolicy = restartPolicy;
    }


    public RestartPolicy getMonitoringPolicy() {
        return restartPolicy;
    }

    public void setMonitoringPolicy(final RestartPolicy restartPolicy) {
        this.restartPolicy = restartPolicy;
    }

    public long getDatasetSkipAmount() {
        return datasetSkipAmount;
    }

    public void setDatasetSkipAmount(final long datasetSkipAmount) {
        this.datasetSkipAmount = datasetSkipAmount;
    }

    public long getDatasetLimitAmount() {
        return datasetLimitAmount;
    }

    public void setDatasetLimitAmount(final long datasetLimitAmount) {
        this.datasetLimitAmount = datasetLimitAmount;
    }

    public boolean isSkipInputReads() {
        return skipInputReads;
    }

    public void setSkipInputReads(final boolean skipInputReads) {
        this.skipInputReads = skipInputReads;
    }
}