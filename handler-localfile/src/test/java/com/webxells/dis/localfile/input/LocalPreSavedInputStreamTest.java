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
package com.webxells.dis.localfile.input;

import com.webxells.dis.test.cases.FileTestCase;
import com.webxells.dis.test.cases.SimpleTestCase;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LocalPreSavedInputStreamTest extends FileTestCase {
    @Test
    void testCopy() throws IOException {
        String description = random();
        ByteArrayInputStream child = new ByteArrayInputStream(description.getBytes());
        LocalPreSavedInputStream fixture = new LocalPreSavedInputStream(child, testDir.getAbsolutePath());
        assertEquals(description, new String(fixture.readAllBytes()));
        assertEquals("", new String(fixture.readAllBytes()));
        InputStream copy = fixture.getCopy();
        assertEquals(description, new String(copy.readAllBytes()));
    }

}