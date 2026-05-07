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
package com.webxells.dis.json.intern;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class NaturalStringOrderTest {

    @Test
    void testManyIterationsCauseSometimesShuffleIsNotThatRandom() {
        IntStream.range(0,100).forEach(this::test);
    }

    private void test(final int count) {
        List<String> expected = List.of(".a", "asdasd", "room", "room[1]", "room[1]", "room[3]", "room[86]",
                "room[87]", "room[100].a", "room[100].a[45]", "room[100].a[045]", "room[100].a[345]", "room[100].a[]", "room[400]" +
                        ".a[45]", "room[superlength]hehehe");

        List<String> shuffled = new ArrayList<>(expected);
        Collections.shuffle(shuffled);

        List<String> actual = new LinkedList<>();
        shuffled.stream()
                .sorted(new NaturalStringOrder<>(a -> a))
                .forEach(actual::add);

        assertEquals(expected, actual, String.format("Order shuffled list failed on run #%d: %s", count, shuffled));

    }

}
