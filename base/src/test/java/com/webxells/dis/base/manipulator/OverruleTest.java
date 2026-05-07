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
package com.webxells.dis.base.manipulator;

import com.webxells.dis.api.Dataset;
import com.webxells.dis.api.DatasetPiece;
import com.webxells.dis.api.config.MappingConfiguration;
import com.webxells.dis.api.config.MappingPart;
import com.webxells.dis.api.validator.Validator;
import com.webxells.dis.base.SimpleDataset;
import com.webxells.dis.base.SimpleDatasetPiece;
import com.webxells.dis.base.SimpleMappingPortrayal;
import com.webxells.dis.base.config.SimpleMappingPart;
import com.webxells.dis.base.manipulator.setter.FieldSetter;
import com.webxells.dis.base.manipulator.setter.StaticValue;
import com.webxells.dis.test.cases.SimpleTestCase;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class OverruleTest extends SimpleTestCase {

    @Test
    void testWithClearing() {
        Overrule fixture = new Overrule();
        fixture.setClearIfLinkerHasNoValue(true);

        Dataset dataset = new SimpleDataset();
        DatasetPiece mappingPiece = new SimpleDatasetPiece(random());
        dataset.collect(List.of(
                mappingPiece,
                new SimpleDatasetPiece(random()),
                new SimpleDatasetPiece(random())
        ));
        fixture.setIfSetter(new StaticValue() {{
            setValue(null);
        }});

        fixture.manipulate(mappingPiece, new SimpleMappingPart(null));
        assertTrue(mappingPiece.value().isEmpty());
    }

    @Test
    void testWithoutValidator() {
        Overrule fixture = new Overrule();
        Dataset dataset = new SimpleDataset();
        DatasetPiece mappingPiece = new SimpleDatasetPiece(random());
        dataset.collect(List.of(
                mappingPiece,
                new SimpleDatasetPiece(random()),
                new SimpleDatasetPiece(random())
        ));
        String value = random();
        fixture.setIfSetter(new StaticValue() {{
            setValue(value);
        }});
        fixture.manipulate(mappingPiece, new SimpleMappingPart(null));
        assertEquals(value, mappingPiece.value().get());
    }

    @Test
    void testSetWithFieldSetter() {
        Overrule fixture = new Overrule();
        final DatasetPiece piece = new SimpleDatasetPiece(random());
        fixture.setValidators(List.of(new Validator() {
            @Override
            public boolean validate(final DatasetPiece datasetPiece, final MappingPart part) {
                assertSame(piece, datasetPiece);
                return true;
            }
            @Override
            public String getType() {
                return null;
            }
        }));
        MappingConfiguration mappingConfiguration = mock(MappingConfiguration.class);
        SimpleMappingPart newPart = new SimpleMappingPart(mappingConfiguration);
        SimpleMappingPart old = new SimpleMappingPart(mappingConfiguration);
        String newValue = random();
        String oldValue = random();
        newPart.getDataset().collect(new SimpleDatasetPiece(newValue));
        old.getDataset().collect(new SimpleDatasetPiece(oldValue));

        SimpleMappingPortrayal simpleMappingPortrayal = new SimpleMappingPortrayal();
        fixture.setIfSetter(new FieldSetter() {{
            setPortrayal(simpleMappingPortrayal);
        }});
        when(mappingConfiguration.getByPortrayal(same(simpleMappingPortrayal)))
                .thenReturn(Optional.of(newPart));
        fixture.manipulate(piece, old);

        assertEquals(newValue, old.value().get());
    }

    @Test
    void testSetIfWithElse() {
        String newValue = random();
        testSet( random(), newValue, random(), newValue, true);
        String elseValue = random();
        testSet( random(), random(), elseValue, elseValue, false);
    }

    @Test
    void testSetIfWithoutElse() {
        String newValue = random();
        testSet( random(), newValue, null, newValue, true);
        String oldValue = random();
        testSet( oldValue, random(), null, oldValue, false);
    }

    @Test
    void testSetWithoutIf() {
        String newValue = random();
        testSet( random(), null, newValue, newValue, false);
        String oldValue = random();
        testSet( oldValue, null, random(), oldValue, true);
    }

    private void testSet(String oldValue, String setIfValue, String setElseValue, String resultValue,
                         boolean validates) {
        Overrule fixture = new Overrule();
        Dataset dataset = new SimpleDataset();
        DatasetPiece mappingPiece = new SimpleDatasetPiece(oldValue);
        dataset.collect(List.of(
                mappingPiece,
                new SimpleDatasetPiece(random()),
                new SimpleDatasetPiece(random())
        ));
        fixture.setValidators(List.of(new Validator() {
            @Override
            public boolean validate(final DatasetPiece datasetPiece, final MappingPart part) {
                return validates;
            }
            @Override
            public String getType() {
                return null;
            }
        }));
        Optional.ofNullable(setIfValue)
                .ifPresent(a -> fixture.setIfSetter(new StaticValue() {{
                    setValue(a);
                }}));
        Optional.ofNullable(setElseValue)
                .ifPresent(a -> fixture.setElseSetter(new StaticValue() {{
                    setValue(a);
                }}));
        fixture.manipulate(mappingPiece, new SimpleMappingPart(null));
        assertEquals(resultValue, mappingPiece.value().get());
    }


}