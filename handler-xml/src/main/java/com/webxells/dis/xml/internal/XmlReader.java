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

import com.webxells.dis.api.error.InputOutputError;
import com.webxells.dis.xml.XmlConfig;
import com.webxells.dis.xml.XmlConfig.UnexpectedMultipleOccurrenceStrategy;
import com.webxells.dis.xml.XmlConfig.UnreachablePathStrategy;
import com.webxells.dis.xml.internal.element.PathElement;
import com.webxells.dis.xml.internal.element.Root;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

public class XmlReader {
    private final XmlEventReaderFacet xmlEventReader;
    private final UnexpectedMultipleOccurrenceStrategy unexpectedMultipleOccurrenceStrategy;
    private final UnreachablePathStrategy unreachablePathStrategy;
    private final XmlPath iterationPath;
    private final LinkedList<StartElement> current = new LinkedList<>();
    private final int iterationPathStrength;
    private List<XmlPath> rawReads;

    public XmlReader(final InputStream inputStream, final XmlConfig config) throws XMLStreamException, XmlParsingError {
        xmlEventReader = createXmlEventReader(inputStream, config);
        unexpectedMultipleOccurrenceStrategy = config.getUnexpectedMultipleOccurrenceStrategy();
        unreachablePathStrategy = config.getUnreachablePathStrategy();
        iterationPath = new XmlPath(config.getIterationPath());
        iterationPathStrength = iterationPath.getPath().size();

        skipToIterationPath(true);
    }

    private XmlEventReaderFacet createXmlEventReader(final InputStream inputStream, final XmlConfig config) throws XMLStreamException {
        final XMLInputFactory factory = XMLInputFactory.newInstance();
        factory.setProperty(XMLInputFactory.IS_COALESCING, true);
        factory.setProperty(XMLInputFactory.SUPPORT_DTD, !config.isIgnoreDtd());
        return new XmlEventReaderFacet(factory.createXMLEventReader(inputStream));
    }

    private XmlReader(final XmlReader parentReader, final XmlPath iterationPath) {
        xmlEventReader = parentReader.xmlEventReader;
        unexpectedMultipleOccurrenceStrategy = parentReader.unexpectedMultipleOccurrenceStrategy;
        unreachablePathStrategy = parentReader.unreachablePathStrategy;
        this.iterationPath = iterationPath;
        iterationPathStrength = 1;
    }

    public Map<XmlPath, XmlPath> readNext(final Set<XmlPath> paths) throws XMLStreamException,
            InputOutputError {
        final Map<XmlPath, XmlPath> result = new HashMap<>();
        skipToNextStartingElement();
        lookUpMapping(paths, result);
        return readTillClosingIterationPath(paths, result);
    }

    private void skipToIterationPath(final boolean build) throws XMLStreamException {
        while(xmlEventReader.hasNext()) {
            skipEmptyLines();
            final XMLEvent next = xmlEventReader.peek();
            if (next.isStartElement()) {
                if (null == iterationPath) {
                    break;
                }
                if (wouldBeIterationPath((StartElement) next)) {
                    break;
                }
            } else if (next.isEndElement() && !build && iterationPathStrength < current.size() - 1) {
                break;
            }
            getNextElement();
        }
    }

    private boolean wouldBeIterationPath(final StartElement next) {
        final LinkedList<StartElement> nextCurrent = new LinkedList<>(current);
        nextCurrent.add(next);
        return iterationPath.matchReaderEvent(nextCurrent);
    }

    private void skipToNextStartingElement() throws XMLStreamException {
        while (xmlEventReader.hasNext()) {
            final XMLEvent xmlEvent = getNextElement();
            if (xmlEvent.isStartElement()) {
                return;
            }
        }
    }

    private void skipToBeforeNextStartingElement() throws XMLStreamException {
        while (xmlEventReader.hasNext()) {
            final XMLEvent xmlEvent = xmlEventReader.peek();
            if (xmlEvent.isStartElement()) {
                return;
            }
            xmlEventReader.nextEvent();
        }
    }

    private XMLEvent getNextElement() throws XMLStreamException {
        final XMLEvent xmlEvent = xmlEventReader.nextEvent();
        if (xmlEvent.isStartElement()) {
            current.add(xmlEvent.asStartElement());
        } else if (xmlEvent.isEndElement()) {
            handleEndElement(xmlEvent.asEndElement().getName());
        }
        return xmlEvent;
    }

    private void handleEndElement(final QName name) {
        if (!name.equals(current.removeLast().getName())) {
            throw new RuntimeException("More ending tags than opened - weired");
        }
    }

    private Map<XmlPath, XmlPath> readTillClosingIterationPath(final Set<XmlPath> mapping, final Map<XmlPath, XmlPath>  result) throws XMLStreamException, InputOutputError {
        while (xmlEventReader.hasNext() && !nextIsANewRead()) {
            final XMLEvent xmlEvent = getNextElement();
            if (xmlEvent.isEndElement()) {
                if (!insideIterationPath()) {
                    return checkForEmptyMapping(mapping, result);
                }
            } else if (xmlEvent.isStartElement()){
                lookUpMapping(mapping, result);
            }
        }
        return checkForEmptyMapping(mapping, result);
    }

    private Map<XmlPath, XmlPath> checkForEmptyMapping(final Set<XmlPath> searchPaths, final Map<XmlPath, XmlPath> result) {
        for (XmlPath entry : searchPaths) {
            if ((!result.containsKey(entry) && !(entry instanceof MultiPath)) || (result.containsKey(entry) && null ==
                    result.get(entry).getValue())) {
                entry.setValue(getUnreachablePathValue(entry));
                result.put(entry, entry);
            }
        }
        return result;
    }

    private String getUnreachablePathValue(final XmlPath searchPath) {
        switch (unreachablePathStrategy) {
            case EMPTY:
                return "";
            case NULL:
                return null;
        }
        throw new RuntimeException("Mapping unreachable: ".concat(searchPath.getPathString()));
    }

    private void lookUpMapping(final Set<XmlPath> searchPaths, final Map<XmlPath, XmlPath> result) throws XMLStreamException, InputOutputError {
        final LinkedList<StartElement> subList =
                current.size() < iterationPathStrength ? new LinkedList<>() :
                        new LinkedList<>(current.subList(iterationPathStrength,
                current.size()));
        setRootTag();
        for (XmlPath path : searchPaths) {
            if (path.matchReaderEvent(subList)) {
                if (path instanceof MultiPath) {
                    result.put(path, path);
                    readMultiDataWithVirtualRoot((MultiPath) path);
                } else {
                    addOccurrenceToResult(result, path);
                }
            }
        }
    }

    private void setRootTag() {
        if (iterationPathStrength > 0 && current.size() >= iterationPathStrength) {
            Root.setCurrent(current.get(iterationPathStrength - 1));
        }
    }

    private void readMultiDataWithVirtualRoot(final MultiPath path) throws XMLStreamException, InputOutputError {
        final StartElement start = current.removeLast();
        xmlEventReader.pasteNext(start);
        final XmlReader reader = new XmlReader(this, new XmlPath(getLastPath(path)));
        while(reader.hasNext()) {
            final List<Map<XmlPath, XmlPath>> transaction = path.newTransaction();
            transaction.add(reader.readNext(createCopiedChildren(path)));
            if (nextIsANewRead()) {
                break;
            }
        }
    }

    private boolean nextIsANewRead() throws XMLStreamException {
        skipEmptyLines();
        return xmlEventReader.peek() instanceof StartElement start && wouldBeIterationPath(start);
    }

    private PathElement getLastPath(final MultiPath path) {
        return path.getPath().get(path.getPath().size() - 1);
    }

    private Set<XmlPath> createCopiedChildren(final MultiPath path) {
        final Set<XmlPath> result =  new HashSet<>();
        path.getChildren().values().forEach(a -> result.add(a.copy()));
        return result;
    }

    private void addOccurrenceToResult(final Map<XmlPath, XmlPath> result, final XmlPath path) throws XMLStreamException {
        if (result.containsKey(path) && null != result.get(path).getValue()) {
            switch (unexpectedMultipleOccurrenceStrategy) {
                case FAIL:
                    throw new RuntimeException("Multiple elements found for: ".concat(path.getPathString()));
                case SELECT_FIRST:
                    return;
            }
        }
        result.put(path, path);
        Optional.ofNullable(this.getPathContent(path))
                .ifPresent(path::setValue);
    }

    private String getPathContent(final XmlPath path) throws XMLStreamException {
        return null != rawReads && rawReads.contains(path) ?
                readRawContent() : path.getContent(xmlEventReader.getReader());
    }

    private String readRawContent() throws XMLStreamException {
        final QName opening = current.getLast().getName();
        final StringWriter result = new StringWriter();
        while(xmlEventReader.hasNext()) {
            final XMLEvent xmlEvent = xmlEventReader.peek();
            if (xmlEvent.isEndElement()) {
                if (opening.equals(xmlEvent.asEndElement().getName())) {
                    break;
                }
            }
            xmlEventReader.nextEvent().writeAsEncodedUnicode(result);
        }
        return result.toString();
    }

    private boolean insideIterationPath() {
        return iterationPathStrength <= current.size();
    }

    public void close() throws XMLStreamException {
        xmlEventReader.close();
        current.clear();
    }

    public boolean hasNext() throws XMLStreamException {
        skipEmptyLines();
        return xmlEventReader.hasNext() && xmlEventReader.peek() instanceof StartElement start && wouldBeIterationPath(start);
    }

    private void skipEmptyLines() throws XMLStreamException {
        while (xmlEventReader.hasNext() && xmlEventReader.peek() instanceof Characters characters && characters.isWhiteSpace()) {
            xmlEventReader.nextEvent();
        }
    }

    public void setRawReads(final List<XmlPath> rawReadsMapping) {
        this.rawReads = rawReadsMapping;
    }
}