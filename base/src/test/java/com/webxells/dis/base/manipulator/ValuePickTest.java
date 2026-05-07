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

import com.webxells.dis.base.SimpleMappingPortrayal;
import com.webxells.dis.base.config.SimpleMappingConfiguration;
import com.webxells.dis.test.cases.ConfigurationBuilder;import com.webxells.dis.test.cases.SimpleTestCase;
import com.webxells.dis.test.cases.ConfigurationBuilder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ValuePickTest extends SimpleTestCase {

    @Test
    void test() {
        String value = random("first");
        String value2 = random("second");
        String value3 = random("third");
        SimpleMappingConfiguration config = newConfiguration()
                .addPart(new ConfigurationBuilder.PartBuilder(ConfigurationBuilder.PartBuilder.PartBuilderState.RANDOM)
                        .withContent(value, value2, value3)
                )
                .addPart(new ConfigurationBuilder.PartBuilder(ConfigurationBuilder.PartBuilder.PartBuilderState.RANDOM)
                        .withContent(value, value2, value3)
                )
                .build();
        ValuePick fixture = new ValuePick();
        fixture.setStart(2);
        fixture.setLength(-1);
        fixture.manipulate(null, config.parts().get(0));
        Assertions.assertEquals(1, config.parts().get(0).getDataset().getContent().size());
        Assertions.assertEquals(value3, config.parts().get(0).value().get());
        fixture.setMain(SimpleMappingPortrayal.destination(config.parts().get(1)));
        fixture.setStart(-2);
        fixture.setLength(1);
        fixture.manipulate(null, config.parts().get(0));
        Assertions.assertEquals(1, config.parts().get(0).getDataset().getContent().size());
        Assertions.assertEquals(value2, config.parts().get(0).value().get());
        fixture.setStart(100);
        fixture.manipulate(null, config.parts().get(0));
        Assertions.assertEquals(0, config.parts().get(0).getDataset().getContent().size());
        fixture.setErrorStrategy(ValuePick.ErrorStrategy.ERROR);
        Assertions.assertThrows(IndexOutOfBoundsException.class, () -> fixture.manipulate(null, config.parts().get(0)));
    }

}