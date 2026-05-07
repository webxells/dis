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
import com.webxells.dis.base.config.SimpleMappingPoint;import com.webxells.dis.test.cases.SimpleTestCase;
import com.webxells.dis.test.cases.ConfigurationBuilder;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ConcatenationTest extends SimpleTestCase {

    @Test
    void test() {
        Concatenation fixture = new Concatenation();
        SimpleMappingPortrayal portrayal1 = new SimpleMappingPortrayal() {{
           setSource(Source.INPUT);
           setPath(random("path-portrayal1"));
           setReference(random("reference-portrayal1"));
        }};
        SimpleMappingPortrayal portrayal2 = new SimpleMappingPortrayal() {{
           setSource(Source.OUTPUT);
           setPath(random("path-portrayal2"));
           setReference(random("reference-portrayal2"));
        }};
        String start = random("start");
        SimpleDatasetPiece piece = new SimpleDatasetPiece(start);
        SimpleMappingConfiguration configuration = new SimpleMappingConfiguration();
        configuration.setParts(List.of(
                new SimpleMappingPart(configuration,
                        new SimpleMappingPoint(portrayal1.getReference(), portrayal1.getPath()),
                        new SimpleMappingPoint(random("some output"), portrayal1.getPath())) {{
                    getDataset().collect(List.of(new SimpleDatasetPiece(random("value-portrayal1")),
                            new SimpleDatasetPiece(random("value-portrayal1b"))));
                }}, new SimpleMappingPart(configuration,
                        new SimpleMappingPoint(portrayal1.getReference(), random("some other path")),
                        new SimpleMappingPoint(portrayal2.getReference(), portrayal2.getPath())) {{
                    getDataset().collect(new SimpleDatasetPiece(random("value-portrayal2")));
                }}, new SimpleMappingPart(configuration,
                        new SimpleMappingPoint(random("some other reference"), random("some other path")),
                        new SimpleMappingPoint(random("some other reference"), portrayal2.getPath())) {{
                    getDataset().collect(new SimpleDatasetPiece(random("other value")));
                }}
        ));
        final String delimiter = random("delimiter");
        String expected = String.format("%2$s%1$s%3$s%1$s%4$s%1$s%5$s", delimiter, start,
                configuration.parts().get(0).getDataset().getContent().get(0).value().get(),
                configuration.parts().get(0).getDataset().getContent().get(1).value().get(),
                configuration.parts().get(1).getDataset().getContent().get(0).value().get());

        fixture.setDelimiter(delimiter);
        fixture.setPieces(List.of(portrayal1, portrayal2));
        fixture.manipulate(piece, configuration.parts().get(1));
        assertEquals(expected, piece.value().get());
    }

}