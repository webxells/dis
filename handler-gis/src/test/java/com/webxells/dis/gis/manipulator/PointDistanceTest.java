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
package com.webxells.dis.gis.manipulator;

import com.webxells.dis.api.config.MappingConfiguration;
import com.webxells.dis.api.error.InvalidApi;
import com.webxells.dis.api.error.InvalidDatasetException;
import com.webxells.dis.base.SimpleDatasetPiece;
import com.webxells.dis.base.SimpleMappingPortrayal;
import com.webxells.dis.gis.PointType;
import com.webxells.dis.test.cases.ConfigurationBuilder;
import com.webxells.dis.test.cases.SimpleTestCase;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PointDistanceTest extends SimpleTestCase {
    private static final NumberFormat FORMAT = DecimalFormat.getInstance(Locale.US);

    @Test
    void test() throws InvalidDatasetException, ParseException, InvalidApi {
        double x1 = randomMax(100d);
        double x2 = randomMax(100d);
        double y1 = randomMax(100d);
        double y2 = randomMax(100d);
        MappingConfiguration configuration = newConfiguration()
                .addPart(ConfigurationBuilder.newPart(ConfigurationBuilder.PartBuilder.PartBuilderState.RANDOM)
                        .withContent(String.format("%s,%s", FORMAT.format(x1), FORMAT.format(y1)))
                )
                .addPart(ConfigurationBuilder.newPart(ConfigurationBuilder.PartBuilder.PartBuilderState.RANDOM)
                        .withContent(String.format("%s,%s", FORMAT.format(x2), FORMAT.format(y2)))
                )
                .addPart(ConfigurationBuilder.newPart(ConfigurationBuilder.PartBuilder.PartBuilderState.RANDOM))
                .build();
        PointDistance fixture = new PointDistance();
        SimpleDatasetPiece data = new SimpleDatasetPiece(null);
        fixture.setPointEnd(SimpleMappingPortrayal.destination(configuration.parts().get(1)));
        fixture.validate();
        fixture.setPointStart(SimpleMappingPortrayal.destination(configuration.parts().get(0)));
        fixture.setPointType(PointType.WITH_COMMA);
        fixture.manipulate(data, configuration.parts().get(2));

        String expected = String.valueOf(Math.sqrt(Math.pow(x2 - x1, 2.0) + Math.pow(y2 - y1, 2.0))).substring(0, 5);

        assertEquals(expected, data.value().get().substring(0, 5));

    }

}