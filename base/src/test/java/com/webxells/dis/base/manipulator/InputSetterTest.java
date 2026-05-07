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

import com.webxells.dis.api.config.MappingConfiguration;
import com.webxells.dis.api.error.DisException;
import com.webxells.dis.api.error.InputOutputError;
import com.webxells.dis.api.error.InvalidApi;
import com.webxells.dis.api.input.linker.JoinLinker;
import com.webxells.dis.base.SimpleDatasetPiece;
import com.webxells.dis.base.SimpleMappingPortrayal;
import com.webxells.dis.base.config.SimpleMappingConfiguration;
import com.webxells.dis.base.config.SimpleMappingPart;
import com.webxells.dis.base.config.SimpleMappingPoint;
import com.webxells.dis.base.manipulator.setter.InputSetter;import com.webxells.dis.test.cases.SimpleTestCase;
import com.webxells.dis.test.cases.ConfigurationBuilder;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class InputSetterTest extends SimpleTestCase {

    @Test
    void failedValidation() throws InvalidApi {
        InputSetter fixture = new InputSetter();
        assertThrows(InvalidApi.class, fixture::validate);
        fixture.setLinker(mock(JoinLinker.class));
        assertThrows(InvalidApi.class, fixture::validate);
        fixture.setValueSourcePath(random());
        fixture.validate();
        fixture.setValueSourcePath(null);
        assertThrows(InvalidApi.class, fixture::validate);
        fixture.setOverwrites(List.of());
        assertThrows(InvalidApi.class, fixture::validate);
        fixture.setOverwrites(List.of(newOverwrite(null, new SimpleMappingPortrayal())));
        assertThrows(InvalidApi.class, fixture::validate);
        fixture.setOverwrites(List.of(newOverwrite("", null)));
        assertThrows(InvalidApi.class, fixture::validate);
        fixture.setOverwrites(List.of(newOverwrite("", new SimpleMappingPortrayal())));
        fixture.validate();
    }

    @Test
    void testAdditionalReadings() throws DisException {
        SimpleMappingConfiguration configuration = new SimpleMappingConfiguration();
        String field1 = random("field1");
        String field2 = random("field2");
        String field3 = random("field3");
        String value3 = random("value3");
        String field4 = random("field4");
        String value4 = random("value4");
        String field5 = random("field5");
        String value5 = random("value5");
        String reference = random("reference");
        JoinLinker linker = mock(JoinLinker.class);
        when(linker.getData(any(MappingConfiguration.class))).thenAnswer(invocation -> {
            MappingConfiguration runtimeConfiguration =  invocation.getArgument(0);
            runtimeConfiguration.parts().get(0).getDataset().collect(new SimpleDatasetPiece(value3));
            runtimeConfiguration.parts().get(1).getDataset().collect(new SimpleDatasetPiece(value4));
            runtimeConfiguration.parts().get(2).getDataset().collect(new SimpleDatasetPiece(value5));
            return 2;
        });

        configuration.setParts(List.of(
                new SimpleMappingPart(configuration, new SimpleMappingPoint(reference, field1),
                        new SimpleMappingPoint(reference, field1)),
                new SimpleMappingPart(configuration, new SimpleMappingPoint(reference, field2),
                        new SimpleMappingPoint(reference, field2))
        ));
        InputSetter fixture = new InputSetter();
        fixture.setValueSourcePath(field3);
        fixture.setOverwrites(List.of(
                newOverwrite(field4, new SimpleMappingPortrayal(reference, field1)),
                newOverwrite(field5, new SimpleMappingPortrayal(reference, field2))
        ));
        fixture.setLinker(linker);

        assertEquals(value3, fixture.getValue(null, configuration.parts().get(1)));

        assertEquals(value4, configuration.parts().get(0).value().get());
        assertEquals(value5, configuration.parts().get(1).value().get());
    }

    @Test
    void testFail() throws InputOutputError {
        InputSetter fixture = new InputSetter();
        JoinLinker linker = Mockito.mock(JoinLinker.class);
        fixture.setValueSourcePath(null);
        fixture.setLinker(linker);
        Mockito.when(linker.getData(any()))
                .thenThrow(new InputOutputError("weired error"));
        Assertions.assertThrows(RuntimeException.class, () ->  fixture.getValue(new SimpleDatasetPiece(null),
                new SimpleMappingPart(null)));
    }

    @Test
    void testNoPathFound() throws DisException {
        SimpleMappingConfiguration configuration = new SimpleMappingConfiguration();
        JoinLinker linker = mock(JoinLinker.class);
        when(linker.getData(any(MappingConfiguration.class))).thenReturn(2);

        configuration.setParts(List.of(
                new SimpleMappingPart(configuration, new SimpleMappingPoint(random(), random()),
                        new SimpleMappingPoint(random(), random()))
        ));
        InputSetter fixture = new InputSetter();
        fixture.setValueSourcePath(random("source"));
        fixture.setOverwrites(List.of(
                newOverwrite(random("ov-source"), new SimpleMappingPortrayal(random(), random("non-existent")))
        ));
        fixture.setLinker(linker);

        assertNull(fixture.getValue(null, configuration.parts().get(0)));
    }

    private InputSetter.OverwriteMapping newOverwrite(String sourcePathForMapping, SimpleMappingPortrayal portrayal) {
        return new InputSetter.OverwriteMapping() {{
            sourcePath = sourcePathForMapping;
            target = portrayal;
        }};
    }

}