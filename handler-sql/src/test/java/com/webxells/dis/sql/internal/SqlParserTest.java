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

import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SqlParserTest {

    @Test
    void test() {
        assertSqlHasParameters("SELECT * FROM x WHERE a = :test AND b = 'a = \\':no-test\\''",
                "test");
        assertSqlHasParameters("SELECT id::uuid FROM :table WHERE a = :test AND b = \":no-test\"",
                "table", "test");
        assertSqlHasParameters("SELECT * FROM :table WHERE a = :test AND b = ? OR c = :after_var d = `:no-test`",
                "table", "test", null, "after_var");
    }

    private void assertSqlHasParameters(final String query, final String... expectedParameters) {
        SqlParser fixture = new SqlParser(query);
        final List<String> actual = fixture.getParameterNames();
        assertEquals(Arrays.asList(expectedParameters), actual);
    }

}