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
import com.webxells.dis.test.cases.ConfigurationBuilder;
import com.webxells.dis.test.cases.ConfigurationBuilder.PartBuilder.PartBuilderState;import com.webxells.dis.test.cases.SimpleTestCase;
import com.webxells.dis.test.cases.ConfigurationBuilder;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ConcatenationSingleCallTest extends SimpleTestCase {

    @Test
    void test() {
        ConcatenationSingleCall fixture = new ConcatenationSingleCall();

        SimpleMappingConfiguration configuration = newConfiguration()
                .addPart(ConfigurationBuilder.newPart(PartBuilderState.RANDOM)
                        .withContent(random(), random(), random())
                )
                .addPart(ConfigurationBuilder.newPart(PartBuilderState.RANDOM)
                        .withContent(random(), random(), random())
                )
                .addPart(ConfigurationBuilder.newPart(PartBuilderState.RANDOM)
                        .withContent(random(), random(), random())
                )
                .build();

        final String delimiter = random("delimiter");
        String expected = String.format("%2$s%1$s%3$s%1$s%4$s%1$s%5$s%1$s%6$s%1$s%7$s%1$s%8$s%1$s%9$s%1$s%10$s", delimiter,
                configuration.parts().get(1).getDataset().getContent().get(0).value().get(),
                configuration.parts().get(1).getDataset().getContent().get(1).value().get(),
                configuration.parts().get(1).getDataset().getContent().get(2).value().get(),
                configuration.parts().get(0).getDataset().getContent().get(0).value().get(),
                configuration.parts().get(0).getDataset().getContent().get(1).value().get(),
                configuration.parts().get(0).getDataset().getContent().get(2).value().get(),
                configuration.parts().get(2).getDataset().getContent().get(0).value().get(),
                configuration.parts().get(2).getDataset().getContent().get(1).value().get(),
                configuration.parts().get(2).getDataset().getContent().get(2).value().get());

        fixture.setDelimiter(delimiter);
        fixture.setPieces(List.of(SimpleMappingPortrayal.source(configuration.parts().get(0)),
                SimpleMappingPortrayal.destination(configuration.parts().get(2))));

        fixture.manipulate(null , configuration.parts().get(1));
        assertEquals(expected, configuration.parts().get(1).value().get());

        fixture.setSkipSelf(true);

        expected = String.format("%2$s%1$s%3$s%1$s%4$s%1$s%5$s%1$s%6$s%1$s%7$s", delimiter,
                configuration.parts().get(0).getDataset().getContent().get(0).value().get(),
                configuration.parts().get(0).getDataset().getContent().get(1).value().get(),
                configuration.parts().get(0).getDataset().getContent().get(2).value().get(),
                configuration.parts().get(2).getDataset().getContent().get(0).value().get(),
                configuration.parts().get(2).getDataset().getContent().get(1).value().get(),
                configuration.parts().get(2).getDataset().getContent().get(2).value().get());

        fixture.manipulate(null , configuration.parts().get(1));
        assertEquals(expected, configuration.parts().get(1).value().get());

    }

}