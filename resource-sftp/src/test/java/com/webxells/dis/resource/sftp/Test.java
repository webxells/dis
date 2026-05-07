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
package com.webxells.dis.resource.sftp;

import com.webxells.dis.api.error.InputOutputError;
import java.io.IOException;

public class Test {
    public static void main(String[] args) throws InputOutputError, IOException {
        SftpResource resource = new SftpResource();
        SftpConfig config = new SftpConfig();
        resource.setFileRegex("\\.xml");
        resource.setPath("upload");
        config.setPassword("pass");
        config.setIgnoreUnknownHostError(true);
        config.setHost("127.0.0.1");
        config.setUsername("foo");
        resource.setConnection(config);

        System.out.println("start....");
        String content = new String(resource.receive().readAllBytes());
        System.out.println(content);
    }
}
