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
package com.webxells.dis.base.validator;

import com.webxells.dis.base.SimpleDatasetPiece;
import com.webxells.dis.base.SimpleMappingPortrayal;
import com.webxells.dis.base.config.SimpleMappingConfiguration;
import com.webxells.dis.base.config.SimpleMappingPart;
import com.webxells.dis.base.config.SimpleMappingPoint;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class EqualsFieldTest extends IsSetTest {

    @Test
    void testEqualsFields() {
        String reference1 = random("reference");
        String path1 = random("path");
        String reference2 = random("reference");
        String path2 = random("path");
        String reference3 = random("reference");
        String path3 = random("path");
        String value1 = random("value1");
        String value2 = random("value2");
        SimpleMappingConfiguration config = new SimpleMappingConfiguration();
        config.setParts(List.of(
                new SimpleMappingPart(config, new SimpleMappingPoint(reference1, path1), new SimpleMappingPoint()) {{
                    getDataset().collect(new SimpleDatasetPiece(value1));
                }},
                new SimpleMappingPart(config, new SimpleMappingPoint(reference2, path2), new SimpleMappingPoint()) {{
                    getDataset().collect(new SimpleDatasetPiece(value2));
                }},
                new SimpleMappingPart(config, new SimpleMappingPoint(reference3, path3), new SimpleMappingPoint())
        ));

        EqualsField fixture = new EqualsField();
        fixture.setField(new SimpleMappingPortrayal(reference1, path1));

        Assertions.assertTrue(fixture.validate(
                new SimpleDatasetPiece(value1), config.parts().get(0)));
        Assertions.assertFalse(fixture.validate(
                new SimpleDatasetPiece(value2), config.parts().get(0)));
        fixture.setField(new SimpleMappingPortrayal(reference1, path2));
        Assertions.assertFalse(fixture.validate(
                new SimpleDatasetPiece(value1), config.parts().get(0)));
        Assertions.assertFalse(fixture.validate(
                new SimpleDatasetPiece(value2), config.parts().get(0)));
        fixture.setField(new SimpleMappingPortrayal(reference3, path3));
        Assertions.assertFalse(fixture.validate(
                new SimpleDatasetPiece(value1), config.parts().get(0)));
        Assertions.assertFalse(fixture.validate(
                new SimpleDatasetPiece(value2), config.parts().get(0)));

    }

}