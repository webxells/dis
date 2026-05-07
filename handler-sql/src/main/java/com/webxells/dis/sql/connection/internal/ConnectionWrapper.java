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

import com.webxells.dis.api.Logger;
import com.webxells.dis.api.error.DisException;
import com.webxells.dis.logging.LoggerProxyFactory;
import com.webxells.dis.sql.connection.ConnectionStrategy;
import java.sql.Array;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.NClob;
import java.sql.PreparedStatement;
import java.sql.SQLClientInfoException;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Savepoint;
import java.sql.Statement;
import java.sql.Struct;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

public class ConnectionWrapper implements Connection {
    private static final Logger LOGGER = LoggerProxyFactory.logger(ConnectionWrapper.class);

    private final ConnectionPoolEntity poolEntity;
    private final ConnectionStrategy connectionStrategy;
    private final String name;
    private Connection connection;

    public ConnectionWrapper(final ConnectionPoolEntity poolEntity, final ConnectionStrategy connectionStrategy, final String name) throws SQLException {
        this.poolEntity = poolEntity;
        this.connectionStrategy = connectionStrategy;
        this.name = name;
        //in case of early calling of GlobalConnectionPool.createConnection()
        //with no connection present, we connect immediately
        connection();
    }

    private Connection connection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            LOGGER.d("Establishing connection: ".concat(name));
            try {
                connection = connectionStrategy.createConnection();
                if (LOGGER.has(Logger.LogLevel.TRACE) && connection != null) {
                    LOGGER.t("Client info:%n%s", connection.getClientInfo()
                            .entrySet().stream()
                            .map(a -> String.format("%s = %s", a.getKey(), a.getValue()))
                            .collect(Collectors.joining()));
                }
            } catch (final DisException e) {
                throw new SQLException("Could not connect to database", e);
            }
        }
        return connection;
    }

    @Override
    public void close() throws SQLException {
        if (poolEntity.getSumOfConnectionAfterClose() < 1) {
            LOGGER.d("Connection not necessary anymore - Closing...");
            connection().close();
        } else {
            LOGGER.d("Connection required by other resources - Skipping");
        }
    }

    @Override
    public Statement createStatement() throws SQLException {
        return connection().createStatement();
    }

    @Override
    public PreparedStatement prepareStatement(final String sql) throws SQLException {
        return connection().prepareStatement(sql);
    }

    @Override
    public CallableStatement prepareCall(final String sql) throws SQLException {
        return connection().prepareCall(sql);
    }

    @Override
    public String nativeSQL(final String sql) throws SQLException {
        return connection().nativeSQL(sql);
    }

    @Override
    public void setAutoCommit(final boolean autoCommit) throws SQLException {
        connection().setAutoCommit(autoCommit);
    }

    @Override
    public boolean getAutoCommit() throws SQLException {
        return connection().getAutoCommit();
    }

    @Override
    public void commit() throws SQLException {
        connection().commit();
    }

    @Override
    public void rollback() throws SQLException {
        connection().rollback();
    }

    @Override
    public boolean isClosed() throws SQLException {
        return connection().isClosed();
    }

    @Override
    public DatabaseMetaData getMetaData() throws SQLException {
        return connection().getMetaData();
    }

    @Override
    public void setReadOnly(final boolean readOnly) throws SQLException {
        connection().setReadOnly(readOnly);
    }

    @Override
    public boolean isReadOnly() throws SQLException {
        return connection().isReadOnly();
    }

    @Override
    public void setCatalog(final String catalog) throws SQLException {
        connection().setCatalog(catalog);
    }

    @Override
    public String getCatalog() throws SQLException {
        return connection().getCatalog();
    }

    @Override
    public void setTransactionIsolation(final int level) throws SQLException {
        connection().setTransactionIsolation(level);
    }

    @Override
    public int getTransactionIsolation() throws SQLException {
        return connection().getTransactionIsolation();
    }

    @Override
    public SQLWarning getWarnings() throws SQLException {
        return connection().getWarnings();
    }

    @Override
    public void clearWarnings() throws SQLException {
        connection().clearWarnings();
    }

    @Override
    public Statement createStatement(final int resultSetType, final int resultSetConcurrency) throws SQLException {
        return connection().createStatement(resultSetType, resultSetConcurrency);
    }

    @Override
    public PreparedStatement prepareStatement(final String sql, final int resultSetType, final int resultSetConcurrency) throws SQLException {
        return connection().prepareStatement(sql, resultSetType, resultSetConcurrency);
    }

    @Override
    public CallableStatement prepareCall(final String sql, final int resultSetType, final int resultSetConcurrency) throws SQLException {
        return connection().prepareCall(sql, resultSetType, resultSetConcurrency);
    }

    @Override
    public Map<String, Class<?>> getTypeMap() throws SQLException {
        return connection().getTypeMap();
    }

    @Override
    public void setTypeMap(final Map<String, Class<?>> map) throws SQLException {
        connection().setTypeMap(map);
    }

    @Override
    public void setHoldability(final int holdability) throws SQLException {
        connection().setHoldability(holdability);
    }

    @Override
    public int getHoldability() throws SQLException {
        return connection().getHoldability();
    }

    @Override
    public Savepoint setSavepoint() throws SQLException {
        return connection().setSavepoint();
    }

    @Override
    public Savepoint setSavepoint(final String name) throws SQLException {
        return connection().setSavepoint(name);
    }

    @Override
    public void rollback(final Savepoint savepoint) throws SQLException {
        connection().rollback(savepoint);
    }

    @Override
    public void releaseSavepoint(final Savepoint savepoint) throws SQLException {
        connection().releaseSavepoint(savepoint);
    }

    @Override
    public Statement createStatement(final int resultSetType, final int resultSetConcurrency, final int resultSetHoldability) throws SQLException {
        return connection().createStatement(resultSetType, resultSetConcurrency, resultSetHoldability);
    }

    @Override
    public PreparedStatement prepareStatement(final String sql, final int resultSetType, final int resultSetConcurrency, final int resultSetHoldability) throws SQLException {
        return connection().prepareStatement(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
    }

    @Override
    public CallableStatement prepareCall(final String sql, final int resultSetType, final int resultSetConcurrency, final int resultSetHoldability) throws SQLException {
        return connection().prepareCall(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
    }

    @Override
    public PreparedStatement prepareStatement(final String sql, final int autoGeneratedKeys) throws SQLException {
        return connection().prepareStatement(sql, autoGeneratedKeys);
    }

    @Override
    public PreparedStatement prepareStatement(final String sql, final int[] columnIndexes) throws SQLException {
        return connection().prepareStatement(sql, columnIndexes);
    }

    @Override
    public PreparedStatement prepareStatement(final String sql, final String[] columnNames) throws SQLException {
        return connection().prepareStatement(sql, columnNames);
    }

    @Override
    public Clob createClob() throws SQLException {
        return connection().createClob();
    }

    @Override
    public Blob createBlob() throws SQLException {
        return connection().createBlob();
    }

    @Override
    public NClob createNClob() throws SQLException {
        return connection().createNClob();
    }

    @Override
    public SQLXML createSQLXML() throws SQLException {
        return connection().createSQLXML();
    }

    @Override
    public boolean isValid(final int timeout) throws SQLException {
        return connection().isValid(timeout);
    }

    @Override
    public void setClientInfo(final String name, final String value) throws SQLClientInfoException {
        try {
            connection().setClientInfo(name, value);
        } catch (final SQLException e) {
            throw new RuntimeException("There was an error",  e);
        }
    }

    @Override
    public void setClientInfo(final Properties properties) throws SQLClientInfoException {
        try {
            connection().setClientInfo(properties);
        } catch (final SQLException e) {
            throw new RuntimeException("There was an error",  e);
        }
    }

    @Override
    public String getClientInfo(final String name) throws SQLException {
        return connection().getClientInfo(name);
    }

    @Override
    public Properties getClientInfo() throws SQLException {
        return connection().getClientInfo();
    }

    @Override
    public Array createArrayOf(final String typeName, final Object[] elements) throws SQLException {
        return connection().createArrayOf(typeName, elements);
    }

    @Override
    public Struct createStruct(final String typeName, final Object[] attributes) throws SQLException {
        return connection().createStruct(typeName, attributes);
    }

    @Override
    public void setSchema(final String schema) throws SQLException {
        connection().setSchema(schema);
    }

    @Override
    public String getSchema() throws SQLException {
        return connection().getSchema();
    }

    @Override
    public void abort(final Executor executor) throws SQLException {
        connection().abort(executor);
    }

    @Override
    public void setNetworkTimeout(final Executor executor, final int milliseconds) throws SQLException {
        connection().setNetworkTimeout(executor, milliseconds);
    }

    @Override
    public int getNetworkTimeout() throws SQLException {
        return connection().getNetworkTimeout();
    }

    @Override
    public <T> T unwrap(final Class<T> iface) throws SQLException {
        return connection().unwrap(iface);
    }

    @Override
    public boolean isWrapperFor(final Class<?> iface) throws SQLException {
        return connection().isWrapperFor(iface);
    }
}