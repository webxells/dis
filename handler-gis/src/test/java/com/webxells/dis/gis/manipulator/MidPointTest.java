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

import com.webxells.dis.api.error.InvalidApi;
import com.webxells.dis.api.error.InvalidDatasetException;
import com.webxells.dis.base.SimpleDatasetPiece;
import com.webxells.dis.base.SimpleMappingPortrayal;
import com.webxells.dis.base.config.SimpleMappingConfiguration;
import com.webxells.dis.base.config.SimpleMappingPart;
import com.webxells.dis.base.config.SimpleMappingPoint;
import com.webxells.dis.gis.PointDefinition;
import com.webxells.dis.gis.PointType;
import com.webxells.dis.test.cases.SimpleTestCase;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MidPointTest extends SimpleTestCase {

    @Test
    void fail() {
        MidPoint fixture = new MidPoint();
        assertThrows(InvalidApi.class, fixture::validate);
        fixture.setPoints(List.of(
                new PointDefinition() {{
                    setSource(new SimpleMappingPortrayal() {{
                        setReference("test");
                        setPath("point1Path");
                    }});
                }}
        ));
        SimpleMappingConfiguration config = new SimpleMappingConfiguration();
        config.setParts(List.of(
                new SimpleMappingPart(config,
                        new SimpleMappingPoint("test", "point1Path"),
                        new SimpleMappingPoint("test", "point1Path")) {{
                    getDataset().collect(new SimpleDatasetPiece("51.2323"));
                }}
        ));
        SimpleDatasetPiece piece = new SimpleDatasetPiece(null);
        assertThrows(InvalidDatasetException.class, () -> fixture.manipulate(piece, config.parts().get(0)));
    }

    @Test
    void test() throws InvalidDatasetException {
        MidPoint fixture = new MidPoint();
        String reference = random("reference");
        String point1Path = random("point1Path");
        String point2Path = random("point2Path");
        String point3Path = random("point3Path");
        assertThrows(InvalidApi.class, fixture::validate);
        fixture.setPoints(List.of(
                new PointDefinition() {{
                    setSource(new SimpleMappingPortrayal() {{
                        setReference(reference);
                        setPath(point1Path);
                    }});
                }},
                new PointDefinition() {{
                    setSource(new SimpleMappingPortrayal() {{
                        setReference(reference);
                        setPointType(PointType.WITH_COMMA);
                        setPath(point2Path);
                    }});
                    setSpatialReferenceSystem("EPSG:3857");
                }},
                new PointDefinition() {{
                    setSource(new SimpleMappingPortrayal() {{
                        setReference(reference);
                        setPointType(PointType.WITH_COMMA);
                        setPath(point3Path);
                    }});
                }}
        ));
        SimpleMappingConfiguration config = new SimpleMappingConfiguration();
        config.setParts(List.of(
                new SimpleMappingPart(config,
                        new SimpleMappingPoint(reference, point1Path),
                        new SimpleMappingPoint(reference, point1Path)) {{
                    getDataset().collect(new SimpleDatasetPiece("51.2323 45.6678"));
                }}, new SimpleMappingPart(config,
                        new SimpleMappingPoint(reference, point2Path),
                        new SimpleMappingPoint(reference, point2Path)) {{
                    getDataset().collect(new SimpleDatasetPiece("1299432.42, 2993595.23"));
                }}, new SimpleMappingPart(config,
                        new SimpleMappingPoint(reference, point3Path),
                        new SimpleMappingPoint(reference, point3Path)) {{
                    getDataset().collect(new SimpleDatasetPiece("21.673,76.9557"));
                }}
        ));
        SimpleDatasetPiece piece = new SimpleDatasetPiece(null);
        fixture.manipulate(piece, config.parts().get(0));
        assertEquals("26.562825008916043 56.3837249948793", piece.value().get());
    }

}