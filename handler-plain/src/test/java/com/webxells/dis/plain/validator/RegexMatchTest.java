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
package com.webxells.dis.plain.validator;

import com.webxells.dis.api.DatasetPiece;
import com.webxells.dis.base.SimpleDatasetPiece;
import com.webxells.dis.base.config.SimpleMappingPart;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RegexMatchTest {

    @Test
    void test() {
        validatorFails("abcabc", "abc", true);
        validatorFails("abcabc", "abc", 4);
        validatorFails("abcabc", "(abc){3}", 0);
        validatorSucceeds("abcabc", "abcabc", true);
        validatorSucceeds("abcabc", "abc", 3);
        validatorSucceeds("abcabc", "(abc){2}", 0);
    }

    private void validatorFails(String value, String search, int start) {
        validator(value, search, false, start, false);
    }

    private void validatorSucceeds(String value, String search, int start) {
        validator(value, search, false, start, true);
    }

    private void validatorSucceeds(String value, String search, boolean matchWhole) {
        validator(value, search, matchWhole, 0, true);
    }

    private void validatorFails(String value, String search, boolean matchWhole) {
        validator(value, search, matchWhole, 0, false);
    }

    private void validator(String value, String search, boolean matchWhole, int start,  boolean assertingResult) {
        RegexMatch fixture = new RegexMatch();
        fixture.setMatchWhole(matchWhole);
        fixture.setSearch(search);
        fixture.setStart(start);

        DatasetPiece data = new SimpleDatasetPiece(value);
        assertEquals(assertingResult, fixture.validate(data, new SimpleMappingPart(null)));
    }

}