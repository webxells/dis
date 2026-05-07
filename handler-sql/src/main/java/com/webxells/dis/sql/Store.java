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
import com.webxells.dis.api.config.MappingConfiguration;
import com.webxells.dis.api.config.MappingPart;
import com.webxells.dis.api.error.DisException;
import com.webxells.dis.api.error.InputOutputError;
import com.webxells.dis.api.output.Output;
import com.webxells.dis.logging.LoggerProxyFactory;
import com.webxells.dis.sql.internal.MatchingPreparedStatement;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class Store extends StatefulSqlOperation implements Output<StoreConfiguration> {
    private static final Logger LOGGER = LoggerProxyFactory.logger(Store.class);

    private final String noValueReplacement;
    private final boolean skipUnusedParameters;

    public Store(final StoreConfiguration config) {
        super(config);
        noValueReplacement = config.getNoValueReplacement();
        skipUnusedParameters = config.isSkipUnusedParameters();
    }

    @Override
    public void write(final MappingConfiguration to) throws InputOutputError {
        try {
            final MatchingPreparedStatement query = new MatchingPreparedStatement(sql, noValueReplacement, skipUnusedParameters);
            query.setParameters(to.partsByDestination(name), MappingPart::getOutput);
            final PreparedStatement statement = query.getStatement(connection);
            LOGGER.d("Executing query: " + statement);
            statement.execute();
        } catch (final SQLException e) {
            throw new InputOutputError("Could not execute query", e);
        }
    }


    @Override
    public void start() throws DisException {
        try {
            connect();
        } catch (final SQLException e) {
            throw new InputOutputError("Could not connect to the database.", e);
        }
    }

    @Override
    public void end() throws DisException {
        try {
            disconnect();
        } catch (final SQLException e) {
            throw new InputOutputError("Could not disconnect from the database.", e);
        }
    }
}