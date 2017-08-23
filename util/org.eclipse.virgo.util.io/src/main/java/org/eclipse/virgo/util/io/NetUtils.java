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

package org.eclipse.virgo.util.io;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.util.Random;

/**
 * Utility methods for working with network IO code.
 * <p/>
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Threadsafe.
 * 
 */
public final class NetUtils {

    private static final int MIN_SAFE_PORT = 1024;

    private static final int MAX_PORT = 65535;

    private static final Random random = new Random();

    /**
     * Checks whether the supplied port is available on any local address.
     * 
     * @param port the port to check for.
     * @return <code>true</code> if the port is available, otherwise <code>false</code>.
     */
    public static boolean isPortAvailable(int port) {
        ServerSocket socket;
        try {
            socket = new ServerSocket();
        } catch (IOException e) {
            throw new IllegalStateException("Unable to create ServerSocket.", e);
        }

        try {
            InetSocketAddress sa = new InetSocketAddress(port);
            socket.bind(sa);
            return true;
        } catch (IOException ex) {
            return false;
        } finally {
            try {
                socket.close();
            } catch (IOException ex) {
            }
        }
    }

    /**
     * Checks whether the supplied port is available on the specified address.
     * 
     * @param hostname the address to check for.
     * @param port the port to check for.
     * @return <code>true</code> if the port is available, otherwise <code>false</code>.
     */
    public static boolean isPortAvailable(String hostname, int port) {
        ServerSocket socket;
        try {
            socket = new ServerSocket();
        } catch (IOException e) {
            throw new IllegalStateException("Unable to create ServerSocket.", e);
        }

        try {
            InetSocketAddress sa = new InetSocketAddress(hostname, port);
            socket.bind(sa);
            return true;
        } catch (IOException ex) {
            return false;
        } finally {
            try {
                socket.close();
            } catch (IOException ex) {
            }
        }
    }

    /**
     * Gets a random free port in the non-privileged range of 1025-65535. After this port has been returned once, it
     * cannot be returned again.
     * 
     * @return A free port number
     */
    public static int getFreePort() {
        return getFreePort(MIN_SAFE_PORT, MAX_PORT);
    }

    /**
     * Gets a random free port in between the minimum and maximum specified port numbers.
     * 
     * @param minPort The minimum port number
     * @param maxPort The maximum port number
     * @return After this number has been returned once, it cannot be returned again.
     */
    public static int getFreePort(int minPort, int maxPort) {
        int portRange = maxPort - minPort;
        int candidatePort;
        int searchCounter = 0;
        do {
            if (++searchCounter > portRange) {
                throw new IllegalStateException(String.format("There were no ports available in the range %d to %d", minPort, maxPort));
            }
            candidatePort = getRandomPort(minPort, portRange);
        } while (!isPortAvailable(candidatePort));

        return candidatePort;
    }

    private static int getRandomPort(int minPort, int portRange) {
        return minPort + random.nextInt(portRange);
    }
}
