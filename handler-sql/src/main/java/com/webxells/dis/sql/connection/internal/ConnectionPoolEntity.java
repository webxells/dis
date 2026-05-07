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
package com.webxells.dis.sql.connection.internal;

import com.webxells.dis.api.error.DisException;
import com.webxells.dis.sql.connection.ConnectionStrategy;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicInteger;

public class ConnectionPoolEntity {
    private final AtomicInteger usageCount = new AtomicInteger();
    private final ConnectionWrapper connectionWrapper;

    public ConnectionPoolEntity(final String name, final ConnectionStrategy connection) throws SQLException, DisException {
        connectionWrapper = new ConnectionWrapper(this, connection, name);
    }

    public Connection connect() {
        usageCount.incrementAndGet();
        return connectionWrapper;
    }

    public int getSumOfConnectionAfterClose() {
        return usageCount.decrementAndGet();
    }
}