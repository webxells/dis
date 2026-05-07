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
package com.webxells.dis.json.http.filter;

import com.webxells.dis.api.rest.filter.RequestFilter;
import com.webxells.dis.test.example.rest.TestRequest;
import java.io.ByteArrayInputStream;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ValidJsonRequestTest {

    @Test
    void test() {
        shouldFail("[invalid");
        shouldFail("[0,2,3");
        shouldFail("{[[invalid]}");
        shouldFail("{\"hashkey\":{\"calculatedHMAC\":\"f7b5addd27a221b216068cddb9abf1f06b3c0e1c\",\"secretkey\":\"12345\"},\"operation\":\"read\",\"attributes\":[\"name\",\"id\"],\"otid:\"12\"}");

        shouldWork("{}");
        shouldWork("\"test\"");
        shouldWork("{\"hashkey\":{\"calculatedHMAC\":\"f7b5addd27a221b216068cddb9abf1f06b3c0e1c\",\"secretkey\":\"12345\"},\"operation\":\"read\",\"attributes\":[\"name\",\"id\"],\"otid\":\"12\"}");
    }
    @Test
    void testRequiredPath() {
        ByteArrayInputStream inputStream = new ByteArrayInputStream("{\"hashkey\":{\"calculatedHMAC\":\"f7b5addd27a221b216068cddb9abf1f06b3c0e1c\",\"secretkey\":\"12345\"},\"operation\":\"read\",\"attributes\":[\"name\",\"id\"],\"otid\":\"12\"}".getBytes());
        TestRequest request = new TestRequest();
        request.setBody(inputStream);
        ValidJsonRequest fixture = new ValidJsonRequest();
        fixture.setRequiredPaths(Map.of("$.hashkey", ValidJsonRequest.Token.OBJECT, "$.attributes[1]", ValidJsonRequest.Token.STRING));
        assertEquals(Optional.empty(), fixture.resolve(request));
        fixture.setRequiredPaths(Map.of("$.hashkey", ValidJsonRequest.Token.OBJECT, "$.attributes[1]", ValidJsonRequest.Token.ARRAY));
        assertEquals(Optional.of(RequestFilter.ReturnState.NOT_ACCEPTABLE), fixture.resolve(request));
    }

    private void shouldWork(final String s) {
        assertEquals(Optional.empty(), callFixture(s));
    }

    private void shouldFail(final String s) {
        assertEquals(Optional.of(RequestFilter.ReturnState.NOT_ACCEPTABLE), callFixture(s));
    }

    private Optional<RequestFilter.ReturnState> callFixture(final String s) {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(s.getBytes());
        TestRequest request = new TestRequest();
        request.setBody(inputStream);
        ValidJsonRequest fixture = new ValidJsonRequest();
        return fixture.resolve(request);
    }

}