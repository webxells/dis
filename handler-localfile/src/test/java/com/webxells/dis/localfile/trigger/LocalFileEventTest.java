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
package com.webxells.dis.localfile.trigger;

import com.webxells.dis.api.error.InvalidApi;
import com.webxells.dis.localfile.FileNameQueue;
import com.webxells.dis.test.cases.FileTestCase;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LocalFileEventTest extends FileTestCase {
    private volatile boolean success;

    @BeforeEach
    void setSuccess() {
        success = false;
    }

    @Test
    void testWaitForLock() throws IOException, InterruptedException {
        LocalFileEventConfiguration config = new LocalFileEventConfiguration();
        config.setPath(testDir.getAbsolutePath());
        config.setWatchForCreate(false);
        config.setWaitForWritingLocks(true);
        LocalFileEvent fixture = new LocalFileEvent(config);
        fixture.awaitAction(() -> success = true);
        Thread.sleep(400);
        assertFalse(success);
        Path newFile = new File(testDir.getAbsolutePath() + "/asdasd").toPath();
        try(final PrintWriter printWriter = new PrintWriter(Files.newBufferedWriter(newFile, Charset.defaultCharset()))){
            printWriter.println("asd");
            Thread.sleep(200);
            printWriter.println("asd");
            Thread.sleep(200);
            assertFalse(success);
        }
        newFile.toFile().deleteOnExit();
        Thread.sleep(400);
        fixture.abort();
        assertTrue(success);
    }

    @Test
    void testFailing() throws InvalidApi {
        LocalFileEventConfiguration config = new LocalFileEventConfiguration();
        assertThrows(InvalidApi.class, config::validate);
        config.setPath("test");
        config.validate();
        LocalFileEvent fixture = new LocalFileEvent(config);
        assertThrows(InvalidApi.class, fixture::validate);
        config.setPath(testDir.getAbsolutePath());
        fixture = new LocalFileEvent(config);
        fixture.validate();
        config.setWatchForModify(false);
        config.setWatchForCreate(false);
        config.setWatchForDelete(false);
        assertThrows(InvalidApi.class, fixture::validate);
    }

    @Test
    void testFilterFailingEndsWith() throws IOException, InterruptedException {
        LocalFileEventConfiguration config = new LocalFileEventConfiguration();
        config.setPath(testDir.getAbsolutePath());
        config.setWatchForModify(false);
        config.setFileFilter(new LocalFileEvent.FileFilter() {{
            endsWith = "wer";
        }});
        test(config);
        assertFalse(success);
    }


    @Test
    void testFilterFailingStartsWith() throws IOException, InterruptedException {
        LocalFileEventConfiguration config = new LocalFileEventConfiguration();
        config.setPath(testDir.getAbsolutePath());
        config.setWatchForModify(false);
        config.setFileFilter(new LocalFileEvent.FileFilter() {{
            startsWith = "wer";
        }});
        test(config);
        assertFalse(success);
    }

    @Test
    void testFilterFailingRegularExpression() throws IOException, InterruptedException {
        LocalFileEventConfiguration config = new LocalFileEventConfiguration();
        config.setPath(testDir.getAbsolutePath());
        config.setWatchForModify(false);
        config.setFileFilter(new LocalFileEvent.FileFilter() {{
            regularExpression = "wer";
        }});
        test(config);
        assertFalse(success);
    }

    @Test
    void testFilterStartsWith() throws IOException, InterruptedException {
        LocalFileEventConfiguration config = new LocalFileEventConfiguration();
        config.setPath(testDir.getAbsolutePath());
        config.setWatchForModify(false);
        config.setFileFilter(new LocalFileEvent.FileFilter() {{
            startsWith = "asd";
        }});
        test(config);
        assertTrue(success);
    }

    @Test
    void testFilterEndsWith() throws IOException, InterruptedException {
        LocalFileEventConfiguration config = new LocalFileEventConfiguration();
        config.setPath(testDir.getAbsolutePath());
        config.setWatchForModify(false);
        config.setFileFilter(new LocalFileEvent.FileFilter() {{
            endsWith = "asd";
        }});
        test(config);
        assertTrue(success);
    }

    @Test
    void testFilterRegex() throws IOException, InterruptedException {
        LocalFileEventConfiguration config = new LocalFileEventConfiguration();
        config.setPath(testDir.getAbsolutePath());
        config.setWatchForModify(false);
        config.setFileFilter(new LocalFileEvent.FileFilter() {{
            regularExpression = ".+";
        }});
        test(config);
        assertTrue(success);
    }

    @Test
    void testCreate() throws IOException, InterruptedException {
        String index = random();
        LocalFileEventConfiguration config = new LocalFileEventConfiguration();
        config.setPath(testDir.getAbsolutePath());
        config.setWatchForModify(false);
        config.setIndex(index);
        test(config);
        assertTrue(success);
        assertEquals(String.format("%s%s%s", testDir.getAbsolutePath(), File.separator, "asdasd"),
                FileNameQueue.getNext(index));
        assertNull(FileNameQueue.getNext(index));
    }

    @Test
    void testModify() throws IOException, InterruptedException {
        String index = random();
        LocalFileEventConfiguration config = new LocalFileEventConfiguration();
        config.setPath(testDir.getAbsolutePath());
        config.setWatchForCreate(false);
        config.setIndex(index);
        test(config);
        assertTrue(success);
        assertEquals(String.format("%s%s%s", testDir.getAbsolutePath(), File.separator, "asdasd"),
                FileNameQueue.getNext(index));
        assertNull(FileNameQueue.getNext(index));
    }


    @Test
    void testDelete() throws IOException, InterruptedException {
        String index = random();
        LocalFileEventConfiguration config = new LocalFileEventConfiguration();
        config.setPath(testDir.getAbsolutePath());
        config.setWatchForCreate(false);
        config.setWatchForModify(false);
        config.setWatchForDelete(true);
        config.setIndex(index);
        test(config);
        assertTrue(success);assertEquals(String.format("%s%s%s", testDir.getAbsolutePath(), File.separator, "asdasd"),
                FileNameQueue.getNext(index));
        assertNull(FileNameQueue.getNext(index));
    }

    private void test(final LocalFileEventConfiguration config) throws IOException, InterruptedException {
        LocalFileEvent fixture = new LocalFileEvent(config);
        fixture.awaitAction(() -> success = true);
        Thread.sleep(400);
        assertFalse(success);
        Path newFile = new File(testDir.getAbsolutePath() + "/asdasd").toPath();
        try(final PrintWriter printWriter = new PrintWriter(Files.newBufferedWriter(newFile, Charset.defaultCharset()))){
            printWriter.println("asd");
        }
        newFile.toFile().delete();
        Thread.sleep(400);
        fixture.abort();
    }

}