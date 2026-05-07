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
package com.webxells.dis.officex.internal;

import java.util.Optional;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.Comment;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

public abstract class OfficeXml {
    protected final XMLEventReader reader;

    protected String readCharacters(final XMLEvent rawValue) {
        return rawValue instanceof Characters characters ? characters.getData() : null;
    }

    protected OfficeXml(final XMLEventReader reader) {
        this.reader = reader;
    }

    protected boolean isStartTag(final String tag, final XMLEvent xmlEvent) {
        return xmlEvent instanceof StartElement startElement && tag.equals(str(startElement.getName()));
    }

    protected boolean isEndTag(final String tag, final XMLEvent xmlEvent) {
        return xmlEvent instanceof EndElement endElement && tag.equals(str(endElement.getName()));
    }

    protected String str(final QName name) {
        return name.getLocalPart();
    }

    protected String attr(final StartElement startElement, final String name) {
        return Optional.ofNullable(startElement.getAttributeByName(QName.valueOf(name)))
                .map(Attribute::getValue)
                .orElse(null);
    }

    protected boolean isNextStartTag(final String tag) throws XMLStreamException {
        return isStartTag(tag, peekElement());
    }

    protected boolean isNextEndTag(final String tag) throws XMLStreamException {
        return isEndTag(tag, peekElement());
    }

    protected XMLEvent peekElement() throws XMLStreamException {
        XMLEvent peek = reader.peek();
        while (peek instanceof Comment) {
            reader.nextEvent();
            peek = reader.peek();
        }
        return peek;
    }

    public void end() throws XMLStreamException {
        reader.close();
    }
}