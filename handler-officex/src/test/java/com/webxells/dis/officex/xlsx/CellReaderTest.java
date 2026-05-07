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
package com.webxells.dis.officex.xlsx;

import com.webxells.dis.api.error.DisException;
import com.webxells.dis.api.error.InputOutputError;
import com.webxells.dis.api.error.InvalidApi;
import com.webxells.dis.api.resource.Resource;
import com.webxells.dis.base.config.SimpleMappingConfiguration;
import com.webxells.dis.base.config.SimpleMappingPoint;
import com.webxells.dis.base.config.StableMappingPart;
import com.webxells.dis.test.cases.ConfigurationBuilder;
import com.webxells.dis.test.cases.SimpleTestCase;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

import static com.webxells.dis.officex.xlsx.OfficeXssfReaderConfig.CellAccessType.EXCEL_REFERENCE;
import static com.webxells.dis.officex.xlsx.OfficeXssfReaderConfig.CellAccessType.INDEX_COUNT;
import static com.webxells.dis.officex.xlsx.OfficeXssfReaderConfig.CellAccessType.VALUE_OF_FIRST_LINE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CellReaderTest extends SimpleTestCase {

    @Test
    void testIndexRowToExcel() {
        assertEquals("A", OfficeXssfReader.createExcelByIndex("1"));
        assertEquals("G", OfficeXssfReader.createExcelByIndex("7"));
        assertEquals("Z", OfficeXssfReader.createExcelByIndex("26"));
        assertEquals("AA", OfficeXssfReader.createExcelByIndex("27"));
        assertEquals("AZ", OfficeXssfReader.createExcelByIndex("52"));
        assertEquals("YZ", OfficeXssfReader.createExcelByIndex("676"));
        assertEquals("ZZ", OfficeXssfReader.createExcelByIndex("702"));
        assertEquals("AAA", OfficeXssfReader.createExcelByIndex("703"));
        assertEquals("FKEEA", OfficeXssfReader.createExcelByIndex("2938703"));
        assertEquals("BBNCU", OfficeXssfReader.createExcelByIndex("958667"));
        assertEquals("FXSHRXW", OfficeXssfReader.createExcelByIndex(String.valueOf(Integer.MAX_VALUE)));
    }

    @Test
    void testMultiWithFirst() throws DisException {
        testMulti(MultiValueStrategy.FIRST);
    }

    @Test
    void testMultiWithLast() throws DisException {
        testMulti(MultiValueStrategy.LAST);
    }

    @Test
    void testMultiWithMerge() throws DisException {
        testMulti(MultiValueStrategy.MERGE);
    }

    void testMulti(final MultiValueStrategy multi) throws InputOutputError {
        CellReaderConfig config = new CellReaderConfig();
        config.setName("test");
        config.setSheetName("sheet-b");
        config.setMultiValueStrategy(multi);
        config.setCellAccessType(VALUE_OF_FIRST_LINE);
        config.setReceiver(new Resource() {
            @Override
            public OutputStream send() {
                return null;
            }

            @Override
            public InputStream receive() {
                return getResourceFileStream("multivalue.xlsx");
            }
        });
        SimpleMappingConfiguration mappingConfiguration = newConfiguration()
                .addPart(new ConfigurationBuilder.PartBuilder()
                        .setInput("test", "multi")
                )
                .build();
        CellReader fixture = new CellReader(config);
        fixture.start();
        assertEquals(config.getName(), fixture.getName());
        assertTrue(fixture.hasNext());
        assertEquals(multi == MultiValueStrategy.MERGE ? 3 : 1, fixture.read(mappingConfiguration));
        switch (multi) {
            case FIRST -> assertEquals("1", getJoinStringForMulti(mappingConfiguration));
            case MERGE -> assertEquals("1,2,3", getJoinStringForMulti(mappingConfiguration));
            case LAST -> assertEquals("3", getJoinStringForMulti(mappingConfiguration));
        }
        mappingConfiguration.clear();
        assertTrue(fixture.hasNext());
        assertEquals(multi == MultiValueStrategy.MERGE ? 3 : 1, fixture.read(mappingConfiguration));
        switch (multi) {
            case FIRST -> assertEquals("4", getJoinStringForMulti(mappingConfiguration));
            case MERGE -> assertEquals("4,5,6", getJoinStringForMulti(mappingConfiguration));
            case LAST -> assertEquals("6", getJoinStringForMulti(mappingConfiguration));
        }
        assertFalse(fixture.hasNext());
        fixture.end();
    }

    private String getJoinStringForMulti(final SimpleMappingConfiguration mappingConfiguration) {
        return mappingConfiguration.parts().get(0).getDataset().getContent().stream()
                .flatMap(a -> a.value().stream()).collect(Collectors.joining(","));
    }

    @Test
    void testValidatingFailing() {
        CellReader fixture = new CellReader(new CellReaderConfig());
        assertThrows(InvalidApi.class, fixture::validate);
    }

    @Test
    void testStartingFailing() {
        CellReaderConfig config = new CellReaderConfig();
        config.setReceiver(new Resource() {
            @Override
            public OutputStream send() {
                return null;
            }

            @Override
            public InputStream receive() {
                return new ByteArrayInputStream("asd".getBytes());
            }

            @Override
            public String getType() {
                return null;
            }
        });
        CellReader fixture = new CellReader(config);
        assertThrows(InputOutputError.class, fixture::start);
    }

    @Test
    void testWithExcelNames() throws DisException {
        test(EXCEL_REFERENCE, Map.of(
                "A", "A",
                "B", "B",
                "C", "C",
                "D", "D",
                "E", "E"
        ));
    }

    @Test
    void testWithIndexNames() throws DisException {
        test(INDEX_COUNT, Map.of(
                "A", "1",
                "B", "2",
                "C", "3",
                "D", "4",
                "E", "5"
        ));
    }

    @Test
    void testFirstRowNamesLibreOffice() throws DisException {
        test(VALUE_OF_FIRST_LINE, Map.of(
                "A", "CellA",
                "B", "CellB",
                "C", "CellC",
                "D", "CellD",
                "E", "Number"
        ));
    }

    @Test
    void testFirstRowNamesFreeOffice() throws DisException {
        test(VALUE_OF_FIRST_LINE, Map.of(
                "A", "CellA",
                "B", "CellB",
                "C", "CellC",
                "D", "CellD",
                "E", "Number"
        ), "freeoffice");
    }

    @Test
    void testFirstRowNamesMsOffice() throws DisException {
        test(VALUE_OF_FIRST_LINE, Map.of(
                "A", "CellA",
                "B", "CellB",
                "C", "CellC",
                "D", "CellD",
                "E", "Number"
        ), "msoffice");
    }

    private void test(OfficeXssfReaderConfig.CellAccessType cellAccessType,
                      Map<String, String> accessMapping) throws DisException {
        test(cellAccessType, accessMapping, "libreoffice");
    }

    private void test(OfficeXssfReaderConfig.CellAccessType cellAccessType,
                      Map<String, String> accessMapping, String file) throws DisException {
        CellReaderConfig config = new CellReaderConfig();
        config.setSkipLines(VALUE_OF_FIRST_LINE.equals(cellAccessType) ? 0 : 2);
        config.setName("test");
        config.setCellAccessType(cellAccessType);
        config.setSheetName("sheet-a");
        config.setReceiver(new Resource() {
            @Override
            public OutputStream send() throws InputOutputError {
                return null;
            }

            @Override
            public InputStream receive() throws InputOutputError {
                return CellReaderTest.class.getClassLoader()
                        .getResourceAsStream(String.format("test-xssf-%s.xlsx", file));
            }

            @Override
            public String getType() {
                return null;
            }
        });
        SimpleMappingConfiguration mappingConfiguration = new SimpleMappingConfiguration();
        mappingConfiguration.setParts(List.of(
                new StableMappingPart(mappingConfiguration,
                        new SimpleMappingPoint(
                                "test", accessMapping.get("A")
                        ),
                        new SimpleMappingPoint(
                                random(), accessMapping.get("A")
                        )
                ), new StableMappingPart(mappingConfiguration,
                        new SimpleMappingPoint(
                                random(), accessMapping.get("D")
                        ),
                        new SimpleMappingPoint(
                                random(), accessMapping.get("A")
                        )
                ), new StableMappingPart(mappingConfiguration,
                        new SimpleMappingPoint(
                                "test", accessMapping.get("C")
                        ),
                        new SimpleMappingPoint(
                                random(), accessMapping.get("C")
                        )
                ), new StableMappingPart(mappingConfiguration,
                        new SimpleMappingPoint(
                                "test", accessMapping.get("E")
                        ),
                        new SimpleMappingPoint(
                                random(), accessMapping.get("E")
                        )
                )
        ));
        CellReader fixture = new CellReader(config);
        fixture.validate();
        fixture.start();
        assertEquals(config.getName(), fixture.getName());
        assertTrue(fixture.hasNext());
        if (VALUE_OF_FIRST_LINE == cellAccessType) {
            fixture.read(mappingConfiguration);
            mappingConfiguration.clear();
        }
        assertEquals(3, fixture.read(mappingConfiguration));
        assertEquals("A3", mappingConfiguration.parts().get(0).value().get());
        assertEquals("C3", mappingConfiguration.parts().get(2).value().get());
        assertEquals("1345234.4954535", mappingConfiguration.parts().get(3).value().get());
        mappingConfiguration.clear();
        assertTrue(fixture.hasNext());
        assertEquals(3, fixture.read(mappingConfiguration));
        assertEquals("a3", mappingConfiguration.parts().get(0).value().get());
        assertEquals("c3", mappingConfiguration.parts().get(2).value().get());
        assertEquals("565656.99", mappingConfiguration.parts().get(3).value().get());
        mappingConfiguration.clear();
        assertTrue(fixture.hasNext());
        assertEquals(1, fixture.read(mappingConfiguration));
        assertEquals("5", mappingConfiguration.parts().get(3).value().get());
        assertFalse(fixture.hasNext());
        mappingConfiguration.clear();
        fixture.end();
    }
}