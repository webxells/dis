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
package com.webxells.dis.mail.internal;

import com.webxells.dis.api.Logger;
import com.webxells.dis.logging.LoggerProxyFactory;
import com.webxells.dis.mail.SendEmail;
import jakarta.mail.Address;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Transport;
import java.io.IOException;
import java.io.OutputStream;

public class MailSendingOutputStream extends OutputStream {
    private static final Logger LOGGER = LoggerProxyFactory.logger(MailSendingOutputStream.class);

    private final StringBuilder content = new StringBuilder();
    private final SendEmail mail;

    public MailSendingOutputStream(final SendEmail mail) {
        this.mail = mail;
    }

    @Override
    public void write(final int b) {
        content.append((char) b);
    }

    @Override
    public void write(final byte[] b) {
        content.append(new String(b));
    }

    @Override
    public void write(final byte[] b, final int off, final int len) {
        content.append(new String(b, off, len));
    }

    @Override
    public void flush() throws IOException {
        if (content.isEmpty()) {
            if (mail.IsErrorOnEmptyContent()) {
                throw new IOException("Empty mail");
            }
            LOGGER.w("Sending empty mail was prevented");
            return;
        }
        sendMail();
        content.setLength(0);
    }

    @Override
    public void close() {
        content.setLength(0);
    }

    private void sendMail() throws IOException {
        try {
            final Message message = mail.createServerMessage();
            message.setFrom(mail.getSender());
            addRecipients(message, Message.RecipientType.TO, mail.getTo());
            addRecipients(message, Message.RecipientType.CC, mail.getCc());
            addRecipients(message, Message.RecipientType.BCC, mail.getBcc());
            message.setSubject(mail.getSubject());
            message.setContent(content.toString(), "text/plain;charset=utf-8");
            LOGGER.t("Sending mail...");
            Transport.send(message);
            LOGGER.d("Successfully send mail!");
        } catch (final MessagingException e) {
            throw new IOException("Could not send mail", e);
        }
    }

    private void addRecipients(final Message message, final Message.RecipientType type, final Address[] addresses) throws MessagingException {
        if (null != addresses && 0 < addresses.length) {
            message.addRecipients(type, addresses);
        }
    }
}