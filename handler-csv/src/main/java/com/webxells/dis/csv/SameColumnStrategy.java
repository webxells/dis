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

import com.webxells.dis.api.Dataset;
import com.webxells.dis.api.config.MappingConfiguration;
import com.webxells.dis.api.config.MappingPart;
import com.webxells.dis.base.SimpleDatasetPiece;
import com.webxells.dis.base.config.SimpleMappingPart;
import com.webxells.dis.base.config.SimpleMappingPoint;
import com.webxells.dis.csv.internal.CsvCell;
import java.io.IOException;
import java.util.List;

public enum SameColumnStrategy {
    MERGE {
        @Override
        public int match(final List<MappingPart> parts, final List<CsvCell> columns, final MappingConfiguration configuration) {
            if (0 < parts.size()) {
                final Dataset dataset = parts.getFirst().getDataset();
                return
                        columns.stream()
                                .mapToInt(a -> {
                                    dataset.collect(new SimpleDatasetPiece(a.value()));
                                    return 1;
                                })
                                .sum();
            }
            return 0;
        }
    },

    MERGE_CREATE {
        @Override
        public int match(List<MappingPart> parts, final List<CsvCell> columns, final MappingConfiguration configuration) throws IOException {
            if (parts.isEmpty()) {
                final MappingPart newPart = createNewPart(configuration, columns.getFirst());
                parts = List.of(newPart);
            }
            return MERGE.match(parts, columns, configuration);
        }
    },

    FIND_CORRESPONDENT_IGNORE {
        @Override
        public int match(final List<MappingPart> parts, final List<CsvCell> columns, final MappingConfiguration configuration) {
            final int max = Math.min(parts.size(), columns.size());
            for (int i = 0; i < max; i++) {
                parts.get(i).getDataset().collect(new SimpleDatasetPiece(columns.get(i).value()));
            }
            return max;
        }
    },

    FIND_CORRESPONDENT_CREATE {
        @Override
        public int match(final List<MappingPart> parts, final List<CsvCell> columns, final MappingConfiguration configuration) {
            final int partSize = parts.size();
            final int max = columns.size();
            for (int i = 0; i < max; i++) {
                if (i > partSize) {
                    final MappingPart newPart = createNewPart(configuration, columns.getFirst());
                    parts.add(newPart);
                }
                parts.get(i).getDataset().collect(new SimpleDatasetPiece(columns.get(i).value()));
            }
            return max;
        }
    },

    IGNORE {
        @Override
        public int match(final List<MappingPart> parts, final List<CsvCell> columns, final MappingConfiguration configuration) {
            if (columns.isEmpty()) {
                return 0;
            }
            return parts.stream()
                    .mapToInt(a -> {
                        a.getDataset().collect(new SimpleDatasetPiece(columns.getFirst().value()));
                        return 1;
                    })
                    .sum();
        }
    },

    ERROR {
        @Override
        public int match(final List<MappingPart> parts, final List<CsvCell> columns, final MappingConfiguration configuration) throws IOException {
            if (parts.size() + columns.size() > 2) {
                throw new IOException("parts and columns are in imbalance");
            }
            return IGNORE.match(parts, columns, configuration);
        }
    };

    public abstract int match(final List<MappingPart> parts, final List<CsvCell> columns, final MappingConfiguration configuration) throws IOException;

    static MappingPart createNewPart(final MappingConfiguration configuration, final CsvCell first) {
        final MappingPart result = new SimpleMappingPart(configuration,
                new SimpleMappingPoint(first.source(), first.header()), new SimpleMappingPoint());
        configuration.parts().add(result);
        return result;
    }
}