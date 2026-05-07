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
package com.webxells.dis.sql.internal;

import com.webxells.dis.api.Logger;
import com.webxells.dis.api.config.MappingPart;
import com.webxells.dis.api.config.MappingPoint;
import com.webxells.dis.logging.LoggerProxyFactory;
import com.webxells.dis.sql.refinement.AccessByIndex;
import com.webxells.dis.sql.refinement.NoValueReplacement;
import com.webxells.dis.sql.refinement.TypeEnforcement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public class MatchingPreparedStatement {
    private static class Value {
        public TypeEnforcement typeEnforcement;
        private String value;
        private boolean set;

        private Value(final String value) {
            this.value = value;
        }
    }

    private static final Logger LOGGER = LoggerProxyFactory.logger(MatchingPreparedStatement.class);

    private final String noValueReplacement;
    private final List<String> parameterNames;
    private final List<Value> parameterValues = new ArrayList<>();
    private final SqlParser parser;
    private final boolean skipUnusedParameters;

    private int currentUnnamedParameterIndex = 0;

    public MatchingPreparedStatement(final String sql, final String noValueReplacement, final boolean skipUnusedParameters) {
        this.noValueReplacement = noValueReplacement;
        parser = new SqlParser(sql);
        this.skipUnusedParameters = skipUnusedParameters;
        parameterNames = parser.getParameterNames();
        parameterNames.forEach(a -> parameterValues.add(new Value(noValueReplacement)));
    }

    public void setParameter(final String index, final String value, final TypeEnforcement typeEnforcement) throws SQLException {
        final int i = parameterNames.indexOf(index);
        if (i < 0) {
            if (skipUnusedParameters) {
                return;
            }
            throw new SQLException(error("Invalid index provided: " + index));
        }
        setParameter(i, value, typeEnforcement);
    }

    public void setParameter(final int index, final String value, final TypeEnforcement typeEnforcement) throws SQLException {
        final Value entity = parameterValues.get(index);
        if (null == entity) {
            throw new SQLException(error("Invalid numeral index provided: " + index));
        }
        entity.set = true;
        entity.value = value;
        entity.typeEnforcement = typeEnforcement;
    }

    public void setParameter(final String value, final TypeEnforcement typeEnforcement) throws SQLException {
        if (currentUnnamedParameterIndex >= parameterValues.size()) {
            LOGGER.w(error(String.format("More parameters (%d) than placeholder found -> Skipping...", currentUnnamedParameterIndex)));
        }
        while (parameterValues.size() > currentUnnamedParameterIndex &&
                parameterValues.get(currentUnnamedParameterIndex).set) {
            currentUnnamedParameterIndex++;
        }
        setParameter(currentUnnamedParameterIndex, value, typeEnforcement);
    }

    private String error(final String message) {
        return String.format("%s in %n%s", message, parser.getSql());
    }

    public PreparedStatement getStatement(final Connection connection) throws SQLException {
        final PreparedStatement preparedStatement = connection.prepareStatement(parser.getSql());
        for (int i = 0, m = parameterValues.size(); i < m; i++) {
            final Value current = parameterValues.get(i);
            final int index = i + 1;
            if (null == current.typeEnforcement) {
                preparedStatement.setString(index, current.value);
            } else {
                current.typeEnforcement.setValueToStatement(preparedStatement, index, current.value);
            }
        }
        return preparedStatement;
    }

    public void setParameters(final List<MappingPart> to, final Function<MappingPart, MappingPoint> howToGetPoint) throws SQLException {
        for (MappingPart part : to) {
            final String value = getValue(part);
            final Optional<AccessByIndex> accessByIndex = part.getFirstRefinement(AccessByIndex.class);
            final TypeEnforcement typeEnforcement = part.getFirstRefinement(TypeEnforcement.class).orElse(null);
            if (accessByIndex.isPresent()) {
                setParameter(getIndex(accessByIndex.get().getIndex(), howToGetPoint.apply(part)), value, typeEnforcement);
            } else {
                setParameter(howToGetPoint.apply(part).getPath(), value, typeEnforcement);
            }
        }
    }

    private String getValue(final MappingPart part) {
        return part.value().orElseGet(() -> part.getFirstRefinement(NoValueReplacement.class)
                .map(NoValueReplacement::getReplacement)
                .orElse(noValueReplacement));
    }


    private int getIndex(final Integer index, final MappingPoint point) throws SQLException {
        if (null == index)  {
            try {
                return Integer.parseInt(point.getPath());
            } catch (final NumberFormatException e) {
                throw new SQLException("Could not parse mapping part path to index", e);
            }
        }
        return index;
    }
}