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
package com.webxells.dis.base.manipulator;

import com.webxells.dis.api.DatasetPiece;
import com.webxells.dis.base.SimpleDatasetPiece;
import com.webxells.dis.base.SimpleMappingPortrayal;
import com.webxells.dis.base.config.SimpleMappingConfiguration;
import com.webxells.dis.base.config.SimpleMappingPart;
import com.webxells.dis.base.config.SimpleMappingPoint;import com.webxells.dis.test.cases.SimpleTestCase;
import com.webxells.dis.test.cases.ConfigurationBuilder;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ValueDifferenceTest extends SimpleTestCase {

    @Test
    void test() {
        String value11 = random("value11");
        String value12 = random("value12");
        String value13 = random("value13");
        String value21 = random("value21");
        String value22 = random("value22");
        String value23 = random("value23");
        SimpleMappingConfiguration configuration = new SimpleMappingConfiguration();
        configuration.setParts(List.of(
                new SimpleMappingPart(configuration, new SimpleMappingPoint(random(), random()),
                        new SimpleMappingPoint(random(), random())) {{
                    getDataset().collect(List.of(new SimpleDatasetPiece(value11),
                            new SimpleDatasetPiece(value12), new SimpleDatasetPiece(value13)));
                }}, new SimpleMappingPart(configuration, new SimpleMappingPoint(random(), random()),
                        new SimpleMappingPoint(random(), random())) {{
                    getDataset().collect(List.of(new SimpleDatasetPiece(value21),
                            new SimpleDatasetPiece(value22), new SimpleDatasetPiece(value23)));
                }}, new SimpleMappingPart(configuration, new SimpleMappingPoint(random(), random()),
                        new SimpleMappingPoint(random(), random())) {{
                    getDataset().collect(List.of(new SimpleDatasetPiece(value11), new SimpleDatasetPiece(value12),
                            new SimpleDatasetPiece(value22), new SimpleDatasetPiece(value23)));
                }}, new SimpleMappingPart(configuration, new SimpleMappingPoint(random(), random()),
                        new SimpleMappingPoint(random(), random()))
        ));
        ValueDifference fixture = new ValueDifference();
        fixture.setMain(SimpleMappingPortrayal.source(configuration.parts().get(2)));
        fixture.setSubsets(List.of(
                SimpleMappingPortrayal.source(configuration.parts().get(0))
        ));

        fixture.manipulate(null, configuration.parts().get(3));

        List<DatasetPiece> content = configuration.parts().get(3).getDataset().getContent();
        Assertions.assertEquals(2, content.size());
        Assertions.assertEquals(4, configuration.parts().get(2).getDataset().getContent().size());
        Assertions.assertTrue(content.stream().anyMatch(a -> value22.equals(a.value().orElse(null))));
        Assertions.assertTrue(content.stream().anyMatch(a -> value23.equals(a.value().orElse(null))));

    }

}