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

package org.eclipse.virgo.kernel.core.internal;

import java.lang.management.ManagementFactory;
import java.util.concurrent.TimeUnit;

import javax.management.JMException;
import javax.management.MBeanServer;
import javax.management.ObjectInstance;
import javax.management.ObjectName;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import org.eclipse.virgo.kernel.config.internal.KernelConfiguration;
import org.eclipse.virgo.kernel.core.BlockingSignal;
import org.eclipse.virgo.kernel.core.BundleUtils;
import org.eclipse.virgo.kernel.core.FailureSignalledException;
import org.eclipse.virgo.kernel.core.FatalKernelException;
import org.eclipse.virgo.kernel.core.Shutdown;
import org.eclipse.virgo.kernel.diagnostics.KernelLogEvents;
import org.eclipse.virgo.medic.dump.DumpGenerator;
import org.eclipse.virgo.medic.eventlog.EventLogger;

/**
 * <code>StartupTracker</code> tracks the startup of the Kernel and produces event log entries, and
 * {@link EventAdmin} events as the kernel starts.
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Thread-safe.
 * 
 */
final class StartupTracker {

    private static final String THREAD_NAME_STARTUP_TRACKER = "startup-tracker";

	static final String APPLICATION_CONTEXT_FILTER = "(objectClass=org.springframework.context.ApplicationContext)";

    private static final String KERNEL_EVENT_TOPIC = "org/eclipse/virgo/kernel/";

    private static final String KERNEL_EVENT_STARTING = KERNEL_EVENT_TOPIC + "STARTING";

    private static final String KERNEL_EVENT_STARTED = KERNEL_EVENT_TOPIC + "STARTED";
    
    private static final String KERNEL_EVENT_START_TIMED_OUT = KERNEL_EVENT_TOPIC + "START_TIMED_OUT";
    
    private static final String KERNEL_EVENT_START_FAILED = KERNEL_EVENT_TOPIC + "START_FAILED";
    
    private static final String KERNEL_BSN_PREFIX = "org.eclipse.virgo.kernel";
    
    private static final Logger LOGGER = LoggerFactory.getLogger(StartupTracker.class);

    private final KernelStatus status = new KernelStatus();

    private final KernelConfiguration configuration;

    private final Thread startupTrackingThread;
    
    private final Shutdown shutdown;
    
    private final DumpGenerator dumpGenerator;

    private volatile ObjectInstance statusInstance;

    StartupTracker(BundleContext context, KernelConfiguration configuration, int startupWaitTime, BundleStartTracker asyncBundleStartTracker, Shutdown shutdown, DumpGenerator dumpGenerator) {
    	Runnable startupTrackingRunnable = new StartupTrackingRunnable(context, startupWaitTime, asyncBundleStartTracker);
        this.startupTrackingThread = new Thread(startupTrackingRunnable, THREAD_NAME_STARTUP_TRACKER);
        this.configuration = configuration;
        this.shutdown = shutdown;
        this.dumpGenerator = dumpGenerator;
    }

    void start() {
        registerKernelStatusMBean();
        this.startupTrackingThread.start();
    }
    
    void stop() {
        unregisterKernelStatusMBean();
    }

    private void registerKernelStatusMBean() {
        MBeanServer server = ManagementFactory.getPlatformMBeanServer();
        try {
            ObjectName name = ObjectName.getInstance(StartupTracker.this.configuration.getDomain(), "type", "KernelStatus");
            this.statusInstance = server.registerMBean(this.status, name);
        } catch (JMException e) {
            throw new FatalKernelException("Unable to register KernelStatus MBean", e);
        }
    }

    private void unregisterKernelStatusMBean() {
        MBeanServer server = ManagementFactory.getPlatformMBeanServer();
        try {
            ObjectInstance instance = this.statusInstance;
            if (instance != null && server.isRegistered(instance.getObjectName())) {
                server.unregisterMBean(this.statusInstance.getObjectName());
            }
        } catch (JMException e) {
            throw new FatalKernelException("Unable to unregister KernelStatus MBean", e);
        }
    }

    private void signalStarted() {
        this.status.setStatus(KernelStatus.STATUS_STARTED);
    }

    private final class StartupTrackingRunnable implements Runnable {        

        private final BundleContext context;
        
        private final int startupWaitTime;
        
        private final BundleStartTracker asyncBundleStartTracker;

        private StartupTrackingRunnable(BundleContext context, int startupWaitTime, BundleStartTracker asyncBundleStartTracker) {
            this.context = context;
            this.startupWaitTime = startupWaitTime;
            this.asyncBundleStartTracker = asyncBundleStartTracker;
        }

        public void run() {
            
            kernelStarting();
            
            Bundle[] bundles = this.context.getBundles();
            
            try {
                for (Bundle bundle : bundles) {
                	if (!BundleUtils.isFragmentBundle(bundle) && isKernelBundle(bundle)) {
	                    BlockingSignal signal = new BlockingSignal();
	                    
	                    this.asyncBundleStartTracker.trackStart(bundle, signal);
	                    
	                    LOGGER.debug("Awaiting signal {} for up to {} seconds", signal, this.startupWaitTime);
	                    
	                    if (!signal.awaitCompletion(this.startupWaitTime, TimeUnit.SECONDS)) {
	                    	LOGGER.error("Bundle {} did not start within {} seconds.", bundle, this.startupWaitTime);
	                        kernelStartTimedOut();
	                        return;
	                    }
                	}
                }
            } catch (FailureSignalledException fse) {
                kernelStartFailed(fse.getCause());
                return;
            } catch (Exception e) {
                kernelStartFailed(e);
                return;
            }
            
            kernelStarted();
        }
        
        private boolean isKernelBundle(Bundle bundle) {
        	String symbolicName = bundle.getSymbolicName();
			return symbolicName != null && symbolicName.startsWith(KERNEL_BSN_PREFIX);
        }

        private void kernelStarting() {
            postEvent(KERNEL_EVENT_STARTING);
            logEvent(KernelLogEvents.KERNEL_STARTING);
        }

        private void kernelStarted() {
            signalStarted();
            postEvent(KERNEL_EVENT_STARTED);
            logEvent(KernelLogEvents.KERNEL_STARTED);
        }
        
        private void kernelStartTimedOut() {
            postEvent(KERNEL_EVENT_START_TIMED_OUT);
            logEvent(KernelLogEvents.KERNEL_START_TIMED_OUT, this.startupWaitTime);
            generateDumpAndShutdown("startupTimedOut", null);
        }
        
        private void kernelStartFailed(Throwable failure) {
            postEvent(KERNEL_EVENT_START_FAILED);
            logEvent(KernelLogEvents.KERNEL_START_FAILED, failure);
            generateDumpAndShutdown("startupFailed", failure);
        }
        
        private void generateDumpAndShutdown(String cause, Throwable failure) {
            if (failure != null) {
                StartupTracker.this.dumpGenerator.generateDump(cause, failure);
            } else {
                StartupTracker.this.dumpGenerator.generateDump(cause);
            }
            StartupTracker.this.shutdown.immediateShutdown();
        }
        
        private void logEvent(KernelLogEvents event, Throwable throwable, Object...args) {
            ServiceReference serviceReference = this.context.getServiceReference(EventLogger.class.getName());
            if (serviceReference != null) {
                EventLogger eventLogger = (EventLogger) this.context.getService(serviceReference);
                if (eventLogger != null) {
                    eventLogger.log(event, throwable, args);
                    this.context.ungetService(serviceReference);
                }
            }
        }

        private void logEvent(KernelLogEvents event, Object... args) {
            this.logEvent(event, null, args);
        }

        private void postEvent(String topic) {
            ServiceReference serviceReference = this.context.getServiceReference(EventAdmin.class.getName());
            if (serviceReference != null) {
                EventAdmin eventAdmin = (EventAdmin) this.context.getService(serviceReference);
                if (eventAdmin != null) {
                    eventAdmin.postEvent(new Event(topic, null));
                    this.context.ungetService(serviceReference);
                }
            }
        }
    }    
}
