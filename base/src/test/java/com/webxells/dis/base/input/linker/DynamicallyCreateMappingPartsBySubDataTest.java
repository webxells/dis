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
package com.webxells.dis.base.input.linker;

import com.webxells.dis.api.error.DisException;
import com.webxells.dis.api.error.InvalidApi;
import com.webxells.dis.api.input.linker.JoinLinker;
import com.webxells.dis.base.SimpleMappingPortrayal;
import com.webxells.dis.base.config.SimpleMappingConfiguration;
import com.webxells.dis.test.cases.ConfigurationBuilder;import com.webxells.dis.test.cases.SimpleTestCase;
import com.webxells.dis.test.cases.ConfigurationBuilder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DynamicallyCreateMappingPartsBySubDataTest extends SimpleTestCase {

    @Test
    void test() throws DisException {
        SimpleMappingConfiguration configuration = newConfiguration()
                .addPart(ConfigurationBuilder.newPart(ConfigurationBuilder.PartBuilder.PartBuilderState.RANDOM))
                .addPart(ConfigurationBuilder.newPart(ConfigurationBuilder.PartBuilder.PartBuilderState.RANDOM)
                        .addSameSubData(newConfiguration()
                                .addPart(ConfigurationBuilder.newPart(ConfigurationBuilder.PartBuilder.PartBuilderState.RANDOM))
                                .build())
                        .addSameSubData(newConfiguration()
                                .addPart(ConfigurationBuilder.newPart()
                                        .withContent(""))
                                .build())
                        .addSameSubData(newConfiguration()
                                .addPart(ConfigurationBuilder.newPart()
                                        .withContent()
                                )
                                .build())
                        .addSameSubData(newConfiguration()
                                .addPart(ConfigurationBuilder.newPart()
                                        .withContent()
                                )
                                .build())
                        .addSameSubData(newConfiguration()
                                .addPart(ConfigurationBuilder.newPart()
                                        .withContent()
                                )
                                .build())
                )
                .build();
        String linkerName = random();
        JoinLinker child = Mockito.mock(JoinLinker.class);
        when(child.getInputName()).thenReturn(linkerName);

        DynamicallyCreateMappingPartsBySubData fixture = new DynamicallyCreateMappingPartsBySubData();
        Assertions.assertThrows(InvalidApi.class, fixture::validate);
        fixture.setChildLinker(child);
        Assertions.assertThrows(InvalidApi.class, fixture::validate);
        fixture.setRootPortrayal(SimpleMappingPortrayal.source(configuration.parts().get(1)));
        Assertions.assertThrows(InvalidApi.class, fixture::validate);
        fixture.setSubDataPortrayalToPathAsValue(SimpleMappingPortrayal.source(configuration.parts().get(1).getSubData().get(0).parts().get(0)));
        fixture.validate();

        fixture.start();
        fixture.getData(configuration);
        fixture.end();

        verify(child, times(1)).start();
        verify(child, times(1)).end();
        verify(child, times(1)).getData(argThat(a -> {
            assertEquals(5, a.parts().size());
            for (int i = 2; i < 5; i++) {
                assertEquals(
                        configuration.parts().get(1).getSubData().get(i).parts().get(0).value().get(),
                        a.parts().get(i).getInput().getPath());
                assertEquals(
                        linkerName,
                        a.parts().get(i).getInput().getReference());
                assertFalse(a.parts().get(i).isStable());
            }
            return true;
        }));
    }

}