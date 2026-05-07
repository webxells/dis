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

import com.webxells.dis.api.error.InvalidDatasetException;
import com.webxells.dis.base.SimpleDatasetPiece;
import com.webxells.dis.base.SimpleMappingPortrayal;
import com.webxells.dis.base.config.SimpleMappingConfiguration;
import com.webxells.dis.base.config.SimpleMappingPart;
import com.webxells.dis.base.config.SimpleMappingPoint;
import com.webxells.dis.gis.PointDefinition;
import com.webxells.dis.gis.PointType;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MultiToWktTest {

    @Test
    void testBigPotsdamToPolygon() throws InvalidDatasetException, IOException, URISyntaxException {
        SimpleMappingConfiguration config = new SimpleMappingConfiguration();
        config.setParts(List.of(
                new SimpleMappingPart(config,
                        new SimpleMappingPoint("test", "root"),
                        new SimpleMappingPoint("test", "root"))
        ));
        SimpleMappingConfiguration subConfig = new SimpleMappingConfiguration();
        subConfig.setParts(List.of(
                new SimpleMappingPart(subConfig,
                        new SimpleMappingPoint("test", "content"),
                        new SimpleMappingPoint("test", "content")) {{
                    getDataset().collect(new SimpleDatasetPiece(getResourceContent("potsdam.raw")));
                }}
        ));
        ((SimpleMappingPart) config.parts().get(0)).addSubData(subConfig);
        MultiToWkt fixture = new MultiToWkt();
        fixture.setDefinition(new PointDefinition() {{
            setSource(new SimpleMappingPortrayal() {{
                setPath("content");
                setReference("test");
            }});
        }});
        SimpleDatasetPiece piece = new SimpleDatasetPiece(null);
        fixture.manipulate(piece, config.parts().get(0));
        assertEquals(getResourceContent("potsdam.expected"), piece.value().get());
    }

    private String getResourceContent(final String s) throws URISyntaxException, IOException {
        return Files.readString(Paths.get(MultiToWkt.class.getClassLoader().getResource(s).toURI()));
    }

    @Test
    void testSimpleMultipolygon() throws InvalidDatasetException {
        SimpleMappingConfiguration config = new SimpleMappingConfiguration();
        config.setParts(List.of(
                new SimpleMappingPart(config,
                        new SimpleMappingPoint("test", "root"),
                        new SimpleMappingPoint("test", "root"))
        ));
        SimpleMappingConfiguration subConfig1 = new SimpleMappingConfiguration();
        subConfig1.setParts(List.of(
                new SimpleMappingPart(subConfig1,
                        new SimpleMappingPoint("test", "content"),
                        new SimpleMappingPoint("test", "content")) {{
                    getDataset().collect(new SimpleDatasetPiece(
                            "12.559325 52.429005 " +
                                    "12.559313 52.428962 " +
                                    "12.559323 52.428962 " +
                                    "12.559325 52.429005"));
                }}
        ));
        SimpleMappingConfiguration subConfig2 = new SimpleMappingConfiguration();
        subConfig2.setParts(List.of(
                new SimpleMappingPart(subConfig2,
                        new SimpleMappingPoint("test", "content"),
                        new SimpleMappingPoint("test", "content")) {{
                    getDataset().collect(new SimpleDatasetPiece(
                            "13.559325 52.429005 " +
                                    "13.559313 52.428962 " +
                                    "13.559323 52.428962 " +
                                    "13.559325 52.429005"));
                }}
        ));
        ((SimpleMappingPart) config.parts().get(0)).addSubData(subConfig1);
        ((SimpleMappingPart) config.parts().get(0)).addSubData(subConfig2);
        MultiToWkt fixture = new MultiToWkt();
        fixture.setDefinition(new PointDefinition() {{
            setSource(new SimpleMappingPortrayal() {{
                setPath("content");
                setReference("test");
            }});
        }});
        SimpleDatasetPiece piece = new SimpleDatasetPiece(null);
        fixture.manipulate(piece, config.parts().get(0));
        assertEquals("MULTIPOLYGON (((12.559325 52.429005," +
                "12.559313 52.428962," +
                "12.559323 52.428962," +
                "12.559325 52.429005))," +
                "((13.559325 52.429005," +
                "13.559313 52.428962," +
                "13.559323 52.428962," +
                "13.559325 52.429005)))", piece.value().get());
    }

    @Test
    void testPolygonWithDifferentOutputSrsAndWithComma() throws InvalidDatasetException {
        SimpleMappingConfiguration config = new SimpleMappingConfiguration();
        config.setParts(List.of(
                new SimpleMappingPart(config,
                        new SimpleMappingPoint("test", "root"),
                        new SimpleMappingPoint("test", "root"))
        ));
        SimpleMappingConfiguration subConfig = new SimpleMappingConfiguration();
        subConfig.setParts(List.of(
                new SimpleMappingPart(subConfig,
                        new SimpleMappingPoint("test", "content"),
                        new SimpleMappingPoint("test", "content")) {{
                            getDataset().collect(new SimpleDatasetPiece(
                                    "12.559325, 52.429005 " +
                                    "12.559313, 52.428962 " +
                                    "12.559323, 52.428962 " +
                                    "12.559325, 52.429005"));
                }}
        ));
        ((SimpleMappingPart) config.parts().get(0)).addSubData(subConfig);
        MultiToWkt fixture = new MultiToWkt();
        fixture.setDefinition(new PointDefinition() {{
            setSource(new SimpleMappingPortrayal() {{
                setPath("content");
                setReference("test");
            }});
            setOutputSpatialReferenceSystem("EPSG:3857");
            setPointType(PointType.WITH_COMMA);
        }});
        SimpleDatasetPiece piece = new SimpleDatasetPiece(null);
        fixture.manipulate(piece, config.parts().get(0));
        assertEquals("POLYGON ((1398097.6637072305 6878069.885203726," +
                "1398096.327873341 6878062.034801348,1398097.4410682488 6878062.034801348," +
                "1398097.6637072305 6878069.885203726))", piece.value().get());
    }

    @Test
    void testSimplePolygon() throws InvalidDatasetException {
        SimpleMappingConfiguration config = new SimpleMappingConfiguration();
        config.setParts(List.of(
                new SimpleMappingPart(config,
                        new SimpleMappingPoint("test", "root"),
                        new SimpleMappingPoint("test", "root"))
        ));
        SimpleMappingConfiguration subConfig = new SimpleMappingConfiguration();
        subConfig.setParts(List.of(
                new SimpleMappingPart(subConfig,
                        new SimpleMappingPoint("test", "content"),
                        new SimpleMappingPoint("test", "content")) {{
                            getDataset().collect(new SimpleDatasetPiece(
                                    "12.559325 52.429005 " +
                                    "12.559313 52.428962 " +
                                    "12.559323 52.428962 " +
                                    "12.559325 52.429005"));
                }}
        ));
        ((SimpleMappingPart) config.parts().get(0)).addSubData(subConfig);
        MultiToWkt fixture = new MultiToWkt();
        fixture.setDefinition(new PointDefinition() {{
            setSource(new SimpleMappingPortrayal() {{
                setPath("content");
                setReference("test");
            }});
        }});
        SimpleDatasetPiece piece = new SimpleDatasetPiece(null);
        fixture.manipulate(piece, config.parts().get(0));
        assertEquals("POLYGON ((12.559325 52.429005," +
                "12.559313 52.428962," +
                "12.559323 52.428962," +
                "12.559325 52.429005))", piece.value().get());
    }

}