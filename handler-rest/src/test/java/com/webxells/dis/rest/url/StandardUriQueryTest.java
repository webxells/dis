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
package com.webxells.dis.rest.url;

import com.webxells.dis.test.cases.SimpleTestCase;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class StandardUriQueryTest extends SimpleTestCase {

    @Test
    void test() {
        StandardUriQuery fixture = new StandardUriQuery();
        final String titleShouldBeEncoded = "öäf+poru asdu";
        final String valueShouldBeEncoded = "äöüß und soä & asdasd xD = sad ??&";
        Map<String, String> parameters = new LinkedHashMap<>() {{
            put("test", random());
            put(titleShouldBeEncoded, valueShouldBeEncoded);
        }};
        fixture.setParameters(parameters);
        assertEquals(String.format("?test=%s&%s=%s", parameters.get("test"),
                "%C3%B6%C3%A4f%2Bporu%20asdu",
                "%C3%A4%C3%B6%C3%BC%C3%9F%20und%20so%C3%A4%20%26%20asdasd%20xD%20%3D%20sad%20%3F%3F%26"),
                fixture.parse());

        fixture.setParameterAssignment("-");
        fixture.setParameterDelimiter("/");
        fixture.setStartString("/");
        fixture.setParameterEncoding("");
        fixture.setParameterEncoding("ISO-8859-1");

        assertEquals(String.format("/test-%s/%s-%s", parameters.get("test"),
                "%F6%E4f%2Bporu%20asdu",
                "%E4%F6%FC%DF%20und%20so%E4%20%26%20asdasd%20xD%20%3D%20sad%20%3F%3F%26"),
                fixture.parse());
    }


}