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

import com.webxells.dis.api.config.MappingConfiguration;
import com.webxells.dis.api.error.InvalidDatasetException;
import com.webxells.dis.base.SimpleDatasetPiece;
import com.webxells.dis.base.SimpleMappingPortrayal;
import com.webxells.dis.base.config.SimpleMappingConfiguration;
import com.webxells.dis.base.config.SimpleMappingPart;
import com.webxells.dis.base.config.SimpleMappingPoint;
import com.webxells.dis.test.cases.ConfigurationBuilder;import com.webxells.dis.test.cases.SimpleTestCase;
import com.webxells.dis.test.cases.ConfigurationBuilder;

import java.util.LinkedList;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SplitByRegularExpressionTest extends SimpleTestCase {

    @Test
    void test() throws InvalidDatasetException {
        String part1 = String.format("%spartA", random());
        String part2 = String.format("%spartB", random());
        String part3 = String.format("%spartC", random());
        String part4 = String.format("%spartD", random());
        String part5 = String.format("%spartE", random());
        String content = String.format("somethingelse%s%s%s%s%s", part1, part2, part3, part4, part5);
        SimpleMappingConfiguration config = new SimpleMappingConfiguration();
        config.setParts(List.of(
                new SimpleMappingPart(config, new SimpleMappingPoint(random(), random()),
                        new SimpleMappingPoint(random(), random())),
                new SimpleMappingPart(config, new SimpleMappingPoint(random(), random()),
                        new SimpleMappingPoint(random(), random())),
                new SimpleMappingPart(config, new SimpleMappingPoint(random(), random()),
                        new SimpleMappingPoint(random(), random())),
                new SimpleMappingPart(config, new SimpleMappingPoint(random(), random()),
                        new SimpleMappingPoint(random(), random())),
                new SimpleMappingPart(config, new SimpleMappingPoint(random(), random()),
                        new SimpleMappingPoint(random(), random()))
        ));
        SplitByRegularExpression fixture = new SplitByRegularExpression();
        fixture.setMaxParts(5);
        fixture.setPattern("[0-9]+");
        fixture.setPartDestinations(List.of(
                new SplitByRegularExpression.Destination() {{
                    part = 1;
                    destination = SimpleMappingPortrayal.destination(config.parts().get(1));
                }}, new SplitByRegularExpression.Destination() {{
                    part = 1;
                    destination = SimpleMappingPortrayal.destination(config.parts().get(0));
                }}, new SplitByRegularExpression.Destination() {{
                    part = 2;
                    destination = SimpleMappingPortrayal.destination(config.parts().get(2));
                }}, new SplitByRegularExpression.Destination() {{
                    part = 3;
                    destination = SimpleMappingPortrayal.destination(config.parts().get(3));
                }}, new SplitByRegularExpression.Destination() {{
                    part = 4;
                    destination = SimpleMappingPortrayal.destination(config.parts().get(4));
                }}
        ));

        fixture.manipulate(new SimpleDatasetPiece(content), config.parts().get(0));

        assertEquals(part1, config.parts().get(0).value().get());
        assertEquals(part1, config.parts().get(1).value().get());
        assertEquals(part2, config.parts().get(2).value().get());
        assertEquals(part3, config.parts().get(3).value().get());
        assertEquals(part4.concat(part5), config.parts().get(4).value().get());
    }

    @Test
    void testOverwriteAndIgnorePatternInDestinationsOption() throws InvalidDatasetException {
        String part1 = "partA";
        String part2 = "partB";
        String part3 = "partC";
        String part4 = "partD";
        String content = part1 + random() + part2 + random() + part3 + random() + part4;

        MappingConfiguration config = createRandomizedConfiguration(4);

        SplitByRegularExpression split = new SplitByRegularExpression();
        split.setMaxParts(4);
        split.setPattern("[0-9]+");
        split.setOverwriteDestinations(true);
        split.setIgnorePatternInDestinations(true);
        split.setPartDestinations(createPartDestinations(4, config));

        SimpleDatasetPiece piece = new SimpleDatasetPiece(content);
        split.manipulate(piece, config.parts().get(0));
        assertEquals(part1, config.parts().get(0).value().get());
        assertEquals(part2, config.parts().get(1).value().get());
        assertEquals(part3, config.parts().get(2).value().get());
        assertEquals(part4, config.parts().get(3).value().get());
    }

    private List<SplitByRegularExpression.Destination> createPartDestinations(final int amount, final MappingConfiguration config) {
        final List<SplitByRegularExpression.Destination> result = new LinkedList<>();

        for (int i=0;i<amount;i++) {
            int finalI = i;
            result.add(new SplitByRegularExpression.Destination() {{
                part = finalI;
                destination = SimpleMappingPortrayal.destination(config.parts().get(finalI));
            }});
        }

        return result;
    }

    private MappingConfiguration createRandomizedConfiguration(final int amount) {
        ConfigurationBuilder configBuilder = newConfiguration();

        for (int i=0;i<amount;i++) {
            configBuilder.addPart(ConfigurationBuilder.newPart(ConfigurationBuilder.PartBuilder.PartBuilderState.RANDOM)
                    .withContent());
        }

        return configBuilder.build();
    }


}