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
import java.lang.management.ManagementFactory;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.apache.sshd.SshServer;
import org.apache.sshd.common.Factory;
import org.apache.sshd.server.Command;
import org.apache.sshd.server.PasswordAuthenticator;
import org.apache.sshd.server.jaas.JaasPasswordAuthenticator;
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.eclipse.virgo.medic.eventlog.EventLogger;
import org.eclipse.virgo.util.io.NetUtils;

/**
 * <p>
 * RemoteShellsManager looks after the exporting of a local shell over ssh. It is mainly concerned with startup and
 * shutdown of the shell and the sshd system.
 * </p>
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * RemoteShellManager is thread safe
 * 
 */
class RemoteShellsManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(RemoteShellsManager.class);

    private static final String TRUE = "true";

    private static final long SHUTDOWN_TIMEOUT = 30000; //30 seconds in milli seconds

    private static final String JAAS_SECURITY_DOMAIN = "virgo-kernel";

    private static final String PROPERTY_SHELL_PORT = "shell.port";

    private static final String PROPERTY_SHELL_ENABLED = "shell.enabled";

    private static final String HOST_KEY_LOCATION = "config/hostkey.ser";

    private static final String DEFAULT_KERNEL_DOMAIN = "org.eclipse.virgo.kernel";

    private static final String MBEAN_VALUE_SHUTDOWN = "Shutdown";

    private static final String MBEAN_KEY_TYPE = "type";

    private final SshdShellFactory shellFactory;

    private final EventLogger eventLogger;

    private final int port;

    private final String portEnabled;

    private SshServer sshd = null;

    RemoteShellsManager(LocalShellFactory shellFactory, Properties configuration, EventLogger eventLogger) {
        this.portEnabled = configuration.getProperty(PROPERTY_SHELL_ENABLED);
        String portString = configuration.getProperty(PROPERTY_SHELL_PORT);
        int initialisingPort = -1;
        try {
        	initialisingPort = Integer.valueOf(portString);
            if(initialisingPort < 0 || initialisingPort > 65535){
            	initialisingPort = -1;
            }
        } catch (NumberFormatException e) {
            initialisingPort = -1;
        }
        this.port = initialisingPort;

        this.shellFactory = new SshdShellFactory(shellFactory);
        this.eventLogger = eventLogger;
    }

    /**
     * Start the sshd shell manager using the port configured in the provided properties set. If there is no configured
     * port then no server will be started.
     */
    final void start() {
        if (this.sshd == null && portEnabled != null && TRUE.equals(portEnabled) && this.port != -1) {
            if (NetUtils.isPortAvailable(port)) {
                doStart(port);
            } else {
                this.eventLogger.log(ShellLogEvents.SERVER_SHELL_PORT_IN_USE, String.valueOf(port));
                callShutdownMBean();
            }
        }
    }

    /**
     * Start the sshd shell manager using the provided port.
     * 
     * @param port
     */
    private void doStart(int port) {
        SshServer sshd = SshServer.setUpDefaultServer();
        sshd.setPort(port);
        sshd.setKeyPairProvider(new SimpleGeneratorHostKeyProvider(HOST_KEY_LOCATION));
        sshd.setShellFactory(this.shellFactory);
        sshd.setPasswordAuthenticator(createPasswordAuthenticator());
        try {
            sshd.start();
            this.sshd = sshd;
            this.eventLogger.log(ShellLogEvents.SERVER_CONSOLE_PORT, port);
        } catch (IOException e) {
            LOGGER.warn("Error occurred while trying to start the ssh shell server.", e);
            try {
				sshd.stop(true);
			} catch (InterruptedException e1) {
                LOGGER.warn("Shell sshd server shutdown was interrupted.", e);
			}finally{
				this.sshd = null;
			}
        }
    }

    /**
     * @return
     */
    protected PasswordAuthenticator createPasswordAuthenticator() {
        JaasPasswordAuthenticator jaasPasswordAuthenticator = new JaasPasswordAuthenticator();
        jaasPasswordAuthenticator.setDomain(JAAS_SECURITY_DOMAIN);
        return jaasPasswordAuthenticator;
    }

    /**
     * 
     */
    final void stop() {
        this.shellFactory.exit();
        SshServer sshd = this.sshd;
        if (sshd != null) {
            try {
                sshd.stop(true);
            } catch (InterruptedException e) {
                LOGGER.warn("Shell sshd server shutdown was interrupted.", e);
            } finally {
                this.sshd = null;
            }
            // Only wait for the port to be cleared if we have actually just tried to shutdown the ssh server
	        long shutdownTimer = 0;
	        while (shutdownTimer < SHUTDOWN_TIMEOUT) {
	            if (NetUtils.isPortAvailable(this.port)) {
	                break;
	            }
	            try {
	                shutdownTimer = shutdownTimer + (500);
	                Thread.sleep(500);
	            } catch (InterruptedException e) {
	                break;
	            }
	        }
        }
    }

    private void callShutdownMBean() {
        MBeanServer server = ManagementFactory.getPlatformMBeanServer();
        try {
            // TODO The kernel's domain needs to be read from configuration
            ObjectName shutdownName = ObjectName.getInstance(DEFAULT_KERNEL_DOMAIN, MBEAN_KEY_TYPE, MBEAN_VALUE_SHUTDOWN);
            server.invoke(shutdownName, "shutdown", new Object[0], new String[0]);
        } catch (Exception e) {
            throw new RuntimeException("Failed to find the shutdown MBean", e);
        }
    }

    /**
     */
    private static final class SshdShellFactory implements Factory<Command> {

        private static final AtomicInteger THREAD_COUNTER = new AtomicInteger();

        private final Set<SshdShell> createdSshdShells = new HashSet<SshdShell>();

        private final LocalShellFactory shellFactory;

        /**
         * Create a new Factory with a backing factory for the local shells
         * 
         * @param shellFactory
         */
        public SshdShellFactory(LocalShellFactory shellFactory) {
            this.shellFactory = shellFactory;
        }

        /**
         * {@inheritDoc}
         */
        public Command create() {
            SshdShell sshdShell = new SshdShell(this.shellFactory, THREAD_COUNTER.incrementAndGet());
            this.createdSshdShells.add(sshdShell);
            return sshdShell;
        }

        /**
         * Call {@link SshdShell#onExit()} to terminate the ssh session associated with each shell
         */
        void exit() {
            for (SshdShell shell : this.createdSshdShells) {
                if (shell != null) {
                    shell.onExit(); // Call this and not destroy. The local factory will handle that, we need to bring
                    // the ssh connection down here.
                }
            }
        }
    }

}
