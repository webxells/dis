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

import com.webxells.dis.xml.internal.XmlParsingError;
import java.util.Iterator;
import java.util.Objects;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

public class Attribute extends SpecifiedElement {
    private final String name;
    private StartElement lastSuccessfullyMatched;

    static void assertValidAttributeName(final String name) throws XmlParsingError {
        if (null == name || name.isEmpty()) {
            throw new XmlParsingError("Attribute name required");
        }
        for (int i = name.length(); --i >= 0;) {
            final int code = name.toUpperCase().codePointAt(i);
            if ((65 > code || 90 < code) && (48 > code || 57 < code) && code != 45 && code != 95 && code != 46) {
                throw new XmlParsingError("Attribute name containing invalid char");
            }
        }

    }

    public Attribute(final PathElement node, final String name) throws XmlParsingError {
        super(node);
        assertValidAttributeName(name);
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return String.format("%s@%s", specifiedElement.toString(), name);
    }

    @Override
    public boolean matchReaderEvent(final XMLEvent event) {
        if (specifiedElement.matchReaderEvent(event) && event.isStartElement()) {
            lastSuccessfullyMatched = event.asStartElement();
            return true;
        }
        return false;
    }

    @Override
    public String getContent(final XMLEventReader xmlEventReader) {
        final StartElement element = getElement();
        if (null != element) {
            final javax.xml.stream.events.Attribute attribute = getAttributeIgnoreNamespace(element);
            lastSuccessfullyMatched = null;
            if (null != attribute) {
                return attribute.getValue();
            }
        }
        return null;
    }

    private javax.xml.stream.events.Attribute getAttributeIgnoreNamespace(final StartElement element) {
        final Iterator<javax.xml.stream.events.Attribute> attributes = element.getAttributes();
        while (attributes.hasNext()) {
            final javax.xml.stream.events.Attribute current = attributes.next();
            if (name.equals(current.getName().getLocalPart())) {
                return current;
            }
        }
        return null;
    }

    private StartElement getElement() {
        return specifiedElement instanceof Root ? Root.getCurrent() : lastSuccessfullyMatched;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof Attribute)) return false;
        if (!super.equals(o)) return false;
        final Attribute attribute = (Attribute) o;
        return name.equals(attribute.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), name);
    }
}