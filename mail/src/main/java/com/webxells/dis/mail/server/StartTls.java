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
package com.webxells.dis.mail.server;

import com.webxells.dis.api.config.description.Default;
import com.webxells.dis.api.error.InvalidApi;
import com.webxells.dis.mail.internal.TrustAnySocketFactory;
import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import java.util.Properties;

public class StartTls implements Server {
    public enum SslMode {
        TLS_1_2("TLSv1.2"), TLS_1_3("TLSv1.3");

        private final String protocol;

        SslMode(final String protocol) {
            this.protocol = protocol;
        }

        public String protocol() {
            return protocol;
        }
    }

    @Default("localhost")
    private String host = "localhost";
    @Default("587")
    private int port = 587;
    private String username;
    private String password;
    @Default("false")
    private boolean trustAnyCertificate;
    @Default("TLS_1_3")
    private SslMode sslMode = SslMode.TLS_1_3;

    @Override
    public void validate() throws InvalidApi {
        if (null == username || null == password) {
            throw new InvalidApi("Missing required parameters");
        }
    }

    @Override
    public Message createServerMessage() {
        final Properties properties = new Properties();
        properties.put("mail.smtp.host", host);
        properties.put("mail.smtp.port", port);
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.transport.protocol", "smtp");
        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("mail.smtp.starttls.required", "true");
        properties.put("mail.smtp.socketFactory.fallback", "false");
        if (trustAnyCertificate) {
            properties.put("mail.smtp.socketFactory.class", TrustAnySocketFactory.class.getName());
            properties.put("mail.smtp.ssl.socketFactory.class", TrustAnySocketFactory.class.getName());
        }
        properties.put("mail.smtp.ssl.protocols", sslMode.protocol());
        return new MimeMessage(Session.getInstance(properties, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        }));
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

    public void setTrustAnyCertificate(final boolean trustAnyCertificate) {
        this.trustAnyCertificate = trustAnyCertificate;
    }

    public void setSslMode(final SslMode sslMode) {
        this.sslMode = sslMode;
    }
}