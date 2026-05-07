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

import com.webxells.dis.api.config.MappingConfiguration;
import com.webxells.dis.api.error.DisException;
import com.webxells.dis.base.SimpleMappingPortrayal;
import com.webxells.dis.base.config.SimpleMappingPoint;
import com.webxells.dis.plain.input.ToStringConfiguration;
import com.webxells.dis.test.cases.ConfigurationBuilder;
import com.webxells.dis.test.cases.FileTestCase;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DynamicLocalFileTest extends FileTestCase {

    @Test
    void test() throws IOException, DisException {
        String content1 = random();
        String content2 = random();
        File file1 = createFile();
        Files.writeString(file1.toPath(), content1);
        File file2 = createFile();
        Files.writeString(file2.toPath(), content2);
        String path = random();
        ToStringConfiguration toString = new ToStringConfiguration();
        toString.setName("read");
        DynamicLocalFileConfig config = new DynamicLocalFileConfig();
        config.setChild(toString);
        config.setSource(new SimpleMappingPortrayal("test", path));
        DynamicLocalFile fixture = new DynamicLocalFile(config);
        MappingConfiguration mappingConfiguration = newConfiguration()
                .addPart(ConfigurationBuilder.newPart()
                        .setInput(new SimpleMappingPoint("test", path))
                        .withContent(file1.getAbsolutePath(), file2.getAbsolutePath())
                )
                .addPart(ConfigurationBuilder.newPart()
                        .setInput(new SimpleMappingPoint("read", "content"))
                )
                .build();

        fixture.start();

        assertTrue(fixture.hasNext());
        assertEquals(1, fixture.read(mappingConfiguration));
        assertTrue(fixture.hasNext());
        assertEquals(1, mappingConfiguration.parts().get(1).getDataset().getContent().size());
        assertEquals(content1, mappingConfiguration.parts().get(1).value().get());
        assertEquals(1, fixture.read(mappingConfiguration));
        assertFalse(fixture.hasNext());
        assertEquals(2, mappingConfiguration.parts().get(1).getDataset().getContent().size());
        assertEquals(content1, mappingConfiguration.parts().get(1).value().get());
        assertEquals(content2, mappingConfiguration.parts().get(1).getDataset().getContent().get(1).value().get());
        fixture.end();
    }

}