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
import com.webxells.dis.test.cases.FileTestCase;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class LocalFileTest extends FileTestCase {
    @Test
    void test() throws Exception {
        LocalFile fixture = new LocalFile(LocalFileTest.class.getClassLoader().getResource("test").getPath());
        Assertions.assertNotNull(fixture.receive());
        Assertions.assertNotNull(fixture.send());
    }

    @Test
    void testAutoNameExtraction() {
        String randomFile = String.format("%s%s", random(), ".txt");
        LocalFile fixture = new LocalFile(String.format("%s%s%s%s", File.separator, random(), File.separator,
                randomFile));
        Assertions.assertEquals("text/plain", fixture.getMimeType());
    }

    @Test
    void testWithDate() throws IOException, InputOutputError {
        LocalFile fixture = new LocalFile();
        fixture.setFileNameContainsDateFormat(true);
        fixture.setPath(testDir.getAbsolutePath());
        fixture.setFileName("'test_'yyyyMMdd");
        OutputStreamWriter send = new OutputStreamWriter(fixture.send());
        String fileContent = random("content");
        send.write(fileContent);
        send.flush();
        send.close();
        Assertions.assertEquals(fileContent,
                new String(Files.readAllBytes(Paths.get(
                        String.format("%s%stest_%s",testDir.getAbsolutePath(), File.separator,
                            LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")))))));
    }
}
