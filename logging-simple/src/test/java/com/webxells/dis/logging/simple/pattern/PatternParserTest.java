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
package com.webxells.dis.logging.simple.pattern;

import com.webxells.dis.logging.simple.pattern.path.Date;
import com.webxells.dis.logging.simple.pattern.path.Level;
import com.webxells.dis.logging.simple.pattern.path.PatternEntity;
import com.webxells.dis.logging.simple.pattern.path.Message;
import com.webxells.dis.logging.simple.pattern.path.StringEntity;
import com.webxells.dis.logging.simple.pattern.path.Thread;
import com.webxells.dis.test.cases.SimpleTestCase;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PatternParserTest extends SimpleTestCase {

    @Test
    void test() {
        String random = random();
        assertPatternEquals(" $thread", " ", Thread.class);
        assertPatternEquals(" $ThReAd ", " ", Thread.class, " ");
        assertPatternEquals("$msg()", Message.class);
        assertPatternEquals("$level(shouldBe:Ignored) $ThReAd$unknown $unknown(sad:das) $date-".concat(random),
                Level.class, " ", Thread.class, "$unknown", " ", "$unknown(sad:das)", " ", Date.class, "-".concat(random));
    }

    private void assertPatternEquals(final String pattern, final Object ...assertionObjects) {
        final List<PatternEntity> result = PatternParser.parse(pattern);
        assertEquals(assertionObjects.length, result.size());
        for (int i = 0; i < result.size(); i++) {
            if (assertionObjects[i] instanceof Class) {
                assertEquals(assertionObjects[i], result.get(i).getClass());
            } else if (assertionObjects[i] instanceof String) {
                assertInstanceOf(StringEntity.class, result.get(i));
                assertEquals( assertionObjects[i], ((StringEntity) result.get(i)).getContent());
            }
        }
    }

}