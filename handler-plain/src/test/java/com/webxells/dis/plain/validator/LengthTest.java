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

import com.webxells.dis.api.error.InvalidApi;
import com.webxells.dis.base.SimpleDatasetPiece;
import com.webxells.dis.test.cases.SimpleTestCase;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class LengthTest extends SimpleTestCase {

    @Test
    void test() {
        int comparative = randomMax(20) + 2;
        int update = comparative / 2;

        runTest(comparative + update, comparative, Length.Operator.LESS, false);
        runTest(comparative + update, comparative, Length.Operator.LESS_OR_EQUAL, false);
        runTest(comparative + update, comparative, Length.Operator.EQUAL, false);
        runTest(comparative + update, comparative, Length.Operator.NOT_EQUAL, true);
        runTest(comparative + update, comparative, Length.Operator.MORE, true);
        runTest(comparative + update, comparative, Length.Operator.MORE_OR_EQUAL, true);

        runTest(comparative, comparative, Length.Operator.LESS, false);
        runTest(comparative, comparative, Length.Operator.LESS_OR_EQUAL, true);
        runTest(comparative, comparative, Length.Operator.EQUAL, true);
        runTest(comparative, comparative, Length.Operator.NOT_EQUAL, false);
        runTest(comparative, comparative, Length.Operator.MORE, false);
        runTest(comparative, comparative, Length.Operator.MORE_OR_EQUAL, true);

        runTest(comparative - update, comparative, Length.Operator.LESS, true);
        runTest(comparative - update, comparative, Length.Operator.LESS_OR_EQUAL, true);
        runTest(comparative - update, comparative, Length.Operator.EQUAL, false);
        runTest(comparative - update, comparative, Length.Operator.NOT_EQUAL, true);
        runTest(comparative - update, comparative, Length.Operator.MORE, false);
        runTest(comparative - update, comparative, Length.Operator.MORE_OR_EQUAL, false);
    }

    private void runTest(final int length, final int comparative, final Length.Operator operator, final boolean result) {
        Length fixture = new Length();
        Assertions.assertThrows(InvalidApi.class, fixture::validate);
        fixture.setOperator(operator);
        Assertions.assertThrows(InvalidApi.class, fixture::validate);
        fixture.setComparativeValue(comparative);
        Assertions.assertDoesNotThrow(() -> fixture.validate());
        String data = "a".repeat(length);

        Assertions.assertEquals(result, fixture.validate(new SimpleDatasetPiece(data), null));
    }

}