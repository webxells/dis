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

import com.webxells.dis.base.SimpleDatasetPiece;
import com.webxells.dis.base.SimpleMappingPortrayal;
import com.webxells.dis.base.config.SimpleMappingConfiguration;
import com.webxells.dis.test.cases.ConfigurationBuilder;
import com.webxells.dis.test.cases.SimpleTestCase;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class FilterInSubDataCopyPartTest extends SimpleTestCase {

    @Test
    void test() {
        SimpleMappingConfiguration subSubConfig = newConfiguration()
                .addPart(ConfigurationBuilder.newPart(ConfigurationBuilder.PartBuilder.PartBuilderState.RANDOM)
                        .withContent("value1")
                ).addPart(ConfigurationBuilder.newPart(ConfigurationBuilder.PartBuilder.PartBuilderState.RANDOM)
                        .withContent("value2")
                ).addPart(ConfigurationBuilder.newPart(ConfigurationBuilder.PartBuilder.PartBuilderState.RANDOM)
                        .withContent("value3")
                )
                .build();
        SimpleMappingConfiguration subConfig = newConfiguration()
                .addPart(ConfigurationBuilder.newPart(ConfigurationBuilder.PartBuilder.PartBuilderState.RANDOM)
                        .addSubData(subSubConfig)
                )
                .build();
        SimpleMappingConfiguration config  = newConfiguration()
                .addPart(ConfigurationBuilder.newPart(ConfigurationBuilder.PartBuilder.PartBuilderState.RANDOM)
                        .addSubData(subConfig)
                )
                .build();
        FilterInSubDataCopyPart fixture = new FilterInSubDataCopyPart();
        fixture.setRoot(List.of(
                SimpleMappingPortrayal.source(config.parts().get(0)),
                SimpleMappingPortrayal.source(subConfig.parts().get(0))
        ));
        fixture.setFilter(SimpleMappingPortrayal.source(subSubConfig.parts().get(1)));
        fixture.setResult(SimpleMappingPortrayal.source(subSubConfig.parts().get(2)));

        SimpleDatasetPiece piece = new SimpleDatasetPiece(subSubConfig.parts().get(1).value().get());
        fixture.manipulate(piece, config.parts().get(0));

        Assertions.assertEquals(subSubConfig.parts().get(1).value().get(), piece.value().get());

        piece = new SimpleDatasetPiece(subSubConfig.parts().get(1).value().get());
        subSubConfig.parts().get(1).getDataset().getContent().get(0).rewriteValue(random("other value"));
        fixture.manipulate(piece, config.parts().get(0));

        Assertions.assertFalse(piece.value().isEmpty());
    }

}