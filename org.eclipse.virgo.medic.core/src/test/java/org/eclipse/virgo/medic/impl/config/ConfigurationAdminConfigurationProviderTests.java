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

package org.eclipse.virgo.medic.impl.config;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.util.Dictionary;
import java.util.Hashtable;

import org.junit.Test;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;

import org.eclipse.virgo.medic.impl.config.ConfigurationAdminConfigurationProvider;
import org.eclipse.virgo.medic.impl.config.ConfigurationProvider;
import org.eclipse.virgo.teststubs.osgi.framework.StubBundleContext;

public class ConfigurationAdminConfigurationProviderTests {

    private final StubBundleContext bundleContext = new StubBundleContext();    

    @Test
    @SuppressWarnings("unchecked")
    public void getConfigurationWithoutConfigurationAdmin() {
    	ConfigurationProvider configurationProvider = new ConfigurationAdminConfigurationProvider(bundleContext);
        Dictionary configuration = configurationProvider.getConfiguration();
        assertNotNull(configuration);
        assertEquals(".", configuration.get(ConfigurationProvider.KEY_DUMP_ROOT_DIRECTORY));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void getConfigurationWithEmptyConfigurationAdminConfiguration() throws IOException {
        ConfigurationAdmin configurationAdmin = createMock(ConfigurationAdmin.class);
        Configuration configuration = createMock(Configuration.class);

        this.bundleContext.registerService(ConfigurationAdmin.class.getName(), configurationAdmin, null);
        expect(configurationAdmin.getConfiguration("org.eclipse.virgo.medic", null)).andReturn(configuration);
        expect(configuration.getProperties()).andReturn(null);

        replay(configurationAdmin, configuration);
        
        ConfigurationProvider configurationProvider = new ConfigurationAdminConfigurationProvider(bundleContext);

        Dictionary configDictionary = configurationProvider.getConfiguration();
        assertNotNull(configDictionary);
        assertEquals(".", configDictionary.get(ConfigurationProvider.KEY_DUMP_ROOT_DIRECTORY));

        verify(configurationAdmin, configuration);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void getConfigurationWithConfigurationAdminConfiguration() throws IOException {
        ConfigurationAdmin configurationAdmin = createMock(ConfigurationAdmin.class);
        Configuration configuration = createMock(Configuration.class);

        this.bundleContext.registerService(ConfigurationAdmin.class.getName(), configurationAdmin, null);
        Dictionary properties = new Hashtable();
        properties.put(ConfigurationProvider.KEY_DUMP_ROOT_DIRECTORY, "target");
        properties.put("a.b.c", "d.e.f");
        expect(configurationAdmin.getConfiguration("org.eclipse.virgo.medic", null)).andReturn(configuration);
        expect(configuration.getProperties()).andReturn(properties);

        replay(configurationAdmin, configuration);

        ConfigurationProvider configurationProvider = new ConfigurationAdminConfigurationProvider(bundleContext);
        Dictionary configDictionary = configurationProvider.getConfiguration();
        assertNotNull(configDictionary);
        assertEquals("target", configDictionary.get(ConfigurationProvider.KEY_DUMP_ROOT_DIRECTORY));
        assertEquals("d.e.f", configDictionary.get("a.b.c"));

        verify(configurationAdmin, configuration);
    }
}
