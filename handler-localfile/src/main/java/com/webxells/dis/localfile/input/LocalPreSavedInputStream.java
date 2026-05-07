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
package com.webxells.dis.localfile.input;

import java.io.*;

public class LocalPreSavedInputStream extends InputStream {
    private final File file;
    private boolean open = true;
    private final InputStream localStream;

    public LocalPreSavedInputStream(final InputStream child, final String directory) throws IOException {
        file = createTmpFile(directory);
        file.deleteOnExit();
        copyToLocalFile(child, new FileOutputStream(file));
        localStream = createStream();
    }

    private InputStream createStream() throws FileNotFoundException {
        return new BufferedInputStream(new FileInputStream(file));
    }

    private void copyToLocalFile(final InputStream child, final FileOutputStream localFile) throws IOException {
        child.transferTo(localFile);
        child.close();
        localFile.close();
    }

    private File createTmpFile(final String directory) {
        return new File(directory, String.format("tmp_saved_stream_%s_%s", System.identityHashCode(this),
                System.currentTimeMillis()));
    }

    public File getFile() {
        return file;
    }

    @Override
    public int read() throws IOException {
        return localStream.read();
    }

    @Override
    public void close() throws IOException {
        localStream.close();
        file.delete();
        this.open = false;
    }

    @Override
    public boolean markSupported() {
        return false;
    }

    @Override
    public int available() throws IOException {
        return localStream.available();
    }

    @Override
    public synchronized void mark(final int readlimit) {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public synchronized void reset() {
        throw new UnsupportedOperationException("Not supported");
    }

    public boolean isOpen() {
        return this.open;
    }

    public InputStream getCopy() throws IOException {
        return createStream();
    }
}