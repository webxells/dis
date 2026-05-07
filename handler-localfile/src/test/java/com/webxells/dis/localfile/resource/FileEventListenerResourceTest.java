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
package com.webxells.dis.localfile.resource;

import com.webxells.dis.api.error.InputOutputError;
import com.webxells.dis.api.resource.MultiResource;
import com.webxells.dis.localfile.FileNameQueue;
import com.webxells.dis.localfile.FileNameStore;
import com.webxells.dis.test.cases.FileTestCase;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class FileEventListenerResourceTest extends FileTestCase {

    @Test
    void test() throws IOException, InputOutputError {
        String instance = random("instance");
        String value1 = random("value1");
        String value2 = random("value2");
        File file1 = createFile(random(), ".c");
        Files.writeString(file1.toPath(), value1);
        File file2 = createFile();
        Files.writeString(file2.toPath(), value2);
        FileNameQueue.add(instance, file1.getAbsolutePath());
        FileNameQueue.add(instance, file2.getAbsolutePath());
        FileEventListenerResource fixture = new FileEventListenerResource();
        fixture.setIndex(instance);
        assertNull(fixture.getResourceName());
        assertNull(fixture.getMimeType());
        fixture.reset();
        assertEquals(file1.getName(), fixture.getResourceName());
        assertEquals("text/plain", fixture.getMimeType());
        assertEquals(value1, getStringFromStream(fixture.receive()));
        assertEquals(file1.getAbsolutePath(), FileNameStore.getPath(instance));
        assertEquals(value1, getStringFromStream(fixture.receive()));
        assertEquals(file1.getAbsolutePath(), FileNameStore.getPath(instance));
        assertEquals(MultiResource.RefreshResult.MORE, fixture.refresh());
        assertEquals(file2.getName(), fixture.getResourceName());
        assertEquals("application/octet-stream", fixture.getMimeType());
        assertEquals(file2.getAbsolutePath(), FileNameStore.getPath(instance));
        assertEquals(value2, getStringFromStream(fixture.receive()));
        assertEquals(MultiResource.RefreshResult.NONE, fixture.refresh());
        assertNull(fixture.receive());
        assertNull(FileNameStore.getPath(instance));
    }

    private String getStringFromStream(final InputStream receive) throws IOException {
        String result = new String(receive.readAllBytes());
        receive.close();
        return result;
    }

}
