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

package org.eclipse.virgo.shell.internal.commands;

import java.lang.management.ManagementFactory;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.eclipse.virgo.shell.Command;
import org.eclipse.virgo.shell.internal.LocalInputOutputManager;


/**
 * Provides the Shell's <code>shutdown</code> command that will shutdown the kernel.
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Thread-safe.
 * 
 */
@Command("shutdown")
final class ShutdownCommand {

    private static final String DEFAULT_KERNEL_DOMAIN = "org.eclipse.virgo.kernel";

    private static final String MBEAN_VALUE_SHUTDOWN = "Shutdown";

    private static final String MBEAN_KEY_TYPE = "type";

    private final LocalInputOutputManager ioManager;

    ShutdownCommand(LocalInputOutputManager ioManager) {
        this.ioManager = ioManager;
    }

    @Command("")
    public List<String> shutdown() {

        this.ioManager.releaseSystemIO();

        MBeanServer server = ManagementFactory.getPlatformMBeanServer();
        try {
            // TODO The kernel's domain needs to be read from configuration
            ObjectName shutdownName = ObjectName.getInstance(DEFAULT_KERNEL_DOMAIN, MBEAN_KEY_TYPE, MBEAN_VALUE_SHUTDOWN);
            server.invoke(shutdownName, "shutdown", new Object[0], new String[0]);
        } catch (Exception e) {
            this.ioManager.grabSystemIO();
            return Arrays.asList(String.format("Error occurred '%s'", e.getMessage()));
        }
        return Collections.emptyList();
    }
}
