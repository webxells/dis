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

import com.webxells.dis.api.resource.Resource;
import com.webxells.dis.base.SimpleDatasetPiece;
import com.webxells.dis.base.config.SimpleMappingConfiguration;
import com.webxells.dis.base.config.SimpleMappingPoint;
import com.webxells.dis.base.config.StableMappingPart;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.function.Consumer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

class CsvOutputTest {

    @Test
    void testWithRfc4180() throws Exception {
        String expected = "fieldD,fieldE,fieldF,emptyField\r\n" +
                "\"A1,\"\"\",B1,C1,\r\n" +
                "A2,B2,C2,\r\n";
        test(expected, a -> {
            a.setRfc4180(true);
            a.setHeaderFields(List.of(
                    "fieldD","fieldE","fieldF","emptyField"
            ));
        });

        expected = "fieldD~fieldE~fieldF~emptyField\n\r" +
                "-A1~---~B1~C1~\n\r" +
                "A2~B2~C2~\n\r";
        test(expected, a -> {
            a.setHeaderFields(List.of(
                    "fieldD","fieldE","fieldF","emptyField"
            ));
            a.setRfc4180(false);
            a.setLineEnding(CsvConfig.NewLine.RISC_OS);
            a.setValueSeparator('-');
            a.setFieldSeparator('~');
        });
    }

    @Test
    void testWithGuessedHeader() throws Exception {
        String expected = "fieldD;fieldE;fieldF\n" +
                "\"A1;\"\"\";B1;C1\n" +
                "A2;B2;C2\n";
        test(expected, a -> {});
    }

    @Test
    void testWithNoHeader() throws Exception {
        String expected = "\"A1;\"\"\";B1;C1\n" +
                "A2;B2;C2\n";
        test(expected, a -> a.setFirstLineAsHeaders(false));
    }

    @Test
    void testWithNullField() throws Exception {
        final ByteArrayOutputStream output = new ByteArrayOutputStream();
        Resource sender = new Resource() {
            @Override
            public String getType() {
                return null;
            }

            @Override
            public OutputStream send() {
                return output;
            }

            @Override
            public InputStream receive() {
                return fail();
            }
        };
        CsvOutputConfig config = new CsvOutputConfig("test", sender);
        SimpleMappingConfiguration mappingConfiguration = new SimpleMappingConfiguration();
        mappingConfiguration.setParts(List.of(
                new StableMappingPart(mappingConfiguration,
                        new SimpleMappingPoint("test", "fieldA"),
                        new SimpleMappingPoint("test", "fieldD")) {{
                            getDataset().collect(new SimpleDatasetPiece(""));
                }},
                new StableMappingPart(mappingConfiguration,
                        new SimpleMappingPoint("test", "fieldB"),
                        new SimpleMappingPoint("test", "fieldE")) {{
                            getDataset().collect(new SimpleDatasetPiece(null));
                }}
        ));
        CsvOutput fixture = new CsvOutput(config);
        fixture.start();
        fixture.write(mappingConfiguration);
        fixture.end();
        String expected =
                "fieldD;fieldE\n" +
                        "\"\";\n";
        Assertions.assertEquals(expected, output.toString());
    }

    private void test(String expected, Consumer<CsvOutputConfig> consumer) throws Exception {
        final ByteArrayOutputStream output = new ByteArrayOutputStream();
        Resource sender = new Resource() {
            @Override
            public String getType() {
                return null;
            }

            @Override
            public OutputStream send() {
                return output;
            }

            @Override
            public InputStream receive() {
                return fail();
            }
        };
        CsvOutputConfig config = new CsvOutputConfig("test", sender);
        consumer.accept(config);
        SimpleMappingConfiguration mappingConfiguration = new SimpleMappingConfiguration();
        mappingConfiguration.setParts(List.of(
                new StableMappingPart(mappingConfiguration,
                        new SimpleMappingPoint("test", "fieldA"),
                        new SimpleMappingPoint("test", "fieldD")),
                new StableMappingPart(mappingConfiguration,
                        new SimpleMappingPoint("test", "fieldB"),
                        new SimpleMappingPoint("test", "fieldE")),
                new StableMappingPart(mappingConfiguration,
                        new SimpleMappingPoint("test", "fieldC"),
                        new SimpleMappingPoint("test", "fieldF")),
                new StableMappingPart(mappingConfiguration,
                        new SimpleMappingPoint("test", "fieldD"),
                        new SimpleMappingPoint("test2", "fieldA"))
        ));
        CsvOutput fixture = new CsvOutput(config);
        assertThrows(IllegalStateException.class, fixture::end);
        assertThrows(IllegalStateException.class, () -> fixture.write(mappingConfiguration));
        fixture.start();
        assertThrows(IllegalStateException.class, fixture::start);
        mappingConfiguration.parts().get(0).getDataset().collect(new SimpleDatasetPiece(String.format("A1%s%s",
                config.getFieldSeparator(), config.getValueSeparator())));
        mappingConfiguration.parts().get(1).getDataset().collect(new SimpleDatasetPiece("B1"));
        mappingConfiguration.parts().get(2).getDataset().collect(new SimpleDatasetPiece("C1"));
        mappingConfiguration.parts().get(3).getDataset().collect(new SimpleDatasetPiece("D1"));
        fixture.write(mappingConfiguration);
        mappingConfiguration.clear();
        mappingConfiguration.parts().get(0).getDataset().collect(new SimpleDatasetPiece("A2"));
        mappingConfiguration.parts().get(1).getDataset().collect(new SimpleDatasetPiece("B2"));
        mappingConfiguration.parts().get(2).getDataset().collect(new SimpleDatasetPiece("C2"));
        mappingConfiguration.parts().get(3).getDataset().collect(new SimpleDatasetPiece("D2"));
        fixture.write(mappingConfiguration);
        fixture.end();
        Assertions.assertEquals(expected, output.toString());
    }
}