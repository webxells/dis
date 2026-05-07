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
package com.webxells.dis.base.validator;

import com.webxells.dis.api.DatasetPiece;
import com.webxells.dis.api.Logger;
import com.webxells.dis.api.config.MappingPart;
import com.webxells.dis.api.config.description.Description;
import com.webxells.dis.api.config.description.Required;
import com.webxells.dis.api.manipulator.Manipulator;
import com.webxells.dis.logging.LoggerProxyFactory;

@Description("Skips errors on manipulating data")
public class SkipFailingManipulator extends SimpleValidator {

    private static final Logger LOGGER = LoggerProxyFactory.logger(SkipFailingManipulator.class);
    @Required
    private Manipulator manipulator;

    @Override
    protected boolean validateCurrentPiece(final DatasetPiece currentPiece, final MappingPart mappingPart) {
        try {
            manipulator.manipulate(currentPiece, mappingPart);
        } catch (final Exception e) {
            LOGGER.debug("Error occurred in manipulator: " + e.getMessage());
            return false;
        }

        return true;
    }

    public void setManipulator(final Manipulator manipulator) {
        this.manipulator = manipulator;
    }
}
