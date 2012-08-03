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

import org.eclipse.core.runtime.adaptor.EclipseStarter;
import org.eclipse.osgi.baseadaptor.BaseAdaptor;
import org.eclipse.osgi.baseadaptor.hooks.AdaptorHook;
import org.eclipse.osgi.framework.internal.core.FrameworkProperties;
import org.eclipse.osgi.framework.log.FrameworkLog;
import org.eclipse.virgo.osgi.console.telnet.TelnetManager;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;

import java.io.IOException;
import java.net.URLConnection;
import java.util.Properties;

/**
 * This adaptor hook starts the telnet implementation on the port, specified by the OSGi property osgi.console.
 * Principally, on this port listens the Equinox shell. In order to avoid this, the value of the property (if integer)
 * is stored, and the property is removed. In this way, when the Equinox ConsoleManager starts, it cannot retrieve any
 * port and does not start a console on it. After that, during the start of the framework, the hook recovers the
 * property and starts the telnet on this port.
 * <p/>
 * It is possible to pass not only the port, but also the host, in order to restrict the opened server socket to a
 * particular network address on the host.
 */
public class TelnetHook implements AdaptorHook {

    private boolean consolePortAvailable;

    private String consoleValue;

    private TelnetManager telnetManager = null;

    public void initialize(BaseAdaptor adaptor) {
        consoleValue = FrameworkProperties.getProperty(EclipseStarter.PROP_CONSOLE);
        if (consoleValue == null) {
            return;
        }
        if (consoleValue.contains(":")) {
            consolePortAvailable = true;
            FrameworkProperties.getProperties().remove(EclipseStarter.PROP_CONSOLE);
        } else {
            try {
                Integer.parseInt(consoleValue);
                consolePortAvailable = true;
                FrameworkProperties.getProperties().remove(EclipseStarter.PROP_CONSOLE);
            } catch (NumberFormatException ex) {
                // do nothing
            }
        }
    }

    public void frameworkStart(BundleContext context) throws BundleException {
        if (consolePortAvailable == true) {
            FrameworkProperties.setProperty(EclipseStarter.PROP_CONSOLE, String.valueOf(consoleValue));
            telnetManager = new TelnetManager(context);
            telnetManager.startConsoleListener();
        }
    }

    public void frameworkStop(BundleContext context) throws BundleException {
        if (telnetManager != null) {
            telnetManager.stop();
        }
    }

    public void frameworkStopping(BundleContext context) {
        // Do nothing - we don't care about stopping event
    }

    public void addProperties(Properties properties) {
        // Do nothing - we don't care about addProperties event
    }

    public URLConnection mapLocationToURLConnection(String location) throws IOException {
        return null; // not supported
    }

    public void handleRuntimeError(Throwable error) {
        // Do nothing - we don't know how to handle runtime errors
    }

    public FrameworkLog createFrameworkLog() {
        return null; // not supported
    }

}
