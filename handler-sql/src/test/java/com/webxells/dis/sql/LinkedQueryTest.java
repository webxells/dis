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
import com.webxells.dis.api.error.DisException;
import com.webxells.dis.sql.connection.ConnectionStrategy;
import com.webxells.dis.sql.refinement.AccessByIndex;
import com.webxells.dis.test.cases.ConfigurationBuilder;
import com.webxells.dis.test.cases.SimpleTestCase;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

class LinkedQueryTest extends SimpleTestCase {

    @Test
    void test() throws DisException, SQLException {
        String sql = "A B C ? ? ?";
        Connection connection = mock(Connection.class);
        ConnectionStrategy connectionStrategy = mock(ConnectionStrategy.class);
        PreparedStatement statement = mock(PreparedStatement.class);
        ResultSet resultSet = mock(ResultSet.class);
        LinkedQuery fixture = new LinkedQuery();
        fixture.setSql(sql);
        fixture.setConnectionStrategy(connectionStrategy);
        fixture.setInputName("input");
        fixture.setOutputName("output");
        when(connectionStrategy.createConnection())
                .thenReturn(connection);
        when(connection.prepareStatement(sql))
                .thenReturn(statement);
        when(statement.getResultSet())
                .thenReturn(resultSet);
        when(resultSet.isLast())
                .thenReturn(false);
        when(resultSet.isAfterLast())
                .thenReturn(false);
        when(resultSet.next())
                .thenReturn(true);
        MappingConfiguration mappingConfiguration = newConfiguration()
                .addPart(ConfigurationBuilder.newPart()
                        .setOutput("output", "0")
                        .withRefinements(new AccessByIndex())
                        .withContent()
                )
                .addPart(ConfigurationBuilder.newPart()
                        .setOutput("output", "1")
                        .withRefinements(new AccessByIndex())
                        .withContent()
                )
                .addPart(ConfigurationBuilder.newPart()
                        .setOutput("output", "2")
                        .withRefinements(new AccessByIndex())
                        .withContent()
                )
                .addPart(ConfigurationBuilder.newPart()
                        .setInput("input", "0")
                        .withRefinements(new AccessByIndex())
                )
                .addPart(ConfigurationBuilder.newPart()
                        .setInput("input", "1")
                        .withRefinements(new AccessByIndex())
                )
                .build();

        fixture.start();
        verifyNoInteractions(statement, resultSet);
        fixture.getData(mappingConfiguration);
        verify(statement).setString(1, mappingConfiguration.parts().get(0).value().get());
        verify(statement).setString(2, mappingConfiguration.parts().get(1).value().get());
        verify(statement).setString(3, mappingConfiguration.parts().get(2).value().get());
        verify(statement).execute();
        verify(statement).getResultSet();
        verify(resultSet).isLast();
        verify(resultSet).isAfterLast();
        verify(resultSet).next();
        verify(resultSet).getString(0);
        verify(resultSet).getString(1);
        fixture.end();
        verify(resultSet).close();
        verifyNoMoreInteractions(statement);
    }

}