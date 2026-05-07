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

import com.webxells.dis.api.config.MappingConfiguration;
import com.webxells.dis.plain.output.EchoConfig;
import com.webxells.dis.test.cases.ConfigurationBuilder;import com.webxells.dis.test.cases.SimpleTestCase;
import com.webxells.dis.test.cases.ConfigurationBuilder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class OutputSetterTest extends SimpleTestCase {

    @Test
    void test() {
        MappingConfiguration config = newConfiguration()
                .addPart(ConfigurationBuilder.newPart(ConfigurationBuilder.PartBuilder.PartBuilderState.RANDOM))
                .addPart(ConfigurationBuilder.newPart(ConfigurationBuilder.PartBuilder.PartBuilderState.RANDOM)
                        .withContent()
                )
                .build();


        String staticRandom = random();
        EchoConfig outputConfig = new EchoConfig();
        outputConfig.setName(random());
        outputConfig.setTemplate(String.format("%s $%s", staticRandom, config.parts().get(1).getOutput().getPath()));
        OutputSetter fixture = new OutputSetter();
        fixture.setOutput(outputConfig);
        fixture.setSkipReferenceValidation(true);

        Assertions.assertEquals(String.format("%s %s", staticRandom, config.parts().get(1).value().get()),
                fixture.getValue(null, config.parts().get(0)));
    }

}
