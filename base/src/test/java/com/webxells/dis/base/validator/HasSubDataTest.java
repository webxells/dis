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

import com.webxells.dis.api.config.MappingConfiguration;
import com.webxells.dis.base.SimpleDatasetPiece;
import com.webxells.dis.base.SimpleMappingPortrayal;
import com.webxells.dis.test.cases.ConfigurationBuilder;import com.webxells.dis.test.cases.SimpleTestCase;
import com.webxells.dis.test.cases.ConfigurationBuilder;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HasSubDataTest extends SimpleTestCase {

    @Test
    void testRequireValues() {
        HasSubData fixture = new HasSubData();
        fixture.setRequireValues(true);
        MappingConfiguration subData = newConfiguration()
                .addPart(ConfigurationBuilder.newPart(ConfigurationBuilder.PartBuilder.PartBuilderState.RANDOM))
                .addPart(ConfigurationBuilder.newPart())
                .build();
        MappingConfiguration configuration = newConfiguration()
                .addPart(ConfigurationBuilder.newPart(ConfigurationBuilder.PartBuilder.PartBuilderState.RANDOM))
                .addPart(ConfigurationBuilder.newPart())
                .build();
        assertFalse(fixture.validate(null, configuration.parts().get(0)));
        assertFalse(fixture.validate(null, configuration.parts().get(1)));
        configuration.parts().get(0).getSubData().add(subData);
        assertFalse(fixture.validate(null, configuration.parts().get(0)));
        assertFalse(fixture.validate(null, configuration.parts().get(1)));
        fixture.setField(SimpleMappingPortrayal.destination(configuration.parts().get(0)));
        assertFalse(fixture.validate(null, configuration.parts().get(1)));
        subData.parts().get(1).getDataset().collect(new SimpleDatasetPiece(random()));
        assertTrue(fixture.validate(null, configuration.parts().get(1)));
    }

    @Test
    void test() {
        HasSubData fixture = new HasSubData();
        MappingConfiguration configuration = newConfiguration()
                .addPart(ConfigurationBuilder.newPart(ConfigurationBuilder.PartBuilder.PartBuilderState.RANDOM))
                .addPart(ConfigurationBuilder.newPart())
                .build();
        assertFalse(fixture.validate(null, configuration.parts().get(0)));
        assertFalse(fixture.validate(null, configuration.parts().get(1)));
        configuration.parts().get(0).getSubData().add(configuration);
        assertTrue(fixture.validate(null, configuration.parts().get(0)));
        assertFalse(fixture.validate(null, configuration.parts().get(1)));
        fixture.setField(SimpleMappingPortrayal.destination(configuration.parts().get(0)));
        assertTrue(fixture.validate(null, configuration.parts().get(1)));
    }

}