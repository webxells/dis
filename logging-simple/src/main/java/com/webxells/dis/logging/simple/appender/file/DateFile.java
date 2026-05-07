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
package com.webxells.dis.logging.simple.appender.file;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DateFile extends CompressingAbandonedFileRotation {
    private static final String DEFAULT_PATTERN = "yyyyMMdd'.log'";
    private String pathToFile;
    private DateTimeFormatter filePattern;
    public boolean dontCompressOlderLogFiles;
    public int maxFiles;
    public int datePathValidationWaitWrites = 10;
    private int currentDatePathValidationWaitWrites;
    private String rawPattern;

    @Override
    public void write(final byte[] content) {
        if (datePathValidationWaitWrites < ++currentDatePathValidationWaitWrites) {
            final String currentDatePath = parseDatePath();
            if (!currentDatePath.equals(path)) {
                setRightDateFile(currentDatePath);
                if (0 < maxFiles) {
                    deleteTooMuchFiles(currentDatePath);
                }
            }
            currentDatePathValidationWaitWrites = 0;
        }
        super.write(content);
    }

    private void setRightDateFile(final String currentDatePath) {
        try {
            if (null != file) {
                file.close();
                compressFile(path);
            }
        } catch (final IOException e) {
            throw new RuntimeException("Could not close file", e);
        }
        append = true;
        super.start(currentDatePath);
    }

    private String parseDatePath() {
        return pathToFile.concat(filePattern.format(LocalDateTime.now()));
    }

    @Override
    public void start(final String path) {
        currentDatePathValidationWaitWrites = datePathValidationWaitWrites;
        final int positionOfLastSeparator = path.lastIndexOf(File.separator);
        if (path.length() < positionOfLastSeparator + 1) {
            pathToFile = path.concat(File.separator);
            setPattern(DEFAULT_PATTERN);
        } else if (positionOfLastSeparator < 0) {
            pathToFile = "";
            setPattern(path);
        } else {
            pathToFile = path.substring(0, positionOfLastSeparator + 1);
            setPattern(path.substring(positionOfLastSeparator + 1));
        }
        if (compressAbandonedFiles && !dontCompressOlderLogFiles) {
            compressExistingAbandonedFiles();
        }
    }

    private void deleteTooMuchFiles(final String current) {
        try (final Stream<Path> myWay = Files.walk(Path.of(pathToFile))) {
            final List<File> files = myWay
                    .map(Path::toFile)
                    .filter(File::isFile)
                    .filter(a -> a.getName().endsWith(compressAbandonedFiles ? ".gz" : ".log"))
                    .filter(a -> !a.getName().equals(current))
                    .filter(a -> matchingCurrentPattern(a, compressAbandonedFiles ? 3 : 0))
                    .toList();
            if (files.size() > maxFiles) {
                final AtomicInteger toGo = new AtomicInteger(files.size() - maxFiles);
                files.stream()
                        .sorted(this::byCreationTime)
                        .forEach(a -> {
                            if(0 < toGo.getAndDecrement()) {
                                a.delete();
                            }
                        });

            }
        } catch (final IOException e) {
            throw new RuntimeException("Could not access file path to logging files", e);
        }
    }

    private int byCreationTime(File file1, File file2) {
        return toFileTime(file1).compareTo(toFileTime(file2));
    }

    private FileTime toFileTime(final File file) {
        try {
            return (FileTime) Files.getAttribute(file.toPath(), "creationTime");
        } catch (final IOException e) {
            throw new RuntimeException("could not get creationTime attribute", e);
        }
    }

    private void setPattern(final String pattern) {
        rawPattern = pattern;
        filePattern = DateTimeFormatter.ofPattern(pattern);
    }

    private void compressExistingAbandonedFiles() {
        final String current = filePattern.format(LocalDateTime.now());
        try (final Stream<Path> myWay = Files.walk(Path.of(pathToFile))) {
            myWay
                    .map(Path::toFile)
                    .filter(File::isFile)
                    .filter(File::canRead)
                    .filter(File::canWrite)
                    .filter(a -> a.getName().endsWith(".log"))
                    .filter(a -> !a.getName().equals(current))
                    .filter(a -> matchingCurrentPattern(a, 0))
                    .forEach(a -> {
                        try {
                            compressFile(a.getAbsolutePath());
                        } catch (final IOException e) {
                            throw new RuntimeException("Could not compress existing abandoned file: ".concat(a.getAbsolutePath()), e);
                        }
                    });
        } catch (final IOException e) {
            throw new RuntimeException("Could not access file path to logging files", e);
        }
        dontCompressOlderLogFiles = true;
    }

    private boolean matchingCurrentPattern(final File file, final int numberOfMoreLetters) {
        final String name = file.getName();
        final int length = name.length();
        boolean skip = false;
        int i = 0;
        int diff = 0;
        for (int m = rawPattern.length(); i < m; i++) {
            final char currentPattern = rawPattern.charAt(i);
            if ('\'' == currentPattern) {
                skip = !skip;
                diff++;
                continue;
            }
            if (i - diff == length) {
                return false;
            }
            final char currentName = name.charAt(i - diff);
            final boolean correctChar = currentPattern != currentName;
            if ((skip && correctChar) || (!skip && !Character.isDigit(currentName) && correctChar)) {
                return false;
            }
        }
        return length == i - diff + numberOfMoreLetters;
    }

    @Override
    public boolean started() {
        return null != filePattern;
    }
}