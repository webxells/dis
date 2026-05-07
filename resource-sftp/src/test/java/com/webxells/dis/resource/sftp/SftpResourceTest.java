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
import com.webxells.dis.resource.sftp.proxy.HttpProxy;
import com.webxells.dis.resource.sftp.proxy.Proxy;
import java.io.File;
import java.io.InputStream;
import java.util.List;
import java.util.Vector;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.notNull;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SftpResourceTest {
    @Mock
    private static JSch jSch;
    @Mock
    private static Session session;
    @Mock
    private ChannelSftp channelSftp;


    static class MockedSftpResource extends SftpResource {
        @Override
        protected JSch createJsch() {
            return jSch;
        }
    }

    @Test
    void testFailing() throws JSchException {
        final SftpConfig config = new SftpConfig();
        config.setUsername(random());
        config.setHost(random());
        SftpResource fixture = new MockedSftpResource();
        fixture.setPath(random());
        fixture.setConnection(config);
        assertThrows(UnsupportedOperationException.class, fixture::send);
        when(jSch.getSession(config.getUsername(), config.getHost(), config.getPort())).thenReturn(session);
        when(session.openChannel("sftp")).thenThrow(new JSchException());
        assertThrows(InputOutputError.class, fixture::receive);
        verify(session).disconnect();
    }

    @Test
    void test() throws InputOutputError, JSchException, SftpException {
        final HttpProxy httpProxy = new HttpProxy();
        httpProxy.setHost(random());
        httpProxy.setPassword(random());
        httpProxy.setUsername(random());
        httpProxy.setPort(666);
        assertNotNull(httpProxy.getJshProxy());
        final SftpConfig config = new SftpConfig();
        final com.jcraft.jsch.Proxy proxyJshMock = mock(com.jcraft.jsch.Proxy.class);
        final Proxy proxyMock = mock(Proxy.class);
        when(proxyMock.getJshProxy()).thenReturn(proxyJshMock);
        config.setKnownHostFile(random());
        config.setUsername(random());
        config.setHost(random());
        config.setPassword(random());
        config.setProxy(proxyMock);
        config.setTimeoutInSeconds((int) Math.round(Math.random() * 857));
        config.setPort(345);
        SftpResource fixture = new MockedSftpResource();
        fixture.setConnection(config);
        String path = random();
        fixture.setPath(path);
        fixture.setFileRegex("\\.csv");
        fixture.setDeepSearch(true);
        fixture.setDepthLimit(2);
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
        InputStream expected1_1 = mock(InputStream.class);
        InputStream expected1_3 = mock(InputStream.class);
        InputStream expected3_1 = mock(InputStream.class);
        when(channelSftp.ls(path)).thenReturn(new Vector<>(lsEntries1));
        when(channelSftp.ls(path(path, lsEntries1.get(1).getFilename()))).thenReturn(new Vector<>(lsEntries2));
        when(channelSftp.ls(path(path(path,
                lsEntries1.get(1).getFilename()), lsEntries2.get(0).getFilename()))).thenReturn(new Vector<>(lsEntries3));
        when(channelSftp.get(path(path, lsEntries1.get(0).getFilename()), null, 0L)).thenReturn(expected1_1);
        when(channelSftp.get(eq(path(path, lsEntries1.get(2).getFilename())), notNull(), eq(0L))).thenReturn(expected1_3);
        when(channelSftp.get(path(path(path(path,
                lsEntries1.get(1).getFilename()), lsEntries2.get(0).getFilename()), lsEntries3.get(0).getFilename()),
                null, 0L)).thenReturn(expected3_1);

        assertSame(expected1_1, fixture.receive());
        fixture.refresh();
        assertSame(expected3_1, fixture.receive());
        fixture.refresh();
        assertSame(expected1_3, fixture.receive());
        fixture.refresh();
        assertNull(fixture.receive());


        verify(jSch).setKnownHosts(config.getKnownHostFile());
        verify(session).setProxy(proxyJshMock);
        verify(session).setPassword(config.getPassword());
        verify(session).setTimeout(config.getTimeoutInSeconds() * 1000);
        verify(session).connect();
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

    private String random() {
        return String.valueOf(Math.round(Math.random() * 89842));
    }

}