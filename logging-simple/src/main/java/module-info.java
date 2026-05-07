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
module com.webxells.dis.logging.simple {
    requires com.webxells.dis.api;
    requires com.webxells.dis.logging;

    requires java.logging;

    requires com.google.gson;

    exports com.webxells.dis.logging.simple;
    exports com.webxells.dis.logging.simple.freeloader;
    exports com.webxells.dis.logging.simple.appender;
    exports com.webxells.dis.logging.simple.appender.file;

    provides java.lang.System.LoggerFinder with com.webxells.dis.logging.simple.freeloader.SystemLoggerAdapter;
}