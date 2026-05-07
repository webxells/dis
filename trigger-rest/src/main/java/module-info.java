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
module com.webxells.dis.trigger.server.rest {
    requires com.webxells.dis.api;
    requires com.webxells.dis.base;
    requires com.webxells.dis.logging;
    requires com.webxells.dis.localfile;

    requires jdk.httpserver;
    requires java.net.http;

    exports com.webxells.dis.server.rest;
    exports com.webxells.dis.server.rest.websocket;
    exports com.webxells.dis.server.rest.websocket.resource;
    exports com.webxells.dis.server.rest.websocket.transaction;
    exports com.webxells.dis.server.rest.strategy;
    exports com.webxells.dis.server.rest.source;
    exports com.webxells.dis.server.rest.binding;
    exports com.webxells.dis.server.rest.http;
    exports com.webxells.dis.server.rest.filter.auth;
    exports com.webxells.dis.server.rest.transaction;
}