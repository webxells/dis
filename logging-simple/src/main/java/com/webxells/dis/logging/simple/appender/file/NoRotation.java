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

import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class NoRotation implements RotationStrategy {
    public boolean append;
    protected OutputStream file;
    protected boolean started;
    protected String path;

    @Override
    public void write(final byte[] content) {
        if (!started) {
            throw new IllegalStateException("not yet started");
        }
        try {
            file.write(content);
            file.flush();
        } catch (final IOException e) {
            throw new RuntimeException("could not write to logging file: " + path, e);
        }
    }

    @Override
    public void start(final String path) {
        this.path = path;
        try {
            createFileStream();
        } catch (final FileNotFoundException e) {
            throw new RuntimeException("could not initial log file: " + path, e);
        }
        started = true;
    }

    @Override
    public boolean started() {
        return started;
    }

    private void createFileStream() throws FileNotFoundException {
        file = new BufferedOutputStream(new FileOutputStream(path, append));
    }
}