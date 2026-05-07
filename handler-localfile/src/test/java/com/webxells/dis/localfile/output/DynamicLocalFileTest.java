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
package com.webxells.dis.localfile.output;

import com.webxells.dis.api.config.MappingConfiguration;
import com.webxells.dis.api.error.DisException;
import com.webxells.dis.base.config.SimpleMappingPoint;
import com.webxells.dis.base.output.SimpleOutputConfig;
import com.webxells.dis.test.cases.ConfigurationBuilder;
import com.webxells.dis.test.cases.FileTestCase;
import java.io.File;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DynamicLocalFileTest extends FileTestCase {

    @Test
    void test() throws DisException {
        String directory = random();
        new File(path(directory)).mkdir();
        DynamicLocalFileConfig config = new DynamicLocalFileConfig();
        config.setPathTemplate(path(directory, "$dynamic"));
        config.setName("test");
        config.setCreateIfNotExists(true);
        config.setConfig(new SimpleOutputConfig());
        DynamicLocalFile fixture = new DynamicLocalFile(config);
        String file1 = random();
        String file2 = random();
        String file3 = random();

        assertFalse(new File(path(directory, file1)).exists());
        assertFalse(new File(path(directory, file2)).exists());
        assertFalse(new File(path(directory, file3)).exists());

        fixture.start();
        fixture.write(createMapping(file1));
        fixture.write(createMapping(file2));
        fixture.write(createMapping(file3));
        fixture.end();

        assertTrue(new File(path(directory, file1)).exists());
        assertTrue(new File(path(directory, file2)).exists());
        assertTrue(new File(path(directory, file3)).exists());
    }

    private MappingConfiguration createMapping(final String content) {
        return newConfiguration()
                .addPart(ConfigurationBuilder.newPart()
                        .setOutput(new SimpleMappingPoint("test", "dynamic"))
                        .withContent(content)
                )
                .addPart(ConfigurationBuilder.newPart()
                        .setOutput(new SimpleMappingPoint(random(), "dynamic"))
                        .withContent()
                )
                .build();
    }

}