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
package com.webxells.dis.csv;

import com.webxells.dis.api.config.MappingConfiguration;
import com.webxells.dis.api.config.MappingPart;
import com.webxells.dis.base.SimpleDatasetPiece;
import com.webxells.dis.csv.internal.CsvCell;
import java.io.IOException;

public enum UnknownColumnStrategy {
    CREATE {
        @Override
        public int handle(final CsvCell column, final MappingConfiguration mappingConfiguration) {
            final MappingPart newPart = SameColumnStrategy.createNewPart(mappingConfiguration, column);
            newPart.getDataset().collect(new SimpleDatasetPiece(column.value()));
            return 1;
        }
    },
    IGNORE {
        @Override
        public int handle(final CsvCell column, final MappingConfiguration mappingConfiguration) {
            return 0;
        }
    },
    ERROR {
        @Override
        public int handle(final CsvCell column, final MappingConfiguration mappingConfiguration) throws IOException {
            throw new IOException("No mapping part found: " + column.header());
        }
    };

    public abstract int handle(CsvCell column, MappingConfiguration mappingConfiguration) throws IOException;
}