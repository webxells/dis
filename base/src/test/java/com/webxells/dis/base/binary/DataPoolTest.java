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
package com.webxells.dis.base.binary;

import com.webxells.dis.api.BinaryData;
import java.io.IOException;
import java.io.InputStream;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;

class DataPoolTest {

    @Test
    void test() throws IOException {
        InputStream binaryData1 = mock(InputStream.class);
        String filename1 = random();
        String filetype1 = random();
        InputStream binaryData2 = mock(InputStream.class);
        String filename2 = random();
        String filetype2 = random();
        InputStream binaryData3 = mock(InputStream.class);
        String filename3 = random();
        String filetype3 = random();
        BinaryData data1 = mock(BinaryData.class);
        Mockito.when(data1.getContent()).thenReturn(binaryData1);
        Mockito.when(data1.getName()).thenReturn(filename1);
        Mockito.when(data1.getMimeType()).thenReturn(filetype1);
        BinaryData data2 = mock(BinaryData.class);
        Mockito.when(data2.getContent()).thenReturn(binaryData2);
        Mockito.when(data2.getName()).thenReturn(filename2);
        Mockito.when(data2.getMimeType()).thenReturn(filetype2);
        BinaryData data3 = mock(BinaryData.class);
        Mockito.when(data3.getContent()).thenReturn(binaryData3);
        Mockito.when(data3.getName()).thenReturn(filename3);
        Mockito.when(data3.getMimeType()).thenReturn(filetype3);
        String index1 = random();
        String index2 = random();
        DataPool.registerData(index1, data1);
        DataPool.registerData(index2, data2);
        DataPool actual1 = new DataPool();
        actual1.setIndex(index1);
        DataPool actual2 = new DataPool();
        actual2.setIndex(index2);
        assertSame(binaryData1, actual1.getContent());
        assertSame(filename1, actual1.getName());
        assertSame(filetype1, actual1.getMimeType());
        assertSame(binaryData2, actual2.getContent());
        assertSame(filename2, actual2.getName());
        assertSame(filetype2, actual2.getMimeType());
        DataPool.registerData(index1, data3);
        assertSame(binaryData3, actual1.getContent());
        assertSame(filename3, actual1.getName());
        assertSame(filetype3, actual1.getMimeType());
        actual2.setIndex(random().concat("nonexistent"));
        assertNull(actual2.getContent());
    }

    private String random() {
        return String.valueOf(Math.round(Math.random() * 94785));
    }


}