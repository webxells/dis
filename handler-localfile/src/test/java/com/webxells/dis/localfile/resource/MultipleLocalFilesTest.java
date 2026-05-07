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
import com.webxells.dis.api.error.InvalidApi;
import com.webxells.dis.api.resource.MultiResource;
import com.webxells.dis.test.cases.FileTestCase;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MultipleLocalFilesTest extends FileTestCase {

    private static final String RESOURCE_PATH = new File(Objects.requireNonNull(
            MultipleLocalFilesTest.class.getClassLoader().getResource("test")).getPath()).getParent();

    @Test
    void testUsingFileNameStore() throws IOException, InputOutputError {
        Files.createFile(Path.of(testDir.getAbsolutePath(), "abc.cba"));
        Files.createFile(Path.of(testDir.getAbsolutePath(), "ab.ba"));
        Files.createFile(Path.of(testDir.getAbsolutePath(), "a.a"));
        MultipleLocalFiles multi = new MultipleLocalFiles();
        String index = random("store");
        multi.setGlobbing(true);
        multi.setFileNameStoreIndex(index);
        multi.setPaths(List.of(path("a*")));

        LocalFile single = new LocalFile();
        Path path = Files.createTempDirectory(testDir.toPath(), "path");

        single.setPath(path.toString());
        single.setFileName("new_$FILE_$TYPE_$FILE_LONG_$FILE_cba");
        single.setFileNameStoreIndex(index);

        Set<String> expectedFiles = Set.of("a_a_a.a_a", "ab_ba_ab.ba_ab", "abc_cba_abc.cba_abc");
        Set<String> actualFiles = new HashSet<>();
        InputStream receive;
        for (int i = expectedFiles.size(); i-- > 0;) {
            receive = multi.receive();
            receive.close();
            actualFiles.add(single.toString());
            assertEquals(i == 0 ? MultiResource.RefreshResult.NONE : MultiResource.RefreshResult.MORE, multi.refresh());
        }

        assertEquals(expectedFiles.size(), actualFiles.size());
        assertEquals(expectedFiles.size(), expectedFiles.stream()
                .map(a -> String.format("LocalFile[%s%snew_%s_cba]", path, File.separator, a))
                .filter(actualFiles::contains)
                .count());
    }

    @Test
    void testWithGlobbingOnlyNew() throws InvalidApi, InputOutputError, IOException, InterruptedException {
        createFile("old");
        createFile("old2");
        MultipleLocalFiles fixture = new MultipleLocalFiles();
        fixture.setGlobbing(true);
        fixture.setGlobOnlyNewFiles(true);
        fixture.setPaths(List.of(path("*old*")));

        fixture.validate();
        assertNull(fixture.receive());
        Thread.sleep(200);
        File newFile = createFile("not-old");
        String content = random("new content");
        Files.write(newFile.toPath(), content.getBytes(), StandardOpenOption.WRITE);
        fixture.reset();
        InputStream actualFile = fixture.receive();
        assertEquals(content, new String(actualFile.readAllBytes()));
        fixture.refresh();
        assertNull(fixture.receive());
        fixture.reset();
        assertNull(fixture.receive());
        fixture.refresh();
        assertNull(fixture.receive());
    }


    @Test
    void failing() {
        failingWithGlobbing("**staticFile");
        failingWithGlobbing("**static/File");
        failingWithGlobbing("/**static/File");

        MultipleLocalFiles fixture = new MultipleLocalFiles();
        fixture.setGlobbing(true);
        fixture.setPaths(List.of("/non-existent-i-hope/"));
        assertThrows(InvalidApi.class, fixture::validate);

    }

    @Test
    void test() throws IOException, InputOutputError, InvalidApi {
        MultipleLocalFiles fixture = new MultipleLocalFiles();
        fixture.setPaths(List.of(getResourcePath("test"), getResourcePath("staticFile")));

        fixture.validate();
        String actualFile1 = new String(fixture.receive().readAllBytes());
        String actualFile1Again = new String(fixture.receive().readAllBytes());
        fixture.refresh();
        String actualFile2 = new String(fixture.receive().readAllBytes());
        fixture.refresh();
        InputStream actualFile3 = fixture.receive();

        assertEquals(actualFile1, actualFile1Again);
        assertEquals(readFile(getResourcePath("test")), actualFile1);
        assertEquals(readFile(getResourcePath("staticFile")), actualFile2);
        assertNull(actualFile3);
    }

    @Test
    void testWithGlobbing() throws InvalidApi, InputOutputError, IOException {
        MultipleLocalFiles fixture = new MultipleLocalFiles();
        fixture.setGlobbing(true);
        fixture.setPaths(List.of(getResourcePath("*test")));

        fixture.validate();
        String actualFile1 = new String(fixture.receive().readAllBytes());
        String actualFile1Again = new String(fixture.receive().readAllBytes());
        fixture.refresh();
        InputStream actualFile2 = fixture.receive();

        assertEquals(actualFile1, actualFile1Again);
        assertEquals(readFile(getResourcePath("test")), actualFile1);
        assertNull(actualFile2);
    }

    @Test
    void testWithDeepGlobbing() throws InvalidApi, InputOutputError, IOException {
        MultipleLocalFiles fixture = new MultipleLocalFiles();
        fixture.setGlobbing(true);
        fixture.setGlobbingMaxDepth(3);
        fixture.setPaths(List.of(getResourcePath("**test"), getResourcePath("staticFile")));
        testWithDeepGlobbing(fixture);
    }

    public static void testWithDeepGlobbing(MultiResource fixture) throws InputOutputError, IOException, InvalidApi {
        fixture.validate();
        String actualFile1 = new String(fixture.receive().readAllBytes());
        String actualFile1Again = new String(fixture.receive().readAllBytes());
        fixture.refresh();
        String actualFile2 = new String(fixture.receive().readAllBytes());
        fixture.refresh();
        String actualFile3 = new String(fixture.receive().readAllBytes());
        fixture.refresh();
        InputStream actualFile4 = fixture.receive();

        assertEquals(actualFile1, actualFile1Again);
        String testFileContent = readFile(getResourcePath("test"));
        if (testFileContent.equals(actualFile1)) {
            assertEquals(readFile(getResourcePath("a/b/deep-test")), actualFile2);
            assertEquals(testFileContent, actualFile1);
        } else {
            assertEquals(readFile(getResourcePath("a/b/deep-test")), actualFile1);
            assertEquals(testFileContent, actualFile2);
        }
        assertEquals(readFile(getResourcePath("staticFile")), actualFile3);
        assertNull(actualFile4);
    }

    private void failingWithGlobbing(final String s) {
        MultipleLocalFiles fixture = new MultipleLocalFiles();
        fixture.setGlobbing(true);
        fixture.setPaths(List.of(s));
        assertThrows(InvalidApi.class, fixture::validate);

        fixture = new MultipleLocalFiles();
        fixture.setGlobbing(true);
        fixture.setPaths(List.of(s));
        assertThrows(RuntimeException.class, fixture::receive);
    }

    private static String readFile(final String resourcePath) throws IOException {
        return Files.readString(Paths.get(resourcePath));
    }

    private static String getResourcePath(final String extra) {
        return String.format("%s%s%s", RESOURCE_PATH, File.separator, extra);
    }
}