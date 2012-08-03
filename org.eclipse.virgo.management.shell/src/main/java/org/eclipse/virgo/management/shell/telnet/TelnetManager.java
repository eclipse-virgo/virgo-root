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

import org.eclipse.virgo.osgi.console.telnet.TelnetConsoleSession;
import org.eclipse.osgi.framework.console.ConsoleSession;
import org.osgi.framework.BundleContext;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class TelnetManager {

    private ConsoleSocketGetter csg;

    private int telnetPort = -1;

    private String host = null;

    private BundleContext context = null;

    private static final String PROP_CONSOLE = "osgi.console";

    public TelnetManager(BundleContext bundleContext) {
        String consoleValue = null;
        try {
            consoleValue = bundleContext.getProperty(PROP_CONSOLE);
            if (consoleValue != null && !"".equals(consoleValue) && !"none".equals(consoleValue)) {
                parseHostAndPort(consoleValue);
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid host/port in " + consoleValue + "; " + e.getMessage());
            e.printStackTrace();
        }

        context = bundleContext;
    }

    public void startConsoleListener() {
        if (telnetPort != -1) {
            try {
                if (host != null) {
                    csg = new ConsoleSocketGetter(new ServerSocket(telnetPort, 0, InetAddress.getByName(host)), context);
                } else {
                    csg = new ConsoleSocketGetter(new ServerSocket(telnetPort), context);
                }
            } catch (IOException e) {
                System.out.println("Unable to open telnet. Reason: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    public void stop() {
        if (csg != null) {
            csg.shutdown();
        }
    }

    public BundleContext getContext() {
        return context;
    }

    private void parseHostAndPort(String consoleValue) {
        int index = consoleValue.lastIndexOf(":");
        if (index > -1) {
            host = consoleValue.substring(0, index);
        }

        telnetPort = Integer.parseInt(consoleValue.substring(index + 1));
    }

    /**
     * ConsoleSocketGetter - provides a Thread that listens on the port for telnet connections.
     */
    static class ConsoleSocketGetter implements Runnable {

        /**
         * The ServerSocket to accept connections from
         */
        private final ServerSocket server;

        private final BundleContext context;

        private volatile boolean shutdown = false;

        /**
         * Constructor - sets the server and starts the thread to listen for connections.
         * 
         * @param server a ServerSocket to accept connections from
         * @param context Bundle context
         */
        ConsoleSocketGetter(ServerSocket server, BundleContext context) {
            this.server = server;
            this.context = context;
            try {
                Method reuseAddress = server.getClass().getMethod("setReuseAddress", new Class[] { boolean.class }); //$NON-NLS-1$
                reuseAddress.invoke(server, Boolean.TRUE);
            } catch (Exception ex) {
                // try to set the socket re-use property, it isn't a problem if it can't be set
            }
            Thread t = new Thread(this, "ConsoleSocketGetter"); //$NON-NLS-1$
            t.setDaemon(true);
            t.start();
        }

        public void run() {
            // Print message containing port console actually bound to..
            System.out.println("Listening on port: " + Integer.toString(server.getLocalPort()));
            while (!shutdown) {
                try {
                    Socket socket = server.accept();
                    if (socket == null)
                        throw new IOException("No socket available.  Probably caused by a shutdown."); //$NON-NLS-1$

                    TelnetConsoleSession session = new TelnetConsoleSession(socket, context);
                    session.start(); // start the Input Handler
                    context.registerService(ConsoleSession.class.getName(), session, null);
                } catch (Exception e) {
                    if (!shutdown)
                        e.printStackTrace();
                }
            }
        }

        public void shutdown() {
            if (shutdown)
                return;
            shutdown = true;
            try {
                server.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
