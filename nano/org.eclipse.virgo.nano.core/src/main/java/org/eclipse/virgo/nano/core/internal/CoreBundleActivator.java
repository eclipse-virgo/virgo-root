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

package org.eclipse.virgo.nano.core.internal;

import java.lang.management.ManagementFactory;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import javax.management.InstanceNotFoundException;
import javax.management.JMException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.ObjectInstance;
import javax.management.ObjectName;

import org.eclipse.virgo.nano.config.internal.KernelConfiguration;
import org.eclipse.virgo.nano.config.internal.ConfigurationInitialiser;
import org.eclipse.virgo.nano.core.BundleStarter;
import org.eclipse.virgo.nano.core.Shutdown;
import org.eclipse.virgo.nano.core.internal.blueprint.ApplicationContextDependencyMonitor;
import org.eclipse.virgo.nano.serviceability.dump.internal.RegionDigraphDumpContributor;
import org.eclipse.virgo.nano.serviceability.dump.internal.ResolutionDumpContributor;
import org.eclipse.virgo.nano.shim.scope.ScopeFactory;
import org.eclipse.virgo.nano.shim.scope.internal.StandardScopeFactory;
import org.eclipse.virgo.nano.shim.serviceability.TracingService;
import org.eclipse.virgo.nano.shim.serviceability.internal.Slf4jTracingService;
import org.eclipse.virgo.medic.dump.DumpContributor;
import org.eclipse.virgo.medic.dump.DumpGenerator;
import org.eclipse.virgo.medic.eventlog.EventLogger;
import org.eclipse.virgo.util.osgi.ServiceRegistrationTracker;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.launch.Framework;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;

/**
 * ComponentContext activator that initialises the core of the Kernel.
 * 
 * <strong>Concurrent Semantics</strong><br />
 * Threadsafe.
 * 
 */
public class CoreBundleActivator {

    private static final String START_SIGNALLING_THREAD_NAME_PREFIX = "start-signalling-";

    private static final String PROPERTY_NAME_SERVICE_SCOPE = "org.eclipse.virgo.service.scope";
    
    private static final String SERVICE_SCOPE_GLOBAL = "global";

    private static final String EVENT_TOPIC_BLUEPRINT_CONTAINER = "org/osgi/service/blueprint/container/*";

    private static final String EVENT_TOPIC_REGION = "org/eclipse/virgo/kernel/region/*";
    
    private static final String MBEAN_VALUE_SHUTDOWN = "Shutdown";

    private static final String MBEAN_KEY_TYPE = "type";
    
    private final ServiceRegistrationTracker tracker = new ServiceRegistrationTracker();

    private final ConfigurationInitialiser configurationInitialiser = new ConfigurationInitialiser();

    private volatile StartupTracker startupTracker;

    private volatile ObjectInstance shutdownMBean;
    
    private volatile ApplicationContextDependencyMonitor dependencyMonitor;
    
    private volatile BundleStartTracker bundleStartTracker;

    public void activate(ComponentContext componentContext) throws Exception {
        BundleContext context = componentContext.getBundleContext();
        
        EventLogger eventLogger = getRequiredService(context, EventLogger.class);

        KernelConfiguration configuration = this.configurationInitialiser.start(context, eventLogger);
        Shutdown shutdown = initializeShutdownManager(context, eventLogger, configuration);
        
        this.bundleStartTracker = createAndRegisterBundleStartTracker(context);    	
        createAndRegisterBundleStarter(this.bundleStartTracker, context);
        
        this.dependencyMonitor = createAndRegisterApplicationContextDependencyMonitor(context, eventLogger);
        
        DumpGenerator dumpGenerator = getRequiredService(context, DumpGenerator.class);
        
        createAndRegisterStateDumpContributors(context);
        
        this.startupTracker = new StartupTracker(context, configuration, configuration.getStartupWaitLimit(), bundleStartTracker, shutdown, dumpGenerator);
        this.startupTracker.start();
        
        initShimServices(context, eventLogger);
    }
    
    private void createAndRegisterStateDumpContributors(BundleContext context) {
        this.tracker.track(context.registerService(DumpContributor.class, new ResolutionDumpContributor(context), null));
        this.tracker.track(context.registerService(DumpContributor.class, new RegionDigraphDumpContributor(context), null));
    }

    private ApplicationContextDependencyMonitor createAndRegisterApplicationContextDependencyMonitor(BundleContext context, EventLogger eventLogger) {
        ScheduledExecutorService scheduledExecutor = Executors.newScheduledThreadPool(1, new ThreadFactory() {            
            private AtomicLong threadCount = new AtomicLong(1);

            public Thread newThread(Runnable r) {
                String name = "service-monitor-thread-" + this.threadCount.getAndIncrement();
                return new Thread(r, name);                
            }            
        });
        
        ApplicationContextDependencyMonitor dependencyMonitor = new ApplicationContextDependencyMonitor(scheduledExecutor, eventLogger);
        
        Dictionary<String, String> properties = new Hashtable<String, String>();
        properties.put(EventConstants.EVENT_TOPIC, EVENT_TOPIC_BLUEPRINT_CONTAINER);
        
        this.tracker.track(context.registerService(EventHandler.class.getName(), dependencyMonitor, properties));
        
       return dependencyMonitor;
    }

    @SuppressWarnings("unchecked")
    private BundleStartTracker createAndRegisterBundleStartTracker(BundleContext context) {
    	BlockingQueue q = new SynchronousQueue();
    	ThreadPoolExecutor executor = new ThreadPoolExecutor(1, Integer.MAX_VALUE, 60, TimeUnit.SECONDS, q, new PrefixingThreadFactory(START_SIGNALLING_THREAD_NAME_PREFIX), new ThreadPoolExecutor.AbortPolicy());
    	BundleStartTracker asynchronousStartTracker = new BundleStartTracker(executor);
    	asynchronousStartTracker.initialize(context);
    	
    	Dictionary<String, Object> properties = new Hashtable<String, Object>();
        properties.put(EventConstants.EVENT_TOPIC, new String[] {EVENT_TOPIC_BLUEPRINT_CONTAINER, EVENT_TOPIC_REGION});        
        
        this.tracker.track(context.registerService(new String[] {EventHandler.class.getName()}, asynchronousStartTracker, properties));
        
        return asynchronousStartTracker;
    }

    private BundleStarter createAndRegisterBundleStarter(BundleStartTracker asynchronousStartTracker, BundleContext bundleContext) {
    	
        StandardBundleStarter bundleStarter = new StandardBundleStarter(asynchronousStartTracker);
        Dictionary<String, Object> properties = new Hashtable<String, Object>();
        properties.put(PROPERTY_NAME_SERVICE_SCOPE, SERVICE_SCOPE_GLOBAL);
        
        this.tracker.track(bundleContext.registerService(new String[] {BundleStarter.class.getName()}, bundleStarter, properties));
        
        return bundleStarter;
    }

    public void deactivate(ComponentContext context) throws Exception {
        this.tracker.unregisterAll();
        this.startupTracker.stop();
        this.configurationInitialiser.stop();
        
        unregisterShutdownMBean();
        
        ApplicationContextDependencyMonitor dependencyMonitor = this.dependencyMonitor;
        if (dependencyMonitor != null) {
            this.dependencyMonitor = null;
            dependencyMonitor.stop();
        }
        
        BundleStartTracker bundleStartTracker = this.bundleStartTracker;
        if (bundleStartTracker != null) {
            this.bundleStartTracker = null;
            bundleStartTracker.stop();
        }
    }

    private Shutdown initializeShutdownManager(BundleContext context, EventLogger eventLogger, KernelConfiguration configuration) {
        Shutdown shutdown = createShutdown(context, eventLogger);
        this.tracker.track(context.registerService(org.eclipse.virgo.nano.core.Shutdown.class.getName(), shutdown, null));

        registerShutdownMBean(configuration, shutdown);
        return shutdown;
    }

    private void registerShutdownMBean(KernelConfiguration configuration, Shutdown shutdown) {
        MBeanServer server = ManagementFactory.getPlatformMBeanServer();

        try {
            ObjectName shutdownName = ObjectName.getInstance(configuration.getDomain(), MBEAN_KEY_TYPE, MBEAN_VALUE_SHUTDOWN);
            this.shutdownMBean = server.registerMBean(new AsyncShutdownDecorator(shutdown), shutdownName);
        } catch (JMException ex) {
            throw new IllegalStateException("Unable to register Shutdown MBean", ex);
        }
    }

    private void unregisterShutdownMBean() throws MBeanRegistrationException, InstanceNotFoundException {
        ObjectInstance localShutdownMBean = this.shutdownMBean;
        if (localShutdownMBean != null) {
            ManagementFactory.getPlatformMBeanServer().unregisterMBean(localShutdownMBean.getObjectName());
            this.shutdownMBean = null;
        }
    }

    protected Shutdown createShutdown(BundleContext context, EventLogger eventLogger) {
        Framework framework = (Framework) context.getBundle(0);
        Runtime runtime = Runtime.getRuntime();
        ShutdownManager manager = new ShutdownManager(eventLogger, framework, runtime);
        return manager;
    }

    private void initShimServices(BundleContext context, EventLogger eventLogger) {
        ScopeFactory scopeFactory = new StandardScopeFactory(eventLogger);
        TracingService tracingService = new Slf4jTracingService();
        this.tracker.track(context.registerService(ScopeFactory.class.getName(), scopeFactory, null));
        this.tracker.track(context.registerService(TracingService.class.getName(), tracingService, null));
    }

    private <T> T getRequiredService(BundleContext context, Class<T> clazz) {
        T result = null;
        ServiceReference<T> ref = context.getServiceReference(clazz);
        if (ref != null) {
            result = context.getService(ref);
        }
        if (result == null) {
            throw new IllegalStateException("Unable to access required service of type '" + clazz.getName() + "' from bundle '"
                + context.getBundle().getSymbolicName() + "'");
        }
        return result;
    }
    
    private class PrefixingThreadFactory implements ThreadFactory {
		private int threadCount = 0;
		private final Object threadCountMonitor = new Object();
		String prefix = "default-thread-prefix";
		
		public PrefixingThreadFactory(String prefix) {
			this.prefix = prefix;
		}
		
		@Override
		public Thread newThread(Runnable r) {
			return new Thread(r, nextThreadName());
		}
		
		private String nextThreadName() {
			int threadNumber = 0;
			synchronized (this.threadCountMonitor) {
				this.threadCount++;
				threadNumber = this.threadCount;
			}
			return this.prefix + threadNumber;
		}
	}
}
