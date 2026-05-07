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
package com.webxells.dis.server.rest;

import com.webxells.dis.api.error.InputOutputError;
import com.webxells.dis.api.resource.Resource;

import com.webxells.dis.server.rest.transaction.HttpTransaction;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;
import java.util.Optional;

public class ServerOutput implements Resource {
    private String name;
    private TransactionQueue<HttpTransaction.Entity> transactionQueue;
    private Boolean noOtherQueueUser;

    @Override
    public OutputStream send() throws InputOutputError {
        return getNextBody()
                .orElseThrow(() -> new RuntimeException("could not find any transaction for ".concat(name)));
    }

    private Optional<OutputStream> getNextBody() {
        if (null == noOtherQueueUser) {
            noOtherQueueUser = null == transactionQueue.current();
        }
        if (noOtherQueueUser) {
            transactionQueue.next();
        }
        return Optional.ofNullable(transactionQueue.current())
                .map(a -> a.request().respond().getBody());
    }

    @Override
    public InputStream receive() {
        throw new UnsupportedOperationException("makes no sense");
    }

    public void setName(final String name) {
        this.name = name;
        transactionQueue = TransactionQueue.getQueue(name);
    }

    public void setHeader(final Map<String, String> stringStringMap) {
    }
}