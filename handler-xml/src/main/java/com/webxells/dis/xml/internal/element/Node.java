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
package com.webxells.dis.xml.internal.element;

import java.util.Objects;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;

public class Node implements PathElement {
    private final String name;

    public Node(final String name) {
        this.name = name;
    }

    @Override
    public boolean matchReaderEvent(final XMLEvent event) {
        return event.isStartElement() && event.asStartElement().getName().getLocalPart().equals(name);
    }

    @Override
    public String toString() {
        return String.format("/%s", name);
    }

    @Override
    public String getContent(final XMLEventReader xmlEventReader) throws XMLStreamException {
        if (xmlEventReader.peek().isCharacters()) {
            return xmlEventReader.peek().asCharacters().getData();
        }
        if (xmlEventReader.peek().isEndElement() && xmlEventReader.peek().asEndElement().getName().getLocalPart().equals(name)) {
            return "";
        }
        return null;
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final Node node = (Node) o;
        return name.equals(node.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}