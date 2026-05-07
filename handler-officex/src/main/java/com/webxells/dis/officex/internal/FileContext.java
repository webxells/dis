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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;

public class FileContext {
    private final XMLInputFactory xmlFactory;

    private final Path root;
    private final Map<String, List<String>> files;

    public FileContext(final Path root) throws XMLStreamException, IOException {
        this.root = root;
        xmlFactory = createXmlFactory();
        files = readContentTypes(xmlFactory);
    }

    public XMLEventReader getXmlReader(final String type) throws IOException, XMLStreamException {
        if (files.containsKey(type)) {
            for (final String current :  files.get(type)) {
                final Path path = root.resolve(current);
                if (Files.isRegularFile(path) && Files.isReadable(path)) {
                    return xmlFactory.createXMLEventReader(new BufferedInputStream(new FileInputStream(path.toFile())));
                }
            }
        }
        throw new FileNotFoundException("could not find file for type: " + type);
    }

    public List<XMLEventReader> getXmlReaders(final String type) throws IOException, XMLStreamException {
        if (files.containsKey(type)) {
            final List<XMLEventReader> result = new ArrayList<>();
            for (final String current :  files.get(type)) {
                final Path path = root.resolve(current);
                if (Files.isRegularFile(path) && Files.isReadable(path)) {
                    result.add(xmlFactory.createXMLEventReader(new BufferedInputStream(new FileInputStream(path.toFile()))));
                }
            }
            if (0 < result.size()) {
                return result;
            }
        }
        throw new FileNotFoundException("could not find file for type: " + type);
    }

    private XMLInputFactory createXmlFactory() {
        final XMLInputFactory result = XMLInputFactory.newInstance();
        result.setProperty(XMLInputFactory.IS_COALESCING, true);
        result.setProperty(XMLInputFactory.IS_NAMESPACE_AWARE, false);
        result.setProperty(XMLInputFactory.SUPPORT_DTD, false);
        return result;
    }

    private Map<String, List<String>> readContentTypes(final XMLInputFactory factory) throws IOException, XMLStreamException {
        final Map<String, List<String>> result = new HashMap<>();
        try (final InputStream file = new BufferedInputStream(new FileInputStream(
                root.resolve("[Content_Types].xml").toFile()))) {
            final XMLEventReader xmlEventReader = factory.createXMLEventReader(file);
            while (xmlEventReader.hasNext()) {
                if (xmlEventReader.nextEvent() instanceof StartElement startElement &&
                        "Override".equals(startElement.getName().getLocalPart())) {
                    saveToMap(result, startElement);
                }
            }
        }
        return result.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, a -> sortedByNumber(a.getValue())));
    }

    private List<String> sortedByNumber(final List<String> value) {
        return value.stream()
                .sorted(Comparator.comparingInt(a -> {
                    final int lastDot = a.lastIndexOf('.');
                    int startNum = lastDot - 1;
                    while (startNum > -1 && Character.isDigit(a.charAt(startNum))) {
                        startNum--;
                    }
                    return lastDot - startNum == 1 ? -1 :
                            Integer.parseInt(a.substring(startNum + 1, lastDot));
                }))
                .toList();
    }

    private void saveToMap(final Map<String, List<String>> result, final StartElement startElement) {
        final Attribute contentType =
                startElement.getAttributeByName(QName.valueOf("ContentType"));
        if (null != contentType && contentType.getValue().startsWith("application/vnd.openxmlformats-")) {
            result.computeIfAbsent(contentType.getValue().substring(31), a -> new ArrayList<>())
                    .add(getPartName(startElement));
        }
    }

    private String getPartName(final StartElement startElement) {
        return Optional.ofNullable(startElement.getAttributeByName(QName.valueOf("PartName")))
                .map(Attribute::getValue)
                .map(a -> a.startsWith("/") ? a.substring(1) : a)
                .orElse(null);
    }
}