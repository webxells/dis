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
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MathDoubleTest extends SimpleTestCase {
    public static final NumberFormat FORMAT = NumberFormat.getInstance(Locale.US);

    @Test
    void testByMappingPart() throws ParseException {
        BigDecimal first = new BigDecimal(String.valueOf(random(1d)));
        BigDecimal second = new BigDecimal(String.valueOf(random(1d)));
        MappingConfiguration mappingConfiguration = newConfiguration()
                .addPart(ConfigurationBuilder.newPart(ConfigurationBuilder.PartBuilder.PartBuilderState.RANDOM)
                        .withContent(first.toPlainString())
                )
                .addPart(ConfigurationBuilder.newPart(ConfigurationBuilder.PartBuilder.PartBuilderState.RANDOM)
                        .withContent(second.toPlainString())
                )
                .build();

        MathDouble fixture = new MathDouble();
        fixture.setOperation(Math.Operation.SUBTRACT_REVERSE);
        fixture.setOperandPortrayal(SimpleMappingPortrayal.auto(mappingConfiguration.parts().get(1)));
        fixture.manipulate(mappingConfiguration.parts().get(0).getDataset().getContent().get(0),
                mappingConfiguration.parts().get(0));

        assertEquals(second.subtract(first).toPlainString(),
                mappingConfiguration.parts().get(0).value().get());
    }

    @Test
    void test() throws ParseException {
        MathDouble fixture = new MathDouble();
        BigDecimal second = new BigDecimal(String.valueOf(random(1d)));
        fixture.setOperand(second.toPlainString());

        fixture.setOperation(MathDouble.Operation.ADD);

        BigDecimal first = new BigDecimal(random());
        DatasetPiece piece = new SimpleDatasetPiece(first.toPlainString());
        fixture.manipulate(piece, null);

        assertEquals(first.add(second).toPlainString(), piece.value().get());

        piece = new SimpleDatasetPiece(first.toPlainString());
        fixture.setOperation(MathDouble.Operation.SUBTRACT);
        fixture.manipulate(piece, null);

        assertEquals(first.subtract(second).toPlainString(), piece.value().get());

        piece = new SimpleDatasetPiece(first.toPlainString());
        fixture.setOperation(MathLong.Operation.SUBTRACT_REVERSE);
        fixture.manipulate(piece, null);

        assertEquals(second.subtract(first).toPlainString(), piece.value().get());
    }

}