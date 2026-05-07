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
package com.webxells.dis.sql.connection;

import com.webxells.dis.api.config.description.Description;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

@Description("Simple connection")
public class StaticConnection implements ConnectionStrategy {
    private String jdbcUrl;
    private String username;
    private String password;
    private Map<String, String> parameters;

    @Override
    public Connection createConnection() throws SQLException {
        final Properties info = new Properties();
        Optional.ofNullable(username)
                .ifPresent(a -> info.put("user", a));
        Optional.ofNullable(password)
                .ifPresent(a -> info.put("password", a));
        Optional.ofNullable(parameters)
                .ifPresent(info::putAll);
        return DriverManager.getConnection(jdbcUrl, info);
    }

    public void setJdbcUrl(final String jdbcUrl) {
        this.jdbcUrl = jdbcUrl;
    }

    public void setUsername(final String username) {
        this.username = username;
    }

    public void setPassword(final String password) {
        this.password = password;
    }

    public void setParameters(final Map<String, String> parameters) {
        this.parameters = parameters;
    }
}