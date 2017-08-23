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

package org.eclipse.virgo.test.framework.plugin;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.virgo.test.framework.plugin.EmptyPlugin;
import org.eclipse.virgo.test.framework.plugin.Plugin;
import org.eclipse.virgo.test.framework.plugin.PluginException;
import org.eclipse.virgo.test.framework.plugin.PluginManager;
import org.eclipse.virgo.test.framework.plugin.Plugins;
import org.junit.Test;
import org.osgi.framework.launch.Framework;

public class PluginManagerTests {

	@Test
	public void testConstructPluginManager() {

		PluginManager manager = new PluginManager(BasicConfiguration.class);
		Plugin[] configuredPlugins = manager.getConfiguredPlugins();
		assertNotNull(configuredPlugins);
		assertEquals(2, configuredPlugins.length);
		assertTrue(configuredPlugins[0] instanceof DummyPlugin);
		assertTrue(configuredPlugins[1] instanceof SecondDummyPlugin);
	}
	
	@Test
	public void testConstructPluginManagerWithNoPlugins() {
		PluginManager manager = new PluginManager(EmptyConfiguration.class);
		Plugin[] configuredPlugins = manager.getConfiguredPlugins();
		assertNotNull(configuredPlugins);
		assertEquals(0, configuredPlugins.length);
	}

	@Test(expected=PluginException.class)
	public void testConstructWithError() {
		new PluginManager(ErrorConfiguration.class);
	}
	
	@Test
	public void testDelegate() {

		PluginManager manager = new PluginManager(CounterConfiguration.class);
		Plugin delegate = manager.getPluginDelegate();
		assertEquals(0, CountingPlugin.counter.get());
		delegate.beforeInstallBundles(null, null);
		assertEquals(1, CountingPlugin.counter.get());
	}
	
	public static class EmptyConfiguration {
		
	}
	
	@Plugins( { DummyPlugin.class, SecondDummyPlugin.class })
	public static class BasicConfiguration {

	}
	
	@Plugins(InvalidPlugin.class)
	public static class ErrorConfiguration {
		
	}

	@Plugins(CountingPlugin.class)
    public static class CounterConfiguration {
        
    }
	
	public static class DummyPlugin extends EmptyPlugin {

	}

	public static class SecondDummyPlugin extends EmptyPlugin {

	}
	
	public static class CountingPlugin implements Plugin {

	    static final AtomicInteger counter = new AtomicInteger();
	    
        public void beforeInstallBundles(Framework framework, Properties configurationProperties) {
            counter.incrementAndGet();
        }
	    
	}
	
	public static class InvalidPlugin extends EmptyPlugin {
		private InvalidPlugin() {
			
		}
	}
}
