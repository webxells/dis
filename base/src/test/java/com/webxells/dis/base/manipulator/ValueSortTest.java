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

import com.webxells.dis.base.config.SimpleMappingConfiguration;
import com.webxells.dis.test.cases.ConfigurationBuilder;
import com.webxells.dis.test.cases.SimpleTestCase;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ValueSortTest extends SimpleTestCase {

    @Test
    void test() {
        ValueSort fixture = new ValueSort();
        SimpleMappingConfiguration configuration = newConfiguration().addPart(
                ConfigurationBuilder.newPart()
                        .withContent("b", "d")
                        .withContent("a", "0")
                        .withContent("c", "ü")
                )
                .build();
        fixture.manipulate(null, configuration.parts().get(0));

        Assertions.assertEquals(List.of("0", "a", "b", "c", "d", "ü"),
                configuration.parts().get(0).getDataset().getContent().stream()
                        .map(a -> a.value().orElse("failed")).collect(Collectors.toList()));

    }

}