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
package com.webxells.dis.info.internal;

import com.webxells.dis.api.Logger;
import com.webxells.dis.api.error.InputOutputError;
import com.webxells.dis.logging.LoggerProxyFactory;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.jar.JarFile;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;

public class ClassDiscoverer {
    private static final Logger LOGGER = LoggerProxyFactory.logger(ClassDiscoverer.class);
    private static final Set<Class<?>> REGISTRY = new HashSet<>();

    public static Class<?> loadClassByName(final String className) {
        try {
            return Class.forName(className);
        } catch (final ClassNotFoundException | NoClassDefFoundError e) {
            return failedClassLoading(className, e);
        }
    }

    private static Class<?> failedClassLoading(final String className, final Throwable e) {
        LOGGER.e("Class could not be loaded: %s", e, className);
        return null;
    }

    public Stream<Class<?>> stream() throws InputOutputError {
        assertRegistryLoaded();
        return REGISTRY.stream();
    }

    private void assertRegistryLoaded() throws InputOutputError {
        if (REGISTRY.isEmpty()) {
            scanForClasses();
        }
    }

    private void scanForClasses() throws InputOutputError {
        try {
            scanInClassPath();
            scanInJarsInMetaInf();
            scanForDirsInResources();
        } catch (final IOException e) {
            throw new InputOutputError("Could not load classes", e);
        }
    }

    private void scanForDirsInResources() throws IOException {
        for(final String file : getResources("")) {
            final File directory = new File(file);
            if (directory.isDirectory() && directory.canRead()) {
                searchInDirectory(directory.getAbsolutePath())
                        .map(ClassDiscoverer::loadClassByName)
                        .filter(Objects::nonNull)
                        .forEach(REGISTRY::add);
            }
        }
    }

    private void scanInJarsInMetaInf() throws InputOutputError, IOException {
        for(final String file : getResources("META-INF")) {
            if (file.endsWith(".jar")) {
                fetchClassesFromJar(new File(file));
            }
        }
    }

    private Set<String> getResources(final String dirName) throws IOException {
        final Enumeration<URL> resources = Thread.currentThread()
                .getContextClassLoader()
                .getResources(dirName);
        return Stream.generate(() -> null)
                .takeWhile(a -> resources.hasMoreElements())
                .map(a -> resources.nextElement())
                .map(a -> toNativeFilePath(a.getFile()))
                .collect(Collectors.toSet());
    }

    private String toNativeFilePath(String current) {
        if (current.contains("://")) {
            current = current.substring(current.indexOf("://") + 3);
        }
        if (current.startsWith("file:")) {
            current = current.substring(5);
        }
        if (current.contains(".jar!")) {
            current = current.substring(0, current.indexOf(".jar!") + 4);
        }
        if (current.endsWith("/")) {
            current = current.substring(0, current.length() - 1);
        }
        return current;
    }

    private void scanInClassPath() throws InputOutputError, IOException {
        for (final String s : System.getProperty("java.class.path", ".").split(File.pathSeparator)) {
            handleClassPathFiles(new File(s));
        }
    }

    private void handleClassPathFiles(final File file) throws InputOutputError, IOException {
        if (file.getName().endsWith(".jar")) {
            fetchClassesFromJar(file);
            return;
        }
        if (file.isDirectory() && file.canRead()) {
            searchInDirectory(file.getAbsolutePath())
                    .map(ClassDiscoverer::loadClassByName)
                    .filter(Objects::nonNull)
                    .forEach(REGISTRY::add);
        }
    }

    private Stream<String> searchInDirectory(final String path) throws IOException {
        return searchInDirectory(path, path.length());
    }

    private Stream<String> searchInDirectory(final String path, final int rootDirEnd) throws IOException {
        final Set<String> result = new LinkedHashSet<>();
        Files.walkFileTree(Path.of(path), new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) {
                if (shouldLoadClass(file.toString())) {
                    final String path = file.toFile().getAbsolutePath();
                    result.add(path.substring(rootDirEnd + 1, path.length() - 6)
                            .replace(File.separatorChar, '.'));
                }
                return FileVisitResult.CONTINUE;
            }
        });

        return result.stream();
    }

    private void fetchClassesFromJar(final File file) throws InputOutputError {
        final Set<String> classNames = extractNamesOfJar(file);

        try (final URLClassLoader ucl = URLClassLoader.newInstance(new URL[] {file.toURI().toURL()})) {
            classNames.stream()
                    .map(a -> loadClassByClassLoader(ucl, a))
                    .forEach(this::add);
        } catch (final IOException e) {
            throw new InputOutputError("Unable to create class loader: ".concat(file.getAbsolutePath()), e);
        }
    }

    private void add(Class<?> clazz) {
        Optional.ofNullable(clazz)
                .filter(a -> !(a.isHidden() || a.isPrimitive() || a.isSealed()))
                .ifPresent(REGISTRY::add);
    }

    private Class<?> loadClassByClassLoader(final ClassLoader urlClassLoader, final String className) {
        try {
            return urlClassLoader.loadClass(className);
        } catch (final ClassNotFoundException | NoClassDefFoundError e) {
            return failedClassLoading(className, e);
        }
    }


    private Set<String> extractNamesOfJar(final File file) throws InputOutputError {
        try (final JarFile jarFile = new JarFile(file)) {
            return jarFile.stream()
                    .map(ZipEntry::getName)
                    .filter(this::shouldLoadClass)
                    .map(a -> a.substring(0, a.length() - 6)
                            .replace("/", "."))
                    .collect(Collectors.toSet());
        } catch (final IOException e) {
            throw new InputOutputError("Could not create JarFile " + file.getName(), e);
        }
    }

    private boolean shouldLoadClass(final String fileName) {
        return fileName.endsWith(".class") &&
                Stream.of("module-info.class", "package-info.class")
                        .noneMatch(fileName::endsWith);
    }
}