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

package test;

import java.lang.management.ManagementFactory;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;
import javax.servlet.http.HttpSession;

import org.eclipse.virgo.nano.shim.serviceability.TracingService;

public class SessionListener implements HttpSessionListener, SessionListenerMBean {

    private final AtomicInteger counter = new AtomicInteger();

    private volatile String applicationName;
    
    private volatile CountDownLatch latch;

	private volatile HttpSession session;
    
    public SessionListener() throws Exception {
        MBeanServer server = ManagementFactory.getPlatformMBeanServer();
        server.registerMBean(this, ObjectName.getInstance("test:name=SessionListener"));
    }

    public void sessionCreated(HttpSessionEvent event) {
        setApplicationName();
        this.counter.incrementAndGet();
        this.latch = new CountDownLatch(1);
		this.session = event.getSession();
    }

    /**
     * 
     */
    private void setApplicationName() {
        TracingService tracingService = TracingServiceHolder.getInstance().getTracingService();
        if(tracingService != null) {
            this.applicationName = tracingService.getCurrentApplicationName();
        }
    }

	public void invalidate() {
		this.session.invalidate();
	}
	
    public void sessionDestroyed(HttpSessionEvent event) {
        setApplicationName();
        this.counter.decrementAndGet();
        CountDownLatch latch = this.latch;
        if(latch != null) {
            latch.countDown();
        }
    }

    public void awaitNextDecrement() throws InterruptedException {
        CountDownLatch latch = this.latch;
        if(latch != null) {
            latch.await();
        }
    }
    
    public String getApplicationName() {
        return this.applicationName;
    }

    public int getSessionCount() {
        return this.counter.get();
    }

}
