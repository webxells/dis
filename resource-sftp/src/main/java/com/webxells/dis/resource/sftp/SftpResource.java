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
import com.jcraft.jsch.SftpException;
import com.jcraft.jsch.SftpProgressMonitor;
import com.webxells.dis.api.Logger;
import com.webxells.dis.api.config.description.Default;
import com.webxells.dis.api.config.description.Description;
import com.webxells.dis.api.config.description.Required;
import com.webxells.dis.api.error.InputOutputError;
import com.webxells.dis.api.error.InvalidApi;
import com.webxells.dis.api.resource.MultiResource;
import com.webxells.dis.logging.LoggerProxyFactory;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.Optional;
import java.util.Vector;
import java.util.regex.Pattern;

@Description("Receives files per sftp")
public class SftpResource implements MultiResource {
    private static final Logger LOGGER = LoggerProxyFactory.logger(SftpResource.class);

    static {
        JSch.setLogger(new JschLogger());
    }

    protected volatile ChannelSftp currentChannel;
    protected volatile Session currentSession;
    protected JSch jsch;
    protected LinkedList<String> filesLeft;
    protected Pattern pattern = Pattern.compile(".*");

    @Required
    @Description("Holds information to establish a sftp session")
    protected SftpConfig connection;
    @Required
    @Description("Initial path to look for a file")
    protected String path;
    @Description("If path locates to a directory, look into it and search there for files")
    @Default("false")
    protected boolean deepSearch;
    @Description("How deep to look into the directories")
    @Default("Maximum depth to look for files")
    protected int depthLimit = 4;
    @Description("Set file separator for server")
    @Default("/")
    private char serverFileSeparator = '/';

    @Override
    public void validate() throws InvalidApi {
        if (null == connection || null == path || null == connection.getPassword() || null == connection.getHost() || null == connection.getUsername()) {
            throw new InvalidApi("Required fields are missing");
        }
    }

    @Override
    public RefreshResult refresh() {
        return RefreshResult.UNKNOWN;
    }

    @Override
    public void reset() {
        if (null != currentChannel) {
            disconnect();
        }
    }

    @Override
    public OutputStream send() {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public InputStream receive() throws InputOutputError {
        try {
            loadFileList();
            if (filesLeft.isEmpty()) {
                return null;
            }
            final String next = filesLeft.pop();
            LOGGER.d("Next file: ".concat(next));
            final SftpProgressMonitor monitor = filesLeft.isEmpty() ? getWaitingForEndToDisconnectMonitor() : null;
            return getChannel().get(next, monitor, 0L);
        } catch (final JSchException | SftpException e) {
            disconnect();
            throw new InputOutputError("Could not connect to sftp", e);
        }
    }

    protected SftpProgressMonitor getWaitingForEndToDisconnectMonitor() {
        return new SftpProgressMonitor() {
            @Override
            public void init(final int op, final String src, final String dest, final long max) { }

            @Override
            public boolean count(final long count) { return true;}

            @Override
            public void end() {
                new Thread(() -> {
                    try {
                        //Hacky workaround: jsch sftp needs to read from channel to close (after this function is
                        // called)
                        Thread.sleep(500);
                        disconnect();
                    } catch (InterruptedException ignored) { }
                }).start();
            }
        };
    }

    protected void disconnect() {
        LOGGER.d("Closing connection");
        Optional.ofNullable(currentChannel)
                .ifPresent(ChannelSftp::disconnect);
        Optional.ofNullable(currentSession)
                .ifPresent(Session::disconnect);
        currentChannel = null;
        currentSession = null;
        filesLeft = null;
    }

    protected void loadFileList() throws JSchException, SftpException {
        if (null == filesLeft) {
            filesLeft = new LinkedList<>();
            addToFileList(path);
        }
    }

    protected void addToFileList(final String path) throws JSchException, SftpException {
        addToFileList(path, 0);
    }

    protected void addToFileList(final String path, int depth) throws JSchException, SftpException {
        //noinspection unchecked
        final Vector<ChannelSftp.LsEntry> ls = getChannel().ls(path);
        LOGGER.d("Reading sftp directory: ".concat(path));
        for (final ChannelSftp.LsEntry a : ls) {
            final String absolutePath = String.format("%s%s%s", path, serverFileSeparator, a.getFilename());
            if (deepSearch && depthLimit > depth && a.getAttrs().isDir()) {
                addToFileList(absolutePath, depth + 1);
            } else if (isInterestingFile(a)) {
                LOGGER.d("Fetched: ".concat(absolutePath));
                filesLeft.add(absolutePath);
            } else {
                LOGGER.d("Skipping: ".concat(absolutePath));
            }
        }
    }

    protected boolean isInterestingFile(final ChannelSftp.LsEntry a) {
        return !a.getAttrs().isDir() && patternMatch(a.getFilename());
    }

    protected boolean patternMatch(final String filename) {
        return pattern.matcher(filename).find();
    }

    public void setConnection(final SftpConfig connection) {
        this.connection = connection;
    }

    protected JSch createJsch() {
        return new JSch();
    }

    protected ChannelSftp getChannel() throws JSchException {
        if (null == currentChannel) {
            currentChannel = createChannel();
            currentChannel.connect();
        }
        return currentChannel;
    }

    protected JSch getJsch() throws JSchException {
        if (null == jsch) {
            jsch = createJsch();
            if (null != connection.getKnownHostFile()) {
                jsch.setKnownHosts(connection.getKnownHostFile());
            }
        }
        return jsch;
    }

    protected ChannelSftp createChannel() throws JSchException {
        connectToSession();
        LOGGER.d("Open sftp channel...");
        return (ChannelSftp) currentSession.openChannel("sftp");
    }

    protected void connectToSession() throws JSchException {
        if (null == currentSession) {
            LOGGER.d("Connecting to sftp...");
            currentSession = getJsch().getSession(connection.getUsername(), connection.getHost(), connection.getPort());
            if (connection.isIgnoreUnknownHostError()) {
                currentSession.setConfig("StrictHostKeyChecking", "no");
            }
            Optional.ofNullable(connection.getProxy())
                            .ifPresent(a -> currentSession.setProxy(a.getJshProxy()));
            currentSession.setPassword(connection.getPassword());
            currentSession.setTimeout(connection.getTimeoutInSeconds() * 1000);
            currentSession.connect();
        }
    }

    public void setFileRegex(final String fileRegex) {
        pattern = Pattern.compile(fileRegex);
    }

    public void setPath(final String path) {
        this.path = path;
    }

    public void setDeepSearch(final boolean deepSearch) {
        this.deepSearch = deepSearch;
    }

    public void setDepthLimit(final int depthLimit) {
        this.depthLimit = depthLimit;
    }

    public void setServerFileSeparator(final char serverFileSeparator) {
        this.serverFileSeparator = serverFileSeparator;
    }
}