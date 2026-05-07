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
package com.webxells.dis.resource.sftp.proxy;

import com.jcraft.jsch.ProxyHTTP;
import com.webxells.dis.api.config.description.Default;
import com.webxells.dis.api.config.description.Required;
import java.util.Optional;

public class HttpProxy implements Proxy {
    @Required
    private String host;
    @Default("0")
    private int port;
    private String username;
    @Default("Blank string")
    private String password;

    @Override
    public com.jcraft.jsch.Proxy getJshProxy() {
        final ProxyHTTP result = new ProxyHTTP(host, port);
        if (null != username) {
            result.setUserPasswd(username, Optional.ofNullable(password).orElse(""));
        }
        return result;
    }

    public void setHost(final String host) {
        this.host = host;
    }

    public void setPort(final int port) {
        this.port = port;
    }

    public void setUsername(final String username) {
        this.username = username;
    }

    public void setPassword(final String password) {
        this.password = password;
    }
}