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
package com.webxells.dis.base.manipulator.setter;

import com.webxells.dis.api.DatasetPiece;
import com.webxells.dis.base.SimpleDatasetPiece;
import com.webxells.dis.base.config.SimpleMappingConfiguration;
import com.webxells.dis.base.config.SimpleMappingPart;
import com.webxells.dis.base.config.SimpleMappingPoint;import com.webxells.dis.test.cases.SimpleTestCase;
import com.webxells.dis.test.cases.ConfigurationBuilder;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DeleteTest extends SimpleTestCase {
    @Test
    void test() {
        SimpleMappingConfiguration configuration = new SimpleMappingConfiguration();
        final DatasetPiece left = new SimpleDatasetPiece(random());
        configuration.setParts(List.of(new SimpleMappingPart(configuration, new SimpleMappingPoint(), new SimpleMappingPoint()) {{
            getDataset().collect(List.of(new SimpleDatasetPiece(random()), left));
        }}));
        Delete fixture = new Delete();
        fixture.getValue(configuration.parts().get(0).getDataset().getContent().get(0), configuration.parts().get(0));
        assertEquals(1, configuration.parts().get(0).getDataset().getContent().size());
        fixture.getValue(null, configuration.parts().get(0));
        assertSame(configuration.parts().get(0).getDataset().getContent().get(0), left);
    }
}