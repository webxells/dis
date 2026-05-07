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
import java.util.Objects;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;

public class NthElement extends SpecifiedElement {
    private final int n;
    private long current = 1;

    public NthElement(final PathElement node, final String stringN) throws XmlParsingError {
        super(node);
        n = parseToInt(stringN);
    }

    @Override
    public String toString() {
        return String.format("%s[%s]", specifiedElement.toString(), n);
    }

    @Override
    public boolean matchReaderEvent(final XMLEvent event) {
        return specifiedElement.matchReaderEvent(event) && current++ % n == 0;
    }

    @Override
    public String getContent(final XMLEventReader xmlEventReader) throws XMLStreamException, XmlParsingError {
        return specifiedElement.getContent(xmlEventReader);
    }

    public int getN() {
        return n;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof NthElement)) return false;
        if (!super.equals(o)) return false;
        final NthElement that = (NthElement) o;
        return n == that.n;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), n);
    }

    private int parseToInt(final String n) throws XmlParsingError {
        try {
            final int result = Integer.parseInt(n);
            if (result < 1) {
                throw new XmlParsingError("n of Nth element is required to be greater zero");
            }
            return result;
        } catch (final NumberFormatException e) {
            throw new XmlParsingError("Number expected but found: ".concat(n));
        }
    }
}