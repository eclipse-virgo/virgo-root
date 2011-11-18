/*******************************************************************************
 * This file is part of the Virgo Kernel.
 *
 * Copyright (c) 2010 VMware Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SpringSource, a division of VMware - initial API and implementation and/or initial documentation
 *******************************************************************************/

package org.eclipse.virgo.kernel.osgicommand;

import java.lang.management.ManagementFactory;
import java.util.Dictionary;
import java.util.Hashtable;

import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.eclipse.virgo.kernel.osgi.framework.OsgiFrameworkUtils;
import org.eclipse.virgo.kernel.osgi.framework.OsgiServiceHolder;
import org.eclipse.virgo.kernel.osgicommand.internal.GogoClassLoadingCommand;
import org.eclipse.virgo.kernel.osgicommand.internal.GogoKernelShellCommand;
import org.eclipse.virgo.kernel.osgicommand.management.ClassLoadingSupport;
import org.eclipse.virgo.kernel.shell.CommandExecutor;
import org.eclipse.virgo.util.osgi.ServiceRegistrationTracker;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

/**
 * {@link BundleActivator} for the Gogo shell command bundle.
 * <p/>
 * <strong>Concurrent Semantics</strong><br />
 * Not thread safe.
 */
public class Activator implements BundleActivator {

    private static final String KERNEL_SHELL_COMMAND = "vsh";

    private static final String[] KERNEL_SHELL_SUBCOMMANDS = new String[] { "bundle", "config", "install", "packages", "par", "plan", "service",
        "shutdown" };

    private static final String[] CLASS_LOADING_SUBCOMMANDS = new String[] { "clhas", "clload", "clexport" };

    private static final int COMMAND_EXECUTOR_SERVICE_WAIT = 20 * 1000; // 20 seconds

    private static final int SERVICE_WAIT_PAUSE = 100; // 100 milliseconds

    private final ServiceRegistrationTracker registrationTracker = new ServiceRegistrationTracker();

    private final MBeanServer server = ManagementFactory.getPlatformMBeanServer();

    private final ObjectName classLoadingObjectName;

    private ServiceRegistration<GogoClassLoadingCommand> classLoadingCommandRegistration;

    public Activator() throws MalformedObjectNameException {
        this.classLoadingObjectName = new ObjectName("org.eclipse.virgo.kernel:type=Classloading");
    }

    @Override
    public void start(BundleContext context) throws Exception {
        // Gogo binding
        Dictionary<String, Object> properties = new Hashtable<String, Object>();
        properties.put(org.apache.felix.service.command.CommandProcessor.COMMAND_SCOPE, KERNEL_SHELL_COMMAND);
        properties.put(org.apache.felix.service.command.CommandProcessor.COMMAND_FUNCTION, CLASS_LOADING_SUBCOMMANDS);
        this.classLoadingCommandRegistration = context.registerService(GogoClassLoadingCommand.class, new GogoClassLoadingCommand(context),
            properties);

        this.server.registerMBean(new ClassLoadingSupport(context), this.classLoadingObjectName);

        Runnable runnable = new PostStartInitialisationRunnable(context, this.registrationTracker);
        Thread thread = new Thread(runnable);
        thread.setDaemon(true);
        thread.start();
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        // Gogo binding
        if (this.classLoadingCommandRegistration != null) {
            this.classLoadingCommandRegistration.unregister();
            this.classLoadingCommandRegistration = null;
        }

        this.registrationTracker.unregisterAll();

        this.server.unregisterMBean(this.classLoadingObjectName);
    }

    /**
     * Get a service which might not be immediately available
     * 
     * @param <T> type of service to get
     * @param context in which to search for service
     * @param serviceClass of service to locate
     * @param millis maximum time to delay in milliseconds
     * @return null if timeout before getting service, otherwise service
     */
    private static <T> T getPotentiallyDelayedService(BundleContext context, Class<T> serviceClass, long millis) {
        T service = null;

        while (service == null) {
            try {
                OsgiServiceHolder<T> serviceHolder = OsgiFrameworkUtils.getService(context, serviceClass);
                if (serviceHolder != null) {
                    service = serviceHolder.getService();
                }
            } catch (IllegalStateException e) {
                try {
                    millis -= SERVICE_WAIT_PAUSE;
                    if (millis > 0) {
                        Thread.sleep(SERVICE_WAIT_PAUSE);
                    } else {
                        return null;
                    }
                } catch (InterruptedException ie) {
                }
            }
        }

        return service;
    }

    private static final class PostStartInitialisationRunnable implements Runnable {

        private final BundleContext context;

        private final ServiceRegistrationTracker registrationTracker;

        public PostStartInitialisationRunnable(BundleContext context, ServiceRegistrationTracker registrationTracker) {
            this.context = context;
            this.registrationTracker = registrationTracker;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void run() {
            CommandExecutor commandExecutor = getPotentiallyDelayedService(this.context, CommandExecutor.class, COMMAND_EXECUTOR_SERVICE_WAIT);
            if (commandExecutor == null) {
                return; // TODO: report this failure -- but where?
            }

            // Gogo binding
            Dictionary<String, Object> properties = new Hashtable<String, Object>();
            properties.put(org.apache.felix.service.command.CommandProcessor.COMMAND_SCOPE, KERNEL_SHELL_COMMAND);
            properties.put(org.apache.felix.service.command.CommandProcessor.COMMAND_FUNCTION, KERNEL_SHELL_SUBCOMMANDS);
            this.registrationTracker.track(context.registerService(GogoKernelShellCommand.class, new GogoKernelShellCommand(commandExecutor),
                properties));
        }
    }
}
