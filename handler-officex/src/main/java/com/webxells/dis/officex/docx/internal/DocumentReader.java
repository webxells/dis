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
package com.webxells.dis.officex.docx.internal;

import com.webxells.dis.officex.Reader;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

public class DocumentReader {
    public record Result(String content, String heading, int headingWeight, List<String> headingPath, boolean newHeading) {}
    private record Heading(String name, int weight) {}

    private interface TagFunction<T,R> {
        R apply(T t) throws IOException, XMLStreamException;
    }

    private final XMLEventReader documentReader;
    private final List<String> availableHeadings;
    private final boolean mergeContentByHeading;
    private final LinkedList<Heading> currentHeadings = new LinkedList<>();

    private Result peek;
    private Result nextPeek;
    private boolean newHeading;
    private boolean nextNewHeading;

    public DocumentReader(final Reader<?> reader, final boolean mergeContentByHeading) throws XMLStreamException, IOException {
        this.documentReader = reader.getXmlReader("officedocument.wordprocessingml.document.main+xml");
        availableHeadings = parseStyles(reader.getXmlReader("officedocument.wordprocessingml.styles+xml"));
        this.mergeContentByHeading = mergeContentByHeading;
    }

    private List<String> parseStyles(final XMLEventReader xmlReader) throws XMLStreamException, IOException {
        final Map<String, Integer> styles = new HashMap<>();
        while (xmlReader.hasNext()) {
            if (xmlReader.nextEvent() instanceof StartElement startElement &&
                    isParagraphStyle(startElement)) {
                getTagsInsideCurrent(xmlReader, tag -> {
                    if (isTag(tag, "name")) {
                        styles.put(getAttributeValue(startElement, "styleId"),
                                styleWeight(getAttributeValue(tag)));
                        return false;
                    }
                    return true;
                });
            }
        }
        return styles.entrySet().stream()
                .filter(entry -> entry.getValue() > -1)
                .sorted(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .toList();
    }

    private int styleWeight(final String style) {
        if ("Title".equals(style)) {
            return 0;
        }
        if (style.startsWith("heading ") && 8 < style.length()) {
            return Integer.parseInt(style.substring(8));
        }
        return -1;
    }

    private String getAttributeValue(final StartElement tag) throws IOException {
        return getAttributeValue(tag, "val");
    }

    private String getAttributeValue(final StartElement tag, final String attributeName) throws IOException {
        return Optional.ofNullable(tag.getAttributeByName(QName.valueOf(attributeName)))
                .map(Attribute::getValue)
                .orElseThrow(() -> new IOException("tag has no attribute: " + tag.getName()));
    }

    private boolean isTag(final StartElement tag, final String name) {
        final String tagName = tag.getName().getLocalPart();
        return name.equals(tagName) || tagName.endsWith(":" + name);
    }

    private void getTagsInsideCurrent(final XMLEventReader xmlReader, final TagFunction<StartElement, Boolean> function)
            throws XMLStreamException, IOException {
        int level = 1;
        boolean continueEvaluation = true;
        //use peek to not close current element required by parent calls of getTagsInsideCurrent
        while (xmlReader.hasNext() && !(level == 1 && xmlReader.peek().isEndElement())) {
            final XMLEvent current = xmlReader.nextEvent();
            if (current instanceof StartElement startElement) {
                level++;
                if (continueEvaluation) {
                    continueEvaluation = function.apply(startElement);
                }
            } else if (current.isEndElement()) {
                level--;
            }
        }
    }

    private boolean isParagraphStyle(final StartElement element) {
        return isTag(element, "style")
                    && hasAttribute(element, "type", "paragraph");
    }

    private boolean hasAttribute(final StartElement element, final String name, final String value) {
        return Optional.ofNullable(element.getAttributeByName(QName.valueOf(name)))
                .map(Attribute::getValue)
                .filter(value::equals)
                .isPresent();
    }

    public Result next() throws IOException, XMLStreamException {
        fillPeek();
        if (null == peek) {
            throw new IOException("Unexpected end of file");
        }
        final Result result = peek;
        peek = null;
        newHeading = false;
        return result;
    }

    public boolean hasNext() throws XMLStreamException, IOException {
        fillPeek();
        return null != peek;
    }

    private void fillPeek() throws XMLStreamException, IOException {
        fillPeek(false);
    }

    private void fillPeek(final boolean fillNext) throws XMLStreamException, IOException {
        if (null == peek) {
            if (!fillNext && null != nextPeek) {
                copyNextPeek();
                return;
            }
            final AtomicReference<String> style = new AtomicReference<>();
            final StringBuilder content = new StringBuilder();
            while (documentReader.hasNext()) {
                if (documentReader.next() instanceof StartElement startElement) {
                    if (isTag(startElement, "p")) {
                        iterateInsidePTag(style, content);
                        break;
                    } else if (isTag(startElement, "tbl")) {
                        content.append(readTable());
                        break;
                    }
                }
            }
            if (documentReader.hasNext() && content.isEmpty()) {
                fillPeek(fillNext);
                return;
            }
            if (fillNext) {
                nextPeek = createResult(content.toString(), style.get(), true);
            } else {
                peek = createMergedResult(createResult(content.toString(), style.get(), false));
            }
        }
    }

    private Result createMergedResult(Result result) throws XMLStreamException, IOException {
        while (mergeContentByHeading && null == nextPeek && null != result && documentReader.hasNext()) {
            fillPeek(true);
            if (null != nextPeek && !nextPeek.newHeading) {
                result = new Result(
                        String.format("%s%n%s", result.content, nextPeek.content),
                        result.heading,
                        result.headingWeight,
                        result.headingPath,
                        result.newHeading
                );
                nextPeek = null;
                nextNewHeading = false;
            }
        }
        return result;
    }

    private void iterateInsidePTag(final AtomicReference<String> style, final StringBuilder content) throws XMLStreamException, IOException {
        getTagsInsideCurrent(documentReader, child -> {
            if (isTag(child, "pPr")) {
                getTagsInsideCurrent(documentReader, pStyle -> {
                    if (isTag(pStyle, "pStyle")) {
                        style.set(getAttributeValue(pStyle));
                        return false;
                    }
                    return true;
                });
            } else if (isTag(child, "t")) {
                content.append(getNextText(documentReader));
            }
            return true;
        });
    }

    private void copyNextPeek() throws XMLStreamException, IOException {
        final Result tmpPeek = nextPeek;
        final boolean tmpNewHeading = nextNewHeading;
        nextPeek = null;
        nextNewHeading = false;
        peek = createMergedResult(tmpPeek);
        newHeading = tmpNewHeading;
    }

    private String getNextText(final XMLEventReader reader) throws XMLStreamException {
        if (reader.peek().isCharacters() && reader.nextEvent() instanceof Characters characters) {
            return characters.getData();
        }
        return "";
    }

    private Result createResult(final String content, final String style, final boolean fillNext) throws XMLStreamException, IOException {
        if (content.isEmpty()) {
            return null;
        }
        if (availableHeadings.contains(style)) {
            final int currentWeight = availableHeadings.indexOf(style);
            while (!currentHeadings.isEmpty() && currentHeadings.getFirst().weight >= currentWeight) {
                currentHeadings.removeFirst();
            }
            currentHeadings.addFirst(new Heading(content, currentWeight));
            if (fillNext) {
                nextNewHeading = true;
            } else {
                newHeading = true;
            }
            fillPeek(fillNext);
            return fillNext ? nextPeek : peek;
        }
        return new Result(content,
                currentHeadings.isEmpty() ? null : currentHeadings.getFirst().name,
                currentHeadings.isEmpty() ? -1 : currentHeadings.getFirst().weight,
                currentHeadings.stream().map(a -> a.name).toList(),
                fillNext ? nextNewHeading : newHeading);
    }

    private String readTable() throws XMLStreamException, IOException {
        final StringBuilder content = new StringBuilder(System.lineSeparator());
        getTagsInsideCurrent(documentReader, tr -> {
            if (isTag(tr, "tr")) {
                getTagsInsideCurrent(documentReader, tc -> {
                    if (isTag(tc, "tc")) {
                        getTagsInsideCurrent(documentReader, t -> {
                            if (isTag(t, "t")) {
                                content.append(getNextText(documentReader));
                            }
                            return true;
                        });
                        content.append("\t");
                    }
                    return true;
                });
                content.append(System.lineSeparator());
            }
            return true;
        });
        content.append(System.lineSeparator());
        return content.toString();
    }
}