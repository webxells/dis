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
package com.webxells.dis.localfile.input;

import com.webxells.dis.api.error.InputOutputError;
import com.webxells.dis.base.config.SimpleMappingConfiguration;
import com.webxells.dis.base.config.SimpleMappingPoint;
import com.webxells.dis.localfile.input.LocalFileMetaDataInput.Attribute;
import com.webxells.dis.test.cases.ConfigurationBuilder;
import com.webxells.dis.test.cases.SimpleTestCase;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.*;

public class LocalFileMetaDataInputTest extends SimpleTestCase {

    private final String dateFormatPattern = "yyyy-MM-dd:HH-mm-ss";

    @Test
    void test() throws Exception {
        final String reference = "meta";
        final String path = Paths.get(ClassLoader.getSystemResource("test").toURI()).toString();
        final BasicFileAttributes attributes = Files.readAttributes(Path.of(path), BasicFileAttributes.class);

        final FileTime expectedCreationDate = attributes.creationTime();
        final FileTime expectedLastModified = attributes.lastModifiedTime();
        final FileTime expectedLastAccess = attributes.lastAccessTime();
        final String expectedSize = attributes.size() + "";

        LocalFileMetaDataInputConfig inputConfig = new LocalFileMetaDataInputConfig();
        inputConfig.setName(reference);
        inputConfig.setPath(path);
        inputConfig.setDateFormat(dateFormatPattern);

        LocalFileMetaDataInput input = new LocalFileMetaDataInput(inputConfig);

        SimpleMappingConfiguration config = newConfiguration()
                .addPart(new ConfigurationBuilder.PartBuilder()
                    .setInput(new SimpleMappingPoint(reference, Attribute.CREATION_TIME.name())))
                .addPart(new  ConfigurationBuilder.PartBuilder()
                        .setInput(new SimpleMappingPoint(reference, Attribute.LAST_MODIFIED.name())))
                .addPart(new  ConfigurationBuilder.PartBuilder()
                        .setInput(new SimpleMappingPoint(reference, Attribute.LAST_ACCESS.name())))
                .addPart(new  ConfigurationBuilder.PartBuilder()
                        .setInput(new SimpleMappingPoint(reference, Attribute.SIZE.name())))
                .build();

        input.start();
        int count = input.read(config);

        assertEquals(4, count);
        assertEquals(formatExpectedDateValue(expectedCreationDate), config.parts().get(0).value().get());
        assertEquals(formatExpectedDateValue(expectedLastModified), config.parts().get(1).value().get());
        assertEquals(formatExpectedDateValue(expectedLastAccess), config.parts().get(2).value().get());
        assertEquals(expectedSize, config.parts().get(3).value().get());
    }

    @Test
    void testNullInput() throws Exception {
        final String reference = "meta";
        final String path = Paths.get(ClassLoader.getSystemResource("test").toURI()).toString();
        final BasicFileAttributes attributes = Files.readAttributes(Path.of(path), BasicFileAttributes.class);

        final FileTime expectedLastModified = attributes.lastModifiedTime();

        LocalFileMetaDataInputConfig inputConfig = new LocalFileMetaDataInputConfig();
        inputConfig.setName(reference);
        inputConfig.setPath(path);
        inputConfig.setDateFormat(dateFormatPattern);

        LocalFileMetaDataInput input = new LocalFileMetaDataInput(inputConfig);

        SimpleMappingConfiguration config = newConfiguration()
                .addPart(new ConfigurationBuilder.PartBuilder()
                        .setInput(new SimpleMappingPoint(null, Attribute.CREATION_TIME.name())))
                .addPart(new  ConfigurationBuilder.PartBuilder()
                        .setInput(new SimpleMappingPoint(reference, Attribute.LAST_MODIFIED.name())))
                .build();

        input.start();
        int count = input.read(config);

        assertEquals(1, count);
        assertEquals(formatExpectedDateValue(expectedLastModified), config.parts().get(1).value().get());
    }

    @Test
    void testIllegalFormat() throws Exception {
        final String reference = "meta";
        final String path = Paths.get(ClassLoader.getSystemResource("test").toURI()).toString();

        LocalFileMetaDataInputConfig inputConfig = new LocalFileMetaDataInputConfig();
        inputConfig.setName(reference);
        inputConfig.setPath(path);
        inputConfig.setDateFormat("ABC");

        LocalFileMetaDataInput input = new LocalFileMetaDataInput(inputConfig);

        SimpleMappingConfiguration config = newConfiguration()
                .addPart(new ConfigurationBuilder.PartBuilder()
                        .setInput(new SimpleMappingPoint(reference, Attribute.CREATION_TIME.name())))
                .addPart(new  ConfigurationBuilder.PartBuilder()
                        .setInput(new SimpleMappingPoint(reference, Attribute.LAST_MODIFIED.name())))
                .addPart(new  ConfigurationBuilder.PartBuilder()
                        .setInput(new SimpleMappingPoint(reference, Attribute.LAST_ACCESS.name())))
                .addPart(new  ConfigurationBuilder.PartBuilder()
                        .setInput(new SimpleMappingPoint(reference, Attribute.SIZE.name())))
                .build();

        input.start();
        assertThrows(InputOutputError.class, () -> input.read(config));

    }

    @Test
    void testIllegalAttribute() throws Exception {
        final String reference = "meta";
        final String path = Paths.get(ClassLoader.getSystemResource("test").toURI()).toString();

        SimpleMappingConfiguration config = newConfiguration()
                .addPart(new ConfigurationBuilder.PartBuilder()
                        .setInput(new SimpleMappingPoint(reference, "ABC")))
                .build();

        LocalFileMetaDataInputConfig inputConfig = new LocalFileMetaDataInputConfig();
        inputConfig.setName(reference);
        inputConfig.setPath(path);

        LocalFileMetaDataInput input = new LocalFileMetaDataInput(inputConfig);

        input.start();
        int count = input.read(config);

        assertEquals(0, count);
        assertNull(config.parts().get(0).value().orElse(null));
    }

    private String formatExpectedDateValue(final FileTime fileTime) {
        return fileTime
                .toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime()
                .format(DateTimeFormatter.ofPattern(dateFormatPattern));
    }


}