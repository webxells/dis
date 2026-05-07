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
module com.webxells.dis.json {
    requires com.webxells.dis.api;
    requires com.webxells.dis.base;
    requires com.webxells.dis.logging;

    requires com.google.gson;

    opens com.webxells.dis.json to com.google.gson;
    opens com.webxells.dis.json.intern to com.google.gson;
    opens com.webxells.dis.json.input to com.google.gson;
    opens com.webxells.dis.json.output to com.google.gson;
    opens com.webxells.dis.json.config to com.google.gson;

    exports com.webxells.dis.json;
    exports com.webxells.dis.json.refinement;
    exports com.webxells.dis.json.config;
    exports com.webxells.dis.json.config.map;
    exports com.webxells.dis.json.http.filter;
    exports com.webxells.dis.json.discover;
    exports com.webxells.dis.json.input;
    exports com.webxells.dis.json.output;
}