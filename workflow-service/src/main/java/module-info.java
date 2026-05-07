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
module com.webxells.dis.workflow.service {
    requires com.webxells.dis.api;
    requires com.webxells.dis.logging;
    requires com.webxells.dis.boot;
    requires com.webxells.dis.base;
    requires com.webxells.dis.event;

    exports com.webxells.dis.workflow.service;
    exports com.webxells.dis.workflow.service.refinement;
    exports com.webxells.dis.workflow.service.event;
    exports com.webxells.dis.workflow.service.config;
    exports com.webxells.dis.workflow.service.trigger;
    exports com.webxells.dis.workflow.service.meta.input;
    exports com.webxells.dis.workflow.service.meta.output;
}