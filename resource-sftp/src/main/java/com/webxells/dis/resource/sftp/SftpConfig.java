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
package com.webxells.dis.resource.sftp;

import com.webxells.dis.api.config.DisConfigApi;
import com.webxells.dis.api.config.description.Default;
import com.webxells.dis.api.config.description.Description;
import com.webxells.dis.api.config.description.Required;
import com.webxells.dis.resource.sftp.proxy.Proxy;

public class SftpConfig implements DisConfigApi {
    @Default("22")
    private int port = 22;
    @Description("Path to knownHostFile")
    private String knownHostFile;
    @Description("Accept all host certificates")
    private boolean ignoreUnknownHostError;
    @Required
    private String username;
    @Required
    private String password;
    @Required
    @Description("Host to connect to")
    private String host;
    @Description("Timeout in seconds used for opening the sftp session")
    @Default("30")
    private int timeoutInSeconds = 30;
    @Description("Proxy implementation to use")
    private Proxy proxy;

    public int getPort() {
        return port;
    }

    public void setPort(final int port) {
        this.port = port;
    }

    public String getKnownHostFile() {
        return knownHostFile;
    }

    public void setKnownHostFile(final String knownHostFile) {
        this.knownHostFile = knownHostFile;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(final String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(final String password) {
        this.password = password;
    }

    public String getHost() {
        return host;
    }

    public void setHost(final String host) {
        this.host = host;
    }

    public int getTimeoutInSeconds() {
        return timeoutInSeconds;
    }

    public void setTimeoutInSeconds(final int timeoutInSeconds) {
        this.timeoutInSeconds = timeoutInSeconds;
    }

    public boolean isIgnoreUnknownHostError() {
        return ignoreUnknownHostError;
    }

    public void setIgnoreUnknownHostError(final boolean ignoreUnknownHostError) {
        this.ignoreUnknownHostError = ignoreUnknownHostError;
    }

    public Proxy getProxy() {
        return proxy;
    }

    public void setProxy(final Proxy proxy) {
        this.proxy = proxy;
    }
}