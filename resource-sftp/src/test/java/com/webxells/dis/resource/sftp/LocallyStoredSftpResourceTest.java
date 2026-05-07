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
package com.webxells.dis.resource.sftp;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpATTRS;
import com.jcraft.jsch.SftpException;
import com.webxells.dis.api.error.InputOutputError;
import com.webxells.dis.api.resource.MultiResource;
import com.webxells.dis.test.cases.FileTestCase;
import com.webxells.dis.test.cases.SimpleTestCase;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Vector;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.notNull;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LocallyStoredSftpResourceTest extends FileTestCase {
    @Mock
    private static JSch jSch;
    @Mock
    private static Session session;
    @Mock
    private ChannelSftp channelSftp;


    static class MockedLocallyStoredSftpResource extends LocallyStoredSftpResource {
        @Override
        protected JSch createJsch() {
            return jSch;
        }
    }

    @Test
    void test() throws JSchException, InputOutputError, SftpException, IOException {
        final SftpConfig config = new SftpConfig();
        config.setKnownHostFile(random());
        config.setUsername(random());
        config.setHost(random());
        config.setPassword(random());
        config.setTimeoutInSeconds(random(1));
        config.setPort(345);
        LocallyStoredSftpResource fixture = new MockedLocallyStoredSftpResource();
        fixture.setConnection(config);
        String path = random();
        fixture.setPath(path);
        fixture.setFileRegex("\\.csv");
        fixture.setDeepSearch(true);
        fixture.setDepthLimit(2);
        fixture.setLocalStoreDirectory(testDir.getAbsolutePath());

        assertThrows(UnsupportedOperationException.class, fixture::send);

        when(jSch.getSession(config.getUsername(), config.getHost(), config.getPort())).thenReturn(session);
        when(session.openChannel("sftp")).thenReturn(channelSftp);
        final List<ChannelSftp.LsEntry> lsEntries1 = List.of(
                createLsEntry(csvFile(), false),
                createLsEntry(random(), true),
                createLsEntry(csvFile(), false),
                createLsEntry(random(), false)
        );
        final List<ChannelSftp.LsEntry> lsEntries2 = List.of(
                createLsEntry(random(), true)
        );
        final List<ChannelSftp.LsEntry> lsEntries3 = List.of(
                createLsEntry(csvFile(), false),
                createLsEntry(random(), false),
                createLsEntry(random(), true)
        );
        String content11 = random("file11");
        String content13 = random("file13");
        String content31 = random("file31");
        ByteArrayInputStream expected1_1 = new ByteArrayInputStream(content11.getBytes());
        ByteArrayInputStream expected1_3 = new ByteArrayInputStream(content13.getBytes());
        ByteArrayInputStream expected3_1 = new ByteArrayInputStream(content31.getBytes());
        when(channelSftp.ls(path)).thenReturn(new Vector<>(lsEntries1));
        when(channelSftp.ls(path(path, lsEntries1.get(1).getFilename()))).thenReturn(new Vector<>(lsEntries2));
        when(channelSftp.ls(path(path(path,
                lsEntries1.get(1).getFilename()), lsEntries2.get(0).getFilename()))).thenReturn(new Vector<>(lsEntries3));
        when(channelSftp.get(eq(path(path, lsEntries1.get(0).getFilename())), eq(null), eq(0L))).thenReturn(expected1_1);
        when(channelSftp.get(eq(path(path, lsEntries1.get(2).getFilename())), eq(null), eq(0L))).thenReturn(expected1_3);
        when(channelSftp.get(eq(path(path(path(path,
                lsEntries1.get(1).getFilename()), lsEntries2.get(0).getFilename()), lsEntries3.get(0).getFilename())),
                eq(null), eq(0L))).thenReturn(expected3_1);

        assertEquals(MultiResource.RefreshResult.UNKNOWN, fixture.refresh());
        fixture.reset();

        verify(jSch).setKnownHosts(config.getKnownHostFile());
        verify(session).setPassword(config.getPassword());
        verify(session).setTimeout(config.getTimeoutInSeconds() * 1000);
        verify(session).connect();
        verify(session).disconnect();

        List<String> collect = Files.list(testDir.toPath())
                .map(a -> {
                    try {
                        return new String(Files.readAllBytes(a));
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }).collect(Collectors.toList());

        assertEquals(3, collect.size());
        assertTrue(collect.contains(content11));
        assertTrue(collect.contains(content13));
        assertTrue(collect.contains(content31));

        List<String> filesContent = IntStream.range(0, 3).mapToObj(a -> {
            try {
                String result = new String(fixture.receive().readAllBytes());
                if (a < 2) {
                    assertEquals(MultiResource.RefreshResult.MORE, fixture.refresh());
                } else {
                    assertEquals(MultiResource.RefreshResult.NONE, fixture.refresh());
                }
                return result;
            } catch (IOException | InputOutputError e) {
                throw new RuntimeException(e);
            }
        }).collect(Collectors.toList());

        assertEquals(3, filesContent.size());

        assertTrue(filesContent.contains(content11));
        assertTrue(filesContent.contains(content13));
        assertTrue(filesContent.contains(content31));


        verifyNoMoreInteractions(jSch, session);
    }

    private String path(final String path, final String filename) {
        return String.format("%s%s%s", path, File.separator, filename);
    }

    private String csvFile() {
        return random().concat(".csv");
    }

    private ChannelSftp.LsEntry createLsEntry(final String filename,
                                              final boolean isDirectory) {
        final SftpATTRS attrs = mock(SftpATTRS.class);
        final ChannelSftp.LsEntry result = mock(ChannelSftp.LsEntry.class);
        lenient().when(result.getFilename()).thenReturn(filename);
        when(result.getAttrs()).thenReturn(attrs);
        when(attrs.isDir()).thenReturn(isDirectory);
        return result;
    }

}