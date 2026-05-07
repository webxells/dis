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
package com.webxells.dis.server.rest.websocket.transaction;

import com.webxells.dis.server.rest.Transaction;
import com.webxells.dis.server.rest.websocket.WebSocketTransaction;

public class WaitForResponse implements Transaction<WebSocketTransaction> {
    public record WebSocketTransactionEntity(WebSocketTransaction request)
            implements Transaction.Entity<WebSocketTransaction> {
        @Override
        public WebSocketTransaction request() {
            return request;
        }
    }

    @Override
    public WebSocketTransactionEntity newEntity(final WebSocketTransaction request) {
        return new WebSocketTransactionEntity(request);
    }
}