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

import com.webxells.dis.api.error.InvalidApi;
import com.webxells.dis.api.error.InvalidDatasetException;
import com.webxells.dis.base.SimpleDatasetPiece;
import com.webxells.dis.base.SimpleMappingPortrayal;
import com.webxells.dis.base.config.SimpleMappingConfiguration;
import com.webxells.dis.base.config.SimpleMappingPart;
import com.webxells.dis.base.config.SimpleMappingPoint;
import com.webxells.dis.test.cases.SimpleTestCase;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CopyFromParentConfigurationTest extends SimpleTestCase {

    @Test
    void testWithoutSource() throws InvalidDatasetException, InvalidApi {
        CopyFromParentConfiguration fixture = new CopyFromParentConfiguration();
        SimpleMappingConfiguration root = new SimpleMappingConfiguration();
        SimpleMappingConfiguration subToCopy = new SimpleMappingConfiguration();
        subToCopy.setParts(List.of(
                new SimpleMappingPart(subToCopy,
                        new SimpleMappingPoint("ref", random("subToCopy")),
                        new SimpleMappingPoint("ref",random("subToCopy"))) {{
                    getDataset().collect(new SimpleDatasetPiece(random("subvalue")));
                }}
        ));
        SimpleMappingConfiguration sub = new SimpleMappingConfiguration();
        sub.setParent(root);
        sub.setParts(List.of(
                new SimpleMappingPart(sub,
                        new SimpleMappingPoint("ref", random("sub path")), new SimpleMappingPoint("ref","sub path"))
        ));
        root.setParts(List.of(
                new SimpleMappingPart(root,
                        new SimpleMappingPoint("ref", sub.parts().get(0).getInput().getPath()),
                        new SimpleMappingPoint("ref", "the path")) {{
                    addSubData(subToCopy);
                    getDataset().collect(new SimpleDatasetPiece(random("value")));
                }},
                new SimpleMappingPart(root,
                        new SimpleMappingPoint("ref", "path"), new SimpleMappingPoint("ref","path")) {{
                    setSubData(sub);
                }}
        ));
        fixture.manipulate(null, sub.parts().get(0));

        assertEquals(root.parts().get(0).value().get(), sub.parts().get(0).value().get());
        assertEquals(subToCopy.parts().get(0).getInput().getPath(),
                sub.parts().get(0).getSubData().get(0).parts().get(0).getInput().getPath());
        assertEquals(subToCopy.parts().get(0).value().get(),
                sub.parts().get(0).getSubData().get(0).parts().get(0).value().get());
    }

    @Test
    void test() throws InvalidDatasetException, InvalidApi {
        CopyFromParentConfiguration fixture = new CopyFromParentConfiguration();
        fixture.setSkipDataset(false);
        fixture.setSkipSubData(false);
        SimpleMappingConfiguration root = new SimpleMappingConfiguration();
        SimpleMappingConfiguration subToCopy = new SimpleMappingConfiguration();
        subToCopy.setParts(List.of(
                new SimpleMappingPart(subToCopy,
                        new SimpleMappingPoint("ref", random("subToCopy")),
                        new SimpleMappingPoint("ref",random("subToCopy"))) {{
                            getDataset().collect(new SimpleDatasetPiece(random("subvalue")));
                }}
        ));
        SimpleMappingConfiguration sub = new SimpleMappingConfiguration();
        sub.setParent(root);
        sub.setParts(List.of(
                new SimpleMappingPart(sub,
                        new SimpleMappingPoint("ref", "sub path"), new SimpleMappingPoint("ref","sub path"))
        ));
        root.setParts(List.of(
                new SimpleMappingPart(root,
                        new SimpleMappingPoint("ref", "the path"), new SimpleMappingPoint("ref","the path")) {{
                    addSubData(subToCopy);
                    getDataset().collect(new SimpleDatasetPiece(random("value")));
                }},
                new SimpleMappingPart(root,
                        new SimpleMappingPoint("ref", "path"), new SimpleMappingPoint("ref","path")) {{
                    setSubData(sub);
                }}
        ));
        fixture.setSource(new SimpleMappingPortrayal("ref", root.parts().get(0).getInput().getPath()));

        fixture.manipulate(null, sub.parts().get(0));

        assertEquals(root.parts().get(0).value().get(), sub.parts().get(0).value().get());
        assertEquals(subToCopy.parts().get(0).getInput().getPath(),
                sub.parts().get(0).getSubData().get(0).parts().get(0).getInput().getPath());
        assertEquals(subToCopy.parts().get(0).value().get(),
                sub.parts().get(0).getSubData().get(0).parts().get(0).value().get());
    }


}