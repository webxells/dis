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
package com.webxells.dis.memory.linker;

import com.webxells.dis.api.DatasetPiece;
import com.webxells.dis.api.error.InputOutputError;
import com.webxells.dis.base.SimpleDatasetPiece;
import com.webxells.dis.base.SimpleMappingPortrayal;
import com.webxells.dis.base.config.SimpleMappingConfiguration;
import com.webxells.dis.base.config.StableMappingPart;
import com.webxells.dis.base.config.SimpleMappingPoint;
import com.webxells.dis.memory.indexer.IgnoreCase;
import com.webxells.dis.memory.indexer.Trim;
import com.webxells.dis.test.cases.SimpleTestCase;
import com.webxells.dis.test.example.input.TestInput;
import com.webxells.dis.test.example.input.TestInputConfig;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MemoryMatchLinkTest extends SimpleTestCase {

    @Test
    void testWithMultipleValues() throws InputOutputError {
        test(true);
    }

    @Test
    void testWithSingleValues() throws InputOutputError {
        test(false);
    }

    void test(boolean multiValue) throws InputOutputError {
        String linkerName = random("linkerName");
        String linkField1 = random("linkField1");
        String linkFieldValue1 = random("linkFieldValue1");
        String linkField2 = random("linkField2");
        String linkFieldValue2 = random("linkFieldValue2");
        String randomLinkerField1 = random("randomField1");
        String randomLinkerField2 = random("randomField2");
        String rootName = random("rootName");
        List<Map<String, DatasetPiece>> inputDatasets = List.of(
                Map.of(
                        randomLinkerField1, new SimpleDatasetPiece(random("inputFieldValue11")),
                        random("inputField12"), new SimpleDatasetPiece(random("inputFieldValue12")),
                        linkField1, new SimpleDatasetPiece(random("otherLinkField1Value")),
                        linkField2, new SimpleDatasetPiece(random("otherLinkField2Value"))
                ), Map.of(
                        randomLinkerField1, new SimpleDatasetPiece(random("inputFieldValue21")),
                        random("inputField22"), new SimpleDatasetPiece(random("inputFieldvalue22")),
                        linkField1, new SimpleDatasetPiece(linkFieldValue1),
                        linkField2, new SimpleDatasetPiece(linkFieldValue2)
                ), Map.of(
                        randomLinkerField1, new SimpleDatasetPiece(random("inputFieldValue22")),
                        random("inputField22"), new SimpleDatasetPiece(random("inputFieldvalue22")),
                        linkField1, new SimpleDatasetPiece(linkFieldValue1),
                        linkField2, new SimpleDatasetPiece(linkFieldValue2)
                ), Map.of(
                        randomLinkerField1, new SimpleDatasetPiece(random("inputFieldValue31")),
                        random("inputField32"), new SimpleDatasetPiece(random("inputFieldValue32")),
                        linkField1, new SimpleDatasetPiece(random("otherLinkField1Value2")),
                        linkField2, new SimpleDatasetPiece(random("otherLinkField2Value2"))
                )
        );
        SimpleMappingConfiguration mappingConfiguration = new SimpleMappingConfiguration();
        mappingConfiguration.setParts(List.of(
                new StableMappingPart(mappingConfiguration,
                        new SimpleMappingPoint(rootName, random("someOtherMappingPath1")),
                        new SimpleMappingPoint(random("someOtherMappingReference1"), random("someOtherMappingPath2"))) {{
                    this.getDataset().collect(new SimpleDatasetPiece(random("rootvalue1")));
                }},
                new StableMappingPart(mappingConfiguration,
                        new SimpleMappingPoint(rootName, linkField1),
                        new SimpleMappingPoint(random("someOtherMappingReference2"), random("someOtherMappingPath2"))) {{
                    this.getDataset().collect(new SimpleDatasetPiece(linkFieldValue1.toUpperCase()));
                }},
                new StableMappingPart(mappingConfiguration,
                        new SimpleMappingPoint(rootName, linkField2),
                        new SimpleMappingPoint(random("someOtherMappingReference2"), random("someOtherMappingPath2"))) {{
                    this.getDataset().collect(new SimpleDatasetPiece(linkFieldValue2.concat(" ")));
                }},
                new StableMappingPart(mappingConfiguration,
                        new SimpleMappingPoint(random("someOtherMappingReference3"), random("someOtherMappingPath3")),
                        new SimpleMappingPoint(random("someOtherMappingReference4"), random("someOtherMappingPath4"))),
                new StableMappingPart(mappingConfiguration,
                        new SimpleMappingPoint(linkerName, randomLinkerField1),
                        new SimpleMappingPoint(random("someOtherMappingReference5"), random("someOtherMappingPath5"))),
                new StableMappingPart(mappingConfiguration,
                        new SimpleMappingPoint(linkerName, randomLinkerField2),
                        new SimpleMappingPoint(random("someOtherMappingReference6"), random("someOtherMappingPath6")))
        ));
        MemoryMatchLink fixture = new MemoryMatchLink();
        fixture.setIndexer(List.of(new IgnoreCase(), new Trim() {{ setTotalErasure(false);}}));
        fixture.setCreateMultipleValues(multiValue);
        fixture.setInputConfig(new TestInputConfig(linkerName));
        fixture.setLinks(List.of(
                new SimpleMappingPortrayal(rootName, linkField1),
                new SimpleMappingPortrayal(rootName, linkField2)
        ));
        TestInput.setData(inputDatasets);
        fixture.start();
        int actual = fixture.getData(mappingConfiguration);
        fixture.end();
        assertEquals( multiValue ? 3 : 1, actual);
        assertEquals(1, mappingConfiguration.parts().get(0).getDataset().getContent().size());
        assertEquals(1, mappingConfiguration.parts().get(1).getDataset().getContent().size());
        assertEquals(1, mappingConfiguration.parts().get(2).getDataset().getContent().size());
        assertEquals(0, mappingConfiguration.parts().get(3).getDataset().getContent().size());
        assertEquals(multiValue ? 2 : 1, mappingConfiguration.parts().get(4).getDataset().getContent().size());
        assertEquals(0, mappingConfiguration.parts().get(5).getDataset().getContent().size());

        if (multiValue) {
            assertEquals(inputDatasets.get(1).get(randomLinkerField1).value(),
                    mappingConfiguration.parts().get(4).getDataset().getContent().get(0).value());
            assertEquals(inputDatasets.get(2).get(randomLinkerField1).value(),
                    mappingConfiguration.parts().get(4).getDataset().getContent().get(1).value());
        } else {
            assertEquals(inputDatasets.get(1).get(randomLinkerField1).value(),
                    mappingConfiguration.parts().get(4).getDataset().getContent().get(0).value());
        }
    }

}