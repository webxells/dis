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
package com.webxells.dis.workflow.service.validator;

import com.webxells.dis.api.DatasetPiece;
import com.webxells.dis.api.Logger;
import com.webxells.dis.api.config.MappingPart;
import com.webxells.dis.api.validator.Validator;
import com.webxells.dis.boot.WorkflowSecurity;
import com.webxells.dis.event.EventManager;
import com.webxells.dis.logging.LoggerProxyFactory;
import com.webxells.dis.workflow.service.event.DatasetSkippedByValidator;

import java.util.Objects;

public class SkipDatasetValidator implements Validator {
    private static final Logger LOGGER = LoggerProxyFactory.logger(SkipDatasetValidator.class);
    private EventManager.ContextProxy eventManager;
    private Validator child;

    @Override
    public boolean validate(final DatasetPiece currentPiece, final MappingPart mappingPart) {
        if (!child.validate(currentPiece, mappingPart)) {
            getEventManager(mappingPart).trigger(new DatasetSkippedByValidator());
            LOGGER.debug("Dataset skipped by Validator");
            return false;
        }

        return true;
    }

    @Override
    public String getType() {
        return SkipDatasetValidator.class.getName();
    }

    public void setChild(final Validator child) {
        this.child = child;
    }

    private EventManager.ContextProxy getEventManager(final MappingPart mappingPart) {
        if (Objects.isNull(eventManager)) {
            return eventManager = EventManager.instance()
                    .new ContextProxy(WorkflowSecurity.instance().getJob(
                            mappingPart.getConfiguration().getJobName()));
        }
        return eventManager;
    }
}