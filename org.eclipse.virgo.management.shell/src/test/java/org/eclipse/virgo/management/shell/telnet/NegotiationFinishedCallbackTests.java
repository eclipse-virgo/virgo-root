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

import java.net.ServerSocket;
import java.net.Socket;

import org.junit.Assert;
import org.junit.Test;

public class NegotiationFinishedCallbackTests {

    @Test
    public void finishTest() throws Exception {
        ServerSocket servSocket = null;
        Socket socketClient = null;
        Socket socketServer = null;
        TelnetConsoleSession consoleSession = null;
        try {
            servSocket = new ServerSocket(0);
            socketClient = new Socket("localhost", servSocket.getLocalPort());
            socketServer = servSocket.accept();

            consoleSession = new TelnetConsoleSession(socketServer, null);
            NegotiationFinishedCallback callback = new NegotiationFinishedCallback(consoleSession);
            callback.finished();
            Assert.assertTrue("Finished not called on console session", consoleSession.isTelnetNegotiationFinished);
        } finally {
            if (socketClient != null) {
                socketClient.close();
            }
            if (consoleSession != null) {
                consoleSession.doClose();
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
