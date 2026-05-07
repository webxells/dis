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
package com.webxells.dis.info.input;

import com.webxells.dis.api.config.MappingConfiguration;
import com.webxells.dis.api.config.Refinement;
import com.webxells.dis.api.config.description.Required;
import com.webxells.dis.api.config.description.RespectsRefinements;
import com.webxells.dis.api.error.DisException;
import com.webxells.dis.api.error.InputOutputError;
import com.webxells.dis.api.input.Input;
import com.webxells.dis.base.refinement.SimpleRefinement;
import java.util.List;

/**
 * Used for test purposes only, to test various cases that can occur.
 */
@RespectsRefinements(TestDisClass.TestRefinement.class)
public class TestDisClass implements Input<TestDisClassConfig> {
    public static class TestRefinement extends SimpleRefinement { }

    private final TestDisClassConfig configuration;

    public TestDisClass(final TestDisClassConfig configuration) {
        this.configuration = configuration;
    }

    @Override
    public int read(final MappingConfiguration from) throws InputOutputError {
        return 0;
    }

    @Override
    public boolean hasNext() throws InputOutputError {
        return false;
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public void start() throws DisException {

    }

    @Override
    public void end() throws DisException {

    }
}