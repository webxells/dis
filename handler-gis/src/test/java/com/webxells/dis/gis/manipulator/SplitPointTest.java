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
package com.webxells.dis.gis.manipulator;

import com.webxells.dis.api.error.InvalidDatasetException;
import com.webxells.dis.base.SimpleDatasetPiece;
import com.webxells.dis.base.config.SimpleMappingConfiguration;
import com.webxells.dis.base.config.SimpleMappingPart;
import com.webxells.dis.base.config.SimpleMappingPoint;
import com.webxells.dis.test.cases.SimpleTestCase;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SplitPointTest extends SimpleTestCase {

    @Test
    void fail() {
        SplitPoint fixture = new SplitPoint();
        SimpleMappingConfiguration configuration = new SimpleMappingConfiguration();
        configuration.setParts(List.of(
                new SimpleMappingPart(configuration,
                        new SimpleMappingPoint("in-reference", "in-path"),
                        new SimpleMappingPoint("out-reference", "out-path"))
        ));
        SimpleDatasetPiece piece = new SimpleDatasetPiece("12.232323");
        assertThrows(InvalidDatasetException.class, () -> fixture.manipulate(piece, configuration.parts().get(0)));
    }

    @Test
    void test() throws InvalidDatasetException {
        SplitPoint fixture = new SplitPoint();
        SimpleMappingConfiguration configuration = new SimpleMappingConfiguration();
        configuration.setParts(List.of(
                new SimpleMappingPart(configuration,
                        new SimpleMappingPoint("in-reference", "in-path"),
                        new SimpleMappingPoint("out-reference", "out-path"))
        ));
        SimpleDatasetPiece piece = new SimpleDatasetPiece("12.232323 45.565656");
        fixture.manipulate(piece, configuration.parts().get(0));
        assertEquals(2, configuration.parts().get(0).getSubData().get(0).parts().size());
        assertEquals("12.232323", configuration.parts().get(0).getSubData().get(0).parts().get(0).value().get());
        assertEquals("in-reference",
                configuration.parts().get(0).getSubData().get(0).parts().get(0).getInput().getReference());
        assertEquals("x",
                configuration.parts().get(0).getSubData().get(0).parts().get(0).getInput().getPath());
        assertEquals("out-reference",
                configuration.parts().get(0).getSubData().get(0).parts().get(0).getOutput().getReference());
        assertEquals("x",
                configuration.parts().get(0).getSubData().get(0).parts().get(0).getOutput().getPath());
        assertEquals("45.565656", configuration.parts().get(0).getSubData().get(0).parts().get(1).value().get());
        assertEquals("in-reference",
                configuration.parts().get(0).getSubData().get(0).parts().get(1).getInput().getReference());
        assertEquals("y",
                configuration.parts().get(0).getSubData().get(0).parts().get(1).getInput().getPath());
        assertEquals("out-reference",
                configuration.parts().get(0).getSubData().get(0).parts().get(1).getOutput().getReference());
        assertEquals("y",
                configuration.parts().get(0).getSubData().get(0).parts().get(1).getOutput().getPath());
        String xName = random("x");
        String yName = random("y");
        fixture.setNameX(xName);
        fixture.setNameY(yName);
        configuration.parts().get(0).getSubData().clear();
        fixture.manipulate(piece, configuration.parts().get(0));
        assertEquals(xName,
                configuration.parts().get(0).getSubData().get(0).parts().get(0).getOutput().getPath());
        assertEquals(yName,
                configuration.parts().get(0).getSubData().get(0).parts().get(1).getOutput().getPath());
    }

}