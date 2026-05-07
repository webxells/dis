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
package com.webxells.dis.csv;

import com.webxells.dis.api.error.InputOutputError;
import com.webxells.dis.api.resource.Resource;
import com.webxells.dis.base.config.SimpleMappingConfiguration;
import com.webxells.dis.base.config.SimpleMappingPoint;
import com.webxells.dis.base.config.StableMappingPart;
import com.webxells.dis.test.cases.ConfigurationBuilder;
import com.webxells.dis.test.cases.SimpleTestCase;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.function.Consumer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;

class CsvInputTest extends SimpleTestCase {

    @Test
    void testReadAllField() throws InputOutputError {
        String content = "\"a\";\"b\";c;\"d\"\r\n\"1\";\"2\";\"3\";\"4\"";

        Resource receiver = Mockito.mock(Resource.class);
        Mockito.when(receiver.receive())
                .thenReturn(new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8)));
        String input = random("input");

        CsvInputConfig config = new CsvInputConfig(input, receiver);
        config.setFirstLineAsHeaders(true);
        config.setLineEnding(CsvConfig.NewLine.WINDOWS);
        config.setUnknownColumn(UnknownColumnStrategy.CREATE);

        SimpleMappingConfiguration mappingConfiguration = new SimpleMappingConfiguration();
        mappingConfiguration.setParts(List.of(
                new StableMappingPart(mappingConfiguration,
                        new SimpleMappingPoint(input, "b"),
                        new SimpleMappingPoint("out0", "null")),
                new StableMappingPart(mappingConfiguration,
                        new SimpleMappingPoint("test2", "a"),
                        new SimpleMappingPoint("out2", "null")),
                new StableMappingPart(mappingConfiguration,
                        new SimpleMappingPoint("test2", "fieldB"),
                        new SimpleMappingPoint("out3", "null"))
        ));
        CsvInput fixture = new CsvInput(config);
        fixture.start();
        assertEquals(3, mappingConfiguration.parts().size());
        int actual = fixture.read(mappingConfiguration);
        assertEquals(4, actual);
        assertEquals(6, mappingConfiguration.parts().size());

        assertEquals("2", mappingConfiguration.parts().get(0).value().get());
        assertTrue(mappingConfiguration.parts().get(1).value().isEmpty());
        assertTrue(mappingConfiguration.parts().get(2).value().isEmpty());
        assertEquals("1", mappingConfiguration.parts().get(3).value().get());
        assertEquals(input, mappingConfiguration.parts().get(3).getInput().getReference());
        assertEquals("a", mappingConfiguration.parts().get(3).getInput().getPath());
        assertNull(mappingConfiguration.parts().get(3).getOutput().getReference());
        assertNull(mappingConfiguration.parts().get(3).getOutput().getPath());
        assertEquals("3", mappingConfiguration.parts().get(4).value().get());
        assertEquals(input, mappingConfiguration.parts().get(4).getInput().getReference());
        assertEquals("c", mappingConfiguration.parts().get(4).getInput().getPath());
        assertNull( mappingConfiguration.parts().get(4).getOutput().getReference());
        assertNull(mappingConfiguration.parts().get(4).getOutput().getPath());
        assertEquals("4", mappingConfiguration.parts().get(5).value().get());
        assertEquals(input, mappingConfiguration.parts().get(5).getInput().getReference());
        assertEquals("d", mappingConfiguration.parts().get(5).getInput().getPath());
        assertNull(mappingConfiguration.parts().get(5).getOutput().getReference());
        assertNull(mappingConfiguration.parts().get(5).getOutput().getPath());
        fixture.end();
    }

    @Test
    void testReadEmptyFields() throws InputOutputError {
        String content = "\"a\";\"b\"\r\n1;\"2\"\r\n\"\";\"\"\r\n;";

        Resource receiver = Mockito.mock(Resource.class);
        Mockito.when(receiver.receive())
                .thenReturn(new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8)));
        String input = random("input");

        CsvInputConfig config = new CsvInputConfig(input, receiver);
        config.setLineEnding(CsvConfig.NewLine.WINDOWS);

        SimpleMappingConfiguration mappingConfiguration = newConfiguration()
                .addPart(ConfigurationBuilder.newPart()
                        .setInput(new SimpleMappingPoint(input, "a"))
                ).addPart(ConfigurationBuilder.newPart()
                        .setInput(new SimpleMappingPoint(input, "b"))
                )
                .build();
        CsvInput fixture = new CsvInput(config);
        fixture.start();
        assertEquals(2, fixture.read(mappingConfiguration));

        assertEquals("1", mappingConfiguration.parts().get(0).value().get());
        assertEquals("2", mappingConfiguration.parts().get(1).value().get());
        mappingConfiguration.clear();
        assertEquals(2, fixture.read(mappingConfiguration));
        assertEquals("", mappingConfiguration.parts().get(0).value().get());
        assertEquals("", mappingConfiguration.parts().get(1).value().get());
        mappingConfiguration.clear();
        assertEquals(2, fixture.read(mappingConfiguration));
        assertTrue(mappingConfiguration.parts().get(0).value().isEmpty());
        assertTrue(mappingConfiguration.parts().get(1).value().isEmpty());

        fixture.end();
    }

    @Test
    void testInRfc4180Standard() throws Exception {
        String content = "fieldA,fieldB,fieldC\r\n" +
                "\"A1,\"\"\",B1,C1\r\n" +
                "A2,B2,C2";
        test(content, a-> {
            a.setRfc4180(true);
            a.setValueSeparator('\'');
            a.setFieldSeparator('-');
            a.setSkipLines(1);
        });
    }

    @Test
    void onCsvReaderErrorsThrowInputOutputErrors() throws Exception {
        Resource badReceiver = Mockito.mock(Resource.class);
        Mockito.when(badReceiver.receive())
                .thenReturn(new ByteArrayInputStream("\";".getBytes(StandardCharsets.UTF_8)));
        CsvInput csvInput = new CsvInput(new CsvInputConfig("test", badReceiver));
        Assertions.assertThrows(InputOutputError.class, csvInput::start);

        badReceiver = Mockito.mock(Resource.class);
        Mockito.when(badReceiver.receive())
                .thenReturn(new ByteArrayInputStream("\";".getBytes(StandardCharsets.UTF_8)));
        CsvInput csvInput2 = new CsvInput(new CsvInputConfig("test", badReceiver) {{
            setFirstLineAsHeaders(false);
        }});
        csvInput2.start();
        Assertions.assertThrows(InputOutputError.class, () -> csvInput2.read(null));
    }

    @Test
    void testIgnoreEndingEmptyLinesOption() throws Exception {
        String content = "fieldA\n" +
                "A1\n" +
                "\n" +
                "A2\n" +
                "";

        Resource receiver = Mockito.mock(Resource.class);
        Mockito.when(receiver.receive())
                .thenReturn(new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8)));

        CsvInputConfig config = new CsvInputConfig("test", receiver);
        config.setIgnoringEmptyLines(true);
        assertEquals(CsvInput.class.getName(), config.getType());

        SimpleMappingConfiguration mappingConfiguration = newConfiguration().addPart(new ConfigurationBuilder.PartBuilder()
                .setInput(new SimpleMappingPoint("test", "fieldA")))
                .build();

        CsvInput fixture = new CsvInput(config);
        assertThrows(IllegalStateException.class, fixture::hasNext);
        assertThrows(IllegalStateException.class, () -> fixture.read(mappingConfiguration));
        fixture.start();
        int actual = fixture.read(mappingConfiguration);
        assertEquals(1, actual);
        assertEquals("A1", mappingConfiguration.parts().get(0).value().get());
        assertTrue(fixture.hasNext());
        mappingConfiguration.clear();
        actual = fixture.read(mappingConfiguration);
        assertEquals(1, actual);
        assertEquals("A2", mappingConfiguration.parts().get(0).value().get());
        assertFalse(fixture.hasNext());
    }

    private void test(String content, Consumer<CsvInputConfig> alterConfig) throws Exception {
        Resource receiver = Mockito.mock(Resource.class);
        Mockito.when(receiver.receive())
                .thenReturn(new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8)));

        CsvInputConfig config = new CsvInputConfig("test", receiver);
        assertEquals(CsvInput.class.getName(), config.getType());
        if (null != alterConfig) {
            alterConfig.accept(config);
        }

        SimpleMappingConfiguration mappingConfiguration = newConfiguration()
                .addPart(new ConfigurationBuilder.PartBuilder()
                        .setInput(new SimpleMappingPoint("test", "fieldB"))
                        .setOutput(new SimpleMappingPoint("out0", "null")))
                .addPart(new ConfigurationBuilder.PartBuilder()
                        .setInput(new SimpleMappingPoint("test", "fieldA"))
                        .setOutput(new SimpleMappingPoint("out1", "null")))
                .addPart(new ConfigurationBuilder.PartBuilder()
                        .setInput(new SimpleMappingPoint("test2", "fieldA"))
                        .setOutput(new SimpleMappingPoint("out2", "null")))
                .addPart(new ConfigurationBuilder.PartBuilder()
                        .setInput(new SimpleMappingPoint("test", "fieldB"))
                        .setOutput(new SimpleMappingPoint("out3", "null")))
                .build();

        CsvInput fixture = new CsvInput(config);
        assertThrows(IllegalStateException.class, fixture::hasNext);
        assertThrows(IllegalStateException.class, () -> fixture.read(mappingConfiguration));
        fixture.start();
        assertThrows(IllegalStateException.class, fixture::start);
        assertTrue(fixture.hasNext());
        int actual = fixture.read(mappingConfiguration);
        assertEquals(3, actual);
        assertEquals("B1", mappingConfiguration.parts().get(0).value().get());
        assertEquals(String.format("A1%s%s", config.getFieldSeparator(), config.getValueSeparator()), mappingConfiguration.parts().get(1).value().get());
        assertTrue( mappingConfiguration.parts().get(2).value().isEmpty());
        assertEquals("B1", mappingConfiguration.parts().get(3).value().get());
        assertTrue(fixture.hasNext());
        mappingConfiguration.clear();
        actual = fixture.read(mappingConfiguration);
        assertEquals(3, actual);
        assertEquals("B2", mappingConfiguration.parts().get(0).value().get());
        assertEquals("A2", mappingConfiguration.parts().get(1).value().get());
        assertTrue( mappingConfiguration.parts().get(2).value().isEmpty());
        assertEquals("B2", mappingConfiguration.parts().get(3).value().get());
        assertFalse(fixture.hasNext());
        assertEquals(0, fixture.read(mappingConfiguration));
        fixture.end();
        verify(receiver, Mockito.only()).receive();
    }
}