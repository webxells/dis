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
package com.webxells.dis.plain.manipulator;

import com.webxells.dis.api.DatasetPiece;
import com.webxells.dis.api.config.MappingPart;
import com.webxells.dis.api.config.description.Description;
import com.webxells.dis.api.config.description.Required;
import com.webxells.dis.api.error.InvalidApi;
import com.webxells.dis.api.error.InvalidDatasetException;
import com.webxells.dis.api.manipulator.Manipulator;
import com.webxells.dis.base.SimpleDatasetPiece;
import com.webxells.dis.plain.intern.RegexComplexStringManipulation;
import java.util.List;
import java.util.regex.MatchResult;

@Description("Calls manipulators on found regexes")
public class RegexToManipulator extends RegexComplexStringManipulation {
    @Required(xor = {"manipulators"})
    @Description("Manipulator called for each regular expression found")
    private Manipulator manipulator;
    @Required(xor = {"manipulator"})
    @Description("Manipulators called for each regular expression found")
    private List<Manipulator> manipulators;
    @Description("regular expression group taken for manipulation")
    private Integer groupAsDataset;

    @Override
    public void validate() throws InvalidApi {
        super.validate();
        if (null == manipulator && null == manipulators) {
            throw new InvalidApi("manipulator or manipulators required");
        }
        if (null != manipulator) {
            manipulator.validate();
        } else {
            for (final Manipulator manipulator : manipulators) {
                manipulator.validate();
            }
        }
    }

    @Override
    protected String complexManipulation(final MatchResult match, final MappingPart part) throws InvalidDatasetException {
        final String toReplace = match.group(getGroup(groupAsDataset, match));
        final DatasetPiece content = new SimpleDatasetPiece(toReplace);
        callManipulate(content, part);
        if (content.value().isPresent()) {
            return content.value().get();
        }
        return getNotFound(toReplace);
    }

    private void callManipulate(final DatasetPiece content, final MappingPart part) throws InvalidDatasetException {
        if (null != manipulator) {
            manipulator.manipulate(content, part);
        } else if (null != manipulators) {
            for (final Manipulator manipulator : manipulators) {
                manipulator.manipulate(content, part);
            }
        }
    }

    private String getNotFound(final String original) throws InvalidDatasetException {
        switch (noResultStrategy) {
            case IGNORE:
                return original;
            case EMPTY:
                return "";
        }
        throw new InvalidDatasetException("Could not evaluate: " + original);
    }

    public void setManipulator(final Manipulator manipulator) {
        this.manipulator = manipulator;
    }

    public void setGroupAsDataset(final Integer groupAsDataset) {
        this.groupAsDataset = groupAsDataset;
    }

    public void setManipulators(final List<Manipulator> manipulators) {
        this.manipulators = manipulators;
    }
}