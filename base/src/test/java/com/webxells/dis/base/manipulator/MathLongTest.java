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
import com.webxells.dis.api.config.MappingConfiguration;
import com.webxells.dis.base.SimpleDatasetPiece;
import com.webxells.dis.base.SimpleMappingPortrayal;
import com.webxells.dis.test.cases.ConfigurationBuilder;
import com.webxells.dis.test.cases.SimpleTestCase;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MathLongTest extends SimpleTestCase {

    @Test
    void testByMappingPart() {
        String first = random();
        String second = random();
        MappingConfiguration mappingConfiguration = newConfiguration()
                .addPart(ConfigurationBuilder.newPart(ConfigurationBuilder.PartBuilder.PartBuilderState.RANDOM)
                        .withContent(first)
                )
                .addPart(ConfigurationBuilder.newPart(ConfigurationBuilder.PartBuilder.PartBuilderState.RANDOM)
                        .withContent(second)
                )
                .build();

        MathLong fixture = new MathLong();
        fixture.setOperation(MathLong.Operation.SUBTRACT_REVERSE);
        fixture.setOperandPortrayal(SimpleMappingPortrayal.auto(mappingConfiguration.parts().get(1)));
        fixture.manipulate(mappingConfiguration.parts().get(0).getDataset().getContent().get(0),
                mappingConfiguration.parts().get(0));

        assertEquals(String.valueOf(Long.parseLong(second) - Long.parseLong(first)),
                mappingConfiguration.parts().get(0).value().get());
    }

    @Test
    void test() {

        MathLong fixture = new MathLong();
        String second = String.valueOf(random(1L));
        fixture.setOperand(second);

        fixture.setOperation(MathLong.Operation.ADD);

        String first = random();
        DatasetPiece piece = new SimpleDatasetPiece(first);
        fixture.manipulate(piece, null);

        assertEquals(String.format("%s", Long.parseLong(first) + Long.parseLong(second)), piece.value().get());

        piece = new SimpleDatasetPiece(first);
        fixture.setOperation(MathLong.Operation.SUBTRACT);
        fixture.manipulate(piece, null);

        assertEquals(String.format("%s", Long.parseLong(first) - Long.parseLong(second)), piece.value().get());

        piece = new SimpleDatasetPiece(first);
        fixture.setOperation(MathLong.Operation.SUBTRACT_REVERSE);
        fixture.manipulate(piece, null);

        assertEquals(String.format("%s", Long.parseLong(second) - Long.parseLong(first)), piece.value().get());
    }

}