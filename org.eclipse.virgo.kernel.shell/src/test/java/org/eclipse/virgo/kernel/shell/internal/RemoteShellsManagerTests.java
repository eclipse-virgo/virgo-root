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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.lang.management.ManagementFactory;
import java.util.Properties;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.apache.sshd.ClientChannel;
import org.apache.sshd.ClientSession;
import org.apache.sshd.SshClient;
import org.apache.sshd.client.future.AuthFuture;
import org.apache.sshd.client.future.ConnectFuture;
import org.apache.sshd.client.future.OpenFuture;
import org.apache.sshd.common.future.CloseFuture;
import org.apache.sshd.server.PasswordAuthenticator;
import org.apache.sshd.server.session.ServerSession;
import org.junit.Ignore;
import org.junit.Test;

import org.eclipse.virgo.kernel.shell.internal.RemoteShellsManager;
import org.eclipse.virgo.medic.test.eventlog.MockEventLogger;
import org.eclipse.virgo.util.io.NetUtils;

/**
 */
public class RemoteShellsManagerTests {

    private static final String PROPERTY_SHELL_PORT = "shell.port";

    private static final String PROPERTY_SHELL_ENABLED = "shell.enabled";

    private static final String KNOWN_USER = "Billy";

    private static final String KNOWN_USER_PASSWORD = "Password";

    private static final String DEFAULT_KERNEL_DOMAIN = "org.eclipse.virgo.kernel";

    private static final String MBEAN_VALUE_SHUTDOWN = "Shutdown";

    private static final String MBEAN_KEY_TYPE = "type";

    @Test
    public void testRemoteShellNotEnabled() throws Exception {
        int port = NetUtils.getFreePort();
        Properties configuration = new Properties();
        configuration.setProperty(PROPERTY_SHELL_PORT, String.valueOf(port));
        configuration.setProperty(PROPERTY_SHELL_ENABLED, String.valueOf(false));
        RemoteShellsManager remoteShellManager = createRemoteShellManager(configuration);
        try {
            SshClient sshClient = SshClient.setUpDefaultClient();
            sshClient.start();

            ConnectFuture connect = connect(sshClient, port);
            assertFalse(connect.isConnected());

            sshClient.stop();
        } finally {
            remoteShellManager.stop();
            assertTrue(NetUtils.isPortAvailable(port));
        }
    }

    @Test
    @Ignore("Remote shells disabled until LocalShell implementation supplied.")
    public void testRemoteShellEnabled() throws Exception {
        int port = NetUtils.getFreePort();
        Properties configuration = new Properties();
        configuration.setProperty(PROPERTY_SHELL_PORT, String.valueOf(port));
        configuration.setProperty(PROPERTY_SHELL_ENABLED, String.valueOf(true));
        RemoteShellsManager remoteShellManager = createRemoteShellManager(configuration);
        try {
            SshClient sshClient = SshClient.setUpDefaultClient();
            sshClient.start();

            ConnectFuture connect = connect(sshClient, port);
            assertTrue(connect.isConnected());

            sshClient.stop();
        } finally {
            remoteShellManager.stop();
            assertTrue(NetUtils.isPortAvailable(port));
        }
    }

    
    @Test
    @Ignore("Remote shells disabled until LocalShell implementation supplied.")
    public void testRemoteShellEnabledPortClash() throws Exception {
        MBeanServer server = ManagementFactory.getPlatformMBeanServer();
        ObjectName shutdownName = ObjectName.getInstance(DEFAULT_KERNEL_DOMAIN, MBEAN_KEY_TYPE, MBEAN_VALUE_SHUTDOWN);
        DummyShutdown dummyShutdownMBean = new DummyShutdown();
        server.registerMBean(dummyShutdownMBean, shutdownName);

        int port = NetUtils.getFreePort();
        Properties configuration = new Properties();
        configuration.setProperty(PROPERTY_SHELL_PORT, String.valueOf(port));
        configuration.setProperty(PROPERTY_SHELL_ENABLED, String.valueOf(true));
        RemoteShellsManager remoteShellManager1 = createRemoteShellManager(configuration);
        RemoteShellsManager remoteShellManager2 = createRemoteShellManager(configuration);
        Thread.sleep(500);
        remoteShellManager1.stop();
        remoteShellManager2.stop();
        assertTrue(dummyShutdownMBean.shutDownCalled);
        server.unregisterMBean(shutdownName);
    }

    @Test
    @Ignore("Remote shells disabled until LocalShell implementation supplied.")
    public void testRemoteShellEnabledBadUser() throws Exception {
        int port = NetUtils.getFreePort();
        Properties configuration = new Properties();
        configuration.setProperty(PROPERTY_SHELL_PORT, String.valueOf(port));
        configuration.setProperty(PROPERTY_SHELL_ENABLED, String.valueOf(true));
        RemoteShellsManager remoteShellManager = createRemoteShellManager(configuration);
        try {
            SshClient sshClient = SshClient.setUpDefaultClient();
            sshClient.start();

            ConnectFuture connect = connect(sshClient, port);
            assertTrue(connect.isConnected());
            ClientSession session = connect.getSession();
            AuthFuture authFuture = session.authPassword("Monkey", "BadPassword").await();
            assertTrue(authFuture.isDone());
            assertFalse(authFuture.isSuccess());

            session.close(true);

            sshClient.stop();

        } finally {
            remoteShellManager.stop();
            assertTrue(NetUtils.isPortAvailable(port));
        }
    }

    @Test
    @Ignore("Remote shells disabled until LocalShell implementation supplied.")
    public void testRemoteShellEnabledConnect() throws Exception {
        int port = NetUtils.getFreePort();
        Properties configuration = new Properties();
        configuration.setProperty(PROPERTY_SHELL_PORT, String.valueOf(port));
        configuration.setProperty(PROPERTY_SHELL_ENABLED, String.valueOf(true));
        RemoteShellsManager remoteShellManager = createRemoteShellManager(configuration);
        try {
            SshClient sshClient = SshClient.setUpDefaultClient();
            sshClient.start();

            ConnectFuture connect = connect(sshClient, port);
            assertTrue(connect.isConnected());
            ClientSession session = connect.getSession();
            AuthFuture authFuture = session.authPassword(KNOWN_USER, KNOWN_USER_PASSWORD).await();
            assertTrue(authFuture.isSuccess());

            ClientChannel channel = session.createChannel(ClientChannel.CHANNEL_SHELL);
            channel.setIn(System.in);
            channel.setOut(System.out);
            channel.setErr(System.err);
            OpenFuture openFuture = channel.open().await();
            assertTrue(openFuture.isOpened());

            CloseFuture close = session.close(true);
            assertTrue(close.await(10000));

            sshClient.stop();

        } finally {
            remoteShellManager.stop();
            assertTrue(NetUtils.isPortAvailable(port));
        }
    }

    private ConnectFuture connect(SshClient sshClient, int port) throws InterruptedException, Exception {
        ConnectFuture connect = sshClient.connect("localhost", port).await();
        return connect;
    }

    private RemoteShellsManager createRemoteShellManager(Properties configuration) {
        RemoteShellsManager remoteShellManager = new RemoteShellsManager(new StubLocalShellFactory(), configuration, new MockEventLogger()) {

            @Override
            protected PasswordAuthenticator createPasswordAuthenticator() {
                return new PasswordAuthenticator() {

                    public boolean authenticate(String username, String password, ServerSession session) {
                        return KNOWN_USER.equals(username) && KNOWN_USER_PASSWORD.equals(password);
                    }
                };
            }

        };
        remoteShellManager.start();
        return remoteShellManager;
    }

    public static interface DummyShutdownMBean {

        public void shutdown();
    }

    public static class DummyShutdown implements DummyShutdownMBean {

        private boolean shutDownCalled = false;

        public void shutdown() {
            this.shutDownCalled = true;
        }
    }

}
