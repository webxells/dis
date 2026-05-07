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
package com.webxells.dis.base.input;

import com.webxells.dis.api.Logger;
import com.webxells.dis.api.config.InputConfig;
import com.webxells.dis.api.config.MappingConfiguration;
import com.webxells.dis.api.error.DisException;
import com.webxells.dis.api.error.InputOutputError;
import com.webxells.dis.api.input.Input;
import com.webxells.dis.base.SimpleDatasetPiece;
import com.webxells.dis.base.SimpleMappingPortrayal;
import com.webxells.dis.boot.ServiceManager;
import com.webxells.dis.logging.LoggerProxyFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class RightJoin implements Input<RightJoinConfiguration> {
    private final static Logger LOGGER = LoggerProxyFactory.logger(RightJoin.class);

    private final Input<?> child;
    private final Input<?> root;
    private final String name;
    private final InputConfig rootConfig;
    private final boolean continueDespiteNoData;

    private MappingConfiguration tempRootResult;
    private int tempRootResultCount;
    private boolean abortDueChildReturning0;

    public RightJoin(final RightJoinConfiguration config) {
        name = config.getName();
        rootConfig = config.getRootConfig();
        child = ServiceManager.loadByConfig(Objects.requireNonNull(config.getChildConfig()));
        root = ServiceManager.loadByConfig(Objects.requireNonNull(rootConfig));
        continueDespiteNoData = config.isContinueDespiteNoData();
    }

    @Override
    public int hashCode() {
        return Objects.hash(child, root, getName());
    }

    @Override
    public int read(final MappingConfiguration from) throws InputOutputError {
        assertRootIsRead(from);
        if (0 == tempRootResultCount) {
            return 0;
        }
        pasteRootResultToCurrent(from);
        final int fromChild = fromChild(from);
        if (0 == fromChild && !continueDespiteNoData) {
            /*
             * @todo: too late - already in reading
             */
            LOGGER.w("RightJoins child returned 0 - unnecessary reading detected - will abort on next turn (See DIS-144)");
            abortDueChildReturning0 = true;
            return 0;
        }
        return tempRootResultCount + fromChild;
    }

    private void assertRootIsRead(final MappingConfiguration from) throws InputOutputError {
        if (null == tempRootResult || !child.hasNext()) {
            try {
                if (null != tempRootResult) {
                    child.end();
                }
                tempRootResult = from.copy();
                tempRootResultCount = root.read(tempRootResult);
                child.start();
            } catch (final DisException e) {
                throw new InputOutputError("Could not (re)start child input", e);
            }
        }
    }

    private int fromChild(final MappingConfiguration from) throws InputOutputError {
        return  child.read(from);
    }

    private void pasteRootResultToCurrent(final MappingConfiguration from) {
        getReferences(rootConfig)
                        .forEach(nameToCopy ->  tempRootResult.partsBySource(nameToCopy)
                                .forEach(rootPart -> from.getByPortrayal(SimpleMappingPortrayal.source(rootPart))
                                        .ifPresent(part -> rootPart.getDataset().getContent().stream()
                                                .flatMap(a -> a.value().stream())
                                                .forEach(a -> part.getDataset().collect(new SimpleDatasetPiece(a))))));
    }

    private List<String> getReferences(final InputConfig rootConfig) {
        if (rootConfig instanceof JoinConfiguration joinConfiguration) {
            final List<String> result = new ArrayList<>(getReferences(joinConfiguration.getRootConfig()));
            joinConfiguration.getChildren().forEach(a -> result.add(a.getInputName()));
            return result;
        }
        if (rootConfig instanceof RightJoinConfiguration rightConfiguration) {
            final List<String> result = new ArrayList<>(getReferences(rightConfiguration.getRootConfig()));
            result.addAll(getReferences(rightConfiguration.getChildConfig()));
            return result;
        }
        return List.of(rootConfig.getName());
    }

    @Override
    public boolean hasNext() throws InputOutputError {
        if (abortDueChildReturning0) {
            LOGGER.i("Aborting due 0-child-returning-policy (See DIS-144)");
            return false;
        }
        return root.hasNext() || (null != tempRootResult && child.hasNext());
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void start() throws DisException {
        root.start();
    }

    @Override
    public void end() throws DisException {
        root.end();
        if (null != tempRootResult) {
            child.end();
            tempRootResult = null;
        }
        abortDueChildReturning0 = false;
    }
}