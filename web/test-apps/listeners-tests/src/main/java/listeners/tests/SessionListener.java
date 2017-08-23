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

package listeners.tests;

import java.lang.management.ManagementFactory;
import java.util.HashMap;

import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionAttributeListener;
import javax.servlet.http.HttpSessionBindingEvent;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

public class SessionListener implements HttpSessionListener,
		HttpSessionAttributeListener, SessionListenerMBean {

	private final AtomicInteger counter = new AtomicInteger();
	private volatile CountDownLatch latch;
	private volatile HttpSession session;
	private static final Map<String, Object> attAdded = new HashMap<String, Object>();
	private static final Map<String, Object> attRemoved = new HashMap<String, Object>();
	private static final Map<String, Object> attReplaced = new HashMap<String, Object>();

	public SessionListener() throws Exception {
		MBeanServer server = ManagementFactory.getPlatformMBeanServer();
		server.registerMBean(this, ObjectName
				.getInstance("test:name=HttpSessionListener"));
	}

	public void sessionCreated(HttpSessionEvent event) {
		this.counter.incrementAndGet();
		this.latch = new CountDownLatch(1);
		this.session = event.getSession();
	}

	public void sessionDestroyed(HttpSessionEvent se) {
		this.counter.decrementAndGet();
		CountDownLatch latch = this.latch;
		if (latch != null) {
			latch.countDown();
		}
	}

	public void awaitNextDecrement() throws InterruptedException {
		CountDownLatch latch = this.latch;
		if (latch != null) {
			latch.await();
		}
	}

	public int getSessionCount() {
		return this.counter.get();
	}

	public String getSessionId() {
		return this.session.getId();
	}

	public void invalidate() {
		this.session.invalidate();
	}

	public void attributeAdded(HttpSessionBindingEvent event) {
		attAdded.put(event.getName(), event.getValue());
	}

	public void attributeRemoved(HttpSessionBindingEvent event) {
		attRemoved.put(event.getName(), event.getValue());
	}

	public void attributeReplaced(HttpSessionBindingEvent event) {
		attReplaced.put(event.getName(), event.getValue());

	}

	public Map<String, Object> getAddedSessionAttribute() {
		return attAdded;
	}

	public Map<String, Object> getRemovedSessionAttribute() {
		return attRemoved;
	}

	public Map<String, Object> getReplacedSessionAttribute() {
		return attReplaced;
	}

}
