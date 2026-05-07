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
package com.webxells.dis.sql.connection;

import com.webxells.dis.api.error.DisException;
import com.webxells.dis.sql.connection.internal.ConnectionWrapper;
import com.webxells.dis.test.cases.SimpleTestCase;
import java.sql.Connection;
import java.sql.SQLException;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

class GlobalConnectionPoolTest extends SimpleTestCase {

    @Test
    void test() throws SQLException, DisException {

        String name = random();
        Connection connection = Mockito.mock(Connection.class);
        ConnectionStrategy connectionStrategy = mock(ConnectionStrategy.class);
        when(connectionStrategy.createConnection())
                .thenReturn(connection);
        GlobalConnectionPool fixture = new GlobalConnectionPool();
        fixture.setConnection(connectionStrategy);
        fixture.setName(name);

        final Connection connectionWrapper = fixture.createConnection();

        verify(connectionStrategy).createConnection();

        assertInstanceOf(ConnectionWrapper.class, connectionWrapper);
        assertSame(connectionWrapper, fixture.createConnection());
        assertSame(connectionWrapper, fixture.createConnection());

        verifyNoMoreInteractions(connectionStrategy);

        connectionWrapper.close();
        verifyNoMoreInteractions(connectionStrategy);
        connectionWrapper.close();
        connectionWrapper.close();

        verify(connection).close();
        verifyNoMoreInteractions(connectionStrategy);
    }

}