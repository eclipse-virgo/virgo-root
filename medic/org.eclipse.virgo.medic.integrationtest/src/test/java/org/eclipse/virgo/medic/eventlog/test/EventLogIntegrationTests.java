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

package org.eclipse.virgo.medic.eventlog.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.List;
import java.util.Locale;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.ServiceReference;

import ch.qos.logback.classic.spi.LoggingEvent;


import org.eclipse.virgo.medic.eventlog.EventLogger;
import org.eclipse.virgo.medic.eventlog.EventLoggerFactory;
import org.eclipse.virgo.medic.eventlog.Level;
import org.eclipse.virgo.medic.log.appender.StubAppender;
import org.eclipse.virgo.test.framework.OsgiTestRunner;
import org.eclipse.virgo.test.framework.TestFrameworkUtils;

//Medic integration tests do not run in the Eclipse IDE.

@RunWith(OsgiTestRunner.class)
public class EventLogIntegrationTests {
	
	private BundleContext bundleContext;
	
	private Bundle messageBundle;

    @Before
    public void before() throws BundleException {
    	Locale.setDefault(Locale.ITALIAN);
        this.bundleContext = TestFrameworkUtils.getBundleContextForTestClass(getClass());
        messageBundle = this.bundleContext.installBundle("file:src/test/resources/message-bundle");
		this.bundleContext.installBundle("file:src/test/resources/message-fragment");        
    }

	@Test
	public void availabilityOfEventLoggerFactory() {
		ServiceReference<EventLoggerFactory> serviceReference = this.bundleContext.getServiceReference(EventLoggerFactory.class);
		assertNotNull(serviceReference);
	}
	
	@Test
	public void availabilityOfEventLogger() {
		ServiceReference<EventLogger> serviceReference = this.bundleContext.getServiceReference(EventLogger.class);
		assertNotNull(serviceReference);
	}
	
	@Test
	public void eventLoggingWithMessageFromCurrentBundle() {
		ServiceReference<EventLogger> serviceReference = this.bundleContext.getServiceReference(EventLogger.class);
		assertNotNull(serviceReference);
		EventLogger eventLogger = this.bundleContext.getService(serviceReference);
		eventLogger.log("1234", Level.WARNING, "orange", "lemon");
		
		List<LoggingEvent> loggingEvent = StubAppender.getAndResetLoggingEvents("default-stub");
		assertEquals(1, loggingEvent.size());
		assertEquals("English orange and lemon", loggingEvent.get(0).getMessage());
		
		loggingEvent = StubAppender.getAndResetLoggingEvents("localized-stub");
		assertEquals(1, loggingEvent.size());
		assertEquals("Italian orange and lemon", loggingEvent.get(0).getMessage());
		
	}
	
	@Test
	public void eventLoggingWithMessageFromFragment() throws Exception {
		ServiceReference<EventLoggerFactory> serviceReference = this.bundleContext.getServiceReference(EventLoggerFactory.class);
		assertNotNull(serviceReference);
		EventLoggerFactory eventLoggerFactory = this.bundleContext.getService(serviceReference);		
		EventLogger eventLogger = eventLoggerFactory.createEventLogger(this.messageBundle);
		eventLogger.log("3456", Level.WARNING, "oak", "sycamore");
		
		List<LoggingEvent> loggingEvent = StubAppender.getAndResetLoggingEvents("default-stub");
		assertEquals(1, loggingEvent.size());
		assertEquals("Shared oak and sycamore", loggingEvent.get(0).getMessage());
		
		loggingEvent = StubAppender.getAndResetLoggingEvents("localized-stub");
		assertEquals(1, loggingEvent.size());
		assertEquals("Shared oak and sycamore", loggingEvent.get(0).getMessage());
	}
	
	@Test
	public void eventLoggingWithMessageFromSpecificBundle() throws Exception {
		ServiceReference<EventLoggerFactory> serviceReference = this.bundleContext.getServiceReference(EventLoggerFactory.class);
		assertNotNull(serviceReference);
		EventLoggerFactory eventLoggerFactory = this.bundleContext.getService(serviceReference);		
		EventLogger eventLogger = eventLoggerFactory.createEventLogger(this.messageBundle);
		eventLogger.log("2345", Level.WARNING, "potato", "cauliflower");
		
		List<LoggingEvent> loggingEvent = StubAppender.getAndResetLoggingEvents("default-stub");
		assertEquals(1, loggingEvent.size());
		assertEquals("English potato and cauliflower", loggingEvent.get(0).getMessage());
		
		loggingEvent = StubAppender.getAndResetLoggingEvents("localized-stub");
		assertEquals(1, loggingEvent.size());
		assertEquals("Italian potato and cauliflower", loggingEvent.get(0).getMessage());
	}		
}
