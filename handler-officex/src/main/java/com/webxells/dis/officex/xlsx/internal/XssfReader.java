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
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

public class XssfReader extends OfficeXml {
    public class XssfRow {
        private final Map<String, List<String>> values = new HashMap<>();

        public XssfRow(final boolean skipEmpty) throws XMLStreamException {
            if (!isStartTag("row", peekElement())) {
                return;
            }
            do {
                getRowValues();
            } while (skipEmpty && isEmpty() && isStartTag("row", peekElement()));
        }

        private boolean isEmpty() {
            return values.values().stream()
                    .allMatch(a -> a.stream().allMatch(String::isBlank));
        }

        private void getRowValues() throws XMLStreamException {
            while(!isNextEndTag("row")) {
                saveColumn(reader.nextEvent());
            }
            reader.nextEvent();
        }

        private void saveColumn(final XMLEvent cell) throws XMLStreamException {
            if (isStartTag("c", cell)) {
                final StartElement startElement = cell.asStartElement();
                final String reference = stripNumbers(attr(startElement, "r"));
                final String type = attr(startElement, "t");
                while (!isNextEndTag("c")) {
                    if ("inlineStr".equals(type)) {
                        saveInlineStringValue(reference);
                    } else {
                        final XMLEvent value = reader.nextEvent();
                        saveValue(reference, type, value);
                    }
                }
            }
        }

        private void saveValue(final String reference, final String type, final XMLEvent value) throws XMLStreamException {
            if (isStartTag("v", value)) {
                final XMLEvent rawValue = reader.nextEvent();
                toValues(reference,
                        rawValue.isCharacters() ? toValue(type, readCharacters(rawValue)) : null);
            }
        }

        private void toValues(final String reference, final String value) {
            values.computeIfAbsent(reference, a -> new ArrayList<>()).add(value);
        }

        private void saveInlineStringValue(final String reference) throws XMLStreamException {
            while (!isNextEndTag("c")) {
                final XMLEvent text = reader.nextEvent();
                if (isStartTag("t", text)) {
                    toValues(reference, readCharacters(reader.nextEvent()));
                    break;
                }
            }
        }

        public Stream<String> find(final List<String> paths) {
            return paths.stream()
                    .map(values::get)
                    .filter(Objects::nonNull)
                    .flatMap(Collection::stream);
        }

        public Set<Map.Entry<String, List<String>>> all() {
            return values.entrySet();
        }

        private String stripNumbers(final String reference) {
            final StringBuilder result = new StringBuilder();
            for (final byte raw : reference.getBytes(StandardCharsets.UTF_8)) {
                if (raw < 65) {
                    break;
                }
                result.append((char) raw);
            }
            return result.toString();
        }
    }

    private final static DecimalFormat DECIMAL_FORMAT = new DecimalFormat("0.0") {{
        setMaximumFractionDigits(8);
        final DecimalFormatSymbols format = new DecimalFormatSymbols();
        format.setDecimalSeparator('.');
        setDecimalFormatSymbols(format);
    }};

    private final SharedStrings sharedStrings;
    private final boolean skipEmptyRows;

    private XssfRow peeked;

    public XssfReader(final XMLEventReader reader, final SharedStrings sharedStrings, final boolean skipEmptyRows) throws XMLStreamException {
        super(reader);
        this.sharedStrings = sharedStrings;
        this.skipEmptyRows = skipEmptyRows;
        while (reader.hasNext() && !isStartTag("sheetData", reader.nextEvent()));
    }

    public boolean hasNext() throws XMLStreamException {
        if (null == peeked) {
            peeked = new XssfRow(skipEmptyRows);
        }
        return !peeked.isEmpty();
    }

    public XssfRow newRow() throws XMLStreamException {
        if (null != peeked) {
            final XssfRow result = peeked;
            peeked = null;
            return result;
        }
        return new XssfRow(skipEmptyRows);
    }

    private String toValue(final String type, final String data) {
        if (null == type || "n".equals(type)) {
            return toNumber(data);
        } else if ("be".contains(type) || "str".equals(type)) {
            return data;
        } else if ("s".equals(type)) {
            return lookUpSharedStringInRichTypeString(data);
        }
        throw new UnsupportedOperationException("Invalid type: ".concat(type));
    }

    private String lookUpSharedStringInRichTypeString(final String data) {
        return sharedStrings.get(Integer.parseInt(data));
    }

    private String toNumber(final String data) {
        return data.contains(".") ? DECIMAL_FORMAT.format(Double.parseDouble(data)) : data;
    }

    public void skipRow() throws XMLStreamException {
        if (null != peeked) {
            peeked = null;
            return;
        }
        new XssfRow(false);
    }
}