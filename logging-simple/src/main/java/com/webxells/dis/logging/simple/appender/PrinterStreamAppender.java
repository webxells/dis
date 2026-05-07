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
package com.webxells.dis.logging.simple.appender;

import com.webxells.dis.logging.simple.Event;
import java.io.PrintStream;

public class PrinterStreamAppender extends SimpleAppender {
    private final PrintStream printStream;

    public PrinterStreamAppender() {
        this(System.out);
    }

    public PrinterStreamAppender(final PrintStream printStream) {
        this.printStream = printStream;
    }

    @Override
    public synchronized void write(final Event event) {
        assertAllSet();
        if (isNotable(event)) {
            forceWrite(event);
        }
    }

    @Override
    public void forceWrite(final Event event) {
        writeRaw(patternPath.parse(event));
    }

    @Override
    public void writeRaw(final String message) {
        printStream.println(message);
    }

}