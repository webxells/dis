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
package com.webxells.dis.rest.resource;

import com.webxells.dis.api.error.InputOutputError;
import com.webxells.dis.rest.RestConfig;
import com.webxells.dis.rest.content.Body;
import com.webxells.dis.rest.content.NumericParameters;
import com.webxells.dis.rest.content.paginating.NumericParameterChange;
import com.webxells.dis.rest.url.StandardUriQuery;
import com.webxells.dis.test.cases.MockServerTestCase;
import com.webxells.dis.test.cases.mock.MockResponse;
import com.webxells.dis.test.cases.mock.RecordedRequest;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PaginatingCallTest extends MockServerTestCase {

    @Test
    void test() throws InputOutputError, InterruptedException {
        PaginatingCall fixture = new PaginatingCall();
        assertThrows(UnsupportedOperationException.class, fixture::send);
        final RestConfig config = new RestConfig();
        config.setBaseUrl(getUrl());
        config.setUrlQueryParameters(new StandardUriQuery() {{
            setParameters(new LinkedHashMap<>() {{
                put("start", "100");
                put("limit", "100");
                put("page", "0");
                put("invalid", "adsda");
                put("perPage", "25");
            }});
        }});
        fixture.setConfiguration(config);
        fixture.setResponseContentStrategy(new Body());
        fixture.setRequestContentStrategy(List.of(new NumericParameters() {{
            setParameterChanges(List.of(
                    new NumericParameterChange() {{
                        setName("page");
                        setIncrement(2);
                        setStartValue(5);
                    }},
                    new NumericParameterChange() {{
                        setName("limit");
                        setIncrement(100);
                        setStartValue(200);
                    }},
                    new NumericParameterChange() {{
                        setName("invalid");
                    }},
                    new NumericParameterChange() {{
                        setName("start");
                        setIncrement(100);
                    }}));
            }}));
        enqueue(new MockResponse()
                .setBody("something")
                .setResponseCode(200));
        fixture.receive();
        RecordedRequest recordedRequest = takeRequest(2, ChronoUnit.SECONDS);
        assertEquals(getUrl("?start=100&limit=200&page=5&invalid=0&perPage=25"),
                recordedRequest.getUrl());
        fixture.refresh();
        enqueue(new MockResponse()
                .setBody("something")
                .setResponseCode(200));
        fixture.receive();
        recordedRequest = takeRequest(2, ChronoUnit.SECONDS);
        assertEquals(getUrl("?start=200&limit=300&page=7&invalid=1&perPage=25"),
                recordedRequest.getUrl());
        fixture.reset();
    }
}