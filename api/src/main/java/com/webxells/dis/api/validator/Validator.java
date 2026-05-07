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
package com.webxells.dis.api.validator;

import com.webxells.dis.api.DatasetPiece;
import com.webxells.dis.api.Logger.LogLevel;
import com.webxells.dis.api.MappingOperation;
import com.webxells.dis.api.config.MappingPart;
import java.util.Optional;

public interface Validator extends MappingOperation {
    enum ErrorStrategy {
        RESET_CONFIGURATION, SKIP_FOLLOWING_OPERATIONS, SKIP_DATASET_PIECE, SKIP_DATASET, CONTINUE_NEXT_READ, ERROR
    }

    boolean validate(DatasetPiece currentPiece, MappingPart mappingPart);

    default Optional<String> getFailMessage() {
        return Optional.empty();
    }

    default LogLevel getFailLevel() {
        return LogLevel.DEBUG;
    }
}