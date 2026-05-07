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
package com.webxells.dis.plain.manipulator;

import com.webxells.dis.api.MappingPortrayal;
import com.webxells.dis.api.config.MappingConfiguration;
import com.webxells.dis.api.error.InvalidDatasetException;
import com.webxells.dis.base.SimpleMappingPortrayal;
import com.webxells.dis.base.config.SimpleMappingPoint;
import com.webxells.dis.base.manipulator.AddToValue;
import com.webxells.dis.base.manipulator.ChangeCase;
import com.webxells.dis.base.manipulator.ConcatenationSingleCall;
import com.webxells.dis.base.manipulator.Overwrite;
import com.webxells.dis.test.cases.ConfigurationBuilder;
import com.webxells.dis.test.cases.SimpleTestCase;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RegexToSubDataManipulationTest extends SimpleTestCase {

    @Test
    void test() throws InvalidDatasetException {
        MappingConfiguration configuration = newConfiguration()
                .addPart(ConfigurationBuilder.newPart(ConfigurationBuilder.PartBuilder.PartBuilderState.RANDOM)
                        .withContent("abcdefabcghiabd"))
                .build();
        RegexToSubDataManipulation.SubData subData = new RegexToSubDataManipulation.SubData();
        subData.parts = List.of(
                new RegexToSubDataManipulation.SubDataPart() {{
                    input = new SimpleMappingPoint("regex", "0");
                    operations = List.of(new ChangeCase() {{
                        setToType(Type.UPPER);
                    }});
                }},
                new RegexToSubDataManipulation.SubDataPart() {{
                    input = new SimpleMappingPoint("regex", "1");
                    operations = List.of(new AddToValue(),
                            new Overwrite() {{
                                setValue("overwritten");
                            }});
                }},
                new RegexToSubDataManipulation.SubDataPart() {{
                    input = new SimpleMappingPoint("regex", "2");
                }},
                new RegexToSubDataManipulation.SubDataPart() {{
                    input = new SimpleMappingPoint("regex", "3");
                }},
                new RegexToSubDataManipulation.SubDataPart() {{
                    output = new SimpleMappingPoint("output", "result");
                    operations = List.of(new ConcatenationSingleCall() {{
                        setDelimiter("|");
                        setPieces(List.of(
                                new SimpleMappingPortrayal("regex", "0"),
                                new SimpleMappingPortrayal("regex", "1"),
                                new SimpleMappingPortrayal("regex", "2"),
                                new SimpleMappingPortrayal("regex", "3")));
                    }});
                }}
        );
        RegexToSubDataManipulation fixture = new RegexToSubDataManipulation();
        fixture.setSearch("(a)(b)(.)");
        fixture.setResultPortrayal(new SimpleMappingPortrayal( MappingPortrayal.Source.OUTPUT, "output", "result"));
        fixture.setSubData(subData);
        fixture.manipulate(configuration.parts().get(0).getDataset().getContent().get(0), configuration.parts().get(0));
        assertEquals("ABC|overwritten|b|cdefABC|overwritten|b|cghiABD|overwritten|b|d", configuration.parts().get(0).value().get());
    }

}