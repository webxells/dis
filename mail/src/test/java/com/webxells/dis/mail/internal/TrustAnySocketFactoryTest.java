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
package com.webxells.dis.mail.internal;

import java.io.IOException;
import java.net.Socket;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TrustAnySocketFactoryTest {

    @Test
    void test() throws NoSuchAlgorithmException, KeyManagementException, IOException {
        TrustAnySocketFactory fixture = new TrustAnySocketFactory();
        Socket socket = Mockito.mock(Socket.class);
        Mockito.when(socket.isConnected()).thenReturn(true);
        fixture.createSocket(socket, null, 0, false);
        assertEquals(0, fixture.getDefaultCipherSuites().length);
        assertEquals(0, fixture.getSupportedCipherSuites().length);

    }

}