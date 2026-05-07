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

import com.webxells.dis.api.error.InvalidApi;
import com.webxells.dis.mail.internal.TrustAnySocketFactory;
import com.webxells.dis.mail.server.StartTls;
import com.webxells.dis.test.cases.SimpleTestCase;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Transport;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.argThat;

@ExtendWith(MockitoExtension.class)
class SendEmailTest extends SimpleTestCase {

    @Test
    void test() throws IOException, InvalidApi {
        try (MockedStatic<Transport> transport
                     = Mockito.mockStatic(Transport.class)) {
            String host = random();
            String password = random();
            String username = random();
            String subject = random();
            Participant from = randomParticipant();
            Participant to = randomParticipant();
            Participant cc1 = randomParticipant();
            Participant cc2 = randomParticipant();
            Participant bcc = randomParticipant();
            int port = random(1);
            AtomicReference<Message> message = new AtomicReference<>();
            StartTls connection =  new StartTls();
            connection.setHost(host);
            assertThrows(InvalidApi.class, connection::validate);
            connection.setPassword(password);
            connection.setUsername(username);
            connection.validate();
            connection.setPort(port);
            connection.setSslMode(StartTls.SslMode.TLS_1_2);
            connection.setTrustAnyCertificate(true);
            SendEmail fixture = new SendEmail();
            assertThrows(UnsupportedOperationException.class, fixture::receive);
            assertThrows(InvalidApi.class, fixture::validate);
            fixture.setFrom(from);
            fixture.setConnection(connection);
            fixture.setTo(List.of(to));
            fixture.validate();
            fixture.setCc(List.of(cc1, cc2));
            fixture.setBcc(List.of(bcc));
            fixture.setSubject(subject);
            fixture.setErrorOnEmptyContent(true);


            transport.when(() -> Transport.send(argThat(a -> {
                message.set(a);
                return true;
            }))).thenAnswer(a -> null);

            try (OutputStream outputStream = fixture.send()) {
                outputStream.write(random(1));
                outputStream.write(random().getBytes(StandardCharsets.UTF_8));
                outputStream.write(random().getBytes(StandardCharsets.UTF_8), 0, 4);
                outputStream.flush();
            }

            Message actualMessage = message.get();
            assertNotNull(actualMessage);
            assertEquals(subject, actualMessage.getSubject());
            Properties properties = actualMessage.getSession().getProperties();
            assertEquals(host, properties.getProperty("mail.smtp.host"));
            assertEquals(port, properties.get("mail.smtp.port"));
            assertEquals("smtp", properties.getProperty("mail.transport.protocol"));
            assertEquals("true", properties.getProperty("mail.smtp.starttls.enable"));
            assertEquals("true", properties.getProperty("mail.smtp.starttls.required"));
            assertEquals("true", properties.getProperty("mail.smtp.auth"));
            assertEquals("TLSv1.2", properties.getProperty("mail.smtp.ssl.protocols"));
            assertEquals("true", properties.getProperty("mail.smtp.auth"));
            assertEquals("false", properties.getProperty("mail.smtp.socketFactory.fallback"));
            assertEquals(TrustAnySocketFactory.class.getName(), properties.getProperty("mail.smtp.socketFactory.class"));
            assertEquals(TrustAnySocketFactory.class.getName(), properties.getProperty("mail.smtp.ssl.socketFactory.class"));

        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
    }

    private Participant randomParticipant() {
        Participant result = new Participant();
        result.setEmail(String.format("%s@%s.com", random(),  random()));
        if (random(true)) {
            result.setName(random());
        }
        result.setForceRfc822(true);
        return result;
    }

}