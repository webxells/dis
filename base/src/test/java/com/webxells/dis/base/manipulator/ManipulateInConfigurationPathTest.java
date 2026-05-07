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

import com.webxells.dis.api.DatasetPiece;
import com.webxells.dis.api.config.MappingPoint;
import com.webxells.dis.api.error.InvalidApi;
import com.webxells.dis.api.error.InvalidDatasetException;
import com.webxells.dis.api.manipulator.Manipulator;
import com.webxells.dis.base.SimpleDatasetPiece;
import com.webxells.dis.base.SimpleMappingPortrayal;
import com.webxells.dis.base.config.SimpleMappingConfiguration;
import com.webxells.dis.base.config.SimpleMappingPoint;
import com.webxells.dis.test.cases.ConfigurationBuilder;import com.webxells.dis.test.cases.SimpleTestCase;
import com.webxells.dis.test.cases.ConfigurationBuilder;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class ManipulateInConfigurationPathTest extends SimpleTestCase {

    @Test
    void test() throws InvalidApi, InvalidDatasetException {
        DatasetPiece manipulatorInput = new SimpleDatasetPiece(random());
        MappingPoint portrayal = new SimpleMappingPoint(random(), random());
        MappingPoint subPortrayal = new SimpleMappingPoint(random(), random());
        final SimpleMappingConfiguration configuration = newConfiguration()
                .addPart(ConfigurationBuilder.newPart()
                        .setInput(portrayal)
                        .withContent()
                )
                .addPart(ConfigurationBuilder.newPart(ConfigurationBuilder.PartBuilder.PartBuilderState.RANDOM)
                        .addSubData(newConfiguration()
                                .addPart(ConfigurationBuilder.newPart()
                                        .setInput(subPortrayal)
                                        .addSubData(newConfiguration()
                                                .addPart(ConfigurationBuilder.newPart(ConfigurationBuilder.PartBuilder.PartBuilderState.RANDOM))
                                                .build())
                                )
                                .build())
                )
                .build();
        ManipulateInConfigurationPath fixture = new ManipulateInConfigurationPath();
        Manipulator manipulator = Mockito.mock(Manipulator.class);
        fixture.setManipulator(manipulator);
        fixture.setPath(List.of(new ManipulateInConfigurationPath.PathDefinition() {{
            pathType = ManipulateInConfigurationPath.Path.PARENT;
            destination = new SimpleMappingPortrayal(subPortrayal.getReference(), subPortrayal.getPath());
        }}, new ManipulateInConfigurationPath.PathDefinition() {{
            pathType = ManipulateInConfigurationPath.Path.PARENT;
            destination = new SimpleMappingPortrayal(portrayal.getReference(), portrayal.getPath());
        }}));
        fixture.validate();

        Mockito.verify(manipulator, Mockito.times(1)).validate();

        fixture.manipulate(manipulatorInput,
                configuration.parts().get(1).getSubData().get(0).parts().get(0).getSubData().get(0).parts().get(0));

        Mockito.verify(manipulator, Mockito.times(1))
                .manipulate(manipulatorInput, configuration.parts().get(0));

        fixture.setPath(List.of(new ManipulateInConfigurationPath.PathDefinition() {{
            pathType = ManipulateInConfigurationPath.Path.CURRENT;
            destination = new SimpleMappingPortrayal(portrayal.getReference(), portrayal.getPath());
        }}));

        fixture.manipulate(manipulatorInput, configuration.parts().get(1));
        Mockito.verify(manipulator, Mockito.times(2))
                .manipulate(manipulatorInput, configuration.parts().get(0));

    }

}