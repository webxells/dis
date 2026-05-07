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
package com.webxells.dis.base.manipulator;

import com.webxells.dis.api.config.MappingConfiguration;
import com.webxells.dis.base.SimpleMappingPortrayal;
import com.webxells.dis.test.cases.ConfigurationBuilder;import com.webxells.dis.test.cases.SimpleTestCase;
import com.webxells.dis.test.cases.ConfigurationBuilder;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CopyToOtherPartTest extends SimpleTestCase {

    @Test
    void test() {
        String old = random();
        MappingConfiguration config = newConfiguration()
                .addPart(ConfigurationBuilder.newPart(ConfigurationBuilder.PartBuilder.PartBuilderState.RANDOM)
                        .withContent()
                )
                .addPart(ConfigurationBuilder.newPart(ConfigurationBuilder.PartBuilder.PartBuilderState.RANDOM)
                        .withContent(old)
                )
                .build();
        CopyToOtherPart fixture = new CopyToOtherPart();
        fixture.setOther(SimpleMappingPortrayal.source(config.parts().get(1)));
        fixture.manipulate(config.parts().get(0).getDataset().getContent().get(0), config.parts().get(0));

        assertEquals(2, config.parts().get(1).getDataset().getContent().size());
        assertEquals(old, config.parts().get(1).getDataset().getContent().get(0).value().get());
        assertEquals(config.parts().get(0).getDataset().getContent().get(0),
                config.parts().get(1).getDataset().getContent().get(1));
    }

}