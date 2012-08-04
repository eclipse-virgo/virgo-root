/*******************************************************************************
 * Copyright (c) 2011 SAP AG
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Lazar Kirchev, SAP AG - initial contribution
 ******************************************************************************/

package org.eclipse.virgo.osgi.console.telnet;

import org.junit.Assert;
import org.junit.Test;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class TelnetConsoleSessionTests {

    private static final int TEST_CONTENT = 100;

    private static final int IAC = 255;

    @Test
    public void testConsoleSession() throws Exception {
        ServerSocket servSocket = null;
        Socket socketClient = null;
        Socket socketServer = null;
        TelnetConsoleSession consoleServer = null;
        OutputStream outClient = null;
        OutputStream outServer = null;

        try {
            servSocket = new ServerSocket(0);
            socketClient = new Socket("localhost", servSocket.getLocalPort());
            socketServer = servSocket.accept();

            consoleServer = new TelnetConsoleSession(socketServer, null);
            consoleServer.start(2000);

            outClient = socketClient.getOutputStream();
            outClient.write(TEST_CONTENT);
            outClient.write('\n');
            outClient.flush();

            InputStream input = consoleServer.getInput();
            int in = input.read();
            Assert.assertTrue("Server received [" + in + "] instead of [" + TEST_CONTENT + "] from the telnet client.", in == TEST_CONTENT);

            input = socketClient.getInputStream();
            in = input.read();
            // here IAC is expected, since when the output stream in TelnetConsoleSession is created, several telnet
            // commands are written to it, each of them starting with IAC
            Assert.assertTrue("Client receive telnet responses from the server unexpected value [" + in + "] instead of [" + IAC + "].", in == IAC);
        } finally {
            if (socketClient != null) {
                socketClient.close();
            }
            if (consoleServer != null) {
                consoleServer.doClose();
            }
            if (outClient != null) {
                outClient.close();
            }
            if (outServer != null) {
                outServer.close();
            }

            try {
                if (socketServer != null) {
                    Assert.assertTrue("Server telnet socket is not closed.", socketServer.isClosed());
                }
            } finally {
                if (servSocket != null) {
                    servSocket.close();
                }
            }
        }
    }

    @Test
    public void testConsoleSessionVoidWrapper() throws Exception {
        ServerSocket servSocket = null;
        Socket socketClient = null;
        Socket socketServer = null;
        TelnetConsoleSession consoleServer = null;

        try {
            servSocket = new ServerSocket(0);
            socketClient = new Socket("localhost", servSocket.getLocalPort());
            socketServer = servSocket.accept();

            consoleServer = new TelnetConsoleSession(socketServer, null);
            consoleServer.start(2000);

            OutputStream outClient = socketClient.getOutputStream();
            outClient.write(TEST_CONTENT);
            outClient.write('\n');
            outClient.flush();

            InputStream input = consoleServer.getInput();
            int in = input.read();

            Assert.assertTrue("Server received [" + in + "] instead of " + TEST_CONTENT + " from the telnet client.", in == TEST_CONTENT);
        } finally {
            if (socketClient != null) {
                socketClient.close();
            }
            if (consoleServer != null) {
                consoleServer.doClose();
            }

            try {
                if (socketServer != null) {
                    Assert.assertTrue("Server telnet socket is not closed.", socketServer.isClosed());
                }
            } finally {
                if (servSocket != null) {
                    servSocket.close();
                }
            }
        }
    }

}
