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
package com.webxells.dis.localfile.input.linker;

import com.webxells.dis.api.config.MappingConfiguration;
import com.webxells.dis.api.config.MappingPart;
import com.webxells.dis.api.error.InvalidApi;
import com.webxells.dis.base.config.SimpleMappingConfiguration;
import com.webxells.dis.base.config.SimpleMappingPart;
import com.webxells.dis.base.config.SimpleMappingPoint;
import com.webxells.dis.localfile.FileNameStore;
import com.webxells.dis.localfile.input.linker.AddCurrentFile.ContentType;
import com.webxells.dis.test.cases.SimpleTestCase;
import java.io.File;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AddCurrentFileTest extends SimpleTestCase {

    @Test
    void test() throws InvalidApi {
        String index = random("index");
        String type = random();
        String name = random();
        String file = String.format("%s.%s", name, type);
        String path = String.format("%s%s%s%s%s",
                random(), File.separator, random(), File.separator, file);
        FileNameStore.setPath(index, path);

        AddCurrentFile fixture = new AddCurrentFile();
        assertThrows(InvalidApi.class, fixture::validate);
        fixture.setName(index);
        fixture.validate();
        SimpleMappingConfiguration config = new SimpleMappingConfiguration();
        config.setParts(List.of(
                new SimpleMappingPart(config,
                        new SimpleMappingPoint(index, ContentType.PATH.toString()), new SimpleMappingPoint()),
                new SimpleMappingPart(config,
                        new SimpleMappingPoint(index, ContentType.TYPE.toString()), new SimpleMappingPoint()),
                new SimpleMappingPart(config,
                        new SimpleMappingPoint(index, ContentType.FILE_WITHOUT_TYPE.toString()), new SimpleMappingPoint()),
                new SimpleMappingPart(config,
                        new SimpleMappingPoint(index, ContentType.FILE.toString()), new SimpleMappingPoint())
        ));

        assertEquals(4, fixture.getData(config));


        assertEquals(path, config.parts().get(0).value().get());
        assertEquals(type, config.parts().get(1).value().get());
        assertEquals(name, config.parts().get(2).value().get());
        assertEquals(file, config.parts().get(3).value().get());
    }

}