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

import com.webxells.dis.api.Logger;
import com.webxells.dis.api.config.description.Default;
import com.webxells.dis.api.config.description.Description;
import com.webxells.dis.api.config.description.Required;
import com.webxells.dis.api.error.InputOutputError;
import com.webxells.dis.api.error.InvalidApi;
import com.webxells.dis.api.resource.MultiResource;
import com.webxells.dis.api.resource.NameProvidingResource;
import com.webxells.dis.api.resource.SizeProvidingResource;
import com.webxells.dis.api.resource.TypeProvidingResource;
import com.webxells.dis.localfile.FileNameStore;
import com.webxells.dis.logging.LoggerProxyFactory;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitOption;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.EnumSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Queue;

@Description("Handles reading and writing of multiple local files")
public class MultipleLocalFiles implements MultiResource, SizeProvidingResource, NameProvidingResource, TypeProvidingResource {
    private class DisFileVisitor extends SimpleFileVisitor<Path> {

        private final PathMatcher matcher;

        private DisFileVisitor(final PathMatcher matcher) {
            this.matcher = matcher;
        }

        @Override
        public FileVisitResult visitFile(final Path path, final BasicFileAttributes attrs) {
            if (!attrs.isDirectory() && matcher.matches(path)) {
                if (!globOnlyNewFiles || lastMilliseconds < attrs.lastModifiedTime().toMillis()) {
                    LOGGER.d("Fetched: ".concat(path.toString()));
                    localFilesQueue.add(new LocalFile(path.toString()));
                } else {
                    LOGGER.t(String.format("Skipped: %s%n(file time (%d) lower than last check time (%d)) ",
                            path.toString(), attrs.lastModifiedTime().toMillis(), lastMilliseconds));
                }
            } else {
                LOGGER.t("Skipped: ".concat(path.toString()));
            }
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFileFailed(final Path file, final IOException exc) {
            return FileVisitResult.CONTINUE;
        }
    }

    private static final Logger LOGGER = LoggerProxyFactory.logger(MultipleLocalFiles.class);

    @Required
    @Description("Paths of local files")
    private List<String> paths;
    private Queue<LocalFile> localFilesQueue;

    @Description("Enables globbing in paths (*)")
    @Default("false")
    private boolean globbing;
    @Description("If globbing enabled, this enables to follow symlinks")
    @Default("false")
    private boolean globbingFollowSymlinks;
    @Description("If globbing enabled, maximum number of directory tree levels to follow")
    @Default("4")
    private int globbingMaxDepth = 4;
    @Description("If globbing enabled, fetches only newly created files (regarding last call of this Resource)")
    @Default("false")
    private boolean globOnlyNewFiles;
    @Description("If enabled, other Features like AddCurrentFile may receive file as well")
    private String fileNameStoreIndex;

    private long lastMilliseconds = System.currentTimeMillis();
    private LocalFile current;

    @Override
    public void validate() throws InvalidApi {
        if (null == paths) {
            throw new InvalidApi("paths required");
        }
        if (null == localFilesQueue) {
            try {
                fetchLocalFiles();
            } catch (final RuntimeException e) {
                throw new InvalidApi("Error while fetching local files", e);
            }
        }
        for (final LocalFile localFile : localFilesQueue) {
            localFile.validate();
        }
    }

    @Override
    public RefreshResult refresh() {
        if (null == localFilesQueue) {
            fetchLocalFiles();
        }
        Optional.ofNullable(localFilesQueue)
                .filter(a -> !a.isEmpty())
                .ifPresent(a -> {
                    a.remove();
                    LOGGER.d(String.format("Next file: %s", a.size() > 0 ? a.element().toString() : "None"));
                });
        return null == localFilesQueue || localFilesQueue.isEmpty() ? RefreshResult.NONE : RefreshResult.MORE;
    }

    @Override
    public void reset() {
        fetchLocalFiles();
    }

    @Override
    public OutputStream send() throws InputOutputError {
        final LocalFile current = getCurrent();
        return null == current ? null : current.send();
    }

    @Override
    public InputStream receive() throws InputOutputError {
        final LocalFile current = getCurrent();
        return null == current ? null : current.receive();
    }

    @Override
    public String getType() {
        return MultipleLocalFiles.class.getName();
    }

    @Override
    public long getSize() {
        return Optional.ofNullable(getCurrent())
                .map(SizeProvidingResource::getSize)
                .orElse(-1L);
    }

    @Override
    public String getResourceName() {
        return Optional.ofNullable(getCurrent())
                .map(NameProvidingResource::getResourceName)
                .orElse(null);
    }

    @Override
    public String getMimeType() {
        return Optional.ofNullable(getCurrent())
                .map(TypeProvidingResource::getMimeType)
                .orElse(null);
    }

    public void setPaths(final List<String> paths) {
        this.paths = new LinkedList<>(paths);
    }

    public void setFileNameStoreIndex(final String fileNameStoreIndex) {
        this.fileNameStoreIndex = fileNameStoreIndex;
    }

    public void setGlobOnlyNewFiles(final boolean globOnlyNewFiles) {
        this.globOnlyNewFiles = globOnlyNewFiles;
    }

    public void setGlobbingFollowSymlinks(final boolean globbingFollowSymlinks) {
        this.globbingFollowSymlinks = globbingFollowSymlinks;
    }

    public void setGlobbing(final boolean globbing) {
        this.globbing = globbing;
    }

    public void setGlobbingMaxDepth(final int globbingMaxDepth) {
        this.globbingMaxDepth = globbingMaxDepth;
    }

    protected LocalFile peekCurrent() {
        return current;
    }

    private LocalFile getCurrent() {
        if (null == localFilesQueue) {
            fetchLocalFiles();
        }
        if (localFilesQueue.isEmpty()) {
            return null;
        }
        current = localFilesQueue.element();
        Optional.ofNullable(fileNameStoreIndex)
                .ifPresent(a -> FileNameStore.setPath(a, current.getPath()));
        return current;
    }

    private void fetchLocalFiles() {
        localFilesQueue = new LinkedList<>();
        if (globbing) {
            paths.forEach(this::addGlobPath);
            if (globOnlyNewFiles) {
                lastMilliseconds = System.currentTimeMillis();
            }
        } else {
            paths.forEach(this::addStringPath);
        }
    }

    private void addStringPath(final String path) {
        localFilesQueue.add(new LocalFile(path));
    }

    private void addGlobPath(final String path) {
        final int globIndex = path.indexOf('*');
        if (globIndex < 0) {
            addStringPath(path);
            return;
        }
        final int pathIndex = path.substring(0, globIndex).lastIndexOf(File.separator);
        if (pathIndex < 3 || globIndex < pathIndex) {
            throw new RuntimeException(String.format("Invalid globbing: %s (Full globbing path required)", path));
        }
        final PathMatcher matcher =
                FileSystems.getDefault().getPathMatcher("glob:".concat(path));
        try {
            Files.walkFileTree(Paths.get(path.substring(0, pathIndex)),
                    globbingFollowSymlinks ? EnumSet.of(FileVisitOption.FOLLOW_LINKS) : EnumSet.noneOf(FileVisitOption.class),
                    path.indexOf("**") > 0 ? globbingMaxDepth : 1,
                    new DisFileVisitor(matcher));
        } catch (final IOException e) {
            throw new RuntimeException("Could not open path: ".concat(path), e);
        }
    }
}