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

import com.webxells.dis.api.DatasetPiece;
import com.webxells.dis.api.config.MappingPart;
import com.webxells.dis.api.config.description.Default;
import com.webxells.dis.api.config.description.Description;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Description("Sorts values in given order")
public class ValueSort extends ValueManipulation {
    public enum SortDirection {
        ASCENDING(((o1, o2) -> o1.value().orElse("").compareTo(o2.value().orElse(""))));

        private final Comparator<? super DatasetPiece> comparator;

        SortDirection(final Comparator<? super DatasetPiece> comparator) {
            this.comparator = comparator;
        }
    }

    @Default("ASCENDING")
    private SortDirection sortDirection = SortDirection.ASCENDING;

    @Override
    public void validate() { }

    @Override
    protected List<DatasetPiece> calculateNewValue(final MappingPart mappingPart) {
        return getMainPartValues(mappingPart).stream()
                .sorted(sortDirection.comparator)
                .collect(Collectors.toList());
    }

    public void setSortDirection(final SortDirection sortDirection) {
        this.sortDirection = sortDirection;
    }

}
