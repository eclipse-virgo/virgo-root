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

package org.eclipse.virgo.kernel.config.internal.commandline;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.junit.Test;


import org.eclipse.virgo.kernel.config.internal.PropertiesSource;
import org.eclipse.virgo.kernel.config.internal.commandline.CommandLinePropertiesSource;
import org.eclipse.virgo.medic.test.eventlog.LoggedEvent;
import org.eclipse.virgo.medic.test.eventlog.MockEventLogger;
import org.eclipse.virgo.teststubs.osgi.framework.StubBundleContext;

public class CommandLinePropertiesSourceTests {
	
	private StubBundleContext bundleContext = new StubBundleContext();
	
	private MockEventLogger eventLogger = new MockEventLogger();
	
	@Test
	public void defaults() {
		PropertiesSource propertiesSource = new CommandLinePropertiesSource(this.bundleContext, this.eventLogger);
		Map<String, Properties> configurationProperties = propertiesSource.getConfigurationProperties();
		
		Properties properties = configurationProperties.get("org.eclipse.virgo.kernel.userregion");
		assertNotNull(properties);
		
		assertEquals("", properties.get("commandLineArtifacts"));
		
		assertEquals(0, this.eventLogger.getLoggedEvents().size());
	}
	
	@Test
	public void singlePlanWithVersion() {
		this.bundleContext.addProperty("org.eclipse.virgo.osgi.launcher.unrecognizedArguments", "-plan,foo,1");
		PropertiesSource propertiesSource = new CommandLinePropertiesSource(this.bundleContext, this.eventLogger);
		Map<String, Properties> configurationProperties = propertiesSource.getConfigurationProperties();
		
		Properties properties = configurationProperties.get("org.eclipse.virgo.kernel.userregion");
		assertNotNull(properties);
		
		assertEquals("repository:plan/foo/1", properties.get("commandLineArtifacts"));
		
		assertEquals(0, this.eventLogger.getLoggedEvents().size());
	}
	
	@Test
	public void singlePlanWithoutVersion() {
		this.bundleContext.addProperty("org.eclipse.virgo.osgi.launcher.unrecognizedArguments", "-plan,foo");
		PropertiesSource propertiesSource = new CommandLinePropertiesSource(this.bundleContext, this.eventLogger);
		Map<String, Properties> configurationProperties = propertiesSource.getConfigurationProperties();
		
		Properties properties = configurationProperties.get("org.eclipse.virgo.kernel.userregion");
		assertNotNull(properties);
		
		assertEquals("repository:plan/foo", properties.get("commandLineArtifacts"));
		
		assertEquals(0, this.eventLogger.getLoggedEvents().size());
	}
	
	@Test
	public void multiplePlans() {
		this.bundleContext.addProperty("org.eclipse.virgo.osgi.launcher.unrecognizedArguments", "-plan,foo,-plan,bar,1.2.3");
		PropertiesSource propertiesSource = new CommandLinePropertiesSource(this.bundleContext, this.eventLogger);
		Map<String, Properties> configurationProperties = propertiesSource.getConfigurationProperties();
		
		Properties properties = configurationProperties.get("org.eclipse.virgo.kernel.userregion");
		assertNotNull(properties);
		
		assertEquals("repository:plan/foo,repository:plan/bar/1.2.3", properties.get("commandLineArtifacts"));
		
		assertEquals(0, this.eventLogger.getLoggedEvents().size());
	}
	
	@Test
	public void planWithMissingArguments() {
		this.bundleContext.addProperty("org.eclipse.virgo.osgi.launcher.unrecognizedArguments", "-plan");
		PropertiesSource propertiesSource = new CommandLinePropertiesSource(this.bundleContext, this.eventLogger);
		Map<String, Properties> configurationProperties = propertiesSource.getConfigurationProperties();
		
		Properties properties = configurationProperties.get("org.eclipse.virgo.kernel.userregion");
		assertNotNull(properties);
		
		assertEquals("", properties.get("commandLineArtifacts"));
		
		List<LoggedEvent> loggedEvents = this.eventLogger.getLoggedEvents();
		assertEquals(1, loggedEvents.size());
		
		assertArrayEquals(new Object[] {0, ""}, loggedEvents.get(0).getInserts());
	}
	
	@Test
	public void planWithSurplusArguments() {
		this.bundleContext.addProperty("org.eclipse.virgo.osgi.launcher.unrecognizedArguments", "-plan,foo,bar,1.2.3");
		PropertiesSource propertiesSource = new CommandLinePropertiesSource(this.bundleContext, this.eventLogger);
		Map<String, Properties> configurationProperties = propertiesSource.getConfigurationProperties();
		
		Properties properties = configurationProperties.get("org.eclipse.virgo.kernel.userregion");
		assertNotNull(properties);
		
		assertEquals("", properties.get("commandLineArtifacts"));
		
		List<LoggedEvent> loggedEvents = this.eventLogger.getLoggedEvents();
		assertEquals(1, loggedEvents.size());
		
		assertArrayEquals(new Object[] {3, "foo, bar, 1.2.3"}, loggedEvents.get(0).getInserts());
	}

}
