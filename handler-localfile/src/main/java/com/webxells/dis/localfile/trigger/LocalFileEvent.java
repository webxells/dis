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

import com.webxells.dis.api.Logger;
import com.webxells.dis.api.config.description.Description;
import com.webxells.dis.api.error.InvalidApi;
import com.webxells.dis.base.trigger.SimpleConcurrentTrigger;
import com.webxells.dis.localfile.FileNameQueue;
import com.webxells.dis.logging.LoggerProxyFactory;
import java.io.File;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class LocalFileEvent extends SimpleConcurrentTrigger<LocalFileEventConfiguration> {
    public static class FileFilter {
        public String startsWith;
        public String endsWith;
        public String regularExpression;
    }

    private static final Logger LOGGER = LoggerProxyFactory.logger(LocalFileEvent.class);

    private final String index;
    private final File path;
    private final LocalFileEventConfiguration configuration;
    private final FileFilter filter;
    private WatchService watchService;


    public LocalFileEvent(final LocalFileEventConfiguration configuration) {
        super(configuration);
        path = new File(configuration.getPath());
        filter = configuration.getFileFilter();
        index = configuration.getIndex();
        this.configuration = configuration;
    }

    @Override
    public void validate() throws InvalidApi {
        if (!path.exists() || !path.isDirectory() || !path.canRead()) {
            throw new InvalidApi("Path does not exist or is not readable: ".concat(path.getAbsolutePath()));
        }
        if (!(configuration.isWatchForDelete() || configuration.isWatchForCreate() || configuration.isWatchForModify())) {
            throw new InvalidApi("Trigger won't run - no watchers defined: ".concat(path.getAbsolutePath()));
        }
    }

    @Override
    protected boolean shouldTrigger() {
        try {
            final WatchKey key = getWatchService().take();
            final List<String> foundEvent = getCorrectEvents(key);
            for (String file : foundEvent) {
                waitForWriteLocks(file);
                if (null != index) {
                    FileNameQueue.add(index, file);
                }
            }
            key.reset();
            return foundEvent.size() > 0;
        } catch (final InterruptedException ignored) { }
        return false;
    }

    private void waitForWriteLocks(final String file) throws InterruptedException {
        if (configuration.shouldWaitForWritingLocks()) {
            final File currentFile = new File(file);
            while(fileLocked(currentFile)) {
                LOGGER.debug("Waiting for file write locks...");
                Thread.sleep(1000);
            }
        }
    }

    private boolean fileLocked(final File currentFile) {
        try (final FileChannel ch = FileChannel.open(currentFile.toPath(), StandardOpenOption.WRITE);
             final FileLock lock = ch.tryLock()) {
            return null == lock;
        } catch (final IOException e) {
            throw new RuntimeException("Error waiting for lock: ".concat(currentFile.getAbsolutePath()));
        }
    }

    private String path(final String file) {
        return String.format("%s%s%s", path.getAbsolutePath(), File.separator, file);
    }

    private List<String> getCorrectEvents(final WatchKey key) {
        return key.pollEvents().stream()
                .filter(a -> isCorrectEventType(a) && passFilter(a))
                .map(a -> path(a.context().toString()))
                .peek(a -> LOGGER.debug("Got file: ".concat(a)))
                .collect(Collectors.toList());
    }

    private boolean passFilter(final WatchEvent<?> event) {
        final Path path = (Path) event.context();
        return null == filter || filter(path);
    }

    private boolean filter(final Path path) {
        final String file = path.getFileName().toString();
        return filterStartsWith(file) && filterEndsWith(file) && filterRegex(file);
    }

    private boolean filterRegex(final String file) {
        return null == filter.regularExpression || Pattern.matches(filter.regularExpression, file);
    }

    private boolean filterEndsWith(final String file) {
        return null == filter.endsWith || file.endsWith(filter.endsWith);
    }

    private boolean filterStartsWith(final String file) {
        return null == filter.startsWith || file.startsWith(filter.startsWith);
    }

    private boolean isCorrectEventType(final WatchEvent<?> event) {
        final WatchEvent.Kind<?> kind = event.kind();
        if (StandardWatchEventKinds.OVERFLOW.equals(kind) && configuration.throwErrorOnOverflow()) {
            throw new RuntimeException("Some overflow detected - maybe something missing now");
        }
        return
                (StandardWatchEventKinds.ENTRY_MODIFY.equals(kind) && configuration.isWatchForModify()) ||
                (StandardWatchEventKinds.ENTRY_DELETE.equals(kind) && configuration.isWatchForDelete()) ||
                (StandardWatchEventKinds.ENTRY_CREATE.equals(kind) && configuration.isWatchForCreate());
    }

    private WatchService getWatchService() {
        if (null == watchService) {
            try {
                watchService = FileSystems.getDefault().newWatchService();
                path.toPath().register(watchService, getWatchFlags());
            } catch (final IOException e) {
                throw new RuntimeException("No watchService could be created", e);
            }
        }
        return watchService;
    }

    private WatchEvent.Kind<?>[] getWatchFlags() {
        int i = 0;
        WatchEvent.Kind<?>[] result = new WatchEvent.Kind<?>[]{};
        if (configuration.isWatchForModify()) {
            result = Arrays.copyOf(result, ++i);
            result[i - 1] = StandardWatchEventKinds.ENTRY_MODIFY;
        }
        if (configuration.isWatchForCreate()) {
            result = Arrays.copyOf(result, ++i);
            result[i - 1] = StandardWatchEventKinds.ENTRY_CREATE;
        }
        if (configuration.isWatchForDelete()) {
            result = Arrays.copyOf(result, ++i);
            result[i - 1] =StandardWatchEventKinds.ENTRY_DELETE;
        }
        return result;
    }
}