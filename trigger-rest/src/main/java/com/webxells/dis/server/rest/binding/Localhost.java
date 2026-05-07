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
package com.webxells.dis.server.rest.binding;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Objects;

public class Localhost implements Binding {
    public enum Format {
        LOCALHOST("localhost"),
        IPV4_LOOPBACK("127.0.0.1"), IPV4_META("0.0.0.0"),
        IPV6_LOOPBACK("[::1]"), IPV6_META("[0:0:0:0:0:0:0:1]") ;
        private final String ip;

        Format(final String ip) {
            this.ip = ip;
        }
    }

    private Format format = Format.IPV4_LOOPBACK;
    @Override
    public InetAddress parse() {
        try {
            return InetAddress.getByName(format.ip);
        } catch (final UnknownHostException e) {
            throw new RuntimeException("Could not create localhost binding", e);
        }
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof final Localhost localhost)) return false;
        return format == localhost.format;
    }

    @Override
    public String hash() {
        return String.format("%s-%s", Localhost.class.getName(), format.ip);
    }

    @Override
    public String getDescription() {
        return format.ip;
    }

    public void setFormat(final Format format) {
        this.format = format;
    }
}