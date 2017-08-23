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

package org.eclipse.virgo.kernel.agent.dm;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.virgo.nano.shim.serviceability.TracingService;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.core.task.TaskExecutor;


/**
 * {@link TaskExecutor} implementation that propagates trace context to spawned threads.
 * <p/>
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Thread-safe.
 * 
 */
public final class ContextPropagatingTaskExecutor implements TaskExecutor, DisposableBean {
    
    private static final NoOpTracingService NO_OP_TRACING_SERVICE = new NoOpTracingService();

    private final AtomicInteger threadCount = new AtomicInteger();

    private final ExecutorService executor;    

    private volatile TracingService tracingService;
    
    private final BundleContext bundleContext;

    public ContextPropagatingTaskExecutor(final String threadNamePrefix, int poolSize, BundleContext bundleContext) {
        
        this.bundleContext = bundleContext;
        
        this.executor = Executors.newFixedThreadPool(poolSize, new ThreadFactory() {

            public Thread newThread(Runnable r) {
                Thread t = new Thread(r);
                t.setName(threadNamePrefix + ContextPropagatingTaskExecutor.this.threadCount.getAndIncrement());
                return t;
            }
        });                
    }

    private TracingService getTracingService() {
        TracingService localTracingService = this.tracingService;
        
        if (localTracingService != null && NO_OP_TRACING_SERVICE.equals(localTracingService)) {
            return localTracingService;
        }                
                
        ServiceReference<TracingService> serviceReference = this.bundleContext.getServiceReference(TracingService.class);
        if (serviceReference != null) {
            localTracingService = (TracingService) this.bundleContext.getService(serviceReference);
        }
        
        if (localTracingService == null) {
            localTracingService = NO_OP_TRACING_SERVICE;
        }
        
        this.tracingService = localTracingService;
        
        return localTracingService;
    }

    /**
     * {@inheritDoc}
     */
    public void execute(final Runnable task) {
        final String applicationName = getTracingService().getCurrentApplicationName();
        final ClassLoader threadContextClassLoader = Thread.currentThread().getContextClassLoader();
        this.executor.execute(new Runnable() {

            public void run() {
                ClassLoader originalContextClassLoader = Thread.currentThread().getContextClassLoader();
                try {
                    Thread.currentThread().setContextClassLoader(threadContextClassLoader);
                    ContextPropagatingTaskExecutor.this.tracingService.setCurrentApplicationName(applicationName);
                    task.run();
                } finally {
                    ContextPropagatingTaskExecutor.this.tracingService.setCurrentApplicationName(null);
                    Thread.currentThread().setContextClassLoader(originalContextClassLoader);
                }
            }

        });
    }

    /**
     * {@inheritDoc}
     */
    public void destroy() throws Exception {
        if (this.executor != null) {
            this.executor.shutdown();
        }
    }
    
    private static final class NoOpTracingService implements TracingService {
        public String getCurrentApplicationName() {            
            return null;
        }

        public void setCurrentApplicationName(String applicationName) {
        }        
    }
}
