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

package org.eclipse.virgo.medic.log.test;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import ch.qos.logback.classic.spi.LoggingEvent;

import org.eclipse.virgo.medic.log.appender.StubAppender;
import org.eclipse.virgo.test.framework.OsgiTestRunner;


@RunWith(OsgiTestRunner.class)
public class PrintStreamWrappingTests {
	
	@Test
	@Ignore("[DMS-2879] Test fails on the CI server as it wraps System.out")
	public void sysOutWrapping() {
		System.out.println("Hello world!");
		List<LoggingEvent> loggingEvents = StubAppender.getAndResetLoggingEvents("root-stub");
		assertEquals(1, loggingEvents.size());
		assertEquals("Hello world!", loggingEvents.get(0).getMessage());
	}
	
	@Test
	@Ignore("[DMS-2879] Test fails on the CI server as it wraps System.err")
	public void sysErrWrapping() {
		System.err.println("Hello world!");
		List<LoggingEvent> loggingEvents = StubAppender.getAndResetLoggingEvents("root-stub");
		assertEquals(1, loggingEvents.size());
		assertEquals("Hello world!", loggingEvents.get(0).getMessage());
	}
}
