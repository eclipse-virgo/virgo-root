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

package org.eclipse.virgo.kernel.shell.internal;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;

import org.apache.sshd.server.Command;
import org.apache.sshd.server.Environment;

/**
 * <p>
 * SshdShell is a shell that Apache sshd knows how to export over the wire. 
 * This impl simply wraps itself around a {@link LocalShell}.
 * </p>
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Thread-safe
 * 
 */
final class SshdShell implements Command, ExitCallback {

    private final LocalShellFactory localShellFactory;

    private final int threadNumber;

    private volatile InputStream in = null;

    private volatile OutputStream out = null;

    private volatile OutputStream err = null;

    private volatile org.apache.sshd.server.ExitCallback callback;

    private volatile Thread wrappedShellThread = null;

    /**
     * @param localShellFactory
     * @param threadNumber
     */
    public SshdShell(LocalShellFactory localShellFactory, int threadNumber) {
        this.localShellFactory = localShellFactory;
        this.threadNumber = threadNumber;
    }

    /**
     * {@inheritDoc}
     */
    public void setErrorStream(OutputStream err) {
        this.err = err;
    }

    /**
     * {@inheritDoc}
     */
    public void setInputStream(InputStream in) {
        this.in = in;
    }

    /**
     * {@inheritDoc}
     */
    public void setOutputStream(OutputStream out) {
        this.out = out;
    }

    /**
     * {@inheritDoc}
     */
    public void setExitCallback(org.apache.sshd.server.ExitCallback callback) {
        this.callback = callback;
    }

    /**
     * {@inheritDoc}
     */
    public void start(Environment env) throws IOException {
        Thread localWrappedShellThread = this.wrappedShellThread;
        if (localWrappedShellThread == null) {
            InputStream localIn = this.in;
            OutputStream localOut = this.out;
            OutputStream localErr = this.err;

            assert localIn != null;
            assert localOut != null;
            assert localErr != null;

            LocalShell localShell = this.localShellFactory.newShell(localIn, new PrintStream(localOut), new PrintStream(localErr));
            localShell.addExitCallback(this);

            Thread shellThread = new Thread(localShell, String.format("remote-shell-thread-%d", this.threadNumber));
            shellThread.setDaemon(true);
            shellThread.start();

            this.wrappedShellThread = shellThread;
        }
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("deprecation")
    public void destroy() {
        Thread shellThread = this.wrappedShellThread;
        if (shellThread != null) {
            shellThread.stop();
            this.wrappedShellThread = null;
        }
    }

    public void onExit() {
        org.apache.sshd.server.ExitCallback localCallback = this.callback;
        if (localCallback != null) {
            localCallback.onExit(0);
        }
    }
}
