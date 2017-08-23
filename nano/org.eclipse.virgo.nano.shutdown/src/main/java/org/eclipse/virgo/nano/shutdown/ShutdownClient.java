/*******************************************************************************
 * Copyright (c) 2008, 2010 VMware Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   VMware Inc. - initial contribution
 *******************************************************************************/

package org.eclipse.virgo.nano.shutdown;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import javax.naming.ServiceUnavailableException;

/**
 * A stand-alone client for shutdown of the kernel.
 * <p />
 * 
 * <strong>Concurrent Semantics</strong> <br />
 * Thread-safe.
 * 
 */
public class ShutdownClient {

    private static final String OPERATION_IMMEDIATE_SHUTDOWN = "immediateShutdown";

    private static final String OPERATION_SHUTDOWN = "shutdown";

    private static final String PROPERTY_JMX_REMOTE_CREDENTIALS = "jmx.remote.credentials";

    private static final String JMX_SERVICE_URL_TEMPLATE = "service:jmx:rmi:///jndi/rmi://127.0.0.1:%d/jmxrmi";

    public static void main(String[] args) {
        new ShutdownClient().performShutdown(args);
    }

    final void performShutdown(String... args) {
        ShutdownCommand command = ShutdownCommandParser.parse(args);

        if (command != null) {
            doShutdown(command);
        } else {
            displayUsageAndExit();
        }
    }

    protected final void doShutdown(ShutdownCommand command) {
        try {
            JMXServiceURL jmxServiceURL = new JMXServiceURL(String.format(JMX_SERVICE_URL_TEMPLATE, command.getPort()));

            Map<String, Object> jmxEnvironment = new HashMap<String, Object>();

            setRemoteCredentials(command, jmxEnvironment);

            JMXConnector connector = JMXConnectorFactory.connect(jmxServiceURL, jmxEnvironment);
            MBeanServerConnection connection = connector.getMBeanServerConnection();

            ObjectName shutdownMBeanName = new ObjectName(command.getDomain(), "type", "Shutdown");

            if (command.isImmediate()) {
                connection.invoke(shutdownMBeanName, OPERATION_IMMEDIATE_SHUTDOWN, null, null);
            } else {
                connection.invoke(shutdownMBeanName, OPERATION_SHUTDOWN, null, null);
            }

            connector.close();
        } catch (IOException ioe) {
            Throwable cause = ioe.getCause();
            if (cause instanceof ServiceUnavailableException) {
                reportServerUnreachable();
            } else {
                reportShutdownFailure(ioe);
            }
        } catch (Exception e) {
            reportShutdownFailure(e);
        }
    }

    private void setRemoteCredentials(ShutdownCommand command, Map<String, Object> jmxEnvironment) {
        KernelAuthenticationConfiguration kac = null;

        String userName = command.getUsername();
        if (userName == null) {
            kac = new KernelAuthenticationConfiguration();
            userName = kac.getUserName();
        }

        String password = command.getPassword();
        if (password == null) {
            if (kac == null) {
                kac = new KernelAuthenticationConfiguration();
            }
            password = kac.getPassword();
        }

        jmxEnvironment.put(PROPERTY_JMX_REMOTE_CREDENTIALS, new String[] { userName, password });
    }

    protected void reportServerUnreachable() {
        System.out.println("The Server could not be reached, it may already be stopped.");
    }

    protected void reportShutdownFailure(Exception failure) {
        failure.printStackTrace();
    }

    private void displayUsageAndExit() {
        System.out.println("Usage: shutdown [-options]");
        System.out.println("Available options:");
        System.out.println("    -jmxport nnnn     Specifies the management port of the kernel");
        System.out.println("                      instance which is to be shutdown.");
        System.out.println("    -immediate        Specifies that the kernel should be shutdown");
        System.out.println("                      immediately.");
        System.out.println("    -username         Specifies the username to use for the");
        System.out.println("                      connection to the kernel.");
        System.out.println("    -password         Specifies the password to use for the");
        System.out.println("                      connection to the kernel.");
        System.out.println("    -domain           Specifies the JMX management domain for the");
        System.out.println("                      kernel.");
        exit();
    }

    protected void exit() {
        System.exit(1);
    }
}
