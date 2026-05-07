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
package com.webxells.dis.base.input;

import com.webxells.dis.api.DatasetPiece;
import com.webxells.dis.api.config.MappingConfiguration;
import com.webxells.dis.api.error.DisException;
import com.webxells.dis.api.input.linker.JoinLinker;
import com.webxells.dis.base.SimpleDatasetPiece;
import com.webxells.dis.base.config.SimpleMappingConfiguration;
import com.webxells.dis.test.example.input.TestInput;
import com.webxells.dis.test.example.input.TestInputConfig;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;


class JoinTest {

    @Test
    void test() throws DisException {
        Map<String, DatasetPiece> rootDs1 = Map.of(
                random(), new SimpleDatasetPiece(random()),
                random(), new SimpleDatasetPiece(random()),
                random(), new SimpleDatasetPiece(random())
        );
        Map<String, DatasetPiece> rootDs2 = Map.of(
                random(), new SimpleDatasetPiece(random()),
                random(), new SimpleDatasetPiece(random()),
                random(), new SimpleDatasetPiece(random())
        );
        TestInput.setData(List.of(rootDs1, rootDs2));
        JoinLinker linker1 = Mockito.mock(JoinLinker.class, "linker1");
        JoinLinker linker2 = Mockito.mock(JoinLinker.class, "linker2");
        MappingConfiguration mapping = new SimpleMappingConfiguration();
        when(linker1.getData(same(mapping))).thenReturn(2);
        when(linker2.getData(same(mapping))).thenReturn(3);
        when(linker1.getData(same(mapping))).thenReturn(2);
        when(linker2.getData(same(mapping))).thenReturn(3);
        JoinConfiguration config = new JoinConfiguration();
        config.setRootConfig(new TestInputConfig());
        config.setChildren(new LinkedList<>() {{
            add(linker1);
            add(linker2);
        }});
        Join fixture = new Join(config);
        fixture.start();
        verify(linker1).start();
        verify(linker2).start();
        fixture.hasNext();
        int actual = fixture.read(mapping);
        int actual2 = fixture.read(mapping);
        verify(linker1, times(2)).getData(same(mapping));
        verify(linker2, times(2)).getData(same(mapping));
        assertEquals(0, fixture.read(mapping));
        fixture.end();
        verify(linker1).end();
        verify(linker2).end();
        verifyNoMoreInteractions(linker1, linker2);

        assertEquals(8, actual);

        assertEquals(8, actual2);
    }

    private String random() {
        return String.valueOf(Math.round(Math.random() * 654782));
    }

}