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
package com.webxells.dis.logging.simple.appender;

import com.webxells.dis.api.Logger;
import com.webxells.dis.logging.simple.Event;
import com.webxells.dis.logging.simple.appender.file.DateFile;
import com.webxells.dis.logging.simple.appender.file.LastXRuns;
import com.webxells.dis.logging.simple.appender.file.NoRotation;
import com.webxells.dis.logging.simple.appender.file.RotationStrategy;
import com.webxells.dis.test.cases.FileTestCase;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.zip.GZIPInputStream;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

class FileAppenderTest extends FileTestCase {
    private static final Thread thread = Thread.currentThread();

    @Test
    void testDateFileWithMaxAbandonedFilesWithAndCompressing() throws IOException, InterruptedException {
        File file1 = createFixedFile("test42.log.gz");
        Thread.sleep(1000);
        File file2 = createFixedFile("test43.log.gz");
        Thread.sleep(1000);
        File file3 = createFixedFile("test44.log.gz");

        String file = path("'test'dd'.log'");
        DateFile rotation = new DateFile();
        rotation.maxFiles = 2;
        rotation.compressAbandonedFiles = true;

        assertTrue(file1.exists());
        assertTrue(file2.exists());
        assertTrue(file3.exists());

        FileAppender fixture = createFileAppender(file, rotation);
        fixture.write(new Event(thread, Logger.LogLevel.INFO, "test", "test"));

        assertFalse(file1.exists());
        assertTrue(file2.exists());
        assertTrue(file3.exists());
    }

    @Test
    void testDateFileWithMaxAbandonedFiles() throws IOException, InterruptedException {
        File file1 = createFixedFile("test42.log");
        Thread.sleep(1000);
        File file2 = createFixedFile("test43.log");
        Thread.sleep(1000);
        File file3 = createFixedFile("test44.log");

        String file = path("'test'dd'.log'");
        DateFile rotation = new DateFile();
        rotation.maxFiles = 2;

        assertTrue(file1.exists());
        assertTrue(file2.exists());
        assertTrue(file3.exists());

        FileAppender fixture = createFileAppender(file, rotation);
        fixture.write(new Event(thread, Logger.LogLevel.INFO, "test", "test"));

        assertFalse(file1.exists());
        assertFalse(file2.exists());
        assertTrue(file3.exists());
        //and myself
    }

    @Test
    void testDateFileRefreshingAndDontCompressingAbandonedOnStart() throws IOException, InterruptedException {
        File file1 = createFile(random(), "test.log");
        File file2 = createFile(random(), "test12323.log");
        File file3 = createFile(random(), "test435435435.log");

        String file = String.format("%s%s%s", testDir.getAbsolutePath(), File.separator, "'test.log'");
        DateFile rotation = new DateFile();
        rotation.compressAbandonedFiles = true;
        rotation.dontCompressOlderLogFiles = true;


        assertTrue(file1.exists());
        assertTrue(file2.exists());
        assertTrue(file3.exists());

        FileAppender fixture = createFileAppender(file, rotation);
        fixture.path = file;
        fixture.write(new Event(thread, Logger.LogLevel.INFO, "test", "test"));

        assertTrue(file1.exists());
        assertTrue(file2.exists());
        assertTrue(file3.exists());

        assertFalse(new File(file1.getAbsolutePath().concat( ".gz")).exists());
        assertFalse(new File(file2.getAbsolutePath().concat( ".gz")).exists());
        assertFalse(new File(file3.getAbsolutePath().concat( ".gz")).exists());
    }

    @Test
    void testDateFileRefreshingAndCompressingAbandonedOnStart() throws IOException, InterruptedException {
        SimpleDateFormat format = new SimpleDateFormat("yyyy'asd'MMddHH_mm'.log'");
        File currentLog = createFixedFile(format.format(new Date()));
        File file1 = createFixedFile(format.format(someMinusDays()));
        File file2 = createFile(random(), ".log");
        File file3 = createFixedFile(format.format(someMinusDays()));

        String file = String.format("%s%s%s", testDir.getAbsolutePath(), File.separator, format.toPattern());
        DateFile rotation = new DateFile();
        rotation.compressAbandonedFiles = true;

        assertTrue(currentLog.exists());
        assertTrue(file1.exists());
        assertTrue(file2.exists());
        assertTrue(file3.exists());

        FileAppender fixture = createFileAppender(file, rotation);
        fixture.path = file;
        fixture.write(new Event(thread, Logger.LogLevel.INFO, "test", "test"));

        assertTrue(currentLog.exists());
        assertFalse(file1.exists());
        assertTrue(file2.exists());
        assertFalse(file3.exists());

        assertFalse(new File(currentLog.getAbsolutePath().concat( ".gz")).exists());
        assertTrue(new File(file1.getAbsolutePath().concat( ".gz")).exists());
        assertFalse(new File(file2.getAbsolutePath().concat( ".gz")).exists());
        assertTrue(new File(file3.getAbsolutePath().concat( ".gz")).exists());
    }

    private Date someMinusDays() {
        return Date.from(LocalDateTime.now().minusDays(randomMax(1000)).atZone(ZoneId.systemDefault()).toInstant());
    }

    @Test
    void testDateFileRefreshingAndCompressing() throws IOException, InterruptedException {
        String file = String.format("%s%s%s", testDir.getAbsolutePath(), File.separator, "'test-'yyMMddHHmmssSSS'.log'");
        DateFile rotation = new DateFile();
        rotation.compressAbandonedFiles = true;
        rotation.datePathValidationWaitWrites = 0;

        FileAppender fixture = createFileAppender(file, rotation);
        fixture.path = file;

        fixture.write(new Event(thread, Logger.LogLevel.INFO, "test", "test"));
        Thread.sleep(100);
        fixture.write(new Event(thread, Logger.LogLevel.INFO, "test", "test"));
        Thread.sleep(100);
        fixture.write(new Event(thread, Logger.LogLevel.INFO, "test", "test"));

        AtomicInteger countGz = new AtomicInteger();
        AtomicInteger countLog = new AtomicInteger();
        Files.walk(Path.of(testDir.getAbsolutePath()))
                .filter(a -> !a.equals(testDir.toPath()))
                .forEach(a -> {
                    String fileName = a.toFile().getName();
                    if (fileName.endsWith(".log")) {
                        countLog.incrementAndGet();
                    } else if (fileName.endsWith(".gz")) {
                        assertTrue(fileName.matches("test-\\d{15}.log.gz$"));
                        countGz.incrementAndGet();
                        try (GZIPInputStream gis = new GZIPInputStream(new FileInputStream(a.toFile()))) {
                            assertEquals("main INFO test".concat(System.lineSeparator()), new String(gis.readAllBytes()));
                        } catch (IOException e) {
                            fail(e.getMessage());
                        }
                    } else {
                        fail("should not happen");
                    }
                });
        assertEquals(2, countGz.get());
        assertEquals(1, countLog.get());
    }

    @Test
    void testDateFileRefreshing() throws IOException, InterruptedException {
        String file = String.format("%s%s%s", testDir.getAbsolutePath(), File.separator, "'test-'yyMMddHHmmssSSS'.log'");
        DateFile rotation = new DateFile();
        rotation.datePathValidationWaitWrites = 0;

        FileAppender fixture = createFileAppender(file, rotation);
        fixture.path = file;

        fixture.write(new Event(thread, Logger.LogLevel.INFO, "test", "test"));
        Thread.sleep(100);
        fixture.write(new Event(thread, Logger.LogLevel.INFO, "test", "test"));
        Thread.sleep(100);
        fixture.write(new Event(thread, Logger.LogLevel.INFO, "test", "test"));

        AtomicInteger count = new AtomicInteger();
        Files.walk(Path.of(testDir.getAbsolutePath()))
                .filter(a -> a.toFile().isFile())
                .forEach(a -> {
                    count.incrementAndGet();
                    assertTrue(a.getFileName().toFile().getName().matches("test-\\d{15}.log$"));
                });
        assertEquals(3, count.get());
    }

    @Test
    void testDateFileAppending() throws IOException {
        String random1 = randomUnique();
        String random2 = randomUnique();

        String file = String.format("%s%s%s", testDir.getAbsolutePath(), File.separator, "'test-'G'.log'");
        DateFile rotation = new DateFile();

        FileAppender fixture = createFileAppender(file, rotation);
        fixture.path = file;

        fixture.write(new Event(thread, Logger.LogLevel.INFO, "test", random1));
        fixture.write(new Event(thread, Logger.LogLevel.INFO, "test", random2));

        File actualFile = new File(String.format("%s%s%s", testDir.getAbsolutePath(), File.separator, "test-AD.log"));
        assertTrue(actualFile.exists());
        assertTrue(read(actualFile).indexOf(random1) > 0);
        assertTrue(read(actualFile).indexOf(random2) > 0);
    }

    @Test
    void testLastXRunsAndCompressing() throws IOException, InterruptedException {
        String random1 = randomUnique();
        String random2 = randomUnique();
        String random3 = randomUnique();
        String random4 = randomUnique();
        String random5 = randomUnique();

        File file = createFile(random(), "test.log");
        file.delete();
        String path = file.getAbsolutePath();
        String pathTillName = path.substring(0, path.lastIndexOf("test.log"));
        File file2 = new File(pathTillName.concat("test-1.log.gz"));
        File file3 = new File(pathTillName.concat("test-2.log.gz"));
        File file4 = new File(pathTillName.concat("test-3.log.gz"));

        assertFalse(file.isFile());
        assertFalse(file2.isFile());
        assertFalse(file3.isFile());
        assertFalse(file4.isFile());

        FileAppender fixture = createLastXRunsFileAppender(file, 3, true);
        fixture.write(new Event(thread, Logger.LogLevel.INFO, "test", random1));

        Thread.sleep(300);
        assertTrue(file.isFile());
        assertTrue(read(file).indexOf(random1) > 0);
        assertFalse(file2.isFile());
        assertFalse(file3.isFile());
        assertFalse(file4.isFile());

        fixture = createLastXRunsFileAppender(file, 3, true);
        fixture.write(new Event(thread, Logger.LogLevel.INFO, "test", random2));

        Thread.sleep(300);
        assertTrue(file.isFile());
        assertTrue(read(file).indexOf(random2) > 0);
        assertTrue(file2.isFile());
        assertTrue(readGz(file2).indexOf(random1) > 0);
        assertFalse(file3.isFile());
        assertFalse(file4.isFile());


        fixture = createLastXRunsFileAppender(file, 3, true);
        fixture.write(new Event(thread, Logger.LogLevel.INFO, "test", random3));

        Thread.sleep(300);
        assertTrue(file.isFile());
        assertTrue(read(file).indexOf(random3) > 0);
        assertTrue(file2.isFile());
        assertTrue(readGz(file2).indexOf(random2) > 0);
        assertTrue(file3.isFile());
        assertTrue(readGz(file3).indexOf(random1) > 0);
        assertFalse(file4.isFile());


        fixture = createLastXRunsFileAppender(file, 3, true);
        fixture.write(new Event(thread, Logger.LogLevel.INFO, "test", random4));

        Thread.sleep(300);
        assertTrue(file.isFile());
        assertTrue(read(file).indexOf(random4) > 0);
        assertTrue(file2.isFile());
        assertTrue(readGz(file2).indexOf(random3) > 0);
        assertTrue(file3.isFile());
        assertTrue(readGz(file3).indexOf(random2) > 0);
        assertFalse(file4.isFile());

        fixture = createLastXRunsFileAppender(file, 3, true);
        fixture.write(new Event(thread, Logger.LogLevel.INFO, "test", random5));

        Thread.sleep(300);
        assertTrue(file.isFile());
        assertTrue(read(file).indexOf(random5) > 0);
        assertTrue(file2.isFile());
        assertTrue(readGz(file2).indexOf(random4) > 0);
        assertTrue(file3.isFile());
        assertTrue(readGz(file3).indexOf(random3) > 0);
        assertFalse(file4.isFile());
    }

    private String readGz(final File file) {
        try (GZIPInputStream gis = new GZIPInputStream(new FileInputStream(file))) {
            return new String(gis.readAllBytes());
        } catch (IOException e) {
            fail(e.getMessage());
        }
        throw new RuntimeException("moep");
    }

    @Test
    void testLastXRuns() throws IOException, InterruptedException {
        String random1 = randomUnique();
        String random2 = randomUnique();
        String random3 = randomUnique();
        String random4 = randomUnique();
        String random5 = randomUnique();

        File file = createFile(random(), "test.log");
        file.delete();
        String path = file.getAbsolutePath();
        String pathTillName = path.substring(0, path.lastIndexOf("test.log"));
        File file2 = new File(pathTillName.concat("test-1.log"));
        File file3 = new File(pathTillName.concat("test-2.log"));
        File file4 = new File(pathTillName.concat("test-3.log"));

        assertFalse(file.isFile());
        assertFalse(file2.isFile());
        assertFalse(file3.isFile());
        assertFalse(file4.isFile());

        FileAppender fixture = createLastXRunsFileAppender(file, 3);
        fixture.write(new Event(thread, Logger.LogLevel.INFO, "test", random1));

        Thread.sleep(300);
        assertTrue(file.isFile());
        assertTrue(read(file).indexOf(random1) > 0);
        assertFalse(file2.isFile());
        assertFalse(file3.isFile());
        assertFalse(file4.isFile());

        fixture = createLastXRunsFileAppender(file, 3);
        fixture.write(new Event(thread, Logger.LogLevel.INFO, "test", random2));

        Thread.sleep(300);
        assertTrue(file.isFile());
        assertTrue(read(file).indexOf(random2) > 0);
        assertTrue(file2.isFile());
        assertTrue(read(file2).indexOf(random1) > 0);
        assertFalse(file3.isFile());
        assertFalse(file4.isFile());


        fixture = createLastXRunsFileAppender(file, 3);
        fixture.write(new Event(thread, Logger.LogLevel.INFO, "test", random3));

        Thread.sleep(300);
        assertTrue(file.isFile());
        assertTrue(read(file).indexOf(random3) > 0);
        assertTrue(file2.isFile());
        assertTrue(read(file2).indexOf(random2) > 0);
        assertTrue(file3.isFile());
        assertTrue(read(file3).indexOf(random1) > 0);
        assertFalse(file4.isFile());


        fixture = createLastXRunsFileAppender(file, 3);
        fixture.write(new Event(thread, Logger.LogLevel.INFO, "test", random4));

        Thread.sleep(300);
        assertTrue(file.isFile());
        assertTrue(read(file).indexOf(random4) > 0);
        assertTrue(file2.isFile());
        assertTrue(read(file2).indexOf(random3) > 0);
        assertTrue(file3.isFile());
        assertTrue(read(file3).indexOf(random2) > 0);
        assertFalse(file4.isFile());

        fixture = createLastXRunsFileAppender(file, 3);
        fixture.write(new Event(thread, Logger.LogLevel.INFO, "test", random5));

        Thread.sleep(300);
        assertTrue(file.isFile());
        assertTrue(read(file).indexOf(random5) > 0);
        assertTrue(file2.isFile());
        assertTrue(read(file2).indexOf(random4) > 0);
        assertTrue(file3.isFile());
        assertTrue(read(file3).indexOf(random3) > 0);
        assertFalse(file4.isFile());
    }

    @Test
    void testNoRotation() throws IOException, InterruptedException {
        File destination = createFile();
        String message = random();
        String name = random();
        Thread thread = new Thread(() -> fail("should not be called"), random());
        FileAppender fixture = createNoRotationFileAppender(destination.getAbsolutePath(), true);

        fixture.write(new Event(thread, Logger.LogLevel.DEBUG, name, message));
        fixture.write(new Event(thread, Logger.LogLevel.INFO, name, message));

        Thread.sleep(300);
        assertEquals(String.format("%s INFO %s%n", thread.getName(), message), new String(Files.readAllBytes(Path.of(destination.toURI()))));

        fixture = createNoRotationFileAppender(destination.getAbsolutePath(), false);
        message = random();

        fixture.write(new Event(thread, Logger.LogLevel.ERROR, name, message));

        Thread.sleep(300);
        assertEquals(String.format("%s ERROR %s%n", thread.getName(), message), new String(Files.readAllBytes(Path.of(destination.toURI()))));

        fixture = createNoRotationFileAppender(destination.getAbsolutePath(), true);
        String newMessage = random();

        fixture.write(new Event(thread, Logger.LogLevel.FATAL, name, newMessage));

        Thread.sleep(300);
        assertEquals(String.format("%s ERROR %s%n%s FATAL %s%n", thread.getName(), message, thread.getName(), newMessage),
                new String(Files.readAllBytes(Path.of(destination.toURI()))));
    }

    private FileAppender createLastXRunsFileAppender(final File file, final int x) {
        return createLastXRunsFileAppender(file, x, false);
    }

    private FileAppender createLastXRunsFileAppender(final File file, final int x, boolean compress) {
        LastXRuns rotationStrategy = new LastXRuns();
        rotationStrategy.x = x;
        rotationStrategy.compressAbandonedFiles = compress;
        return createFileAppender(file.getAbsolutePath(), rotationStrategy);
    }

    private FileAppender createNoRotationFileAppender(final String path, final boolean append) {
        NoRotation rotationStrategy = new NoRotation();
        rotationStrategy.append = append;
        return createFileAppender(path, rotationStrategy);
    }

    private FileAppender createFileAppender(final String path, final RotationStrategy rotationStrategy) {
        FileAppender result = new FileAppender();
        result.level = Logger.LogLevel.INFO;
        result.rotationStrategy = rotationStrategy;
        result.path = path;
        result.pattern = "$thread $level $msg";
        return result;
    }

}