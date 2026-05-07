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
package com.webxells.dis.logging.simple;

import com.webxells.dis.api.Logger;
import com.webxells.dis.logging.simple.appender.Appender;
import com.webxells.dis.logging.simple.freeloader.Freeloader;
import java.util.List;
import java.util.Map;

public record DisLogManagerConfig(List<Appender> appenders,
                                  Logger.LogLevel defaultLevel,
                                  String defaultPattern,
                                  Map<String, Logger.LogLevel>defaultExceptions,
                                  List<Freeloader.Type> freeloaders,
                                  boolean synchronLogging) {

}