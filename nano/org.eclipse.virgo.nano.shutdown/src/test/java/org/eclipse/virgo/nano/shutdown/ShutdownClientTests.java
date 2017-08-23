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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.Serializable;
import java.lang.management.ManagementFactory;
import java.net.ServerSocket;
import java.net.Socket;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.RMIClientSocketFactory;
import java.rmi.server.RMIServerSocketFactory;
import java.security.Permission;
import java.util.HashMap;
import java.util.Map;

import javax.management.MBeanServer;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXConnectorServerFactory;
import javax.management.remote.JMXServiceURL;
import javax.management.remote.rmi.RMIConnectorServer;

import org.eclipse.virgo.nano.shutdown.ShutdownClient;
import org.eclipse.virgo.nano.shutdown.ShutdownCommand;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class ShutdownClientTests {

    private static SecurityManager securityManager = System.getSecurityManager();

    @BeforeClass
    public static void installSecurityManager() {
        System.setSecurityManager(new UnitTestSecurityManager());
    }

    @AfterClass
    public static void reinstateSecurityManager() {
        System.setSecurityManager(securityManager);
    }

    @Test(expected = RuntimeException.class)
    public void malformedCommandCausesExit() {
        UnitTestShutdownClient client = new UnitTestShutdownClient();
        client.performShutdown("-alpha");
    }

    @Test
    public void unreachableMBeanServerReportsServerUnreachable() throws Exception {
        UnitTestShutdownClient client = new UnitTestShutdownClient();

        ShutdownCommand command = new ShutdownCommand();
        command.setPort(14); // privileged unassigned port - should never be open

        client.doShutdown(command);

        assertTrue(client.serverUnreachable);
        assertFalse(client.exited);
        assertFalse(client.shutdownFailureReported);
    }

    @Test
    public void missingShutdownMBeanReportsShutdownFailure() throws Exception {
        UnitTestShutdownClient client = new UnitTestShutdownClient();

        ShutdownCommand command = new ShutdownCommand();

        JMXConnectorServer server = bootstrapMBeanServer(9875);

        try {
            client.doShutdown(command);

            assertFalse(client.serverUnreachable);
            assertFalse(client.exited);
            assertTrue(client.shutdownFailureReported);
        } finally {
            server.stop();
        }
    }

    @Test
    public void immediateShutdownInvokesImmediateShutdownOperation() throws Exception {
        UnitTestShutdownClient client = new UnitTestShutdownClient();

        ShutdownCommand command = new ShutdownCommand();
        command.setImmediate(true);

        JMXConnectorServer server = bootstrapMBeanServer(9875);

        UnitTestShutdown shutdown = new UnitTestShutdown();

        ObjectInstance shutdownMBean = registerShutdownMBean(shutdown, "org.eclipse.virgo.kernel");

        try {
            client.doShutdown(command);

            assertFalse(client.serverUnreachable);
            assertFalse(client.exited);
            assertFalse(client.shutdownFailureReported);

            assertTrue(shutdown.immediateShutdown);
            assertFalse(shutdown.shutdown);
        } finally {
            server.stop();
            ManagementFactory.getPlatformMBeanServer().unregisterMBean(shutdownMBean.getObjectName());
        }
    }

    @Test
    public void shutdownInvokesShutdownOperation() throws Exception {
        UnitTestShutdownClient client = new UnitTestShutdownClient();

        ShutdownCommand command = new ShutdownCommand();

        JMXConnectorServer server = bootstrapMBeanServer(9875);

        UnitTestShutdown shutdown = new UnitTestShutdown();

        ObjectInstance shutdownMBean = registerShutdownMBean(shutdown, "org.eclipse.virgo.kernel");

        try {
            client.doShutdown(command);

            assertFalse(client.serverUnreachable);
            assertFalse(client.exited);
            assertFalse(client.shutdownFailureReported);

            assertFalse(shutdown.immediateShutdown);
            assertTrue(shutdown.shutdown);
        } finally {
            server.stop();
            ManagementFactory.getPlatformMBeanServer().unregisterMBean(shutdownMBean.getObjectName());
        }
    }

    @Test
    public void clientUsesDomainSpecifiedInCommand() throws Exception {
        UnitTestShutdownClient client = new UnitTestShutdownClient();

        ShutdownCommand command = new ShutdownCommand();
        command.setDomain("the.domain");

        JMXConnectorServer server = bootstrapMBeanServer(9875);

        UnitTestShutdown shutdown = new UnitTestShutdown();

        ObjectInstance shutdownMBean = registerShutdownMBean(shutdown, "the.domain");

        try {
            client.doShutdown(command);

            assertFalse(client.serverUnreachable);
            assertFalse(client.exited);
            assertFalse(client.shutdownFailureReported);
        } finally {
            server.stop();
            ManagementFactory.getPlatformMBeanServer().unregisterMBean(shutdownMBean.getObjectName());
        }
    }

    @Test
    public void clientUsesPortSpecifiedInCommand() throws Exception {

        ShutdownCommand command = new ShutdownCommand();
        command.setPort(9999);

        UnitTestShutdown shutdown = new UnitTestShutdown();

        ObjectInstance shutdownMBean = registerShutdownMBean(shutdown, "the.domain");

        UnitTestShutdownClient client = new UnitTestShutdownClient();

        JMXConnectorServer server = bootstrapMBeanServer(9999);

        try {
            assertFalse(client.serverUnreachable);
            assertFalse(client.exited);
            assertFalse(client.shutdownFailureReported);
        } finally {
            server.stop();
            ManagementFactory.getPlatformMBeanServer().unregisterMBean(shutdownMBean.getObjectName());
        }
    }

    private JMXConnectorServer bootstrapMBeanServer(int port) throws Exception {

        createRegistry(port);

        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();

        Map<String, Object> env = new HashMap<String, Object>();

        env.put(RMIConnectorServer.RMI_CLIENT_SOCKET_FACTORY_ATTRIBUTE, new UnitTestClientSocketFactory());

        env.put(RMIConnectorServer.RMI_SERVER_SOCKET_FACTORY_ATTRIBUTE, new UnitTestServerSocketFactory());

        JMXServiceURL url = new JMXServiceURL("service:jmx:rmi:///jndi/rmi://:" + port + "/jmxrmi");
        JMXConnectorServer server = JMXConnectorServerFactory.newJMXConnectorServer(url, env, mbs);
        server.start();

        return server;
    }

    private void createRegistry(int port) {
        try {
            LocateRegistry.createRegistry(port);
        } catch (RemoteException ignored) {
        }
    }

    private static final class UnitTestClientSocketFactory implements RMIClientSocketFactory, Serializable {

        private static final long serialVersionUID = 4445230934659082311L;

        public Socket createSocket(String host, int port) throws IOException {
            return new Socket(host, port);
        }
    }

    private static final class UnitTestServerSocketFactory implements RMIServerSocketFactory, Serializable {

        private static final long serialVersionUID = 5261406192116261111L;

        public ServerSocket createServerSocket(int port) throws IOException {
            return new ServerSocket(port);
        }
    }

    private ObjectInstance registerShutdownMBean(Shutdown shutdown, String domain) throws Exception {
        MBeanServer server = ManagementFactory.getPlatformMBeanServer();
        return server.registerMBean(shutdown, new ObjectName(domain + ":type=Shutdown"));
    }

    private final class UnitTestShutdownClient extends ShutdownClient {

        private boolean serverUnreachable = false;

        private boolean exited = false;

        private boolean shutdownFailureReported = false;

        @Override
        protected void exit() {
            throw new RuntimeException();
        }

        @Override
        protected void reportServerUnreachable() {
            super.reportServerUnreachable();
            this.serverUnreachable = true;
        }

        @Override
        protected void reportShutdownFailure(Exception failure) {
            super.reportShutdownFailure(failure);
            this.shutdownFailureReported = true;
        }
    }

    private static final class UnitTestShutdown implements Shutdown {

        private boolean immediateShutdown;

        private boolean shutdown;

        public void immediateShutdown() {
            this.immediateShutdown = true;
        }

        public void shutdown() {
            this.shutdown = true;
        }
    }

    private static final class UnitTestSecurityManager extends SecurityManager {

        @Override
        public void checkPermission(Permission perm, Object context) {
        }

        @Override
        public void checkPermission(Permission perm) {
        }
    }
}
