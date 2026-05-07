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
package com.webxells.dis.rest.content;

import com.webxells.dis.rest.execution.Response;
import com.webxells.dis.test.cases.SimpleTestCase;
import java.io.IOException;
import java.io.InputStream;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;

class HttpStatusCodeTest extends SimpleTestCase {
    @Test
    void test() throws IOException {
        int code = random(1);
        HttpStatusCode fixture = new HttpStatusCode();
        assertThrows(UnsupportedOperationException.class,
                () -> fixture.parseInputRequest(null, null, null));

        Response response = Mockito.mock(Response.class);
        Mockito.when(response.getStatusCode()).thenReturn(code);
        try(InputStream result = fixture.parseOutputRequest(response)) {
            assertEquals(code, Integer.parseInt(new String(result.readAllBytes())));
        }


    }

}