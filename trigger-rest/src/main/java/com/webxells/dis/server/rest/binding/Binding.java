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
package com.webxells.dis.server.rest.binding;

import com.webxells.dis.api.config.ConfigurableByType;
import java.net.InetAddress;

public interface Binding extends ConfigurableByType {
    public InetAddress parse();

    /**
     * like hashCode just mandatory
     * @return hash for recognition
     */
    public String hash();

    public default String getDescription() {
        return hash();
    }
}