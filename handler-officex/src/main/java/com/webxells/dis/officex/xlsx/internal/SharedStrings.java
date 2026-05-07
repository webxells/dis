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
package com.webxells.dis.officex.xlsx.internal;

import com.webxells.dis.officex.internal.OfficeXml;
import java.util.ArrayList;
import java.util.List;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;

public class SharedStrings extends OfficeXml {
    private final List<String> data = new ArrayList<>();

    public SharedStrings(final XMLEventReader xmlReader) throws XMLStreamException {
        super(xmlReader);
        while(xmlReader.hasNext() && !isNextEndTag("sst") ) {
            final XMLEvent item = xmlReader.nextEvent();
            if (isStartTag("si", item)) {
                final StringBuilder builder = new StringBuilder();
                while (xmlReader.hasNext() && !isNextEndTag("si")) {
                    final XMLEvent text = xmlReader.nextEvent();
                    if (isStartTag("t", text)) {
                        builder.append(readCharacters(xmlReader.nextEvent()));
                    }
                }
                data.add(builder.toString());
            }
        }
    }

    public String get(final int index) {
        return data.get(index);
    }
}