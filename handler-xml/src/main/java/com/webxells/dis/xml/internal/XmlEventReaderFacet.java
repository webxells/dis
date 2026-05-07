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
package com.webxells.dis.xml.internal;

import java.util.LinkedList;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;

public class XmlEventReaderFacet {
    private final XMLEventReader reader;
    private final LinkedList<XMLEvent> next = new LinkedList<>();

    public XmlEventReaderFacet(final XMLEventReader reader) {
        this.reader = reader;
    }


    public XMLEvent peek() throws XMLStreamException {
        return next.isEmpty() ? reader.peek() : next.getFirst();
    }

    public boolean hasNext() {
        return !next.isEmpty() || reader.hasNext();
    }

    public XMLEventReader getReader() {
        return reader;
    }

    public XMLEvent nextEvent() throws XMLStreamException {
        return next.isEmpty() ? reader.nextEvent() : next.pop();
    }

    public void close() throws XMLStreamException {
        reader.close();
    }

    public void pasteNext(final XMLEvent nextElement) {
        next.add(nextElement);
    }
}