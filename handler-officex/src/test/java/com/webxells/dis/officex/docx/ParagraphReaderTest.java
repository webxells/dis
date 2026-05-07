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
package com.webxells.dis.officex.docx;

import com.webxells.dis.api.config.MappingConfiguration;
import com.webxells.dis.api.error.InputOutputError;
import com.webxells.dis.api.resource.Resource;
import com.webxells.dis.test.cases.ConfigurationBuilder;
import com.webxells.dis.test.cases.FileTestCase;
import java.io.InputStream;
import java.io.OutputStream;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

class ParagraphReaderTest extends FileTestCase {

    @Test
    void test() throws InputOutputError {
        testSimple("freeoffice_simple.docx", false);
        testSimple("libreoffice_simple.docx", false);
        testSimple("msoffice_simple.docx", false);
    }

    @Test
    void testWithMerge() throws InputOutputError {
        testSimple("freeoffice_simple.docx", true);
        testSimple("libreoffice_simple.docx", true);
        testSimple("msoffice_simple.docx", true);
    }

    void testSimple(String file, final boolean merged) throws InputOutputError {
        String name = random();
        MappingConfiguration mapping = newConfiguration()
                .addPart(ConfigurationBuilder.newPart()
                        .setInput(name, "content"))
                .addPart(ConfigurationBuilder.newPart()
                        .setInput(name, "content"))
                .addPart(ConfigurationBuilder.newPart()
                        .setInput(name, "heading"))
                .addPart(ConfigurationBuilder.newPart()
                        .setInput(name, "heading-weight"))
                .addPart(ConfigurationBuilder.newPart()
                        .setInput(name, "heading-path"))
                .build();
        ParagraphReaderConfig config = new ParagraphReaderConfig();
        config.setName(name);
        config.setMergeContentByHeading(merged);
        config.setReceiver(new Resource() {
            @Override
            public OutputStream send() {
                return fail();
            }

            @Override
            public InputStream receive() {
                return getResourceFileStream(file);
            }
        });
        config.setTmpDirectory(testDir.getAbsolutePath());
        ParagraphReader fixture = new ParagraphReader(config);
        fixture.start();
        assertTrue(fixture.hasNext());
        assertEquals(6, fixture.read(mapping));
        assertEquals(mapping.parts().get(0).value().get(), mapping.parts().get(1).value().get());

        assertEquals(566, mapping.parts().get(0).value().get().split("Text").length);

        assertEquals("Heading1", mapping.parts().get(2).value().get());
        assertEquals("1", mapping.parts().get(3).value().get());
        assertEquals("Heading1", mapping.parts().get(4).getDataset().getContent().get(0).value().get());
        assertEquals("TITLE", mapping.parts().get(4).getDataset().getContent().get(1).value().get());

        assertTrue(fixture.hasNext());
        mapping.clear();

        assertEquals(7, fixture.read(mapping));
        assertEquals(mapping.parts().get(0).value().get(), mapping.parts().get(1).value().get());

        assertEquals(100, mapping.parts().get(0).value().get().split("Text").length);

        assertEquals("Heading1.1", mapping.parts().get(2).value().get());
        assertEquals("2", mapping.parts().get(3).value().get());
        assertEquals("Heading1.1", mapping.parts().get(4).getDataset().getContent().get(0).value().get());
        assertEquals("Heading1", mapping.parts().get(4).getDataset().getContent().get(1).value().get());
        assertEquals("TITLE", mapping.parts().get(4).getDataset().getContent().get(2).value().get());

        assertTrue(fixture.hasNext());
        mapping.clear();

        assertEquals(8, fixture.read(mapping));
        assertEquals(mapping.parts().get(0).value().get(), mapping.parts().get(1).value().get());

        assertEquals(191, mapping.parts().get(0).value().get().split("Text").length);

        assertEquals("Heading1.1.1", mapping.parts().get(2).value().get());
        assertEquals("3", mapping.parts().get(3).value().get());
        assertEquals("Heading1.1.1", mapping.parts().get(4).getDataset().getContent().get(0).value().get());
        assertEquals("Heading1.1", mapping.parts().get(4).getDataset().getContent().get(1).value().get());
        assertEquals("Heading1", mapping.parts().get(4).getDataset().getContent().get(2).value().get());
        assertEquals("TITLE", mapping.parts().get(4).getDataset().getContent().get(3).value().get());

        assertTrue(fixture.hasNext());
        mapping.clear();

        assertEquals(7, fixture.read(mapping));
        assertEquals(mapping.parts().get(0).value().get(), mapping.parts().get(1).value().get());

        assertEquals(124, mapping.parts().get(0).value().get().split("Text").length);

        assertEquals("Heading1.2", mapping.parts().get(2).value().get());
        assertEquals("2", mapping.parts().get(3).value().get());
        assertEquals("Heading1.2", mapping.parts().get(4).getDataset().getContent().get(0).value().get());
        assertEquals("Heading1", mapping.parts().get(4).getDataset().getContent().get(1).value().get());
        assertEquals("TITLE", mapping.parts().get(4).getDataset().getContent().get(2).value().get());

        assertTrue(fixture.hasNext());
        mapping.clear();

        assertEquals(5, fixture.read(mapping));
        assertEquals(mapping.parts().get(0).value().get(), mapping.parts().get(1).value().get());

        assertEquals("NEW TITLE", mapping.parts().get(2).value().get());
        assertEquals("0", mapping.parts().get(3).value().get());
        assertEquals("NEW TITLE", mapping.parts().get(4).getDataset().getContent().get(0).value().get());

        if (merged) {
            assertEquals(238, mapping.parts().get(0).value().get().split("Text").length);
        } else {
            assertEquals(88, mapping.parts().get(0).value().get().split("Text").length);

            assertTrue(fixture.hasNext());
            mapping.clear();

            assertEquals(5, fixture.read(mapping));
            assertEquals(mapping.parts().get(0).value().get(), mapping.parts().get(1).value().get());

            assertEquals(150, mapping.parts().get(0).value().get().split("Text").length);

            assertEquals("NEW TITLE", mapping.parts().get(2).value().get());
            assertEquals("0", mapping.parts().get(3).value().get());
            assertEquals("NEW TITLE", mapping.parts().get(4).getDataset().getContent().get(0).value().get());
        }

        assertFalse(fixture.hasNext());

        fixture.end();
    }

}