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

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.webxells.dis.api.config.MappingConfiguration;
import com.webxells.dis.test.cases.ConfigurationBuilder;
import com.webxells.dis.test.cases.SimpleTestCase;
import java.util.List;
import org.junit.jupiter.api.Test;

class SplitByLengthTest extends SimpleTestCase {

    @Test
    void test() {
        SplitByLength fixture = new  SplitByLength();
        fixture.setLength(3);
        MappingConfiguration config = newConfiguration()
                .addPart(ConfigurationBuilder.newPart()
                        .withContent("1234567", "1234")
                )
                .build();
        fixture.manipulate(null, config.parts().get(0));
        assertEquals(5, config.parts().get(0).getDataset().getContent().size());
        assertEquals(List.of("123", "456", "7", "123", "4"),
                config.parts().get(0).getDataset().getContent().stream().flatMap(a -> a.value().stream()).toList());

    }

}