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
import com.webxells.dis.api.config.MappingConfiguration;
import com.webxells.dis.api.config.MappingPart;
import com.webxells.dis.api.config.OutputConfig;
import com.webxells.dis.api.config.description.Alias;
import com.webxells.dis.api.config.description.Default;
import com.webxells.dis.api.config.description.Description;
import com.webxells.dis.api.error.DisException;
import com.webxells.dis.api.output.Output;
import com.webxells.dis.api.validator.SingleCallForAllValuesValidator;
import com.webxells.dis.boot.ServiceManager;
import com.webxells.dis.logging.LoggerProxyFactory;

@Description("Writes an Output")
public class ImmediateOutput extends SimpleValidator implements SingleCallForAllValuesValidator {
    private static final Logger LOGGER = LoggerProxyFactory.logger(ImmediateOutput.class);

    @Alias("output")
    @Description("Datasource to send data to")
    private OutputConfig configuration;
    @Description("Even when the writing fails, the validation will succeed")
    @Default("false")
    private boolean alwaysValidate;

    @Override
    protected boolean validateCurrentPiece(final DatasetPiece currentPiece, final MappingPart mappingPart) {
        return writeOutput(mappingPart.getConfiguration()) || alwaysValidate;
    }

    private boolean writeOutput(final MappingConfiguration configuration) {
        final Output<?> output = ServiceManager.loadByConfig(this.configuration);
        try {
            output.start();
            output.write(configuration);
            output.end();
        } catch (final DisException e) {
            LOGGER.e("Output failed", e);
            return false;
        }
        return true;
    }


    public void setConfiguration(final OutputConfig configuration) {
        this.configuration = configuration;
    }

    public void setAlwaysValidate(final boolean alwaysValidate) {
        this.alwaysValidate = alwaysValidate;
    }
}
