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
package com.webxells.dis.rest;

import com.webxells.dis.api.BinaryData;
import com.webxells.dis.test.cases.SimpleTestCase;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MultiPartInputStreamTest extends SimpleTestCase {

    @Test
    void testEmpty() throws IOException {
        String boundary = random();
        MultiPartInputStream fixture = new MultiPartInputStream(boundary);
        StringBuilder actual = new StringBuilder();
        int next;
        while ((next = fixture.read()) > -1) {
            actual.append((char) next);
        }
        assertTrue(actual.toString().isEmpty());
    }

    /**
     * fyi: long time during Mockito loading
     */
    @Test
    void test() throws IOException {
        String boundary = random();
        String name1 = random();
        String filename1 = random();
        String fileType1 = random();
        String content1 = random();
        BinaryData data1 = mock(BinaryData.class);
        ByteArrayInputStream part1 = new ByteArrayInputStream(content1.getBytes());
        when(data1.getContent()).thenReturn(part1);
        when(data1.getName()).thenReturn(filename1);
        when(data1.getMimeType()).thenReturn(fileType1);
        String name2 = random();
        String filename2 = random();
        String fileType2 = random();
        String content2 = random();
        BinaryData data2 = mock(BinaryData.class);
        ByteArrayInputStream part2 = new ByteArrayInputStream(content2.getBytes());
        when(data2.getContent()).thenReturn(part2);
        when(data2.getName()).thenReturn(filename2);
        when(data2.getMimeType()).thenReturn(fileType2);
        String name3 = random();
        String filename3 = random();
        String fileType3 = random();
        String content3 = random();
        BinaryData data3 = mock(BinaryData.class);
        ByteArrayInputStream part3 = new ByteArrayInputStream(content3.getBytes());
        when(data3.getContent()).thenReturn(part3);
        when(data3.getName()).thenReturn(filename3);
        when(data3.getMimeType()).thenReturn(fileType3);
        MultiPartInputStream fixture = new MultiPartInputStream(boundary);
        fixture.addData(name1, data1);
        fixture.addData(name2, data2);
        fixture.addData(name3, data3);
        StringBuilder actual = new StringBuilder();
        int next;
        while ((next = fixture.read()) > -1) {
            actual.append((char) next);
        }
        StringBuilder expected = new StringBuilder();
        appendData(expected, boundary, name1, data1, content1);
        appendData(expected, boundary, name2, data2, content2);
        appendData(expected, boundary, name3, data3, content3);
        expected.append(String.format("--%s--", boundary));
        assertEquals(expected.toString(), actual.toString());
    }

    private void appendData(final StringBuilder expected, final String boundary, final String name, final BinaryData data, final String content) throws IOException {
        expected.append(String.format("--%s\r\n", boundary));
        expected.append(String.format("Content-Disposition:form-data;name=\"%s\";filename=\"%s\"\r\n", name,
                data.getName()));
        expected.append(String.format("Content-Type:%s\r\n\r\n", data.getMimeType()));
        expected.append(String.format("%s\r\n", content));
    }


}