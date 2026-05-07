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
package com.webxells.dis.plain;

import com.webxells.dis.api.error.DisException;
import com.webxells.dis.api.error.InputOutputError;
import com.webxells.dis.api.resource.Resource;
import com.webxells.dis.base.config.SimpleMappingConfiguration;
import com.webxells.dis.base.config.StableMappingPart;
import com.webxells.dis.base.config.SimpleMappingPoint;
import com.webxells.dis.plain.input.Regex;
import com.webxells.dis.plain.input.RegexConfig;
import com.webxells.dis.test.cases.SimpleTestCase;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RegexTest extends SimpleTestCase {

    @Test
    void readMerge() throws DisException {
        String first = random();
        String second = random();
        String third = random();
        ByteArrayInputStream stream =
                new ByteArrayInputStream(String.format("first line%s%nsecond%sline%n%sthird line",first, second, third).getBytes());
        SimpleMappingConfiguration mappingConfiguration = new SimpleMappingConfiguration();
        mappingConfiguration.setParts(List.of(
                new StableMappingPart(mappingConfiguration,
                        new SimpleMappingPoint("test", "0"),
                        new SimpleMappingPoint("nobody", "cares")
                ), new StableMappingPart(mappingConfiguration,
                        new SimpleMappingPoint("test", "1"),
                        new SimpleMappingPoint("nobody", "cares")
                ), new StableMappingPart(mappingConfiguration,
                        new SimpleMappingPoint("test", "number"),
                        new SimpleMappingPoint("nobody", "cares")
                ), new StableMappingPart(mappingConfiguration,
                        new SimpleMappingPoint("test", "someText"),
                        new SimpleMappingPoint("nobody", "cares")
                )
        ));
        RegexConfig config = new RegexConfig();
        config.setName("test");
        config.setReceiver(new Resource() {
            @Override
            public OutputStream send() throws InputOutputError {
                return null;
            }

            @Override
            public InputStream receive() throws InputOutputError {
                return stream;
            }

            @Override
            public String getType() {
                return null;
            }
        });
        config.setRules(List.of(
                new Rule() {{
                    setRegex("[A-Z ]*(?<number>\\d+)[A-Z ]*");
                    setOptions(Set.of(Option.CASE_INSENSITIVE, Option.MULTILINE));
                }},
                new Rule() {{
                    setRegex("[A-Z ]*\\d+(?<someText>[A-Z ]*)");
                    setOptions(Set.of(Option.CASE_INSENSITIVE, Option.MULTILINE));
                }}
        ));
        Regex fixture = new Regex(config);
        fixture.start();
        assertTrue(fixture.hasNext());
        assertEquals(4, fixture.read(mappingConfiguration));
        checkMergedDataset(mappingConfiguration, "first line".concat(first), first, "");
        assertTrue(fixture.hasNext());
        mappingConfiguration.clear();
        assertEquals(4, fixture.read(mappingConfiguration));
        checkMergedDataset(mappingConfiguration, String.format("second%sline", second), second, "line");
        assertTrue(fixture.hasNext());
        mappingConfiguration.clear();
        assertEquals(4, fixture.read(mappingConfiguration));
        checkMergedDataset(mappingConfiguration, third.concat("third line"), third, "third line");
        assertFalse(fixture.hasNext());mappingConfiguration.clear();
        assertEquals(0, fixture.read(mappingConfiguration));
        fixture.end();
    }

    private void checkMergedDataset(SimpleMappingConfiguration mappingConfiguration, String all, String number, String text) {
        assertEquals(all, mappingConfiguration.parts().get(0).value().get());
        assertEquals(number, mappingConfiguration.parts().get(1).value().get());
        assertEquals(number, mappingConfiguration.parts().get(2).value().get());
        assertEquals(text, mappingConfiguration.parts().get(3).value().get());
    }

}