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
package com.webxells.dis.sql.internal.query;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class StaticQuery implements QueryStrategy {
    private final String sql;

    public StaticQuery(final String sql) {
        this.sql = sql;
    }

    @Override
    public PreparedStatement createStatement(final Connection connection) throws SQLException {
        return connection.prepareStatement(sql);
    }
}
