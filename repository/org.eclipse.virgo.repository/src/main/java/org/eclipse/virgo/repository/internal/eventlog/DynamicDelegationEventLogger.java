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

package org.eclipse.virgo.repository.internal.eventlog;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

import org.eclipse.virgo.medic.eventlog.EventLogger;
import org.eclipse.virgo.medic.eventlog.Level;
import org.eclipse.virgo.medic.eventlog.LogEvent;

/**
 * An {@link EventLogger} implementation that delegates to all of the
 * <code>EventLogger</code> implementations available in the service registry
 * when <code>log(...)</code> is called.
 * 
 * <p />
 * 
 * <strong>Note</strong>: a <code>DynamicDelegationEventLogger</code> must be
 * {@link #start() started} before it will find any <code>EventLoggers</code> in
 * the service registry and prior to disposal, it should be {@link #stop()
 * stopped}.
 * 
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Thread-safe.
 * 
 */
public final class DynamicDelegationEventLogger implements EventLogger {

	private final ServiceTracker<EventLogger, EventLogger> serviceTracker;

	private final EventLoggerTracker eventLoggerTracker;

	public DynamicDelegationEventLogger(BundleContext bundleContext) {
		this.eventLoggerTracker = new EventLoggerTracker(bundleContext);
		this.serviceTracker = new ServiceTracker<EventLogger, EventLogger>(
				bundleContext, EventLogger.class.getName(),
				this.eventLoggerTracker);
	}

	public void start() {
		this.serviceTracker.open();
	}

	public void stop() {
		this.serviceTracker.close();
	}

	/**
	 * {@inheritDoc}
	 */
	public void log(LogEvent logEvent, Object... inserts) {
		log(logEvent.getEventCode(), logEvent.getLevel(), null, inserts);
	}

	/**
	 * {@inheritDoc}
	 */
	public void log(String code, Level level, Object... inserts) {
		log(code, level, null, inserts);
	}

	/**
	 * {@inheritDoc}
	 */
	public void log(LogEvent logEvent, Throwable throwable, Object... inserts) {
		log(logEvent.getEventCode(), logEvent.getLevel(), throwable, inserts);
	}

	/**
	 * {@inheritDoc}
	 */
	public void log(String code, Level level, Throwable throwable,
			Object... inserts) {
		Object[] objects = this.serviceTracker.getServices();

		if (objects != null) {
			for (Object object : objects) {
				((EventLogger) object).log(code, level, throwable, inserts);
			}
		}
	}

	private static final class EventLoggerTracker implements
			ServiceTrackerCustomizer<EventLogger, EventLogger> {

		private final BundleContext bundleContext;

		private EventLoggerTracker(BundleContext bundleContext) {
			this.bundleContext = bundleContext;
		}

		public EventLogger addingService(ServiceReference<EventLogger> reference) {
			EventLogger service = (EventLogger) this.bundleContext
					.getService(reference);
			return service;
		}

		public void modifiedService(ServiceReference<EventLogger> reference,
				EventLogger service) {
		}

		public void removedService(ServiceReference<EventLogger> reference,
				EventLogger service) {
			this.bundleContext.ungetService(reference);
		}
	}
}
