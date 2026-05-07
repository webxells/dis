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
package com.webxells.dis.csv.discover;

import com.webxells.dis.api.discover.DiscoverSummary;
import com.webxells.dis.api.discover.Explorer;
import com.webxells.dis.api.discover.ExplorerSummary;
import com.webxells.dis.test.example.discover.SimpleDiscoverySummary;
import com.webxells.dis.test.example.discover.TestDiscoverer;
import java.io.File;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ColumnExplorerTest {

    @Test
    void testWithEmptyFieldsLines() {
        test(List.of(
                new TestDiscoverer.Entry("0", "textA1"),
                new TestDiscoverer.Entry("1", "textB1"),
                new TestDiscoverer.Entry("2", "1"),
                new TestDiscoverer.Entry("0", "textA2"),
                new TestDiscoverer.Entry("1", "textB2"),
                new TestDiscoverer.Entry("2", "2"),
                new TestDiscoverer.Entry("0", "textA3"),
                new TestDiscoverer.Entry("1", "textB3"),
                new TestDiscoverer.Entry("2", "3"),
                new TestDiscoverer.Entry("0", "textA4"),
                new TestDiscoverer.Entry("1", "textB4"),
                new TestDiscoverer.Entry("2", "4"),
                new TestDiscoverer.Entry("0", "textA5"),
                new TestDiscoverer.Entry("1", "textB5"),
                new TestDiscoverer.Entry("2", "5"),
                new TestDiscoverer.Entry("0", ""),
                new TestDiscoverer.Entry("1", ""),
                new TestDiscoverer.Entry("2", null),
                new TestDiscoverer.Entry("0", null),
                new TestDiscoverer.Entry("1", null),
                new TestDiscoverer.Entry("2", null),
                new TestDiscoverer.Entry("0", "textA8"),
                new TestDiscoverer.Entry("1", "textB8"),
                new TestDiscoverer.Entry("2", "8"),
                new TestDiscoverer.Entry("0", "textA9"),
                new TestDiscoverer.Entry("1", "textB9"),
                new TestDiscoverer.Entry("2", "9")
        ), "a-csv-with-empty-lines-and-fields.csv", 9, 27);
    }

    @Test
    void testWithHeaders() {
        test(List.of(
                new TestDiscoverer.Entry("cellA", "textA1"),
                new TestDiscoverer.Entry("123", "textB1"),
                new TestDiscoverer.Entry("cellC", "1"),
                new TestDiscoverer.Entry("cellA", "textA2"),
                new TestDiscoverer.Entry("123", "textB2"),
                new TestDiscoverer.Entry("cellC", "2"),
                new TestDiscoverer.Entry("cellA", "textA3"),
                new TestDiscoverer.Entry("123", "textB3"),
                new TestDiscoverer.Entry("cellC", "3"),
                new TestDiscoverer.Entry("cellA", "textA4"),
                new TestDiscoverer.Entry("123", "textB4"),
                new TestDiscoverer.Entry("cellC", "4"),
                new TestDiscoverer.Entry("cellA", "textA5"),
                new TestDiscoverer.Entry("123", "textB5"),
                new TestDiscoverer.Entry("cellC", "5"),
                new TestDiscoverer.Entry("cellA", "textA6"),
                new TestDiscoverer.Entry("123", "textB6"),
                new TestDiscoverer.Entry("cellC", "6"),
                new TestDiscoverer.Entry("cellA", "textA7"),
                new TestDiscoverer.Entry("123", "textB7"),
                new TestDiscoverer.Entry("cellC", "7"),
                new TestDiscoverer.Entry("cellA", "textA8"),
                new TestDiscoverer.Entry("123", "textB8"),
                new TestDiscoverer.Entry("cellC", "8")
        ), "a-csv.csv", 8, 24);
    }

    @Test
    void testWithoutHeader() {
        test(List.of(
                new TestDiscoverer.Entry("0", "textA1"),
                new TestDiscoverer.Entry("1", "textB1"),
                new TestDiscoverer.Entry("2", "1"),
                new TestDiscoverer.Entry("0", "textA2"),
                new TestDiscoverer.Entry("1", "textB2"),
                new TestDiscoverer.Entry("2", "2"),
                new TestDiscoverer.Entry("0", "textA3"),
                new TestDiscoverer.Entry("1", "textB3"),
                new TestDiscoverer.Entry("2", "3"),
                new TestDiscoverer.Entry("0", "textA4"),
                new TestDiscoverer.Entry("1", "textB4"),
                new TestDiscoverer.Entry("2", "4"),
                new TestDiscoverer.Entry("0", "textA5"),
                new TestDiscoverer.Entry("1", "textB5"),
                new TestDiscoverer.Entry("2", "5"),
                new TestDiscoverer.Entry("0", "textA6"),
                new TestDiscoverer.Entry("1", "textB6"),
                new TestDiscoverer.Entry("2", "6"),
                new TestDiscoverer.Entry("0", "textA7"),
                new TestDiscoverer.Entry("1", "textB7"),
                new TestDiscoverer.Entry("2", "7"),
                new TestDiscoverer.Entry("0", "textA8"),
                new TestDiscoverer.Entry("1", "textB8"),
                new TestDiscoverer.Entry("2", "8")
        ), "a-csv-without-header-fuck.csv", 8, 24);
    }

    void test(List<TestDiscoverer.Entry> entries, String fileName, long  datasetCount, int currentDiscoverEntryCount) {
        DiscoverSummary inputSummary = new SimpleDiscoverySummary();
        TestDiscoverer testDiscoverer = new TestDiscoverer(entries);
        inputSummary.setPathDiscoverer(List.of(testDiscoverer));

        ColumnExplorer fixture = new ColumnExplorer();
        Explorer.Run run = fixture.newRun(new File(ColumnExplorerTest.class.getClassLoader().getResource(fileName).getPath()));

        assertTrue(run.isCapable());

        DiscoverSummary outputSummary = run.explore(inputSummary);
        assertSame(inputSummary, outputSummary);
        ExplorerSummary explorerSummary = outputSummary.getExplorerSummary();
        assertEquals(datasetCount, explorerSummary.getDatasets());

        assertEquals(currentDiscoverEntryCount, testDiscoverer.getCurrent());
    }

}