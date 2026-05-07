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
import com.webxells.dis.api.resource.Resource;
import com.webxells.dis.test.cases.FileTestCase;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class PreSavedTempFileTest extends FileTestCase {

    private final URL url = PreSavedTempFileTest.class.getClassLoader().getResource("staticFile");

    @Test
    void test() throws Exception {
        final Resource resource = mock(Resource.class);
        when(resource.receive()).thenReturn(new FileInputStream(url.getPath()));

        final PreSavedTempFile fixture = new PreSavedTempFile();
        fixture.setReceiver(resource);
        fixture.setLocallyPreSaveDirectory(testDir.getAbsolutePath());
        fixture.receive();
        fixture.getSize();

        assertThrows(UnsupportedOperationException.class, fixture::send);

        // checks if tmp file was created
        try(Stream<Path> paths = Files.walk(Path.of(testDir.getAbsolutePath()), 1)) {
            final Optional<String> first = paths.filter(p -> !Files.isDirectory(p))
                    .map(Path::toString)
                    .findFirst();

            assertNotNull(first.get());
            byte[] file1 = Files.readAllBytes(Paths.get(url.toURI()));
            byte[] file2 = Files.readAllBytes(Path.of(first.get()));
            assertArrayEquals(file1, file2);
        }
        Mockito.verify(resource, times(1)).receive();
    }

    @Test
    void testSend() {
        final PreSavedTempFile fixture = new PreSavedTempFile();
        assertThrows(UnsupportedOperationException.class, fixture::send);
    }

    @Test
    void testGetSizeBeforeReceive() throws Exception {
        final Resource resource = mock(Resource.class);
        when(resource.receive()).thenReturn(new FileInputStream(url.getPath()));
        long size = new File(url.getFile()).length();

        final PreSavedTempFile fixture = new PreSavedTempFile();
        fixture.setReceiver(resource);

        assertEquals(size, fixture.getSize());
        fixture.receive();
        Mockito.verify(resource, times(1)).receive();
    }

    @Test
    void testGetSizeAndThrowInputOutputError() throws InputOutputError {
        final Resource resource = mock(Resource.class);
        when(resource.receive()).thenThrow(InputOutputError.class);

        final PreSavedTempFile fixture = new PreSavedTempFile();
        fixture.setReceiver(resource);

        assertEquals(-1, fixture.getSize());
    }

    @Test
    void testMultipleCall() throws Exception {
        final Resource resource = mock(Resource.class);
        when(resource.receive()).thenReturn(new FileInputStream(url.getPath()));

        final PreSavedTempFile fixture = new PreSavedTempFile();
        fixture.setReceiver(resource);
        fixture.setLocallyPreSaveDirectory(testDir.getAbsolutePath());
        InputStream inputStream1 = fixture.receive();
        inputStream1.close();
        when(resource.receive()).thenReturn(new FileInputStream(url.getPath()));
        InputStream inputStream2 = fixture.receive();

        assertNotSame(inputStream1, inputStream2);
    }
}