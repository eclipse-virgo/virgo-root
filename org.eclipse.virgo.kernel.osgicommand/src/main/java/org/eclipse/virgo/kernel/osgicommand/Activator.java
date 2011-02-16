/*******************************************************************************
 * This file is part of the Virgo Web Server.
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

import org.eclipse.osgi.framework.console.CommandProvider;
import org.eclipse.virgo.kernel.osgi.framework.OsgiFrameworkUtils;
import org.eclipse.virgo.kernel.osgi.framework.OsgiServiceHolder;
import org.eclipse.virgo.kernel.osgicommand.internal.OsgiKernelShellCommand;
import org.eclipse.virgo.kernel.shell.CommandExecutor;
import org.eclipse.virgo.kernel.osgicommand.internal.commands.classloading.ClassLoadingCommandProvider;
import org.eclipse.virgo.util.osgi.ServiceRegistrationTracker;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

/**
 * {@link BundleActivator} for the osgi.console command extension bundle
 * <p />
 *
 * <strong>Concurrent Semantics</strong><br />
 * thread-safe
 */
public class Activator implements BundleActivator {

    private static final int COMMAND_EXECUTOR_SERVICE_WAIT = 20*1000; // 20 seconds
    private static final int SERVICE_WAIT_PAUSE = 100; // 100 milliseconds
    private static final String PROVIDER_NAME = "org.eclipse.osgi.framework.console.CommandProvider"; //$NON-NLS-1$
    
    private ServiceRegistration<?> providerRegistration = null;
    private final ServiceRegistrationTracker registrationTracker = new ServiceRegistrationTracker();
    
    public void start(BundleContext context) throws Exception {
        boolean registerCommands = true;
        try {
            Class.forName(PROVIDER_NAME);
        } catch (ClassNotFoundException e) {
            registerCommands = false;
        }

        if (registerCommands) {
            ClassLoadingCommandProvider provider = new ClassLoadingCommandProvider(context);
            providerRegistration = context.registerService(PROVIDER_NAME, provider, null);
        }

        Runnable runnable = new PostStartInitialisationRunnable(context, this.registrationTracker);
        Thread thread = new Thread(runnable);
        thread.setDaemon(true);
        thread.start();
    }

    public void stop(BundleContext context) throws Exception {
        if (providerRegistration != null)
            providerRegistration.unregister();
        providerRegistration = null;

        this.registrationTracker.unregisterAll();
    }

    /**
     * Get a service which might not be immediately available
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
                    if (millis>0) {
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
        public void run() {
            CommandExecutor commandExecutor = getPotentiallyDelayedService(context, CommandExecutor.class, COMMAND_EXECUTOR_SERVICE_WAIT);
            if (commandExecutor == null)
                return; // TODO: report this failure -- but where?

            this.registrationTracker.track(context.registerService(CommandProvider.class.getName(), new OsgiKernelShellCommand(commandExecutor), null));
        }
    }
}
