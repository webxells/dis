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

import com.webxells.dis.api.MappingPortrayal;
import com.webxells.dis.api.config.MappingPart;
import com.webxells.dis.api.error.InvalidDatasetException;
import com.webxells.dis.base.config.SimpleMappingConfiguration;
import com.webxells.dis.test.cases.ConfigurationBuilder;
import com.webxells.dis.test.cases.SimpleTestCase;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertSame;

class RewriteMappingPointByOppositeForSubDataTest extends SimpleTestCase {

    @Test
    void test() throws InvalidDatasetException {
        RewriteMappingPointByOppositeForSubData fixture = new RewriteMappingPointByOppositeForSubData();
        fixture.setSource(MappingPortrayal.Source.OUTPUT);

        SimpleMappingConfiguration configuration = newConfiguration()
                .addPart(ConfigurationBuilder.newPart(ConfigurationBuilder.PartBuilder.PartBuilderState.RANDOM))
                .addPart(
                        ConfigurationBuilder.newPart(ConfigurationBuilder.PartBuilder.PartBuilderState.RANDOM)
                                .addSubData(
                                        newConfiguration()
                                                .addPart(ConfigurationBuilder.newPart(ConfigurationBuilder.PartBuilder.PartBuilderState.RANDOM))
                                                .addPart(ConfigurationBuilder.newPart(ConfigurationBuilder.PartBuilder.PartBuilderState.RANDOM))
                                                .build()
                                ).addSubData(
                                        newConfiguration()
                                                .addPart(ConfigurationBuilder.newPart(ConfigurationBuilder.PartBuilder.PartBuilderState.RANDOM))
                                                .addPart(ConfigurationBuilder.newPart(ConfigurationBuilder.PartBuilder.PartBuilderState.RANDOM))
                                                .build()
                                ).addSubData(
                                        newConfiguration()
                                                .addPart(ConfigurationBuilder.newPart(ConfigurationBuilder.PartBuilder.PartBuilderState.RANDOM))
                                                .addPart(ConfigurationBuilder.newPart(ConfigurationBuilder.PartBuilder.PartBuilderState.RANDOM))
                                                .build()
                                )
                )
                .addPart(ConfigurationBuilder.newPart(ConfigurationBuilder.PartBuilder.PartBuilderState.RANDOM))
                .build();

        fixture.manipulate(null, configuration.parts().get(1));

        checkSubData(configuration.parts().get(1));
    }

    private void checkSubData(final MappingPart mappingPart) {
        mappingPart.getSubData().stream()
                .flatMap(a -> a.parts().stream())
                .forEach(this::checkPart);
    }

    private void checkPart(final MappingPart a) {
        assertSame(a.getInput(), a.getOutput());
        checkSubData(a);
    }

}