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
package com.webxells.dis.test.cases;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Comparator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;


public class FileTestCase extends SimpleTestCase {
    private final static Path TMP_DIR = new File(System.getProperty("java.io.tmpdir")).toPath();

    protected File testDir;

    @BeforeEach
    public void setUpTestDir() throws IOException {
        testDir = Files.createTempDirectory(TMP_DIR, this.getClass().getSimpleName()).toFile();
        testDir.deleteOnExit();
    }

    @AfterEach
    public void cleanTestDir() throws IOException {
        Files.walk(testDir.toPath())
                .sorted(Comparator.reverseOrder())
                .map(Path::toFile)
                .peek(File::deleteOnExit)
                .forEach(File::delete);
    }

    public File createFile() throws IOException {
        return createFile(random());
    }

    public File createFile(String prefix) throws IOException {
        return createFile(prefix, random());
    }

    public File createFile(String prefix, String suffix) throws IOException {
        return File.createTempFile(prefix, suffix, testDir);
    }

    public File createFixedFile(String name) throws IOException {
        File result = new File(path(name));
        result.createNewFile();
        result.deleteOnExit();
        return result;

    }

    public String read(File file) throws IOException {
        return new String(Files.readAllBytes(Path.of(file.getAbsolutePath())));
    }

    public String path(String... path) {
        StringBuilder result = new StringBuilder(testDir.getAbsolutePath());
        Arrays.stream(path).forEach(a -> {
            result.append(File.separator);
            result.append(a);
        });
        return result.toString();
    }
}