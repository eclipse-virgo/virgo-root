/*******************************************************************************
 * Copyright (c) 2010 SAP AG
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
import org.osgi.framework.BundleContext;

import java.io.OutputStream;
import java.net.ConnectException;
import java.net.Socket;

import static org.easymock.EasyMock.*;

public class TelnetManagerTests {

    private static final String PROP_CONSOLE = "osgi.console";

    private static final String PROP_CONSOLE_VALUE_POSITIVE = "localhost:38888";

    private static final String PROP_CONSOLE_VALUE_NEGATIVE = "localhost38889";

    private static final int PORT_POSITIVE_TEST = 38888;

    private static final int PORT_NEGATIVE_TEST = 38889;

    private static final long WAIT_TIME = 2000;

    private static final int TEST_CONTENT = 100;

    @Test
    public void testTelnetManager() throws Exception {

        BundleContext bundleContext = createMock(BundleContext.class);

        expect(bundleContext.getProperty(PROP_CONSOLE)).andReturn(PROP_CONSOLE_VALUE_POSITIVE);

        replay(bundleContext);

        TelnetManager telnetMngr = new TelnetManager(bundleContext);
        telnetMngr.startConsoleListener();
        Socket socketClient = null;

        try {
            socketClient = new Socket("localhost", PORT_POSITIVE_TEST);
            OutputStream outClient = socketClient.getOutputStream();
            outClient.write(TEST_CONTENT);
            outClient.write('\n');
            outClient.flush();

            // wait for the accept thread to finish execution
            try {
                Thread.sleep(WAIT_TIME);
            } catch (InterruptedException ie) {
                // do nothing
            }
        } finally {
            if (socketClient != null) {
                socketClient.close();
            }
            telnetMngr.stop();
        }

        verify(bundleContext);
    }

    @Test
    public void testTelnetManagerNegative() throws Exception {
        BundleContext bundleContext = createMock(BundleContext.class);

        expect(bundleContext.getProperty(PROP_CONSOLE)).andReturn(PROP_CONSOLE_VALUE_NEGATIVE);

        replay(bundleContext);

        TelnetManager telnetMngr = new TelnetManager(bundleContext);
        telnetMngr.startConsoleListener();

        boolean serverSocketNotCreated = false;
        try {
            Socket socketClient = new Socket("localhost", PORT_NEGATIVE_TEST);
            socketClient.close();
        } catch (ConnectException ec) {
            serverSocketNotCreated = true;
        }

        telnetMngr.stop();
        verify(bundleContext);

        Assert.assertTrue("Telnet console socket getter is created on localhost", serverSocketNotCreated);
    }

    @Test
    public void testTelnetManagerWithoutHost() throws Exception {
        BundleContext bundleContext = createMock(BundleContext.class);

        expect(bundleContext.getProperty(PROP_CONSOLE)).andReturn(String.valueOf(PORT_POSITIVE_TEST));

        replay(bundleContext);

        TelnetManager telnetMngr = new TelnetManager(bundleContext);
        telnetMngr.startConsoleListener();
        Socket socketClient = null;

        try {
            socketClient = new Socket("localhost", PORT_POSITIVE_TEST);
            OutputStream outClient = socketClient.getOutputStream();
            outClient.write(TEST_CONTENT);
            outClient.write('\n');
            outClient.flush();

            // wait for the accept thread to finish execution
            try {
                Thread.sleep(WAIT_TIME);
            } catch (InterruptedException ie) {
                // do nothing
            }
        } finally {
            if (socketClient != null) {
                socketClient.close();
            }
            telnetMngr.stop();
        }

        verify(bundleContext);
    }

}
