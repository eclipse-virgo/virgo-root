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

package org.eclipse.virgo.medic.log.impl.config;

import static org.easymock.EasyMock.createMock;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Map;
import java.util.TreeMap;

import org.junit.Test;

import org.eclipse.virgo.medic.log.LoggingConfiguration;
import org.eclipse.virgo.medic.log.impl.config.ServiceRegistryConfigurationLocator;
import org.eclipse.virgo.test.stubs.framework.StubBundle;
import org.eclipse.virgo.test.stubs.framework.StubBundleContext;
import org.eclipse.virgo.test.stubs.support.FalseFilter;
import org.eclipse.virgo.test.stubs.support.PropertiesFilter;

public class ServiceRegistryConfigurationLocatorTests {

    private final StubBundle bundle = new StubBundle();

    private final StubBundleContext bundleContext = new StubBundleContext();

    private final ServiceRegistryConfigurationLocator locator = new ServiceRegistryConfigurationLocator(this.bundleContext);

    @Test
    public void noConfigurationInServiceRegistry() {
        this.bundle.addHeader("Medic-LoggingConfiguration", "foo");
        this.bundleContext.addFilter(new FalseFilter("(org.eclipse.virgo.medic.log.configuration.id=foo)"));
        LoggingConfiguration configuration = this.locator.locateConfiguration(this.bundle);
        assertNull(configuration);
    }

    @Test
    public void singleConfigurationInServiceRegistry() {
        this.bundle.addHeader("Medic-LoggingConfiguration", "foo");
        LoggingConfiguration loggingConfiguration = createMock(LoggingConfiguration.class);
        this.bundleContext.addFilter(new PropertiesFilter(getDefaultMap()));
        this.bundleContext.registerService(LoggingConfiguration.class, loggingConfiguration, getDefaultDictionary());
        LoggingConfiguration configuration = this.locator.locateConfiguration(this.bundle);
        assertEquals(loggingConfiguration, configuration);
    }

    @Test
    public void multipleConfigurationsInServiceRegistry() {
        this.bundle.addHeader("Medic-LoggingConfiguration", "foo");
        LoggingConfiguration loggingConfiguration = createMock(LoggingConfiguration.class);
        this.bundleContext.addFilter(new PropertiesFilter(getDefaultMap()));
        this.bundleContext.registerService(LoggingConfiguration.class.getName(), loggingConfiguration, getDefaultDictionary());
        this.bundleContext.registerService(LoggingConfiguration.class.getName(), loggingConfiguration, getDefaultDictionary());
        this.bundleContext.registerService(LoggingConfiguration.class.getName(), loggingConfiguration, getDefaultDictionary());
        LoggingConfiguration configuration = this.locator.locateConfiguration(this.bundle);
        assertEquals(loggingConfiguration, configuration);
    }

    @Test
    public void noHeaderInManifest() {
        this.locator.locateConfiguration(this.bundle);
    }

    private Dictionary<String,String> getDefaultDictionary() {
        Dictionary<String,String> properties = new Hashtable<String,String>();
        properties.put("org.eclipse.virgo.medic.log.configuration.id", "foo");
        return properties;
    }

    private Map<String, Object> getDefaultMap() {
        Map<String, Object> properties = new TreeMap<String, Object>();
        properties.put("org.eclipse.virgo.medic.log.configuration.id", "foo");
        return properties;
    }
}
