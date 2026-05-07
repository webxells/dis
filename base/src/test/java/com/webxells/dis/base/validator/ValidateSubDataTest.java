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
package com.webxells.dis.base.validator;

import com.webxells.dis.api.config.MappingConfiguration;
import com.webxells.dis.base.SimpleMappingPortrayal;
import com.webxells.dis.test.cases.ConfigurationBuilder;import com.webxells.dis.test.cases.SimpleTestCase;
import com.webxells.dis.test.cases.ConfigurationBuilder;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ValidateSubDataTest extends SimpleTestCase {

    @Test
    void testCreateNullPieceWhenNoneProvided() {
        MappingConfiguration configuration = newConfiguration()
                .addPart(ConfigurationBuilder.newPart(ConfigurationBuilder.PartBuilder.PartBuilderState.RANDOM)
                        .addSubData(newConfiguration()
                                .addPart(ConfigurationBuilder.newPart(ConfigurationBuilder.PartBuilder.PartBuilderState.RANDOM))
                                .build())
                )
                .build();
        ValidateSubData fixture = new ValidateSubData();
        fixture.setSubDataPortrayal(
                SimpleMappingPortrayal.source(configuration.parts().get(0).getSubData().get(0).parts().get(0)));

        fixture.setValidation(new IsSet());

        assertFalse(fixture.validate(null, configuration.parts().get(0)));
    }

    @Test
    void testErrorIfMissing() {
        MappingConfiguration configuration = newConfiguration()
                .addPart(ConfigurationBuilder.newPart(ConfigurationBuilder.PartBuilder.PartBuilderState.RANDOM))
                .build();
        ValidateSubData fixture = new ValidateSubData();
        fixture.setSubDataPortrayal(new SimpleMappingPortrayal(random(), random()));
        fixture.setValidation(new Equals() {{setValue(random());}});

        assertTrue(fixture.validate(null, configuration.parts().get(0)));

        fixture.setErrorIfMissing(true);

        assertFalse(fixture.validate(null, configuration.parts().get(0)));
    }

    @Test
    void test() {
        String random = random("unique string");
        String subDataPath = random("sub-path");
        String reference = random("reference");
        MappingConfiguration configuration = newConfiguration()
                .addPart(ConfigurationBuilder.newPart(ConfigurationBuilder.PartBuilder.PartBuilderState.RANDOM)
                        .addSubData(newConfiguration()
                                .addPart(ConfigurationBuilder.newPart()
                                        .setInput(createMappingPoint(reference, subDataPath))
                                        .withContent()
                                )
                                .build())
                        .addSubData(newConfiguration()
                                .addPart(ConfigurationBuilder.newPart()
                                        .setInput(createMappingPoint(reference, subDataPath))
                                        .withContent(random)
                                )
                                .build())
                        .addSubData(newConfiguration()
                                .addPart(ConfigurationBuilder.newPart()
                                        .setInput(createMappingPoint(reference, subDataPath))
                                        .withContent()
                                )
                                .build())
                )
                .build();
        ValidateSubData fixture = new ValidateSubData();
        fixture.setRoot(null);
        fixture.setValidationType(ValidateSubData.VALIDATION_TYPE.ANY);
        fixture.setSubDataPortrayal(new SimpleMappingPortrayal(reference, subDataPath));
        fixture.setValidation(new Equals() {{setValue(random);}});

        assertTrue(fixture.validate(null, configuration.parts().get(0)));

        fixture.setRoot(SimpleMappingPortrayal.source(configuration.parts().get(0)));
        assertTrue(fixture.validate(null, configuration.parts().get(0)));

        fixture.setValidation(new Equals() {{setValue(random("failing"));}});

        assertFalse(fixture.validate(null, configuration.parts().get(0)));

        fixture.setValidationType(ValidateSubData.VALIDATION_TYPE.ALL);
        fixture.setValidation(new Equals() {{setValue(random);}});

        assertFalse(fixture.validate(null, configuration.parts().get(0)));

        configuration.parts().get(0).getSubData().get(0).parts().get(0).getDataset().getContent().get(0).rewriteValue(random);
        configuration.parts().get(0).getSubData().get(2).parts().get(0).getDataset().getContent().get(0).rewriteValue(random);

        assertTrue(fixture.validate(null, configuration.parts().get(0)));
    }

}