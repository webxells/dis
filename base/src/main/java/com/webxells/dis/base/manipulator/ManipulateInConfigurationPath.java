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
import com.webxells.dis.api.MappingPortrayal;
import com.webxells.dis.api.config.MappingConfiguration;
import com.webxells.dis.api.config.MappingPart;
import com.webxells.dis.api.config.description.Default;
import com.webxells.dis.api.config.description.Description;
import com.webxells.dis.api.config.description.Required;
import com.webxells.dis.api.error.InvalidApi;
import com.webxells.dis.api.error.InvalidDatasetException;
import com.webxells.dis.api.manipulator.Manipulator;
import com.webxells.dis.api.manipulator.SingleCallForAllValuesManipulator;
import com.webxells.dis.base.SimpleDatasetPiece;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.stream.Collectors;

@Description("Executes Manipulator in other MappingParts")
public class ManipulateInConfigurationPath implements Manipulator {
    public enum Path {
        @Description("Looks up portrayal in current configuration")
        CURRENT(a -> List.of(a.getConfiguration())),
        @Description("Looks into the subData configurations of this MappingPart")
        CHILDREN(MappingPart::getSubData),
        @Description("Looks into the first subData configuration of this MappingPart")
        FIRST_CHILD(a -> a.getSubData().isEmpty() ? List.of() : List.of(a.getSubData().get(0))),
        @Description("Looks for MappingPart in the parent configuration")
        PARENT(a -> a.getConfiguration().parent().map(List::of).orElse(List.of()));

        private final Function<MappingPart, List<MappingConfiguration>> getElements;

        Path(final Function<MappingPart, List<MappingConfiguration>> getElements) {
            this.getElements = getElements;
        }
    }

    @Description("Defines location of the MappingPart")
    public static class PathDefinition {
        @Required
        public Path pathType;
        @Required
        @Description("Defines MappingPart to find")
        public MappingPortrayal destination;
    }

    @Required
    private List<PathDefinition> path;
    @Required
    private Manipulator manipulator;
    @Description("Use content of MappingPart to manipulate")
    @Default("false")
    private boolean callWithOriginalDataset;

    @Override
    public void manipulate(final DatasetPiece currentPiece, final MappingPart mappingPart) throws InvalidDatasetException {
        for (final MappingPart part : resolvePath(mappingPart)) {
            if (callWithOriginalDataset) {
               callOriginal(part);
            } else {
                manipulator.manipulate(currentPiece, part);
            }
        }
    }

    private void callOriginal(final MappingPart part) throws InvalidDatasetException {
        final List<DatasetPiece> content = new LinkedList<>(part.getDataset().getContent());
        if (content.isEmpty()) {
            content.add(new SimpleDatasetPiece(null));
        }
        for (final DatasetPiece originalPiece : content) {
            manipulator.manipulate(originalPiece, part);
            if (manipulator instanceof SingleCallForAllValuesManipulator) {
                break;
            }
        }
    }

    private List<MappingPart> resolvePath(final MappingPart mappingPart) {
        final AtomicReference<List<MappingPart>> result = new AtomicReference<>(List.of(mappingPart));
        path.forEach(a -> result.set(result.get().stream()
                .flatMap(b -> a.pathType.getElements.apply(b).stream())
                .flatMap(b -> b.getByPortrayal(a.destination).stream())
                .collect(Collectors.toList())));
        return result.get();
    }

    @Override
    public void validate() throws InvalidApi {
        if (null == path || path.isEmpty() || null == manipulator) {
            throw new InvalidApi("Required fields are missing");
        }
        if (path.stream().anyMatch(a -> null == a || null == a.destination || null == a.pathType)) {
            throw new InvalidApi("Invalid path provided");
        }
        manipulator.validate();
    }

    public void setPath(final List<PathDefinition> path) {
        this.path = path;
    }

    public void setManipulator(final Manipulator manipulator) {
        this.manipulator = manipulator;
    }

    public void setCallWithOriginalDataset(final boolean callWithOriginalDataset) {
        this.callWithOriginalDataset = callWithOriginalDataset;
    }
}