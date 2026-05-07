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
package com.webxells.dis.sql.refinement;

import com.webxells.dis.api.Logger;
import com.webxells.dis.api.config.description.Default;
import com.webxells.dis.api.config.description.Description;
import com.webxells.dis.base.refinement.SimpleRefinement;
import com.webxells.dis.logging.LoggerProxyFactory;
import com.webxells.dis.sql.refinement.error.Error;
import com.webxells.dis.sql.refinement.error.Strategy;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;

@Description("Enforces type for part value in sql handling")
public class TypeEnforcement extends SimpleRefinement {
    public enum Type {
        STRING {
            @Override
            void setTypeToStatement(final PreparedStatement statement, final int index, final String value) throws SQLException {
                statement.setString(index, value);
            }
        },
        @Description("Enforces a boolean value")
        TO_BOOLEAN {
            @Override
            void setTypeToStatement(final PreparedStatement statement, final int index, final String value) throws SQLException {
                statement.setBoolean(index, !(value.equals("0") || value.equalsIgnoreCase("false")));
            }
        },
        DECIMAL {
            @Override
            void setTypeToStatement(final PreparedStatement statement, final int index, final String value) throws SQLException {
                statement.setBigDecimal(index, new BigDecimal(value));
            }
        },
        INTEGER {
            @Override
            void setTypeToStatement(final PreparedStatement statement, final int index, final String value) throws SQLException {
                statement.setLong(index, Long.parseLong(value));
            }
        };

        abstract void setTypeToStatement(PreparedStatement statement, int index, String value) throws SQLException;

        void setStatement(final PreparedStatement statement, final int index, final String value) throws SQLException {
            if (null == value) {
                statement.setNull(index, Types.VARCHAR);
            } else {
                setTypeToStatement(statement, index, value);
            }
        }
    }

    private static final Logger LOGGER = LoggerProxyFactory.logger(TypeEnforcement.class);

    @Description("Type that will be enforced")
    @Default("STRING")
    private Type to = Type.STRING;
    @Description("If type enforcement fails try with this strategy")
    @Default("Throws an error")
    private Strategy errorStrategy = new Error();

    public TypeEnforcement() { }

    public TypeEnforcement(final Type to) {
        this.to = to;
    }

    public void setValueToStatement(final PreparedStatement statement, int index, String rawValue) throws SQLException {
        try {
            to.setStatement(statement, index, rawValue);
        } catch (final Throwable e) {
            LOGGER.e("Could not enforce type to ".concat(to.name()), e);
            final String noValueValue = errorStrategy.getValueForErrorEnforcement();
            LOGGER.i("Switch to errorValue: " + noValueValue);
            try {
                to.setStatement(statement, index, noValueValue);
            } catch (final Throwable f) {
                throw new SQLException("Could not enforce type to ".concat(to.name()), f);
            }
        }
    }

    public void setTo(final Type to) {
        this.to = to;
    }

    public void setErrorStrategy(final Strategy errorStrategy) {
        this.errorStrategy = errorStrategy;
    }
}