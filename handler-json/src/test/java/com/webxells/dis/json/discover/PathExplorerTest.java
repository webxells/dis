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
package com.webxells.dis.json.discover;

import com.webxells.dis.api.discover.DiscoverSummary;
import com.webxells.dis.api.discover.Explorer;
import com.webxells.dis.api.discover.ExplorerSummary;
import com.webxells.dis.test.example.discover.SimpleDiscoverySummary;
import com.webxells.dis.test.example.discover.TestDiscoverer;
import java.io.File;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PathExplorerTest {

    @Test
    void test() {
        DiscoverSummary inputSummary = new SimpleDiscoverySummary();
        TestDiscoverer testDiscoverer = new TestDiscoverer(List.of(
                new TestDiscoverer.Entry("$.previous[].here", "some"),
                new TestDiscoverer.Entry("$.previous[].here", "data"),
                new TestDiscoverer.Entry("$.main[].a", "a1"),
                new TestDiscoverer.Entry("$.main[].b", "b1"),
                new TestDiscoverer.Entry("$.main[].c", "c1"),
                new TestDiscoverer.Entry("$.main[].d[]", "1"),
                new TestDiscoverer.Entry("$.main[].d[]", "2"),
                new TestDiscoverer.Entry("$.main[].d[]", "3"),
                new TestDiscoverer.Entry("$.main[].d[]", "4"),
                new TestDiscoverer.Entry("$.main[].a", "a2"),
                new TestDiscoverer.Entry("$.main[].b", "b2"),
                new TestDiscoverer.Entry("$.main[].c", "c2"),
                new TestDiscoverer.Entry("$.main[].a", "a3"),
                new TestDiscoverer.Entry("$.main[].b", "b3"),
                new TestDiscoverer.Entry("$.main[].c", "c3"),
                new TestDiscoverer.Entry("$.main[].a", "a4"),
                new TestDiscoverer.Entry("$.main[].b", "b4"),
                new TestDiscoverer.Entry("$.main[].c", "c4"),
                new TestDiscoverer.Entry("$.main[].a", "a5"),
                new TestDiscoverer.Entry("$.main[].b", "b5"),
                new TestDiscoverer.Entry("$.main[].c", "c5"),
                new TestDiscoverer.Entry("$.next[].there", null),
                new TestDiscoverer.Entry("$.next[].there", "data")
        ));
        inputSummary.setPathDiscoverer(List.of(testDiscoverer));

        PathExplorer fixture = new PathExplorer();

        Explorer.Run run = fixture.newRun(new File(PathExplorerTest.class.getClassLoader().getResource("some-data.json").getPath()));

        assertTrue(run.isCapable());

        DiscoverSummary outputSummary = run.explore(inputSummary);
        assertSame(inputSummary, outputSummary);
        ExplorerSummary explorerSummary = outputSummary.getExplorerSummary();
        assertEquals(5L, explorerSummary.getDatasets());

        assertEquals(23, testDiscoverer.getCurrent());

    }

}