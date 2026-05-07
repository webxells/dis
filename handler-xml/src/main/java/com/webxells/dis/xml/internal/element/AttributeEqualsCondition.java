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
import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;

public class AttributeEqualsCondition extends SpecifiedElement {
    private String attributeName;
    private String attributeValue;

    public AttributeEqualsCondition(final PathElement node, final String attributeName) throws XmlParsingError {
        this(node, attributeName, null);
    }

    public AttributeEqualsCondition(final PathElement node, final String attributeName, final String attributeValue) throws XmlParsingError {
        super(node);
        Attribute.assertValidAttributeName(attributeName);
        this.attributeName = attributeName;
        this.attributeValue = attributeValue;
    }

    @Override
    public boolean matchReaderEvent(final XMLEvent event) {
        if (event.isStartElement()) {
            final javax.xml.stream.events.Attribute attribute =
                    event.asStartElement().getAttributeByName(QName.valueOf(attributeName));
            return null != attribute && (null == attributeValue || attributeValue.equals(attribute.getValue()));
        }
        return false;
    }

    @Override
    public String toString() {
        return String.format("%s[@%s='%s']", specifiedElement.toString(), attributeName, attributeValue);
    }

    @Override
    public String getContent(final XMLEventReader xmlEventReader) throws XMLStreamException, XmlParsingError {
        return specifiedElement.getContent(xmlEventReader);
    }

    public String getAttributeName() {
        return attributeName;
    }

    public String getAttributeValue() {
        return attributeValue;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof AttributeEqualsCondition)) return false;
        if (!super.equals(o)) return false;
        final AttributeEqualsCondition that = (AttributeEqualsCondition) o;
        return attributeName.equals(that.attributeName) &&
                attributeValue.equals(that.attributeValue);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), attributeName, attributeValue);
    }
}