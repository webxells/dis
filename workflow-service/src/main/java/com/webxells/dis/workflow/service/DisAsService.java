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
package com.webxells.dis.workflow.service;

import com.webxells.dis.api.Logger;
import com.webxells.dis.api.config.DisConfig;
import com.webxells.dis.api.workflow.DisSystem;
import com.webxells.dis.boot.WorkflowSecurity;
import com.webxells.dis.logging.LoggerProxyFactory;
import com.webxells.dis.workflow.service.internal.Director;
import com.webxells.dis.workflow.service.internal.ServiceSecurity;
import java.util.Map;

public class DisAsService implements DisSystem {
    private final static Logger LOGGER = LoggerProxyFactory.logger(DisAsService.class);

    private final Director director;
    private final boolean forcefullyStopJobsOnShutdown;

    public static void registerServiceSecurity() {
        WorkflowSecurity.register(new ServiceSecurity());
    }

    DisAsService(final DisConfig configuration, final ServiceSupplier serviceSupplier) {
        forcefullyStopJobsOnShutdown = serviceSupplier.shouldForcefullyStopJobsOnShutdown();
        director = new Director(configuration.getConfigurations(), serviceSupplier);
    }

    @Override
    public void start() {
        LOGGER.i("Starting Dis...");
        director.start();
        LOGGER.i("Successfully started");
    }

    @Override
    public void stop() {
        LOGGER.i("Stopping Dis...");
        if (null == director) {
            LOGGER.i("Nothing to stop!");
            return;
        }
        LOGGER.i("Disabling triggers...");
        director.disable();
        if (forcefullyStopJobsOnShutdown) {
            LOGGER.i("Stopping running jobs...");
            director.interrupt();
        } else {
            while (director.isSomethingRunning()) {
                LOGGER.i("Waiting for jobs to finish...");
                try {
                    Thread.sleep(500);
                } catch (final InterruptedException e) {
                    LOGGER.e("Nightmare!", e);
                }
            }
        }
        director.stop();
        LOGGER.i("Successfully stopped");
    }

    @Override
    public void join() {
        LOGGER.d("Joining Dis service...");
        try {
            director.join();
        } catch (final InterruptedException e) {
            throw new RuntimeException("Nightmare!", e);
        }
    }

    public Map<String, Object> getJobStatus() {
        return director.parseJobStatus();
    }
}