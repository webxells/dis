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

import com.webxells.dis.api.config.description.Description;
import com.webxells.dis.api.config.description.Required;
import com.webxells.dis.api.error.InvalidApi;
import com.webxells.dis.sql.connection.ConnectionStrategy;
import com.webxells.dis.sql.internal.query.QueryStrategy;

public class SqlConfiguration {
    @Description("Reference of this handler")
    @Required
    protected String name;
    @Required
    @Description("Connection settings")
    private ConnectionStrategy connection;
    private String sql;
    private QueryStrategy queryStrategy;

    public void validate() throws InvalidApi {
        if (null == name || null == sql || null == connection) {
            throw new InvalidApi("Required fields are missing");
        }
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public ConnectionStrategy getConnection() {
        return connection;
    }

    public void setConnection(final ConnectionStrategy connection) {
        this.connection = connection;
    }

    @Required
    public void setSql(final String sql) {
        this.sql = sql;
    }

    public String getSql() {
        return sql;
    }

    QueryStrategy getQueryStrategy() {
        return queryStrategy;
    }

    void setQueryStrategy(final QueryStrategy queryStrategy) {
        this.queryStrategy = queryStrategy;
    }
}