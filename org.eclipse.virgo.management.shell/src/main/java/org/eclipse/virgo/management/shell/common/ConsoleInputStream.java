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

package org.eclipse.virgo.osgi.console.common;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

/**
 * This class serves as an input stream, which wraps the actual input (e.g. from the telnet) and buffers the lines.
 */
public class ConsoleInputStream extends InputStream {

    private final ArrayList<byte[]> buffer = new ArrayList<byte[]>();

    private byte[] current;

    private int pos;

    private boolean isClosed;

    public synchronized int read() {
        while (current == null && buffer.isEmpty() && !isClosed) {
            try {
                wait();
            } catch (InterruptedException e) {
                return -1;
            }
        }
        if (isClosed) {
            return -1;
        }

        try {
            if (current == null) {
                current = buffer.remove(0);
                return current[pos++] & 0xFF;
            } else {

                return current[pos++] & 0xFF;
            }
        } finally {
            if (current != null) {
                if (pos == current.length) {
                    current = null;
                    pos = 0;
                }
            }
        }

    }

    public int read(byte b[], int off, int len) throws IOException {
        if (len == 0) {
            return len;
        }
        int i = read();
        if (i == -1) {
            return -1;
        }
        b[off] = (byte) i;
        return 1;
    }

    public synchronized void close() throws IOException {
        isClosed = true;
        notifyAll();
    }

    public synchronized void add(byte[] data) {
        if (data.length > 0) {
            buffer.add(data);
            notify();
        }
    }

}
