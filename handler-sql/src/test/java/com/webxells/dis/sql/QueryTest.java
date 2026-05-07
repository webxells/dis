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

import com.webxells.dis.api.config.MappingConfiguration;
import com.webxells.dis.api.config.MappingPart;
import com.webxells.dis.api.error.DisException;
import com.webxells.dis.base.config.SimpleMappingPoint;
import com.webxells.dis.base.config.StableMappingPart;
import com.webxells.dis.sql.connection.ConnectionStrategy;
import com.webxells.dis.sql.connection.StaticConnection;
import com.webxells.dis.sql.refinement.AccessByIndex;
import com.webxells.dis.test.cases.ConfigurationBuilder;
import com.webxells.dis.test.cases.SimpleTestCase;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class QueryTest extends SimpleTestCase {

    @Test
    void test() throws DisException, SQLException {
        String queryString = random();
        String field1result1 = random();
        String field1result2 = random();
        String field2result1 = random();
        String field2result2 = random();
        Connection connection = mock(Connection.class);
        PreparedStatement preparedStatement = mock(PreparedStatement.class);
        ResultSet resultSet = mock(ResultSet.class);
        ConnectionStrategy connectionStrategy = mock(ConnectionStrategy.class);
        QueryConfiguration configuration = new QueryConfiguration();
        assertEquals(Query.class.getName(), configuration.getType());
        configuration.setName(random());
        configuration.setConnection(connectionStrategy);
        configuration.setSql(queryString);
        MappingConfiguration mapping = newConfiguration()
                .addPart(ConfigurationBuilder.newPart()
                        .setInput(new SimpleMappingPoint(configuration.getName(), random()))
                )
                .build();
        mapping.parts().add(createIndexSqlPart(mapping, configuration.getName()));
        when(connectionStrategy.createConnection()).thenReturn(connection);
        when(connection.prepareStatement(queryString)).thenReturn(preparedStatement);
        when(preparedStatement.getResultSet()).thenReturn(resultSet);
        when(resultSet.isAfterLast()).thenReturn(false);
        when(resultSet.isLast())
                .thenReturn(false)
                .thenReturn(false)
                .thenReturn(true);
        when(resultSet.next())
                .thenReturn(true)
                .thenReturn(true)
                .thenReturn(true);
        when(resultSet.getString(mapping.parts().get(0).getInput().getPath()))
                .thenReturn(field1result1)
                .thenReturn(field1result2);
        when(resultSet.getString(mapping.parts().get(1).getFirstRefinement(AccessByIndex.class).orElseThrow().getIndex()))
                .thenReturn(field2result1)
                .thenReturn(field2result2);

        Query fixture = new Query(configuration);
        fixture.start();
        assertTrue(fixture.hasNext());
        assertEquals(2, fixture.read(mapping));
        assertEquals(field1result1, mapping.parts().get(0).value().get());
        assertEquals(field2result1, mapping.parts().get(1).value().get());
        mapping.clear();
        assertTrue(fixture.hasNext());
        assertEquals(2, fixture.read(mapping));
        assertEquals(field1result2, mapping.parts().get(0).value().get());
        assertEquals(field2result2, mapping.parts().get(1).value().get());
        assertFalse(fixture.hasNext());
        fixture.end();

        InOrder inOrder = inOrder(connectionStrategy, connection, preparedStatement, resultSet);
        inOrder.verify(connectionStrategy).createConnection();
        inOrder.verify(connection).prepareStatement(queryString);
        inOrder.verify(preparedStatement).execute();
        inOrder.verify(preparedStatement).getResultSet();
        inOrder.verify(resultSet).isLast();
        inOrder.verify(resultSet).isAfterLast();
        inOrder.verify(resultSet).next();
        inOrder.verify(resultSet).getString(mapping.parts().get(0).getInput().getPath());
        inOrder.verify(resultSet).getString(mapping.parts().get(1).getFirstRefinement(AccessByIndex.class).orElseThrow().getIndex());
        inOrder.verify(resultSet).isLast();
        inOrder.verify(resultSet).isAfterLast();
        inOrder.verify(resultSet).next();
        inOrder.verify(resultSet).getString(mapping.parts().get(0).getInput().getPath());
        inOrder.verify(resultSet).getString(mapping.parts().get(1).getFirstRefinement(AccessByIndex.class).orElseThrow().getIndex());
        inOrder.verify(resultSet).isLast();
        inOrder.verify(resultSet).close();
        inOrder.verify(connection).close();
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    void testSetter() {
        StaticConnection staticConnection = new StaticConnection();
        staticConnection.setJdbcUrl(random());
        staticConnection.setUsername(random());
        staticConnection.setPassword(random());
        staticConnection.setParameters(Map.of(random(), random()));
    }

    private MappingPart createIndexSqlPart(final MappingConfiguration mapping, final String name) {
        StableMappingPart result = new StableMappingPart(mapping);
        result.setInput(new SimpleMappingPoint(name, random()));
        result.addRefinement(new AccessByIndex(random(1)));
        return result;
    }

}
