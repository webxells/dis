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

import com.webxells.dis.api.resource.Resource;
import com.webxells.dis.server.rest.intern.ClearRequestOnClose;
import com.webxells.dis.server.rest.source.Source;
import com.webxells.dis.server.rest.transaction.HttpTransaction;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Optional;

public class ServerInput implements Resource {
    private TransactionQueue<HttpTransaction.Entity> transactionQueue;
    private Source source;
    private boolean alreadyFetchedRequest;

    @Override
    public OutputStream send() {
        throw new UnsupportedOperationException("You can only read by server");
    }

    @Override
    public InputStream receive() {
        return Optional.ofNullable(transactionQueue)
                .map(a -> alreadyFetchedRequest ? a.current() : transactionQueue.next())
                .map(a -> new ClearRequestOnClose(a.request(), b -> source.parse(b)))
                .orElse(null);
    }

    public void setName(final String name) {
        transactionQueue = TransactionQueue.getQueue(name);
    }

    public void setSource(final Source source) {
        this.source = source;
    }

    public boolean isAlreadyFetchedRequest() {
        return alreadyFetchedRequest;
    }

    public void setAlreadyFetchedRequest(final boolean alreadyFetchedRequest) {
        this.alreadyFetchedRequest = alreadyFetchedRequest;
    }
}