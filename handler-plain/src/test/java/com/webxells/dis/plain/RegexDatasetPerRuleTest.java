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
import com.webxells.dis.plain.input.RegexConfig;
import com.webxells.dis.plain.input.RegexDatasetPerRule;
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

class RegexDatasetPerRuleTest extends SimpleTestCase {

    @Test
    void readDatasetPerRule() throws DisException {
        String first = random();
        String second = String.format("ASD%sIJFD", random());
        ByteArrayInputStream stream =
                new ByteArrayInputStream(String.format("first value = %s%n%nsecond:%s",first, second).getBytes());
        SimpleMappingConfiguration mappingConfiguration = new SimpleMappingConfiguration();
        mappingConfiguration.setParts(List.of(
                new StableMappingPart(mappingConfiguration,
                        new SimpleMappingPoint("test", "first"),
                        new SimpleMappingPoint()
                ), new StableMappingPart(mappingConfiguration,
                        new SimpleMappingPoint("test", "second"),
                        new SimpleMappingPoint()
                ), new StableMappingPart(mappingConfiguration,
                        new SimpleMappingPoint("test", "1"),
                        new SimpleMappingPoint()
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
                    setRegex("first value = (?<first>\\d+)");
                    setOptions(Set.of(Option.CASE_INSENSITIVE));
                }},
                new Rule() {{
                    setRegex("second:(?<second>[0-9a-z]+)");
                    setOptions(Set.of(Option.CASE_INSENSITIVE));
                }}
        ));
        RegexDatasetPerRule fixture = new RegexDatasetPerRule(config);
        fixture.start();
        assertTrue(fixture.hasNext());
        assertEquals(3, fixture.read(mappingConfiguration));
        checkPerRuleDataset(mappingConfiguration, first, null);
        assertTrue(fixture.hasNext());
        mappingConfiguration.clear();
        assertEquals(3, fixture.read(mappingConfiguration));
        checkPerRuleDataset(mappingConfiguration, null, second);
        assertFalse(fixture.hasNext());
        mappingConfiguration.clear();
        assertEquals(0, fixture.read(mappingConfiguration));
        fixture.end();
    }

    private void checkPerRuleDataset(final SimpleMappingConfiguration mappingConfiguration, String first, String second) {
        if (null == first) {
            assertTrue(mappingConfiguration.parts().get(0).value().isEmpty());
        } else {
            assertEquals(first, mappingConfiguration.parts().get(0).value().get());
        }
        if (second == null) {
            assertTrue(mappingConfiguration.parts().get(1).value().isEmpty());
        } else {
            assertEquals(second, mappingConfiguration.parts().get(1).value().get());
        }
        assertEquals(null == first ? second : first, mappingConfiguration.parts().get(2).value().get());
    }

}
