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
import com.webxells.dis.api.error.InvalidApi;
import com.webxells.dis.api.error.InvalidDatasetException;
import com.webxells.dis.base.SimpleMappingPortrayal;
import com.webxells.dis.base.config.SimpleMappingConfiguration;
import com.webxells.dis.test.cases.ConfigurationBuilder;
import com.webxells.dis.test.cases.SimpleTestCase;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class AddToSubDataTest extends SimpleTestCase {

    @Test
    void test() throws InvalidDatasetException, InvalidApi {
        SimpleMappingConfiguration subData = newConfiguration()
                .addPart(ConfigurationBuilder.newPart(ConfigurationBuilder.PartBuilder.PartBuilderState.RANDOM))
                .addPart(ConfigurationBuilder.newPart(ConfigurationBuilder.PartBuilder.PartBuilderState.RANDOM))
                .build();
        SimpleMappingConfiguration configuration = newConfiguration()
                .addPart(ConfigurationBuilder.newPart(ConfigurationBuilder.PartBuilder.PartBuilderState.RANDOM))
                .addPart(ConfigurationBuilder.newPart(ConfigurationBuilder.PartBuilder.PartBuilderState.RANDOM)
                        .withContent(random(), random()))
                .addPart(ConfigurationBuilder.newPart(ConfigurationBuilder.PartBuilder.PartBuilderState.RANDOM))
                .build();

        configuration.parts().get(0).getSubData().add(subData);

        MappingConfiguration configuration2 = configuration.copy();
        AddToSubData fixture = new AddToSubData();

        List<String> staticData1 = List.of(random(), random());
        List<String> staticData2 = List.of(random(), random());

        fixture.setAdd(List.of(
                new AddToSubData.SubDataRow() {{
                    staticValues = List.of(new AddToSubData.StaticValues() {{
                        destination = SimpleMappingPortrayal.source(subData.parts().get(1));
                        data = staticData1;
                    }});
                }},
                new AddToSubData.SubDataRow() {{
                    staticValues = List.of(new AddToSubData.StaticValues() {{
                        destination = SimpleMappingPortrayal.source(subData.parts().get(0));
                        data = staticData2;
                    }});
                    map = List.of(new AddToSubData.MapData() {{
                        destination = SimpleMappingPortrayal.source(subData.parts().get(1));
                        data = SimpleMappingPortrayal.destination(configuration.parts().get(1));
                    }});
                }},
                new AddToSubData.SubDataRow() {{
                    map = List.of(new AddToSubData.MapData() {{
                        destination = SimpleMappingPortrayal.source(subData.parts().get(0));
                        data = SimpleMappingPortrayal.destination(configuration.parts().get(1));
                    }});
                }}
        ));

        fixture.validate();

        fixture.manipulate(null, configuration.parts().get(0));

        assertSubData(configuration, staticData1, staticData2);

        fixture.setField(SimpleMappingPortrayal.destination(configuration2.parts().get(0)));
        fixture.manipulate(null, configuration2.parts().get(1));

        assertSubData(configuration2, staticData1, staticData2);
    }

    private void assertSubData(final MappingConfiguration configuration, final List<String> staticData1, final List<String> staticData2) {
        Assertions.assertEquals(staticData1.get(0),
                configuration.parts().get(0).getSubData().get(0).parts().get(1).getDataset().getContent().get(0).value().get());
        Assertions.assertEquals(staticData1.get(1),
                configuration.parts().get(0).getSubData().get(0).parts().get(1).getDataset().getContent().get(1).value().get());

        Assertions.assertEquals(staticData2.get(0),
                configuration.parts().get(0).getSubData().get(1).parts().get(0).getDataset().getContent().get(0).value().get());
        Assertions.assertEquals(staticData2.get(1),
                configuration.parts().get(0).getSubData().get(1).parts().get(0).getDataset().getContent().get(1).value().get());
        Assertions.assertEquals(configuration.parts().get(1).getDataset().getContent().get(0).value().get(),
                configuration.parts().get(0).getSubData().get(1).parts().get(1).getDataset().getContent().get(0).value().get());
        Assertions.assertEquals(configuration.parts().get(1).getDataset().getContent().get(1).value().get(),
                configuration.parts().get(0).getSubData().get(1).parts().get(1).getDataset().getContent().get(1).value().get());

        Assertions.assertEquals(configuration.parts().get(1).getDataset().getContent().get(0).value().get(),
                configuration.parts().get(0).getSubData().get(2).parts().get(0).getDataset().getContent().get(0).value().get());
        Assertions.assertEquals(configuration.parts().get(1).getDataset().getContent().get(1).value().get(),
                configuration.parts().get(0).getSubData().get(2).parts().get(0).getDataset().getContent().get(1).value().get());
    }

}