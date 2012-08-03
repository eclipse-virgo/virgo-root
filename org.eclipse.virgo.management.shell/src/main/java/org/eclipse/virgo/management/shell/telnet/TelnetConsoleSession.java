/*******************************************************************************
 * Copyright (c) 2011 SAP AG
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Lazar Kirchev, SAP AG - initial contribution
 *    Hristo Iliev, SAP AG
 ******************************************************************************/

package org.eclipse.virgo.osgi.console.telnet;

import org.eclipse.virgo.osgi.console.telnet.Callback;
import org.eclipse.virgo.osgi.console.telnet.NegotiationFinishedCallback;
import org.eclipse.virgo.osgi.console.telnet.TelnetInputHandler;
import org.eclipse.virgo.osgi.console.telnet.TelnetOutputStream;
import org.eclipse.osgi.framework.console.ConsoleSession;
import org.eclipse.virgo.osgi.console.common.ConsoleInputStream;
import org.eclipse.virgo.osgi.console.supportability.ConsoleInputHandler;
import org.osgi.framework.BundleContext;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

/**
 * This class provides an implementation of a ConsoleSession. It creates a handler for the input from telnet and wraps
 * its streams to add handling for command line editing.
 */
public class TelnetConsoleSession extends ConsoleSession {

    private final Socket s;

    private InputStream input;

    private final ConsoleInputStream in;

    private final TelnetOutputStream out;

    protected boolean isTelnetNegotiationFinished = false;

    private Callback callback;

    private static final long TIMEOUT = 1000;

    private static final long NEGOTIATION_TIMEOUT = 60000;

    private final BundleContext context;

    public TelnetConsoleSession(Socket s, BundleContext context) throws IOException {
        in = new ConsoleInputStream();
        out = new TelnetOutputStream(s.getOutputStream());
        out.autoSend();
        this.s = s;
        this.context = context;

        callback = new NegotiationFinishedCallback(this);
    }

    public synchronized void start() throws IOException {
        start(NEGOTIATION_TIMEOUT);
    }

    public synchronized void start(long negotiationTimeout) throws IOException {
        TelnetInputHandler telnetInputHandler = new TelnetInputHandler(s.getInputStream(), in, out, callback);
        telnetInputHandler.start();
        long start = System.currentTimeMillis();
        while (isTelnetNegotiationFinished == false && System.currentTimeMillis() - start < negotiationTimeout) {
            try {
                wait(TIMEOUT);
            } catch (InterruptedException e) {
                // do nothing
            }
        }

        ConsoleInputStream inp = new ConsoleInputStream();

        ConsoleInputHandler consoleInputHandler = new ConsoleInputHandler(in, inp, out, context);
        consoleInputHandler.getScanner().setBackspace(telnetInputHandler.getScanner().getBackspace());
        consoleInputHandler.getScanner().setDel(telnetInputHandler.getScanner().getDel());
        consoleInputHandler.getScanner().setCurrentEscapesToKey(telnetInputHandler.getScanner().getCurrentEscapesToKey());
        consoleInputHandler.getScanner().setEscapes(telnetInputHandler.getScanner().getEscapes());

        consoleInputHandler.start();
        input = inp;
    }

    public synchronized void telnetNegotiationFinished() {
        isTelnetNegotiationFinished = true;
        notify();

    }

    public synchronized InputStream getInput() {
        return input;
    }

    public synchronized OutputStream getOutput() {
        return out;
    }

    public synchronized void doClose() {
        if (s != null) {
            try {
                s.close();
            } catch (IOException ioe) {
                // do nothing
            }
        }

        if (out != null) {
            try {
                out.close();
            } catch (IOException e) {
                // do nothing
            }
        }

        if (in != null) {
            try {
                in.close();
            } catch (IOException ioe) {
                // do nothing
            }
        }

        if (input != null) {
            try {
                input.close();
            } catch (IOException ioe) {
                // do nothing
            }
        }
    }

}
