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
package com.webxells.dis.gis.input;

import com.webxells.dis.api.config.MappingConfiguration;
import com.webxells.dis.api.error.DisException;
import com.webxells.dis.test.cases.ConfigurationBuilder;
import com.webxells.dis.test.cases.SimpleTestCase;
import java.io.IOException;
import java.net.URISyntaxException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EsriShpFileTest extends SimpleTestCase {
    @Test
    void testSimple() throws DisException, URISyntaxException, IOException {
        testFirst("simple-test");
        MappingConfiguration mappingConfiguration = newConfiguration()
                .addPart(ConfigurationBuilder.newPart()
                        .setInput("test", "id")
                ).addPart(ConfigurationBuilder.newPart()
                        .setInput("test", "test")
                )
                .build();
        EsriShpFileConfig config = new EsriShpFileConfig();
        config.setFile(getResourceFile("simple-test/simple-test.shp").getAbsolutePath());
        config.setName("test");
        EsriShpFile fixture = new EsriShpFile(config);
        fixture.start();
        assertTrue(fixture.hasNext());
        assertEquals(2, fixture.read(mappingConfiguration));
        assertEquals("1", mappingConfiguration.parts().get(0).value().get());
        assertEquals("first", mappingConfiguration.parts().get(1).value().get());
        assertFalse(fixture.hasNext());
    }

    @Test
    void brandenburg() throws DisException, URISyntaxException, IOException {
        testFirst("brandenburg");
    }

    @Test
    void realLife() throws DisException, URISyntaxException, IOException {
        MappingConfiguration mappingConfiguration = newConfiguration()
                .addPart(ConfigurationBuilder.newPart()
                        .setInput("test", "coordinates")
                ).addPart(ConfigurationBuilder.newPart()
                        .setInput("test", "GEW_ID_NUM")
                )
                .build();
        EsriShpFileConfig config = new EsriShpFileConfig();
        config.setFile(getResourceFile("real-life/real-life.shp").getAbsolutePath());
        config.setName("test");
        config.setShapePathName("coordinates");
        EsriShpFile fixture = new EsriShpFile(config);
        fixture.start();
        assertTrue(fixture.hasNext());
        assertEquals(2, fixture.read(mappingConfiguration));
        assertEquals(new String(getResourceFileStream("real-life-1.expected").readAllBytes()),
                mappingConfiguration.parts().get(0).value().get());
        assertEquals("600000003", mappingConfiguration.parts().get(1).value().get());
        assertTrue(fixture.hasNext());
        mappingConfiguration.clear();
        assertEquals(2, fixture.read(mappingConfiguration));
        assertEquals(new String(getResourceFileStream("real-life-2.expected").readAllBytes()),
                mappingConfiguration.parts().get(0).value().get());
        assertEquals("600000101", mappingConfiguration.parts().get(1).value().get());
        assertTrue(fixture.hasNext());
        mappingConfiguration.clear();
        assertEquals(2, fixture.read(mappingConfiguration));
        assertEquals(new String(getResourceFileStream("real-life-3.expected").readAllBytes()),
                mappingConfiguration.parts().get(0).value().get());
        assertEquals("600000102", mappingConfiguration.parts().get(1).value().get());
        assertTrue(fixture.hasNext());
        for(int i = 3;i < 1001; i++) {
            mappingConfiguration.clear();
            assertEquals(2, fixture.read(mappingConfiguration));
            assertTrue(
                    mappingConfiguration.parts().get(0).value().get().startsWith("POLYGON") ||
                             mappingConfiguration.parts().get(0).value().get().startsWith("MULTIPOLYGON")
            );
            assertTrue(mappingConfiguration.parts().get(1).value().get().length() > 5);
            assertTrue(fixture.hasNext() || 1000 == i);
        }
        assertFalse(fixture.hasNext());
        fixture.end();
    }


    @Test
    void realLifeTransformed() throws DisException, URISyntaxException, IOException {
        MappingConfiguration mappingConfiguration = newConfiguration()
                .addPart(ConfigurationBuilder.newPart()
                        .setInput("test", "coordinates")
                )
                .build();
        EsriShpFileConfig config = new EsriShpFileConfig();
        config.setFile(getResourceFile("real-life/real-life.shp").getAbsolutePath());
        config.setName("test");
        config.setShapePathName("coordinates");
        config.setCharset("UTF-8");
        config.setDateFormat("yyyy-MM-dd HH:mm:ss");
        config.setInputSpatialReferenceSystem("EPSG:25833");
        config.setOutputSpatialReferenceSystem("EPSG:4326");
        EsriShpFile fixture = new EsriShpFile(config);
        fixture.start();
        assertTrue(fixture.hasNext());
        assertEquals(1, fixture.read(mappingConfiguration));
        assertEquals(new String(getResourceFileStream("real-life-1-epsg4326.expected").readAllBytes()),
                mappingConfiguration.parts().get(0).value().get());
        assertTrue(fixture.hasNext());
        mappingConfiguration.clear();
        assertEquals(1, fixture.read(mappingConfiguration));
        assertEquals(new String(getResourceFileStream("real-life-2-epsg4326.expected").readAllBytes()),
                mappingConfiguration.parts().get(0).value().get());
        assertTrue(fixture.hasNext());
        fixture.end();
    }

    void testFirst(String file) throws DisException, URISyntaxException, IOException {
        MappingConfiguration mappingConfiguration = newConfiguration()
                .addPart(ConfigurationBuilder.newPart()
                        .setInput("test", "the_geom")
                )
                .build();
        EsriShpFileConfig config = new EsriShpFileConfig();
        config.setFile(getResourceFile(String.format("%s/%1$s.shp", file)).getAbsolutePath());
        config.setName("test");
        EsriShpFile fixture = new EsriShpFile(config);
        fixture.start();
        assertTrue(fixture.hasNext());
        assertEquals(1, fixture.read(mappingConfiguration));
        assertEquals(new String(getResourceFileStream(String.format("%s.expected", file)).readAllBytes()),
                mappingConfiguration.parts().get(0).value().get());
        assertFalse(fixture.hasNext());
        fixture.end();
    }

}