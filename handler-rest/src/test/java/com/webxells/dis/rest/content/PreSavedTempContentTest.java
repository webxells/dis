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

import com.webxells.dis.api.error.InvalidApi;
import com.webxells.dis.rest.execution.Response;
import com.webxells.dis.localfile.input.LocalPreSavedInputStream;
import com.webxells.dis.test.cases.FileTestCase;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PreSavedTempContentTest extends FileTestCase {

    @Test
    void test() throws InvalidApi, IOException {
        String rawContent = random();
        PreSavedTempContent fixture = new PreSavedTempContent();
        assertThrows(InvalidApi.class, fixture::validate);
        ContentStrategy child = Mockito.mock(ContentStrategy.class);
        Response response = Mockito.mock(Response.class);
        ByteArrayInputStream bais = new ByteArrayInputStream(rawContent.getBytes(StandardCharsets.UTF_8));
        fixture.setChild(child);
        fixture.validate();
        fixture.setLocallyPreSaveDirectory(testDir.getAbsolutePath());
        Mockito.when(child.parseOutputRequest(response)).thenReturn(bais);

        final InputStream result = fixture.parseOutputRequest(response);
        assertInstanceOf(LocalPreSavedInputStream.class, result);
        assertEquals(rawContent, new String(result.readAllBytes()));
        Mockito.verify(child).parseOutputRequest(response);
        result.close();
    }

}