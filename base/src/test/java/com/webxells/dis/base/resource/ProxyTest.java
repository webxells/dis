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
package com.webxells.dis.base.resource;

import com.webxells.dis.api.config.MappingConfiguration;
import com.webxells.dis.test.cases.ConfigurationBuilder;
import com.webxells.dis.test.cases.SimpleTestCase;
import java.io.IOException;
import org.junit.jupiter.api.Test;

import static com.webxells.dis.test.cases.ConfigurationBuilder.PartBuilder.PartBuilderState.RANDOM;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProxyTest extends SimpleTestCase {
    @Test
    void test() throws IOException {
        String index = randomUnique();
        String otherIndex = randomUnique();
        Proxy fixture = new Proxy();
        fixture.setIndex(index);
        fixture.setOverwriteType(Proxy.OverwriteType.ERROR);
        fixture.setNotFoundStrategy(Proxy.NotFoundStrategy.ERROR);
        MappingConfiguration configuration = newConfiguration()
                .addPart(ConfigurationBuilder.newPart(RANDOM)
                        .withContent()
                )
                .build();

        assertTrue(fixture.validate(configuration.parts().get(0).getDataset().getContent().get(0),
                configuration.parts().get(0)));
        assertEquals(configuration.parts().get(0).value().get(), new String(fixture.receive().readAllBytes()));
    }

}