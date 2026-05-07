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
package com.webxells.dis.rest.resource;

import com.webxells.dis.api.config.description.Description;
import com.webxells.dis.api.resource.MultiResource;
import com.webxells.dis.rest.content.PaginatingContentStrategy;
import java.io.OutputStream;
import java.util.Optional;

@Description("Executes a rest request repetitive by altering data (like rest parameters)")
public class PaginatingCall extends Call implements MultiResource {
    private int round;

    @Override
    public void reset() {
        setRound(0);
    }

    private void setRound(final int current) {
        round = current;
        Optional.ofNullable(getRequestContentStrategy())
                .ifPresent(a -> a.stream()
                        .filter(b -> b instanceof PaginatingContentStrategy)
                        .forEach(b -> ((PaginatingContentStrategy) b).setContent(current))
                );
    }

    @Override
    public RefreshResult refresh() {
        setRound(round + 1);
        return RefreshResult.UNKNOWN;
    }

    @Override
    public OutputStream send() {
        throw new UnsupportedOperationException("makes no sense, hu?");
    }
}