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

import com.webxells.dis.api.config.description.Default;
import com.webxells.dis.api.config.description.Description;
import com.webxells.dis.api.config.description.Required;
import com.webxells.dis.api.error.DisException;
import com.webxells.dis.api.error.InvalidApi;
import com.webxells.dis.sql.connection.internal.ConnectionPoolEntity;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Description("Pool of connections - reusable connections identified by name")
public class GlobalConnectionPool implements ConnectionStrategy {
    private static final Map<String, ConnectionPoolEntity> REGISTRY = new ConcurrentHashMap<>();

    @Description("Name of connection")
    @Required
    private String name;

    @Description("Connection to connect")
    @Default("connection of previously used GlobalConnection with same name")
    private ConnectionStrategy connection;

    @Override
    public Connection createConnection() throws DisException, SQLException {
        assertValidConnectionArguments();
        return REGISTRY.get(name).connect();
    }

    @Override
    public void validate() throws InvalidApi {
        if (name == null) {
            throw new InvalidApi("name is required");
        }
    }

    private void assertValidConnectionArguments() throws DisException, SQLException {
        if (!REGISTRY.containsKey(name)) {
            if (connection == null) {
                throw new InvalidApi("Unknown name: " + name);
            }
            REGISTRY.put(name, new ConnectionPoolEntity(name, connection));
        }
    }

    public void setConnection(final ConnectionStrategy connection) throws SQLException, DisException {
        this.connection = connection;
   //     createConnectionWrapper();
    }

    public void setName(final String name) throws SQLException, DisException {
        this.name = name;
    //    createConnectionWrapper();
    }

    private void createConnectionWrapper() throws SQLException, DisException {
        if (null != name && null != connection) {
            assertValidConnectionArguments();
        }
    }
}