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

import java.io.IOException;
import java.util.Dictionary;
import java.util.HashSet;
import java.util.Hashtable;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.cm.ConfigurationEvent;
import org.osgi.service.cm.ConfigurationListener;

public final class ConfigurationAdminConfigurationProvider implements ConfigurationProvider, ConfigurationListener {
    
    private static final String CONFIG_ADMIN_PID = "org.eclipse.virgo.medic";

    private static final Dictionary<String, Object> DEFAULT_CONFIG = createDefaultConfiguration();

    private final BundleContext bundleContext;
    
	private volatile Dictionary<String, Object> configuration = DEFAULT_CONFIG;

    private HashSet<ConfigurationChangeListener> listeners;
    
    public ConfigurationAdminConfigurationProvider(BundleContext context) {
        this.bundleContext = context;
        this.listeners = new HashSet<ConfigurationChangeListener>();
        initialisePropertiesFromConfigurationAdmin();
    }

    public Dictionary<String, Object> getConfiguration() {
        return this.configuration;
    }
    
    
	private void initialisePropertiesFromConfigurationAdmin() {
    	ServiceReference<ConfigurationAdmin> configAdminReference = this.bundleContext.getServiceReference(ConfigurationAdmin.class);
    	
    	if (configAdminReference != null) {
    		this.bundleContext.registerService(ConfigurationListener.class.getName(), new MedicConfigurationListener(), null);
    		setPropertiesFromConfigurationAdmin(configAdminReference);
    	}
    }
    
    private void setPropertiesFromConfigurationAdmin(ServiceReference<ConfigurationAdmin> configAdminReference) {
    	ConfigurationAdmin configurationAdmin = this.bundleContext.getService(configAdminReference);
        
        if (configurationAdmin != null) {
            try {
				Configuration configuration = configurationAdmin.getConfiguration(CONFIG_ADMIN_PID, null);
                
				Dictionary<String, Object> properties = configuration.getProperties();
                
                if (properties == null) {
                	properties = DEFAULT_CONFIG; 
                } else {
                	DictionaryUtils.mergeGeneral(properties, DEFAULT_CONFIG);
                }
                
				this.configuration = properties;
            } catch (IOException ioe) {
            }
        } else {
        	this.configuration = DEFAULT_CONFIG;
        }
        
        notifyListeners();
    }

    private static Dictionary<String, Object> createDefaultConfiguration() {
        Dictionary<String, Object> configuration = new Hashtable<String, Object>();
        configuration.put(KEY_DUMP_ROOT_DIRECTORY, ".");
            configuration.put(KEY_LOG_WRAP_SYSOUT, Boolean.toString(Boolean.TRUE));
            configuration.put(KEY_LOG_WRAP_SYSERR, Boolean.toString(Boolean.TRUE));
        return configuration;
    }    
    
    public void addChangeListener(ConfigurationChangeListener listener) {
        listeners.add(listener);
    }
    
    public boolean removeChangeListener(ConfigurationChangeListener listener) {
        return listeners.remove(listener);
    }
    
    private void notifyListeners() {
        for (Object listener: listeners.toArray()) {
            ((ConfigurationChangeListener) listener).configurationChanged(this);
        }
    }
    
    private final class MedicConfigurationListener implements ConfigurationListener {
    	
		public void configurationEvent(ConfigurationEvent configEvent) {
			if (CONFIG_ADMIN_PID.equals(configEvent.getPid()) && configEvent.getType() == ConfigurationEvent.CM_UPDATED) {
				setPropertiesFromConfigurationAdmin((ServiceReference<ConfigurationAdmin>)configEvent.getReference());
			}			
		}    	
    }
    
    public void configurationEvent(ConfigurationEvent event) {
		if (event.getType() == ConfigurationEvent.CM_UPDATED && CONFIG_ADMIN_PID.equals(event.getPid())) {
			setPropertiesFromConfigurationAdmin((ServiceReference<ConfigurationAdmin>)event.getReference());
		}
	}
}