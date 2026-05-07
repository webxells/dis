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
package com.webxells.dis.server.rest.websocket.resource;

import com.webxells.dis.api.error.InvalidApi;
import com.webxells.dis.api.resource.Resource;
import com.webxells.dis.server.rest.TransactionQueue;
import com.webxells.dis.server.rest.websocket.transaction.WaitForResponse.WebSocketTransactionEntity;
import java.io.InputStream;
import java.io.OutputStream;

public class WebsocketReceiver implements Resource {
    private String name;
    private boolean useCurrentOpenTransaction;
    private TransactionQueue<WebSocketTransactionEntity> transactionQueue;

    @Override
    public void validate() throws InvalidApi {
        if (null == name) {
            throw new InvalidApi("name is null");
        }
    }

    @Override
    public OutputStream send() {
        throw new UnsupportedOperationException("Not supported, use WebsocketSender instead");
    }

    @Override
    public InputStream receive() {
        return getEntity().request().getRequest();
    }

    private WebSocketTransactionEntity getEntity() {
        return useCurrentOpenTransaction ?
                transactionQueue.current() : transactionQueue.next();
    }

    public void setName(final String name) {
        this.name = name;
        transactionQueue = TransactionQueue.getQueue(name);
    }
}