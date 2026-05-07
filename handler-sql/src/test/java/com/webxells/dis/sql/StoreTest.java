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
import com.webxells.dis.sql.refinement.TypeEnforcement;
import com.webxells.dis.sql.refinement.error.Static;
import com.webxells.dis.test.cases.ConfigurationBuilder;
import com.webxells.dis.test.cases.SimpleTestCase;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

class StoreTest extends SimpleTestCase {

    @Test
    void test() throws DisException, SQLException {
        String sql = "A B C :first ? :second :third :fourth `:first` :fifth #:first";
        String parsedSql = "A B C ? ? ? ? ? `:first` ? #:first";
        Connection connection = mock(Connection.class);
        ConnectionStrategy connectionStrategy = mock(ConnectionStrategy.class);
        when(connectionStrategy.createConnection())
                .thenReturn(connection);
        PreparedStatement preparedStatement = mock(PreparedStatement.class);
        when(connection.prepareStatement(parsedSql))
                .thenReturn(preparedStatement);
        MappingConfiguration mappingConfiguration = newConfiguration()
                .addPart(ConfigurationBuilder.newPart()
                        .setOutput("test", "first")
                        .withContent(String.valueOf(random(1L)))
                        .withRefinements(new TypeEnforcement(TypeEnforcement.Type.INTEGER))
                ).addPart(ConfigurationBuilder.newPart()
                        .setOutput("test", "1")
                        .withContent()
                        .withRefinements(new AccessByIndex(), new TypeEnforcement(TypeEnforcement.Type.STRING))
                ).addPart(ConfigurationBuilder.newPart()
                        .setOutput("test", "second")
                        .withContent()
                ).addPart(ConfigurationBuilder.newPart()
                        .setOutput("test", "third")
                        .withContent()
                        .withRefinements(new TypeEnforcement(TypeEnforcement.Type.TO_BOOLEAN))
                ).addPart(ConfigurationBuilder.newPart()
                        .setOutput("test", "fourth")
                        .withContent("1.1234")
                        .withRefinements(new TypeEnforcement(TypeEnforcement.Type.DECIMAL))
                ).addPart(ConfigurationBuilder.newPart()
                        .setOutput("test", "fifth")
                        .withContent("moep")
                        .withRefinements(new TypeEnforcement(TypeEnforcement.Type.INTEGER) {{
                            setErrorStrategy(new Static() {{
                                setValue("666");
                            }});
                        }})
                )
                .build();
        StoreConfiguration config = new StoreConfiguration();
        config.setConnection(connectionStrategy);
        config.setSql(sql);
        config.setName("test");
        config.setNoValueReplacement("0");
        Store fixture = new Store(config);
        fixture.start();
        fixture.write(mappingConfiguration);
        fixture.end();

        verify(preparedStatement).setLong(1, Long.valueOf(mappingConfiguration.parts().get(0).value().get()));
        verify(preparedStatement).setString(2, mappingConfiguration.parts().get(1).value().get());
        verify(preparedStatement).setString(3, mappingConfiguration.parts().get(2).value().get());
        verify(preparedStatement).setBoolean(4, true);
        verify(preparedStatement).setBigDecimal(5, new BigDecimal("1.1234"));
        verify(preparedStatement).setLong(6, 666L);
        verify(preparedStatement).execute();
        verifyNoMoreInteractions(preparedStatement);
    }

}