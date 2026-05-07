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
package com.webxells.dis.base.manipulator.setter;

import com.webxells.dis.api.config.MappingConfiguration;
import com.webxells.dis.api.error.InvalidDatasetException;
import com.webxells.dis.api.manipulator.Manipulator;
import com.webxells.dis.test.cases.ConfigurationBuilder;import com.webxells.dis.test.cases.SimpleTestCase;
import com.webxells.dis.test.cases.ConfigurationBuilder;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.only;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

class ManipulateTest extends SimpleTestCase {
    @Test
    void test() throws InvalidDatasetException {
        MappingConfiguration configuration = newConfiguration()
                .addPart(new ConfigurationBuilder.PartBuilder()
                        .withContent())
                .build();
        Manipulate fixture = new Manipulate();
        final Manipulator manipulator = Mockito.mock(Manipulator.class);
        fixture.setManipulator(manipulator);
        assertNull(fixture.getValue(configuration.parts().get(0).getDataset().getContent().get(0), configuration.parts().get(0)));
        verify(manipulator, only()).manipulate(configuration.parts().get(0).getDataset().getContent().get(0), configuration.parts().get(0));
        verifyNoMoreInteractions(manipulator);
    }
}