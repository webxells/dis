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

import com.webxells.dis.base.SimpleDatasetPiece;
import com.webxells.dis.base.SimpleMappingPortrayal;
import com.webxells.dis.base.config.SimpleMappingConfiguration;
import com.webxells.dis.base.config.SimpleMappingPart;
import com.webxells.dis.base.config.SimpleMappingPoint;
import com.webxells.dis.test.cases.SimpleTestCase;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class MergeFromSubConfigurationTest extends SimpleTestCase {

    @Test
    void test() {
        String subPath = random("subPath");
        String subReference = random("subReference");

        SimpleMappingConfiguration sub1 = new SimpleMappingConfiguration();
        sub1.setParts(List.of(
                new SimpleMappingPart(sub1, new SimpleMappingPoint(subReference, subPath),
                        new SimpleMappingPoint(subReference, subPath)) {{
                            getDataset().collect(new SimpleDatasetPiece(random("v1")));
                }}
        ));
        SimpleMappingConfiguration sub2 = new SimpleMappingConfiguration();
        sub2.setParts(List.of(
                new SimpleMappingPart(sub2, new SimpleMappingPoint(subReference, subPath),
                        new SimpleMappingPoint(subReference, subPath)) {{
                            getDataset().collect(new SimpleDatasetPiece(random("v2")));
                }}
        ));
        SimpleMappingConfiguration sub3 = new SimpleMappingConfiguration();
        sub3.setParts(List.of(
                new SimpleMappingPart(sub3, new SimpleMappingPoint(subReference, subPath),
                        new SimpleMappingPoint(subReference, subPath)) {{
                            getDataset().collect(new SimpleDatasetPiece(random("v3")));
                }}
        ));
        SimpleMappingConfiguration config = new SimpleMappingConfiguration();
        config.setParts(List.of(
                new SimpleMappingPart(config, new SimpleMappingPoint(random(), random()),
                        new SimpleMappingPoint(random(), random())) {{
                            getSubData().addAll(List.of(sub1, sub2, sub3));
                }},
                new SimpleMappingPart(config, new SimpleMappingPoint(random(), random()),
                        new SimpleMappingPoint(random(), random())) {{
                            getDataset().collect(new SimpleDatasetPiece("gone"));
                }}
        ));
        MergeFromSubConfiguration fixture = new MergeFromSubConfiguration();
        fixture.setRootSource(SimpleMappingPortrayal.source(config.parts().get(0)));
        fixture.setSubDataSource(new SimpleMappingPortrayal(subReference, subPath));
        fixture.setKeepDataset(false);
        fixture.setCreateDatasetPieceCopies(false);

        fixture.manipulate(null, config.parts().get(1));

        Assertions.assertEquals(3, config.parts().get(1).getDataset().getContent().size());
        Assertions.assertEquals(sub1.parts().get(0).value(), config.parts().get(1).getDataset().getContent().get(0).value());
        Assertions.assertEquals(sub2.parts().get(0).value(), config.parts().get(1).getDataset().getContent().get(1).value());
        Assertions.assertEquals(sub3.parts().get(0).value(), config.parts().get(1).getDataset().getContent().get(2).value());

    }

}