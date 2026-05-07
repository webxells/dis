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
package com.webxells.dis.base.output;

import com.webxells.dis.api.error.DisException;
import com.webxells.dis.api.input.linker.JoinLinker;
import com.webxells.dis.base.SimpleDatasetPiece;
import com.webxells.dis.base.config.SimpleMappingConfiguration;
import com.webxells.dis.base.config.SimpleMappingPart;
import com.webxells.dis.base.config.SimpleMappingPoint;import com.webxells.dis.test.cases.SimpleTestCase;
import com.webxells.dis.test.cases.ConfigurationBuilder;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

class LinkerOutputTest extends SimpleTestCase {

    @Test
    void test() throws DisException {
        String name = random("name");
        String value = random("value");
        SimpleMappingConfiguration mappingConfiguration = new SimpleMappingConfiguration();
        mappingConfiguration.setParts(List.of(
                new SimpleMappingPart(mappingConfiguration, new SimpleMappingPoint(name, random()),
                        new SimpleMappingPoint(random(), random()))
        ));
        JoinLinker linker = Mockito.mock(JoinLinker.class);
        Mockito.when(linker.getData(same(mappingConfiguration))).thenAnswer(a -> {
            mappingConfiguration.parts().get(0).getDataset().collect(new SimpleDatasetPiece(value));
            return 1;
        });
        LinkerOutputConfig config = new LinkerOutputConfig();
        config.setLinker(linker);
        config.setName(name);
        LinkerOutput fixture = new LinkerOutput(config);
        fixture.start();
        fixture.write(mappingConfiguration);
        fixture.end();

        Assertions.assertEquals(value, mappingConfiguration.parts().get(0).value().get());
        verify(linker).start();
        verify(linker).getData(same(mappingConfiguration));
        verify(linker).end();
        verifyNoMoreInteractions(linker);
    }

}