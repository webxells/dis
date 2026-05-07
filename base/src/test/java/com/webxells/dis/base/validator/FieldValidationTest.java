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

import com.webxells.dis.api.error.InvalidApi;
import com.webxells.dis.api.validator.Validator;
import com.webxells.dis.base.SimpleDatasetPiece;
import com.webxells.dis.base.SimpleMappingPortrayal;
import com.webxells.dis.base.config.SimpleMappingConfiguration;
import com.webxells.dis.base.config.SimpleMappingPart;
import com.webxells.dis.base.config.SimpleMappingPoint;import com.webxells.dis.test.cases.SimpleTestCase;
import com.webxells.dis.test.cases.ConfigurationBuilder;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.when;

class FieldValidationTest extends SimpleTestCase {

    @Test
    void test() throws InvalidApi {
        SimpleMappingConfiguration config = new SimpleMappingConfiguration();
        config.setParts(List.of(
                new SimpleMappingPart(config, new SimpleMappingPoint(random(), random()),
                        new SimpleMappingPoint(random(), random())),
                new SimpleMappingPart(config, new SimpleMappingPoint(random(), random()),
                        new SimpleMappingPoint(random(), random())) {{
                            getDataset().collect(new SimpleDatasetPiece(random()));
                }}));
        Validator validator = Mockito.mock(Validator.class);
        when(validator.validate(eq(new SimpleDatasetPiece(config.parts().get(1).value().get())),
                same(config.parts().get(1))))
                .thenReturn(true)
                .thenReturn(false)
                .thenReturn(false)
                .thenReturn(true);
        FieldValidation fixture = new FieldValidation();
        fixture.setField(new SimpleMappingPortrayal(config.parts().get(1).getInput().getReference(),
                config.parts().get(1).getInput().getPath()));
        fixture.setValidate(validator);
        assertTrue(fixture.validate(new SimpleDatasetPiece(null), config.parts().get(0)));
        assertFalse(fixture.validate(new SimpleDatasetPiece(null), config.parts().get(0)));
        assertFalse(fixture.validate(new SimpleDatasetPiece(null), config.parts().get(0)));
        assertTrue(fixture.validate(new SimpleDatasetPiece(null), config.parts().get(0)));
    }
}