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
package com.webxells.dis.mail;

import com.webxells.dis.api.config.description.Default;
import com.webxells.dis.api.config.description.Description;
import com.webxells.dis.api.config.description.Required;
import com.webxells.dis.api.error.InvalidApi;
import com.webxells.dis.api.resource.Resource;
import com.webxells.dis.mail.internal.MailSendingOutputStream;
import com.webxells.dis.mail.server.Server;
import jakarta.mail.Address;
import jakarta.mail.Message;
import jakarta.mail.internet.AddressException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * {@code @ToDo:
 *  Mime/Types
 *  Attachments
 *  Dynamic participants
 *  Dynamic subject
 *  Auth methods}
 */
@Description("Sends an email via smtp server")
public class SendEmail implements Resource {
    private Server connection;
    private Participant from;
    @Required(or = {"cc", "bcc"})
    private List<Participant> to;
    @Required(or = {"to", "bcc"})
    private List<Participant> cc;
    @Required(or = {"cc", "to"})
    private List<Participant> bcc;
    private String subject;
    @Description("This will accept any certificate - DO NOT USE IN PRODUCTION!")
    @Default("false")
    private boolean errorOnEmptyContent;

    @Override
    public void validate() throws InvalidApi {
        if (null == connection || null == from || (null == to && null == cc && null == bcc)) {
            throw new InvalidApi("required fields are missing");
        }
    }

    @Override
    public OutputStream send() {
        return new MailSendingOutputStream(this);
    }

    @Override
    public InputStream receive() {
        throw new UnsupportedOperationException("Makes no sense");
    }

    public void setConnection(final Server connection) {
        this.connection = connection;
    }

    public void setFrom(final Participant from) {
        this.from = from;
    }

    public void setTo(final List<Participant> to) {
        this.to = to;
    }

    public void setCc(final List<Participant> cc) {
        this.cc = cc;
    }

    public void setBcc(final List<Participant> bcc) {
        this.bcc = bcc;
    }

    public void setSubject(final String subject) {
        this.subject = subject;
    }

    public void setErrorOnEmptyContent(final boolean errorOnEmptyContent) {
        this.errorOnEmptyContent = errorOnEmptyContent;
    }

    public Message createServerMessage() {
        return connection.createServerMessage();
    }

    public Address getSender() throws AddressException {
        return from.getAddress();
    }

    public String getSubject() {
        return subject;
    }

    public boolean IsErrorOnEmptyContent() {
        return errorOnEmptyContent;
    }

    public Address[] getTo() throws AddressException {
        return getAddressList(to);
    }

    public Address[] getCc() throws AddressException {
        return getAddressList(cc);
    }

    public Address[] getBcc() throws AddressException {
        return getAddressList(bcc);
    }

    private Address[] getAddressList(final List<Participant> participants) throws AddressException {
        if (null != participants) {
            final ArrayList<Address> addresses = new ArrayList<>();
            for (final Participant participant : participants) {
                addresses.add(participant.getAddress());
            }
            return addresses.toArray(new Address[0]);
        }
        return new Address[0];
    }
}