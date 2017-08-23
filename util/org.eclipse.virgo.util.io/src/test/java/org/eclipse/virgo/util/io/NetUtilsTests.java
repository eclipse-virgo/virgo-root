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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.ServerSocket;

import org.eclipse.virgo.util.io.NetUtils;
import org.junit.Test;

public class NetUtilsTests {

    @Test
    public void testUnassignedPort() {
        assertNotNull(NetUtils.getFreePort());
    }

    @Test
    public void testBoundPort() {
        int port = 65535;
        while (!NetUtils.isPortAvailable(port)) {
            port--;
        }

        ServerSocket socket = null;
        try {
            socket = new ServerSocket(port);
            assertFalse(NetUtils.isPortAvailable(port));
        } catch (IOException e) {
        } finally {
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException e) {
                }
            }
        }
    }

    @Test(expected = IllegalStateException.class)
    public void testNoPortsAvailable() {
        int port = 65535;
        while (!NetUtils.isPortAvailable(port)) {
            port--;
        }

        ServerSocket socket = null;
        try {
            socket = new ServerSocket(port);
            NetUtils.getFreePort(port, port);
        } catch (IOException e) {
        } finally {
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException e) {
                }
            }
        }
    }

    @Test
    public void available() {
        int port = 65535;
        while (!NetUtils.isPortAvailable(port)) {
            port--;
        }

        assertTrue(NetUtils.isPortAvailable(port));
    }
}
