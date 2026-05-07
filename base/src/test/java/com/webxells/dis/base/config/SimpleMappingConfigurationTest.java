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
package com.webxells.dis.base.config;

import com.webxells.dis.api.MappingOperation;
import com.webxells.dis.api.MappingPortrayal;
import com.webxells.dis.api.config.MappingConfiguration;
import com.webxells.dis.api.config.MappingPart;
import com.webxells.dis.api.validator.Validator;
import com.webxells.dis.base.SimpleDatasetPiece;
import com.webxells.dis.base.SimpleMappingPortrayal;
import com.webxells.dis.test.cases.ConfigurationBuilder;
import com.webxells.dis.test.cases.SimpleTestCase;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class SimpleMappingConfigurationTest extends SimpleTestCase {

    @Test
    void partsBy() {
        String input1 = random();
        String input2 = random();
        String input3 = random();
        String output1 = random();
        String output2 = random();
        String output3 = random();

        SimpleMappingConfiguration fixture = new SimpleMappingConfiguration();
        fixture.addPart(new SimpleMappingPart(fixture) {{
            setInput(new SimpleMappingPoint(input1, null));
            setOutput(new SimpleMappingPoint(output1, null));
        }});
        fixture.addPart(new SimpleMappingPart(fixture) {{
            setInput(new SimpleMappingPoint(input1, null));
            setOutput(new SimpleMappingPoint(output2, null));
        }});
        fixture.addPart(new SimpleMappingPart(fixture) {{
            setInput(new SimpleMappingPoint(input2, null));
            setOutput(new SimpleMappingPoint(output2, null));
        }});
        fixture.addPart(new SimpleMappingPart(fixture) {{
            setInput(new SimpleMappingPoint(input3, null));
            setOutput(new SimpleMappingPoint(output3, null));
        }});
        fixture.addPart(new SimpleMappingPart(fixture) {{
            setInput(new SimpleMappingPoint(random(), null));
            setOutput(new SimpleMappingPoint(random(), null));
        }});
        fixture.addPart(new SimpleMappingPart(fixture) {{
            setInput(new SimpleMappingPoint(null, null));
            setOutput(new SimpleMappingPoint(null, null));
        }});

        List<MappingPart> actualAllParts = fixture.parts();
        List<MappingPart> actualInput1 = fixture.partsBySource(input1);
        List<MappingPart> actualInput2 = fixture.partsBySource(input2);
        List<MappingPart> actualInput3 = fixture.partsBySource(input3);
        List<MappingPart> actualOutput1 = fixture.partsByDestination(output1);
        List<MappingPart> actualOutput2 = fixture.partsByDestination(output2);
        List<MappingPart> actualOutput3 = fixture.partsByDestination(output3);
        assertEquals(6, actualAllParts.size());
        assertEquals(2, actualInput1.size());
        assertEquals(1, actualInput2.size());
        assertEquals(1, actualInput3.size());

        assertEquals(1, actualOutput1.size());
        assertEquals(2, actualOutput2.size());
        assertEquals(1, actualOutput3.size());
    }

    @Test
    void clear() {
        SimpleMappingConfiguration subData2 = new SimpleMappingConfiguration();
        SimpleMappingConfiguration subData1 = new SimpleMappingConfiguration();
        subData1.setParts(List.of(
                new SimpleMappingPart(subData1, new SimpleMappingPoint(random(), random()),
                        new SimpleMappingPoint(random(), random())),
                new StableMappingPart(subData1, new SimpleMappingPoint(random(), random()),
                        new SimpleMappingPoint(random(), random())),
                new SimpleMappingPart(subData1, new SimpleMappingPoint(random(), random()),
                        new SimpleMappingPoint(random(), random()))
        ));
        SimpleMappingConfiguration fixture = new SimpleMappingConfiguration();
        fixture.addPart(new StableMappingPart(fixture) {{
            getDataset().collect(List.of(new SimpleDatasetPiece("asd")));
            getSubData().add(subData1);
            getSubData().add(subData2);
        }});
        assertEquals(1, fixture.parts().stream().mapToInt(a -> a.getDataset().getContent().size()).sum());
        fixture.clear();
        assertEquals(0, fixture.parts().stream().mapToInt(a -> a.getDataset().getContent().size()).sum());
        assertEquals(1, fixture.parts().get(0).getSubData().size());
        assertEquals(1, fixture.parts().get(0).getSubData().get(0).parts().size());
        assertTrue( fixture.parts().get(0).getSubData().get(0).parts().get(0) instanceof StableMappingPart);
    }

    @Test
    void byPortrayal() {
        SimpleMappingConfiguration fixture = createFilledMappingConfiguration();
        fixture.addPart(fixture.parts().get(8).copy());
        SimpleMappingPortrayal portrayal = new SimpleMappingPortrayal();
        portrayal.setReference(fixture.parts().get(3).getInput().getReference());
        portrayal.setPath(fixture.parts().get(3).getInput().getPath());
        assertSame(fixture.parts().get(3), fixture.getByPortrayal(portrayal).get());
        assertEquals(List.of(fixture.parts().get(3)), fixture.getAllByPortrayal(portrayal));

        portrayal = new SimpleMappingPortrayal();
        portrayal.setReference(fixture.parts().get(8).getOutput().getReference());
        portrayal.setPath(fixture.parts().get(8).getOutput().getPath());
        portrayal.setSource(MappingPortrayal.Source.OUTPUT);
        assertSame(fixture.parts().get(8), fixture.getByPortrayal(portrayal).get());
        assertEquals(List.of(fixture.parts().get(8), fixture.parts().get(9)), fixture.getAllByPortrayal(portrayal));

        final SimpleMappingPortrayal portrayalNotFound = new SimpleMappingPortrayal();
        portrayalNotFound.setReference("not");
        portrayalNotFound.setPath("found");
        assertTrue(fixture.getByPortrayal(portrayalNotFound).isEmpty());

        portrayalNotFound.setRequired(true);
        assertThrows(RuntimeException.class, () -> fixture.getByPortrayal(portrayalNotFound));

    }

    private SimpleMappingConfiguration createFilledMappingConfiguration() {
        SimpleMappingConfiguration result = new SimpleMappingConfiguration();
        for (int i = 0; i < 9; i++) {
            SimpleMappingPart part = new SimpleMappingPart(result);
            part.setInput(new SimpleMappingPoint(random(), random()));
            part.setOutput(new SimpleMappingPoint(random(), random()));
            result.addPart(part);
        }
        return result;
    }

    @Test
    void copy() {
        SimpleMappingConfiguration fixture = newConfiguration()
                .addPart(new ConfigurationBuilder.PartBuilder(ConfigurationBuilder.PartBuilder.PartBuilderState.RANDOM)
                        .withContent()
                )
                .addPart(new ConfigurationBuilder.PartBuilder(ConfigurationBuilder.PartBuilder.PartBuilderState.RANDOM)
                        .withContent()
                )
                .build();
        fixture.parts().get(0).setValidatorErrorStrategy(Validator.ErrorStrategy.ERROR);
        fixture.parts().get(0).setOperations(List.of(mock(MappingOperation.class)));
        fixture.parts().get(1).setValidatorErrorStrategy(Validator.ErrorStrategy.ERROR);
        fixture.parts().get(1).setOperations(List.of(mock(MappingOperation.class)));

        MappingConfiguration actual = fixture.copy();
        assertNotSame(fixture, actual);
        assertEquals(fixture.size(), actual.size());
        assertParts(fixture.parts().get(0), actual.parts().get(0));
        assertParts(fixture.parts().get(1), actual.parts().get(1));

    }

    private void assertParts(final MappingPart mappingPart, final MappingPart copied) {
        assertEquals(mappingPart.getInput(), copied.getInput());
        assertEquals(mappingPart.getOutput(), mappingPart.getOutput());
        assertEquals(mappingPart.getOperations(), mappingPart.getOperations());
        assertEquals(mappingPart.getValidatorErrorStrategy(), copied.getValidatorErrorStrategy());
        assertEquals(mappingPart.getMultiToSingleSelectStrategy(), copied.getMultiToSingleSelectStrategy());
        assertNotSame(mappingPart.getConfiguration(), copied.getConfiguration());
        assertNotSame(mappingPart.getDataset(), copied.getDataset());
        assertEquals(mappingPart.getDataset().getContent().size(), copied.getDataset().getContent().size());
        assertEquals(mappingPart.getDataset().getContent().get(0).value().get(),
                copied.getDataset().getContent().get(0).value().get());

    }

}