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

package org.eclipse.virgo.osgi.console.telnet.hook;

import java.io.OutputStream;
import java.net.ConnectException;
import java.net.Socket;

import org.eclipse.core.runtime.adaptor.EclipseStarter;
import org.eclipse.osgi.baseadaptor.BaseAdaptor;
import org.junit.Assert;
import org.junit.Test;
import org.osgi.framework.BundleContext;

import static org.easymock.EasyMock.*;

public class TelnetHookTests {

    @Test
    public void testHostAndPort() throws Exception {
        System.setProperty(EclipseStarter.PROP_CONSOLE, "localhost:3333");
        System.setProperty("osgi.configuration.area", ".");
        BaseAdaptor adaptor = new BaseAdaptor(null);
        TelnetHook telnetHook = new TelnetHook();
        telnetHook.initialize(adaptor);
        String consoleProp = System.getProperty(EclipseStarter.PROP_CONSOLE);
        Assert.assertNull("Console port should not be present in the properties", consoleProp);

        BundleContext bundleContext = createMock(BundleContext.class);

        expect(bundleContext.getProperty(EclipseStarter.PROP_CONSOLE)).andReturn("localhost:3333");
        replay(bundleContext);

        telnetHook.frameworkStart(bundleContext);

        consoleProp = System.getProperty(EclipseStarter.PROP_CONSOLE);
        Assert.assertEquals("Console port should not be present in the properties", "localhost:3333", consoleProp);

        telnetHook.frameworkStop(bundleContext);

        telnetHook.frameworkStopping(bundleContext);
        telnetHook.addProperties(null);
        telnetHook.mapLocationToURLConnection(null);
        telnetHook.handleRuntimeError(null);
        Assert.assertNull(telnetHook.createFrameworkLog());

        Socket socketClient = null;
        try {
            socketClient = new Socket("localhost", 3333);
            OutputStream outClient = socketClient.getOutputStream();
            outClient.write(100);
            outClient.write('\n');
            outClient.flush();
            Assert.fail("Server socket should be closed, no connection should be established");
        } catch (ConnectException ex) {
            // this is expected
        } finally {
            if (socketClient != null) {
                socketClient.close();
            }
        }

        verify(bundleContext);
    }

    @Test
    public void testPort() {
        BaseAdaptor adaptor = new BaseAdaptor(null);
        TelnetHook telnetHook = new TelnetHook();
        System.setProperty(EclipseStarter.PROP_CONSOLE, "3333");
        telnetHook.initialize(adaptor);
        String consoleProp = System.getProperty(EclipseStarter.PROP_CONSOLE);
        Assert.assertNull("Console port should not be present in the properties", consoleProp);
    }
}
