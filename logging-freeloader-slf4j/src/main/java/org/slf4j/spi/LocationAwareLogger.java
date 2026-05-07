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
package org.slf4j.spi;

import org.slf4j.Marker;
import org.slf4j.Logger;

public interface LocationAwareLogger extends Logger {
    final public int TRACE_INT = 0;
    final public int DEBUG_INT = 10;
    final public int INFO_INT = 20;
    final public int WARN_INT = 30;
    final public int ERROR_INT = 40;

    public void log(Marker marker, String fqcn, int level, String message, Object[] argArray, Throwable t);


}