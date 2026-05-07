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

import com.webxells.dis.api.DatasetPiece;
import com.webxells.dis.api.error.InvalidDatasetException;
import com.webxells.dis.base.SimpleDatasetPiece;
import com.webxells.dis.base.SimpleMappingPortrayal;
import com.webxells.dis.base.config.SimpleMappingConfiguration;
import com.webxells.dis.base.config.SimpleMappingPart;
import com.webxells.dis.base.config.SimpleMappingPoint;
import com.webxells.dis.gis.PointDefinition;
import com.webxells.dis.gis.PointType;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConvertPointTest {

    @Test
    void test() throws InvalidDatasetException {
        ConvertPoint fixture = new ConvertPoint();
        DatasetPiece piece = new SimpleDatasetPiece("50.958427, 11.601563");
        fixture.setPointDefinition(new PointDefinition() {{
            setSpatialReferenceSystem("EPSG:4326");
            setPointType(PointType.WITH_COMMA_SWAPPED);
            setOutputSpatialReferenceSystem("EPSG:3857");
            setOutputPointType(PointType.WITH_SPACE_SWAPPED);
        }});
        fixture.manipulate(piece, null);
        assertEquals("6613943.232350362 1291480.0855660834", piece.value().get());
    }

    @Test
    void testWithMappingPortrayal() throws InvalidDatasetException {
        ConvertPoint fixture = new ConvertPoint();
        SimpleMappingConfiguration config = new SimpleMappingConfiguration();
        config.setParts(List.of(
                new SimpleMappingPart(config, new SimpleMappingPoint("test", "source"), new SimpleMappingPoint("test",
                        "test")) {{
                    getDataset().collect(new SimpleDatasetPiece("11.601563,50.958427"));
                }},
                new SimpleMappingPart(config, new SimpleMappingPoint("test", "destination"), new SimpleMappingPoint(
                        "test",
                        "test"))
        ));
        DatasetPiece piece = new SimpleDatasetPiece(null);
        fixture.setPointDefinition(new PointDefinition() {{
            setSource(new SimpleMappingPortrayal() {{
                setReference("test");
                setPath("source");
            }});
            setDestination(new SimpleMappingPortrayal() {{
                setReference("test");
                setPath("destination");
            }});
            setSpatialReferenceSystem("EPSG:4326");
            setPointType(PointType.WITH_COMMA);
            setOutputSpatialReferenceSystem("EPSG:3857");
            setOutputPointType(PointType.WITH_SPACE_SWAPPED);
        }});
        fixture.manipulate(piece, config.parts().get(0));
        assertTrue(piece.value().isEmpty());
        assertEquals("6613943.232350362 1291480.0855660834", config.parts().get(1).value().get());
    }

}