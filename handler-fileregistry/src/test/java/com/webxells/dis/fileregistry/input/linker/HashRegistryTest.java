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
package com.webxells.dis.fileregistry.input.linker;

import com.webxells.dis.api.MappingPortrayal;
import com.webxells.dis.api.config.MappingConfiguration;
import com.webxells.dis.api.error.DisException;
import com.webxells.dis.api.input.linker.JoinLinker;
import com.webxells.dis.base.SimpleDatasetPiece;
import com.webxells.dis.base.SimpleMappingPortrayal;
import com.webxells.dis.base.config.SimpleMappingConfiguration;
import com.webxells.dis.base.config.SimpleMappingPart;
import com.webxells.dis.base.config.SimpleMappingPoint;
import com.webxells.dis.test.cases.SimpleTestCase;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

class HashRegistryTest extends SimpleTestCase {
    private final static Path TMP_DIR = new File(System.getProperty("java.io.tmpdir")).toPath();

    private File testDir;

    @BeforeEach
    void setUpTestDir() throws IOException {
        testDir = Files.createTempDirectory(TMP_DIR, HashFileRegistry.class.getSimpleName()).toFile();
        testDir.deleteOnExit();
    }

    @AfterEach
    void cleanTestDir() throws IOException {
        Files.walk(testDir.toPath())
                .sorted(Comparator.reverseOrder())
                .map(Path::toFile)
                .forEach(File::deleteOnExit);
    }

    @Test
    void testStrictWontActForNullValues() throws DisException {
        assertThrows(StrictnessFailed.class, () -> test(true));
    }

    @Test
    void testLenient() throws DisException {
        test(false);
    }

    void test(final boolean strict) throws DisException {
        SimpleMappingConfiguration from = new SimpleMappingConfiguration();
        from.setParts(List.of(
                new SimpleMappingPart(from, new SimpleMappingPoint("input", "some-value"), new SimpleMappingPoint(
                        "output", "some-value")) {{
                    getDataset().collect(new SimpleDatasetPiece(random("some-value")));
                }},
                new SimpleMappingPart(from, new SimpleMappingPoint("input", "some-value2"), new SimpleMappingPoint(
                        "output", "some-value2")),

                new SimpleMappingPart(from, new SimpleMappingPoint("input", "target"), new SimpleMappingPoint(
                        "output", "target"))
        ));
        AtomicInteger callCount = new AtomicInteger();
        JoinLinker child = mock(JoinLinker.class);
        MappingPortrayal sourcePortrayal = new SimpleMappingPortrayal() {{
            setPath("some-child");
            setReference("child-linker");
        }};
        String newValue = random("test-value");
        when(child.getData(any(MappingConfiguration.class))).thenAnswer(a -> {{
            MappingConfiguration config = a.getArgument(0, MappingConfiguration.class);
            assertNotSame(from, config);
            assertEquals(3, config.parts().size());
            config.getByPortrayal(sourcePortrayal).ifPresent(b -> b.getDataset().collect(new SimpleDatasetPiece(newValue)));
            callCount.getAndIncrement();
            return 1;
        }});

        HashFileRegistry fixture = new HashFileRegistry();
        fixture.setStrict(strict);
        fixture.setRegistryDirectory(testDir.getAbsolutePath());
        fixture.setHashFields(List.of(new SimpleMappingPortrayal() {{
            setReference("input");
            setPath("some-value");
        }}, new SimpleMappingPortrayal() {{
            setReference("input");
            setPath("some-value2");
        }}));

        fixture.setTarget(new SimpleMappingPortrayal() {{
            setReference("input");
            setPath("target");
        }});
        fixture.setSource(new HashFileRegistry.Source() {{
            linker = child;
            portrayal = sourcePortrayal;
        }});

        fixture.start();
        fixture.getData(from);
        assertEquals(newValue, from.parts().get(2).value().get());
        from.parts().get(2).getDataset().clear();
        fixture.getData(from);
        assertEquals(newValue, from.parts().get(2).value().get());
        fixture.end();

        verify(child).start();
        verify(child).end();
        verify(child).getData(any(MappingConfiguration.class));
        verifyNoMoreInteractions(child);
    }

}