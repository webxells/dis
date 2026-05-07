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

import com.webxells.dis.api.error.InputOutputError;
import com.webxells.dis.api.error.InvalidApi;
import com.webxells.dis.api.resource.Resource;
import com.webxells.dis.server.rest.TransactionQueue;
import com.webxells.dis.server.rest.websocket.transaction.WaitForResponse.WebSocketTransactionEntity;
import java.io.InputStream;
import java.io.OutputStream;

public class WebsocketSender implements Resource {
    public enum Type {
        TEXT, BINARY
    }
    private static final int DEFAULT_MESSAGE_CAPACITY = 1_048_576;

    private Type sendingType = Type.TEXT;
    private String name;
    private boolean instantFlush = false;
    private int sendMessageMaxCapacity = DEFAULT_MESSAGE_CAPACITY;

    private TransactionQueue<WebSocketTransactionEntity> transactionQueue;
    private Boolean shouldUseNext;

    @Override
    public void validate() throws InvalidApi {
        if (null == name) {
            throw new InvalidApi("name is null");
        }
    }

    @Override
    public OutputStream send() throws InputOutputError {
        return switch (sendingType) {
            case TEXT -> getEntity().request().getTextResponseStream(sendMessageMaxCapacity, instantFlush);
            case BINARY -> getEntity().request().getBinaryResponseStream(sendMessageMaxCapacity, instantFlush);
        };
    }

    @Override
    public InputStream receive() {
        throw new UnsupportedOperationException("Not supported, use WebsocketReceiver instead");
    }

    public void setSendingType(final Type sendingType) {
        this.sendingType = sendingType;
    }

    public void setInstantFlush(final boolean instantFlush) {
        this.instantFlush = instantFlush;
    }

    public void setSendMessageMaxCapacity(final int sendMessageMaxCapacity) {
        this.sendMessageMaxCapacity = sendMessageMaxCapacity;
    }

    public void setName(final String name) {
        this.name = name;
        transactionQueue = TransactionQueue.getQueue(name);
    }

    private WebSocketTransactionEntity getEntity() {
        if (null == shouldUseNext) {
            shouldUseNext = null == transactionQueue.current();
        }
        return shouldUseNext ? transactionQueue.next() : transactionQueue.current();
    }
}