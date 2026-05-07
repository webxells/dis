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
package com.webxells.dis.fileregistry.validator;

import com.webxells.dis.api.MappingPortrayal;
import com.webxells.dis.base.SimpleDatasetPiece;
import com.webxells.dis.base.SimpleMappingPortrayal;
import com.webxells.dis.base.config.SimpleMappingConfiguration;
import com.webxells.dis.base.config.SimpleMappingPart;
import com.webxells.dis.base.config.SimpleMappingPoint;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HashFileRegistryRejectKnownTest {
    private final static Path TMP_DIR = new File(System.getProperty("java.io.tmpdir")).toPath();

    private File testDir;

    @BeforeEach
    void setUpTestDir() throws IOException {
        testDir = Files.createTempDirectory(TMP_DIR, HashFileRegistryRejectKnownTest.class.getSimpleName()).toFile();
        testDir.deleteOnExit();
    }

    @AfterEach
    void cleanTestDir() throws IOException {
        Files.walk(testDir.toPath())
                .sorted(Comparator.reverseOrder())
                .map(Path::toFile)
                .forEach(File::deleteOnExit);
    }

    @Test
    void test() {
        List<MappingPortrayal> additionalIdMappings = List.of(new SimpleMappingPortrayal() {{
            setPath(random());
            setReference(random());
            setSource(Source.INPUT);
        }}, new SimpleMappingPortrayal() {{
            setPath(random());
            setReference(random());
            setSource(Source.OUTPUT);
        }});
        SimpleMappingConfiguration mappingConfiguration = new SimpleMappingConfiguration();
        mappingConfiguration.setParts(List.of(
                new SimpleMappingPart(mappingConfiguration, new SimpleMappingPoint(random(), random()),
                        new SimpleMappingPoint()) {{
                    getDataset().collect(List.of(new SimpleDatasetPiece(random()), new SimpleDatasetPiece(random())));
                }},
                new SimpleMappingPart(mappingConfiguration),
                new SimpleMappingPart(mappingConfiguration,
                        new SimpleMappingPoint(additionalIdMappings.get(0).getReference(),
                                additionalIdMappings.get(0).getPath()), new SimpleMappingPoint(random(), random())) {{
                    getDataset().collect(List.of(new SimpleDatasetPiece(random())));
                }},
                new SimpleMappingPart(mappingConfiguration, new SimpleMappingPoint(random(), random()),
                        new SimpleMappingPoint(additionalIdMappings.get(1).getReference(),
                                additionalIdMappings.get(1).getPath())) {{
                    getDataset().collect(List.of(new SimpleDatasetPiece(random())));
                }}
        ));

        HashFileRegistryRejectKnown fixture = new HashFileRegistryRejectKnown();
        fixture.setRegistryDirectory(testDir.getAbsolutePath());
        fixture.setHashFields(additionalIdMappings);
        assertTrue(fixture.validate(null, mappingConfiguration.parts().get(0)));
        assertFalse(fixture.validate(null, mappingConfiguration.parts().get(0)));
        mappingConfiguration.parts().get(0).getDataset().getContent().get(1).rewriteValue("new value");
        assertTrue(fixture.validate(null, mappingConfiguration.parts().get(0)));
        assertFalse(fixture.validate(null, mappingConfiguration.parts().get(0)));
    }

    private String random() {
        return String.valueOf(Math.round(Math.random() * 92384));
    }
}