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

import org.eclipse.virgo.test.stubs.framework.StubBundleContext;
import org.junit.Test;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.cm.ConfigurationEvent;

import java.io.IOException;
import java.util.Dictionary;
import java.util.Hashtable;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

public class ConfigurationAdminConfigurationProviderTests implements ConfigurationChangeListener {
    
    private static final String CONFIG_ADMIN_PID = "org.eclipse.virgo.medic";
    
    private final StubBundleContext bundleContext = new StubBundleContext();
    
    private int notificationCount;
    
    @Test
    public void getConfigurationWithoutConfigurationAdmin() {
        ConfigurationProvider configurationProvider = new ConfigurationAdminConfigurationProvider(bundleContext);
        Dictionary<String, Object> configuration = configurationProvider.getConfiguration();
        assertNotNull(configuration);
        assertEquals(".", configuration.get(ConfigurationProvider.KEY_DUMP_ROOT_DIRECTORY));
    }
    
    @Test
    public void getConfigurationWithEmptyConfigurationAdminConfiguration() throws IOException {
        ConfigurationAdmin configurationAdmin = createMock(ConfigurationAdmin.class);
        Configuration configuration = createMock(Configuration.class);
        
        this.bundleContext.registerService(ConfigurationAdmin.class.getName(), configurationAdmin, null);
        expect(configurationAdmin.getConfiguration("org.eclipse.virgo.medic", null)).andReturn(configuration);
        expect(configuration.getProperties()).andReturn(null);
        
        replay(configurationAdmin, configuration);
        
        ConfigurationProvider configurationProvider = new ConfigurationAdminConfigurationProvider(bundleContext);
        
        Dictionary<String, Object> configDictionary = configurationProvider.getConfiguration();
        assertNotNull(configDictionary);
        assertEquals(".", configDictionary.get(ConfigurationProvider.KEY_DUMP_ROOT_DIRECTORY));
        
        verify(configurationAdmin, configuration);
    }
    
    @Test
    public void getConfigurationWithConfigurationAdminConfiguration() throws IOException {
        ConfigurationAdmin configurationAdmin = createMock(ConfigurationAdmin.class);
        Configuration configuration = createMock(Configuration.class);
        
        Dictionary<String, Object> properties = new Hashtable<String, Object>();
        properties.put(ConfigurationProvider.KEY_DUMP_ROOT_DIRECTORY, "build");
        properties.put("a.b.c", "d.e.f");
        createConfigurationMocks(configurationAdmin, configuration, properties, 1);
        
        ConfigurationProvider configurationProvider = new ConfigurationAdminConfigurationProvider(bundleContext);
        Dictionary<String, Object> configDictionary = configurationProvider.getConfiguration();
        assertNotNull(configDictionary);
        assertEquals("build", configDictionary.get(ConfigurationProvider.KEY_DUMP_ROOT_DIRECTORY));
        assertEquals("d.e.f", configDictionary.get("a.b.c"));
        
        verify(configurationAdmin, configuration);
    }
    
    @Test
    public void configurationListenerNotification() throws IOException {
        ConfigurationAdmin configurationAdmin = createMock(ConfigurationAdmin.class);
        Configuration configuration = createMock(Configuration.class);
        
        Dictionary<String, Object> properties = new Hashtable<String, Object>();
        properties.put(ConfigurationProvider.KEY_DUMP_ROOT_DIRECTORY, "build");
        properties.put("a.b.c", "d.e.f");
        ServiceRegistration<ConfigurationAdmin> serviceRegistration = createConfigurationMocks(configurationAdmin, configuration, properties, 5);
        
        ConfigurationAdminConfigurationProvider configurationProvider = new ConfigurationAdminConfigurationProvider(bundleContext);
        ConfigurationEvent event = new ConfigurationEvent(serviceRegistration.getReference(), ConfigurationEvent.CM_UPDATED, null, CONFIG_ADMIN_PID);
        
        notificationCount = 0;
        
        updateConfigurationAndCheckForNotification(configurationProvider, event, 0);
        
        configurationProvider.addChangeListener(this);
        updateConfigurationAndCheckForNotification(configurationProvider, event, 1);
        
        updateConfigurationAndCheckForNotification(configurationProvider, event, 2);
        
        configurationProvider.removeChangeListener(this);
        updateConfigurationAndCheckForNotification(configurationProvider, event, 2);
        
        verify(configurationAdmin, configuration);
    }
    
    @Test
    public void configurationChange() throws IOException {
        ConfigurationAdmin configurationAdmin = createMock(ConfigurationAdmin.class);
        Configuration configuration = createMock(Configuration.class);
        
        Dictionary<String, Object> properties = new Hashtable<String, Object>();
        properties.put(ConfigurationProvider.KEY_DUMP_ROOT_DIRECTORY, "build");
        properties.put("a.b.c", "d.e.f");
        ServiceRegistration<ConfigurationAdmin> serviceRegistration = createConfigurationMocks(configurationAdmin, configuration, properties, 2);
        
        // Initial configuration publishing
        ConfigurationAdminConfigurationProvider configurationProvider = new ConfigurationAdminConfigurationProvider(bundleContext);
        ConfigurationEvent event = new ConfigurationEvent(serviceRegistration.getReference(), ConfigurationEvent.CM_UPDATED, null, CONFIG_ADMIN_PID);
        
        notificationCount = 0;
        
        configurationProvider.addChangeListener(this);
        updateConfigurationAndCheckForNotification(configurationProvider, event, 1);
        
        verify(configurationAdmin, configuration);
        
        // Update the configuration
        Dictionary<String, Object> newProperties = new Hashtable<String, Object>((Hashtable<String, Object>) properties);
        newProperties.put("1.2.3", "4.5.6");
        
        reset(configurationAdmin, configuration);
        
        serviceRegistration = createConfigurationMocks(configurationAdmin, configuration, newProperties, 1);
        event = new ConfigurationEvent(serviceRegistration.getReference(), ConfigurationEvent.CM_UPDATED, null, CONFIG_ADMIN_PID);
        
        // Trigger change event
        configurationProvider.addChangeListener(this);
        updateConfigurationAndCheckForNotification(configurationProvider, event, 2);
        
        Dictionary<String, Object> configDictionary = configurationProvider.getConfiguration();
        assertNotNull(configDictionary);
        assertEquals("build", configDictionary.get(ConfigurationProvider.KEY_DUMP_ROOT_DIRECTORY));
        assertEquals("d.e.f", configDictionary.get("a.b.c"));
        assertEquals("4.5.6", configDictionary.get("1.2.3"));
        
        verify(configurationAdmin, configuration);
    }
    
    private ServiceRegistration<ConfigurationAdmin> createConfigurationMocks(ConfigurationAdmin configurationAdmin, Configuration configuration, Dictionary<String, Object> properties, int times) throws IOException {
        ServiceRegistration<ConfigurationAdmin> serviceRegistration = this.bundleContext.registerService(ConfigurationAdmin.class, configurationAdmin, null);
        
        expect(configurationAdmin.getConfiguration("org.eclipse.virgo.medic", null)).andReturn(configuration).times(times);
        expect(configuration.getProperties()).andReturn(properties).times(times);
        
        replay(configurationAdmin, configuration);
        
        return serviceRegistration;
    }
    
    private void updateConfigurationAndCheckForNotification(ConfigurationAdminConfigurationProvider configurationProvider, ConfigurationEvent event, int count) {
        configurationProvider.configurationEvent(event);
        assertEquals(count, notificationCount);
    }
    
    @Override
    public void configurationChanged(ConfigurationProvider configurationProvider) {
        this.notificationCount++;
    }
}