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

import com.webxells.dis.officex.Reader;
import com.webxells.dis.officex.xlsx.selector.SheetSelector;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;

public class WorksheetReader {
    private final List<Map.Entry<String, XMLEventReader>> worksheets = new ArrayList<>();
    private final SharedStrings sharedStrings;
    private final boolean skipEmptyLines;
    private Queue<XMLEventReader> loadedWorksheets;

    public WorksheetReader(final Reader<?> reader, final boolean skipEmptyLines) throws XMLStreamException, IOException {
        this.skipEmptyLines = skipEmptyLines;
        sharedStrings = new SharedStrings(reader.getXmlReader("officedocument.spreadsheetml.sharedStrings+xml"));
        final XMLEventReader workbook = reader.getXmlReader("officedocument.spreadsheetml.sheet.main+xml");
        final List<XMLEventReader> sheets = reader.getXmlReaders("officedocument.spreadsheetml.worksheet+xml");
        readSheets(workbook, sheets);
        workbook.close();
    }

    private void readSheets(final XMLEventReader workbook, final List<XMLEventReader> sheets) throws IOException {
        int worksheetIndex = 0;
        while (workbook.hasNext()) {
            if (workbook.next() instanceof StartElement startElement) {
                if ("sheet".equals(startElement.getName().getLocalPart())) {
                    final Attribute name = startElement.getAttributeByName(QName.valueOf("name"));
                    if (worksheetIndex >= sheets.size()) {
                        throw new IOException("Sheet number " + worksheetIndex + " not found");
                    }
                    worksheets.add(Map.entry(name.getValue(), sheets.get(worksheetIndex++)));
                }
            }
        }
    }

    public XssfReader createXssfReader(final String sheetName, final int sheetNameNumber)
            throws IOException, XMLStreamException {
        int currentSheetNumber = 1;
        for (final Map.Entry<String, XMLEventReader> current : worksheets) {
            if (sheetName.equals(current.getKey())) {
                if (currentSheetNumber++ == sheetNameNumber) {
                    return new XssfReader(current.getValue(), sharedStrings, skipEmptyLines);
                }
            }
        }
        throw new IOException(String.format("Could not find worksheet %s (#%d)", sheetName, sheetNameNumber));
    }

    public void loadSelected(final SheetSelector selector) {
        final AtomicInteger current = new AtomicInteger();
        loadedWorksheets = worksheets.stream()
                .filter(a -> selector.approve(a.getKey(), current.getAndIncrement()))
                .map(Map.Entry::getValue)
                .collect(Collectors.toCollection(LinkedList::new));
    }

    public Optional<XssfReader> getNext() throws XMLStreamException {
        return null == loadedWorksheets || loadedWorksheets.isEmpty() ? Optional.empty() :
                Optional.of(new XssfReader(loadedWorksheets.poll(), sharedStrings, skipEmptyLines));
    }

    public void end() throws XMLStreamException {
        for (final Map.Entry<String, XMLEventReader> worksheet : worksheets) {
            worksheet.getValue().close();
        }
        sharedStrings.end();
        if (null != loadedWorksheets) {
            loadedWorksheets.clear();
        }
    }
}