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
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class XmlPathTest {

    @Test
    void testMeanFailures() {
        negativeTest(UnsupportedOperationException.class, "/a/b");
        negativeTest("//@b/asd");
        negativeTest("//a@");
        negativeTest("//a[@]");
        negativeTest("//a[@ ]");
        negativeTest("//a/@");
        negativeTest("//a/b@[asd");
        negativeTest("//a@b[asd]");
        negativeTest("//a[asd=\"asdsad\"]");
        negativeTest("//a[@asd=\"asdsad]");
        negativeTest("//a[\"asdsad]");
        negativeTest("//a[@asdasd");
    }

    private void negativeTest(String xmlPathFormat) {
        negativeTest(XmlParsingError.class, xmlPathFormat);
    }

    private void negativeTest(Class<? extends Throwable> throwableClass, String xmlPathFormat) {
        assertThrows(throwableClass, () -> new XmlPath(xmlPathFormat));
    }

    @Test
    void testSimple() throws XmlParsingError {
        String n1 = random();
        String n2 = random();
        String n3 = random();
        XmlPath fixture = new XmlPath(String.format("//%s/%s/%s", n1, n2, n3));
        List<PathElement> path = fixture.getPath();
        assertEquals(3, path.size());
        assertTrue(path.get(0) instanceof Node);
        assertTrue(path.get(1) instanceof Node);
        assertTrue(path.get(2) instanceof Node);
        assertEquals(n1, ((Node) path.get(0)).getName());
        assertEquals(n2, ((Node) path.get(1)).getName());
        assertEquals(n3, ((Node) path.get(2)).getName());
    }

    @Test
    void testNthElementWithSomeMeanWhiteSpaces() throws XmlParsingError {
        testNthElement("//%s /%s[%s]");
        testNthElement("//%s /%s [%s]");
        testNthElement("//%s /%s [ %s]");
        testNthElement("//%s /%s [ %s ]");
        testNthElement("//%s /%s [ %s ] ");
        testNthElement("// %s /%s [ %s ] ");
        testNthElement("// %s / %s [ %s ] ");
    }

    @Test
    void testNthElement() throws XmlParsingError {
        testNthElement("//%s/%s[%s]");
    }

    private void testNthElement(String xmlPathFormat) throws XmlParsingError {
        String n1 = random();
        String n2 = random();
        long n3 = random(20) + 1;
        XmlPath fixture = new XmlPath(String.format(xmlPathFormat, n1, n2, n3));
        List<PathElement> path = fixture.getPath();
        assertEquals(2, path.size());
        assertTrue(path.get(0) instanceof Node);
        assertTrue(path.get(1) instanceof NthElement);
        assertEquals(n1, ((Node) path.get(0)).getName());
        assertEquals(n3, ((NthElement) path.get(1)).getN());
        assertTrue(((NthElement) path.get(1)).getSpecifiedElement() instanceof Node);
        assertEquals(n2, ((Node) ((NthElement) path.get(1)).getSpecifiedElement()).getName());
    }

    @Test
    void testAttributeElementWithSomeMeanWhiteSpace() throws XmlParsingError {
        testAttributeElement("//%s /%s@%s");
        testAttributeElement("//%s /%s @%s");
        testAttributeElement("//%s /%s @ %s");
        testAttributeElement("// %s /%s @ %s");
    }

    @Test
    void testAttributeElement() throws XmlParsingError {
        testAttributeElement("//%s/%s@%s");
    }

    private void testAttributeElement(String xmlPathFormat) throws XmlParsingError {
        String n1 = random();
        String n2 = random();
        String n3 = random();
        XmlPath fixture = new XmlPath(String.format(xmlPathFormat, n1, n2, n3));
        List<PathElement> path = fixture.getPath();
        assertEquals(2, path.size());
        assertTrue(path.get(0) instanceof Node);
        assertTrue(path.get(1) instanceof Attribute);
        assertEquals(n1, ((Node) path.get(0)).getName());
        assertEquals(n3, ((Attribute) path.get(1)).getName());
        assertTrue(((Attribute) path.get(1)).getSpecifiedElement() instanceof Node);
        assertEquals(n2, ((Node) ((Attribute) path.get(1)).getSpecifiedElement()).getName());
    }

    @Test
    void testAttributeConditionWithSomeMeanWhiteSpaces() throws XmlParsingError {
        testAttributeCondition('"', "//%s/%s[ @%s= %s%s%s]");
        testAttributeCondition('"', "//%s/%s[@%s =%s%s%s ]");
        testAttributeCondition('"', "//%s/%s [@%s= %s%s%s]");
        testAttributeCondition('"', "//%s/%s[@%s = %s%s%s]");
        testAttributeCondition('"', "//%s/%s [ @%s = %s%s%s]");
    }

    @Test
    void testAttributeConditionWithDoubleQuotes() throws XmlParsingError {
        testAttributeCondition('"');
    }

    @Test
    void testAttributeConditionWithSingleQuotes() throws XmlParsingError {
        testAttributeCondition('\'');
    }

    private void testAttributeCondition(final char quotationChar) throws XmlParsingError {
        testAttributeCondition(quotationChar, "//%s/%s[@%s=%s%s%s]");
    }

    private void testAttributeCondition(char quotationChar, String xmlPathFormat) throws XmlParsingError {
        String n1 = random();
        String n2 = random();
        String n3 = random();
        String n4 = random();
        XmlPath fixture = new XmlPath(String.format(xmlPathFormat, n1, n2, n3, quotationChar, n4,
                quotationChar));
        List<PathElement> path = fixture.getPath();
        assertEquals(2, path.size());
        assertTrue(path.get(0) instanceof Node);
        assertTrue(path.get(1) instanceof AttributeEqualsCondition);
        assertEquals(n1, ((Node) path.get(0)).getName());
        assertEquals(n3, ((AttributeEqualsCondition) path.get(1)).getAttributeName());
        assertEquals(n4, ((AttributeEqualsCondition) path.get(1)).getAttributeValue());
        assertTrue(((AttributeEqualsCondition) path.get(1)).getSpecifiedElement() instanceof Node);
        assertEquals(n2, ((Node) ((AttributeEqualsCondition) path.get(1)).getSpecifiedElement()).getName());
    }

    private long random(final int i) {
        return Math.round(Math.random() * i);
    }

    private String random() {
        return String.valueOf(random(8947));
    }

}