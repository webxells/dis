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
package org.apache.commons.logging;

public interface Log {
    void fatal(Object message);
    void fatal(Object message, Throwable t);
    void error(Object message);
    void error(Object message, Throwable t);
    void warn(Object message);
    void warn(Object message, Throwable t);
    void info(Object message);
    void info(Object message, Throwable t);
    void debug(Object message);
    void debug(Object message, Throwable t);

    void trace(Object message);
    void trace(Object message, Throwable t);
    boolean isDebugEnabled();
    boolean isErrorEnabled();
    boolean isFatalEnabled();
    boolean isInfoEnabled();
    boolean isTraceEnabled();
    boolean isWarnEnabled();
}