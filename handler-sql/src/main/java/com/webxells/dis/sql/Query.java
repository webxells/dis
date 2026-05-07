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
import com.webxells.dis.api.input.Input;
import com.webxells.dis.base.SimpleDatasetPiece;
import com.webxells.dis.logging.LoggerProxyFactory;
import com.webxells.dis.sql.refinement.AccessByIndex;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Query extends StatefulSqlOperation implements Input<QueryConfiguration> {
    private static final Logger LOGGER = LoggerProxyFactory.logger(Query.class);

    private ResultSet result;

    public Query(final QueryConfiguration config) {
        super(config);
    }

    @Override
    public int read(final MappingConfiguration from) throws InputOutputError {
        int sum = 0;
        if (toNext()) {
            for (MappingPart part : from.partsBySource(name)) {
                if (null != part.getInput().getPath()) {
                    try {
                        part.getDataset().collect(new SimpleDatasetPiece(getResult(part)));
                    } catch (final SQLException e) {
                        throw new InputOutputError("Could not read path from result", e);
                    }
                    sum++;
                }
            }
        }
        return sum;
    }

    private String getResult(final MappingPart part) throws SQLException, InputOutputError {
        final String path = part.getInput().getPath();
        if (part.hasRefinement(AccessByIndex.class)) {
            try {
                final int index = part.getFirstRefinement(AccessByIndex.class)
                    .map(AccessByIndex::getIndex)
                    .orElseGet(() -> Integer.parseInt(path));
                return result.getString(index);
            } catch (final NumberFormatException e) {
                throw new InputOutputError("mapping path is not a number: " + path, e);
            }

        }
        return result.getString(path);
    }

    private boolean toNext() {
        try {
            if (!result.next()) {
                LOGGER.i("No more rows");
                return false;
            }
        } catch (final SQLException e) {
            throw new RuntimeException(e);
        }
        return true;
    }

    @Override
    public boolean hasNext() throws InputOutputError {
        try {
            return !(result.isLast() || result.isAfterLast());
        } catch (final SQLException e) {
            throw new InputOutputError("could not read result", e);
        }
    }

    @Override
    public void start() throws DisException {
        try {
            connect();
            final PreparedStatement statement = getQueryStrategy().createStatement(connection);
            LOGGER.d("Executing query: " + statement);
            statement.execute();
            LOGGER.d("Fetching results...");
            result = statement.getResultSet();
        } catch (final SQLException e) {
            throw new InputOutputError("Could not connect to the database.", e);
        }
    }

    @Override
    public void end() throws DisException {
        try {
            result.close();
            disconnect();
        } catch (final SQLException e) {
            throw new InputOutputError("Could not disconnect to the database.", e);
        }
    }
}