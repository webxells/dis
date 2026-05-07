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

import com.webxells.dis.xml.internal.element.Attribute;
import com.webxells.dis.xml.internal.element.AttributeEqualsCondition;
import com.webxells.dis.xml.internal.element.Node;
import com.webxells.dis.xml.internal.element.NthElement;
import com.webxells.dis.xml.internal.element.PathElement;
import com.webxells.dis.xml.internal.element.Root;
import com.webxells.dis.xml.internal.element.SpecifiedElement;
import com.webxells.dis.xml.internal.element.Wildcard;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.StartElement;

public class XmlPath {
    protected final LinkedList<PathElement> path = new LinkedList<>();
    protected final String pathString;
    private String value;

    public XmlPath(final String path) throws XmlParsingError {
        pathString = path;
        parseString(path);
    }

    protected XmlPath(final LinkedList<PathElement> path, final String pathString) {
        this.path.addAll(path);
        this.pathString = pathString;
    }

    protected XmlPath(final PathElement path) {
        this.path.add(path);
        this.pathString = path.toString();
    }

    public XmlPath copy() {
        return new XmlPath(path, pathString);
    }

    public List<PathElement> getPath() {
        return path;
    }

    public boolean matchReaderEvent(final LinkedList<StartElement> current) {
        if (isMatchingPathSize(current)) {
            for (int i = 0; i < current.size(); i++) {
                if (!path.get(i).matchReaderEvent(current.get(i))) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    private boolean isMatchingPathSize(final LinkedList<StartElement> current) {
        if (current.isEmpty()) {
            return 1 == path.size() &&
                    (path.get(0) instanceof Root || isSpecificElementWithRoot(path));

        }
        return current.size() == path.size();
    }

    private boolean isSpecificElementWithRoot(final LinkedList<PathElement> path) {
        return path.get(0) instanceof SpecifiedElement &&
                ((SpecifiedElement) path.get(0)).getSpecifiedElement() instanceof Root;
    }

    public String getContent(final XMLEventReader xmlEventReader) {
        try {
            return path.getLast().getContent(xmlEventReader);
        } catch (XMLStreamException | XmlParsingError e) {
            throw new RuntimeException("Could not get Content", e);
        }
    }

    private void parseString(final String stringPath) throws XmlParsingError {
        if (!stringPath.startsWith("//")) {
            throw new UnsupportedOperationException("Only root paths supported");
        }
        for (String a : stringPath.substring(2).split("/")) {
            if (!path.isEmpty() && path.get(path.size() - 1) instanceof Attribute) {
                throw new XmlParsingError("Attribute is a final element");
            }
            addToPath(a.trim());
        }
    }

    private void addToPath(final String part) throws XmlParsingError {
        final LinkedList<Character> characters = splitByLetter(part);
        PathElement node = getNodeOrRoot(getNextName(characters, List.of('@', '['), true).trim());
        outer: while (characters.size() > 0) {
            Character character = characters.pop();
            switch (character) {
                case '[':
                    node = getElementOfSquaredBrackets(node, characters);
                    break;
                case '@':
                    node = new Attribute(node, toString(characters).trim());
                    break outer;
                default:
                    if (33 > (int) character) {
                        throw new XmlParsingError("Unexpected character in main part: ".concat(character.toString()));
                    }
            }
        }
        path.add(node);
    }

    private PathElement getNodeOrRoot(final String name) {
        if (name.isEmpty()) {
            return Root.instance();
        }
        if ("*".equals(name)) {
            return Wildcard.instance();
        }
        return new Node(name);
    }

    private PathElement getElementOfSquaredBrackets(final PathElement node, final LinkedList<Character> characters)
            throws XmlParsingError {
        while(characters.size() > 0) {
            final Character next = characters.get(0);
            if (isNumeric(next)) {
                return new NthElement(node, getNextNameRemoveDelimiter(characters, List.of(']'), true).trim());
            } else if (next.equals('@')) {
                characters.pop();
                return createAttributeCondition(node, characters);
            } else if(33 > (int) next) {
                characters.pop();
            } else {
                throw new XmlParsingError("Unexpected start token in squared brackets");
            }
        }
        throw new XmlParsingError("Nothing in squared brackets");
    }

    private PathElement createAttributeCondition(final PathElement node, final LinkedList<Character> characters) throws XmlParsingError {
        final String attributeName = getNextName(characters, List.of('=', ']')).trim();
        switch (characters.pop()) {
            case '=':
                final String attributeValue = getQuotedText(characters);
                characters.pop();
                return new AttributeEqualsCondition(node, attributeName, attributeValue);
            case ']':
                return new AttributeEqualsCondition(node, attributeName);
        }
        throw new XmlParsingError("Invalid attribute condition for: ".concat(attributeName));
    }

    private String getQuotedText(final LinkedList<Character> characters) throws XmlParsingError {
        Character quoteChar = null;
        final StringBuilder result = new StringBuilder();
        while(characters.size() > 0) {
            final Character current = characters.pop();
            if (null == quoteChar) {
                if (32 < (int) current) {
                    if ('"' != current && '\'' != current) {
                        throw new XmlParsingError("Invalid quotation char: ".concat(current.toString()));
                    }
                    quoteChar = current;
                }
            } else if (quoteChar.equals(current)) {
                return result.toString();
            } else {
                result.append(current);
            }
        }
        throw new XmlParsingError("Broken quotation detected");
    }

    private String getNextNameRemoveDelimiter(final LinkedList<Character> characters, final List<Character> delimiter,
                                              final boolean endAllowed) throws XmlParsingError {
        String nextName = getNextName(characters, delimiter, endAllowed);
        characters.pop();
        return nextName;
    }

    private String toString(final LinkedList<Character> characters) {
        final StringBuilder result = new StringBuilder();
        characters.forEach(result::append);
        return result.toString();
    }

    private boolean isNumeric(final Character character) {
        final int characterDec = (int) character;
        return characterDec > 47 && characterDec < 58;
    }

    private String getNextName(final LinkedList<Character> characters, final List<Character> delimiter)
            throws XmlParsingError {
        return getNextName(characters, delimiter, false);
    }

    private String getNextName(final LinkedList<Character> characters, final List<Character> delimiter,
                               final boolean endAllowed) throws XmlParsingError {
        final StringBuilder result = new StringBuilder();
        while (characters.size() > 0) {
            if (delimiter.contains(characters.get(0))) {
                return result.toString();
            }
            result.append(characters.pop());
        }
        if (endAllowed) {
            return result.toString();
        }
        throw new XmlParsingError("One of these expected: ".concat(Arrays.toString(delimiter.toArray())));
    }

    private LinkedList<Character> splitByLetter(final String part) {
        final LinkedList<Character> result = new LinkedList<>();
        for (char c : part.toCharArray()) {
            result.add(c);
        }
        return result;
    }

    public String getOriginalPathString() {
        return pathString;
    }

    public String getPathString() {
        return "//" .concat(path.stream()
                .map(Object::toString)
                .collect(Collectors.joining("/")));
    }

    public String getValue() {
        return value;
    }

    public void setValue(final String value) {
        this.value = value;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final XmlPath xmlPath = (XmlPath) o;
        return path.equals(xmlPath.path);
    }

    @Override
    public int hashCode() {
        return Objects.hash(path);
    }
}