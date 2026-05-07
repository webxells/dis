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

import com.webxells.dis.api.config.MappingPart;
import com.webxells.dis.base.SimpleMappingPortrayal;
import com.webxells.dis.base.config.SimpleMappingConfiguration;
import com.webxells.dis.test.cases.ConfigurationBuilder;import com.webxells.dis.test.cases.SimpleTestCase;
import com.webxells.dis.test.cases.ConfigurationBuilder;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ValueMergeTest extends SimpleTestCase {

    @Test
    void test() {
        SimpleMappingConfiguration configuration = newConfiguration()
                .addPart(ConfigurationBuilder.newPart(ConfigurationBuilder.PartBuilder.PartBuilderState.RANDOM)
                        .withContent(random(), random(), random()))
                .addPart(ConfigurationBuilder.newPart(ConfigurationBuilder.PartBuilder.PartBuilderState.RANDOM)
                        .withContent(random(), random()))
                .addPart(ConfigurationBuilder.newPart(ConfigurationBuilder.PartBuilder.PartBuilderState.RANDOM)
                        .withContent(random(), random(), random()))
                .addPart(ConfigurationBuilder.newPart(ConfigurationBuilder.PartBuilder.PartBuilderState.RANDOM)
                        .withContent(random(), random(), random()))
                .build();
        List<String> result = new LinkedList<>();
        result.addAll(partToStringList(configuration.parts().get(1)));
        result.addAll(partToStringList(configuration.parts().get(0)));
        result.addAll(partToStringList(configuration.parts().get(2)));

        ValueMerge fixture = new ValueMerge();
        fixture.setMain(SimpleMappingPortrayal.destination(configuration.parts().get(1)));
        fixture.setSubsets(List.of(
                SimpleMappingPortrayal.source(configuration.parts().get(0)),
                SimpleMappingPortrayal.destination(configuration.parts().get(2))
        ));

        fixture.manipulate(null, configuration.parts().get(1));

        assertEquals(result,
                configuration.parts().get(1).getDataset().getContent().stream().map(a -> a.value().get()).collect(Collectors.toList()));

    }

    private List<String> partToStringList(final MappingPart mappingPart) {
        return mappingPart.getDataset().getContent().stream().map(a -> a.value().get()).collect(Collectors.toList());
    }


}