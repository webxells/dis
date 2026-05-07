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

import java.util.Comparator;
import java.util.function.Function;

//@todo: skip leading zeros e.g. file00001 is currently "greater" than file002
public class NaturalStringOrder<T> implements Comparator<T> {
    private final Function<T, String> function;

    public NaturalStringOrder(final Function<T, String> function) {
        this.function = function;
    }

    @Override
    public int compare(final T o1, final T o2) {
        return compareStringInNaturalOrder(function.apply(o1), function.apply(o2));
    }

    private int compareStringInNaturalOrder(final String string1, final String string2) {
        final int length1 = string1.length() - 1;
        final int length2 = string2.length() - 1;
        boolean inNumber = false;
        for(int i=0,m = Math.min(length1, length2) + 1; i<=m; i++) {
            if (i == m) {
                if (length1 == length2) {
                    return 0;
                }
                return length1 < length2 ? -1 : 1;
            }
            final char c1 = string1.charAt(i);
            final char c2 = string2.charAt(i);
            final boolean isNumber1 = Character.isDigit(c1);
            final boolean isNumber2 = Character.isDigit(c2);
            if (c1 == c2) {
                inNumber = isNumber1 | isNumber2;
                continue;
            }
            final int nextNumbers1 = isNumber1 ? nextNumbers(string1, i, length1) : 0;
            final int nextNumbers2 = isNumber2 ? nextNumbers(string2, i, length2) : 0;
            if (isNumber1) {
                if (!isNumber2) {
                    return inNumber ? 1 : -1;
                }
                if (nextNumbers1 != nextNumbers2) {
                    return nextNumbers1 > nextNumbers2 ? 1 : -1;
                }
            } else if (isNumber2) {
                return inNumber ? -1 : 1;
            }
            return c1 - c2;
        }
        return 0;
    }

    private int nextNumbers(final String string, int i, final int length) {
        int result = 0;
        while(i < length && Character.isDigit(string.charAt(i++))) {
            result++;
        }
        return result;
    }
}
