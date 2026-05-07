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
package com.webxells.dis.sql;

import com.webxells.dis.api.Logger;
import com.webxells.dis.api.StatefulDisOperation;
import com.webxells.dis.api.error.DisException;
import com.webxells.dis.logging.LoggerProxyFactory;
import com.webxells.dis.sql.connection.ConnectionStrategy;
import com.webxells.dis.sql.internal.query.QueryStrategy;
import com.webxells.dis.sql.internal.query.StaticQuery;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;

abstract class StatefulSqlOperation implements StatefulDisOperation {
    private static final Logger LOGGER = LoggerProxyFactory.logger(StatefulSqlOperation.class);
    static {{ AvailableDrivers.init(); }}

    protected final ConnectionStrategy connectionStrategy;
    protected final String sql;
    protected final SqlConfiguration configuration;
    protected final String name;
    protected final QueryStrategy queryStrategy;

    protected Connection connection;

    StatefulSqlOperation(final SqlConfiguration configuration) {
        this.configuration = configuration;
        connectionStrategy = configuration.getConnection();
        name = configuration.getName();
        sql = configuration.getSql();
        queryStrategy = configuration.getQueryStrategy();
    }

    public String getName() {
        return name;
    }

    QueryStrategy getQueryStrategy() {
        return Optional.ofNullable(queryStrategy)
                .orElseGet(() -> new StaticQuery(sql));
    }

    protected void connect() throws DisException, SQLException {
        LOGGER.d("Connecting to database...");
        connection = connectionStrategy.createConnection();
    }

    protected void disconnect() throws SQLException {
        LOGGER.d("Closing connection...");
        connection.close();
    }
}
