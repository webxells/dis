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
package com.webxells.dis.test.cases.websocket;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import org.opentest4j.AssertionFailedError;

public class WebSocketClient {
    private static final long SLEEP_TIMEOUT = 200;
    private static final int PING_TIMEOUT = 1500;

    private final Socket client;
    private final BufferedReader in;
    private final BufferedOutputStream out;
    private final MessageTransition messageTransition;
    private final DataInputStream dataIn;
    private final StringBuilder pingData = new StringBuilder();

    private Thread clientThread;
    private volatile boolean stop;
    private volatile long lastResponse;

    public WebSocketClient(final Socket client) throws IOException {
        this.client = client;
        in = new BufferedReader(new InputStreamReader(client.getInputStream()));
        dataIn = new DataInputStream(client.getInputStream());
        out = new BufferedOutputStream(client.getOutputStream());
        this.messageTransition = new MessageTransition();
    }

    public void close() throws IOException {
        stop = true;
        messageTransition.clear();
        client.close();
        in.close();
        dataIn.close();
        out.close();
        try {
            Thread.sleep(SLEEP_TIMEOUT);
        } catch (final InterruptedException ignored) {
            //don't yell on shutdown
        }
        if (clientThread.isAlive()) {
            clientThread.interrupt();
        }
    }

    public String readLine() throws IOException {
        return in.readLine();
    }

    public String readDecodedData() throws IOException {
        if (!hasData()) {
            return null;
        }
        final byte firstByte = dataIn.readByte();
        //if first bit of first byte is 1 => message finished
        final boolean fin = (firstByte & 0x80) != 0;
        if (!fin) {
            throw new AssertionFailedError("we don't accept unfinished data");
        }
        updateResponse();
        //last bit of first byte is opCode => 0001 is for text
        final byte opCode = (byte) (firstByte & 0x0F);
        if (!isByte(opCode,  0x1)) {
            if (isByte(opCode, 0xA)) {
                //pong
                assertCorrectPong();
            }
            else if (isByte(opCode,  0x9)) {
                //ping
                sendPong();
            } else {
                throw new AssertionFailedError("we don't accept data that is no text");
            }
            return null;
        }
        final byte secondByte = dataIn.readByte();
        //first bit of second byte tells if data is masked
        final boolean masked = (secondByte & 0x80) != 0;
        //other is length of provided data length or data length if it fits
        final int dataLengthLength = secondByte & 0x7F;
        final long dataLength = switch (dataLengthLength) {
            case 126 -> dataIn.readShort();
            case 127 -> dataIn.readLong();
            //we do not need data length, length of length is length ;)
            default -> dataLengthLength;
        };

        if (Integer.MAX_VALUE - 8 < dataLength) {
            throw new AssertionFailedError("we dont accept data that exceeds 2^32 length");
        }

        //get masking key if provided
        final byte[] maskingKey = new byte[4];
        if (masked) {
            dataIn.readFully(maskingKey);
        }

        //rest is payload
        byte[] payloadData = new byte[(int) dataLength];
        dataIn.readFully(payloadData);

        if (masked) {
            for (int i = 0; i < dataLength; i++) {
                payloadData[i] ^= maskingKey[i % 4];
            }
        }
        return new String(payloadData, StandardCharsets.UTF_8);
    }

    private boolean isByte(final byte byteToCheck, final int compare) {
        return (byteToCheck & compare) == compare;
    }

    public void sendRawText(final String text) throws IOException {
        final byte[] toWrite = text.getBytes(StandardCharsets.UTF_8);
        out.write(toWrite, 0, toWrite.length);
        out.flush();
    }


    public void sendRawText(final byte[] toWrite) throws IOException {
        out.write(toWrite, 0, toWrite.length);
        out.flush();
    }

    public void startWatchInput() throws IOException {
        if (null != clientThread) {
            throw new IllegalStateException("client thread is already started");
        }
        readMessage();
        clientThread = new Thread(this::monitorIncoming);
        clientThread.start();
    }

    private void updateResponse() {
        lastResponse = System.currentTimeMillis();
    }

    private void readMessage() throws IOException {
        final String request = readDecodedData();
        if (null != request) {
            messageTransition.newRequest(request);
        }
        final String response = messageTransition.getNextResponse();
        if (null != response) {
            sendEncodedText(response);
        }
    }

    private void monitorIncoming() {
        try {
            while (!stop) {
                if (hasData()) {
                    readMessage();
                } else if (messageTransition.hasACommand()) {
                    sendEncodedText(messageTransition.getNextCommand());
                } else if (PING_TIMEOUT < System.currentTimeMillis() - lastResponse) {
                    sendPing();
                }  else {
                    Thread.sleep(SLEEP_TIMEOUT);
                }
            }
        } catch (final IOException e) {
            throw new RuntimeException("error while monitor", e);
        } catch (final InterruptedException ignored) {
            //dont yell if shut down
        }
    }

    private void sendPong() throws IOException {
        //read data of ping
        final int length = dataIn.readByte();
        final byte[] payloadData = new byte[(int) length];
        dataIn.readFully(payloadData);
        final ByteArrayOutputStream result = new ByteArrayOutputStream();
        //opcode of pong is 0xA
        result.write(0b10001010);
        //just copy read data
        result.write(length);
        result.write(payloadData, 0, length);
        sendRawText(result.toByteArray());
        updateResponse();
    }

    private synchronized void sendPing() throws IOException {
        if (0 < pingData.length()) {
            final ByteArrayOutputStream result = new ByteArrayOutputStream();
            //opcode of ping is 0x9
            result.write(0b10001001);
            //some random ping data
            pingData.setLength(0);
            for (int i = rand(125); i-- > 0; ) {
                pingData.append((char) (rand(42) + 48));
            }
            result.write(pingData.length());
            result.write(pingData.toString().getBytes(StandardCharsets.UTF_8));
            sendRawText(result.toByteArray());
        }
    }

    private void assertCorrectPong() throws IOException {
        // read payload
        final int length = dataIn.readByte();
        byte[] payloadData = new byte[(int) length];
        dataIn.readFully(payloadData);

        if (pingData.isEmpty()) {
            //you might get a pong without ever sending a ping
            //=> ignore it
            return;
        }
        //assert same data as ping
        if (pingData.length() != length) {
            throw new AssertionFailedError("pong data length is incorrect");
        }
        if (!pingData.toString().equals(new String(payloadData))) {
            throw new AssertionFailedError("pong data is incorrect");
        }
        updateResponse();
        pingData.setLength(0);
    }

    private int rand(final int max) {
        return Math.toIntExact(Math.round(Math.random() * max));
    }

    private boolean hasData() throws IOException {
        return dataIn.available() > 0 || in.ready();
    }

    public void sendEncodedText(final String message) throws IOException {
        final byte[] messageBytes = message.getBytes(StandardCharsets.UTF_8);
        final int messageLength = messageBytes.length;

        final ByteArrayOutputStream result = new ByteArrayOutputStream();
        // Set FIN (is whole message => 1)
        // set RSV1-3 to 0
        // set opCode (to 0001 => text)
        result.write(0b10000001);

        //next optional length of data length
        if (messageLength <= 125) {
            result.write(messageLength);
        } else if (messageLength <= 65535) {
            //1 full byte required
            result.write(126);
            result.write((messageLength >> 8) & 0xFF);
            result.write(messageLength & 0xFF);
        } else {
            //2 bytes required
            result.write(127);
            for (int i = 7; i >= 0; i--) {
                result.write((byte) ((messageLength >> (i * 8)) & 0xFF));
            }
        }
        result.write(messageBytes);
        sendRawText(result.toByteArray());
    }

    public MessageTransition getMessageTransition() {
        return messageTransition;
    }
}