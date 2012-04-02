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

package org.eclipse.virgo.kernel.config.internal;

import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import javax.management.JMException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectInstance;
import javax.management.ObjectName;

import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.cm.ConfigurationEvent;
import org.osgi.service.cm.ConfigurationListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


// TODO Remove alien calls made while holding this.monitor 
public class ConfigurationAdminExporter implements ConfigurationListener {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final MBeanServer server = ManagementFactory.getPlatformMBeanServer();

    private static final String OBJECT_NAME_PATTERN = "%s:type=Configuration,name=%s";

    private final Map<String, ObjectInstance> configurationInfos = new HashMap<String, ObjectInstance>();

    private final Object monitor = new Object();

    private final String managementDomain;

    private final ConfigurationAdmin configurationAdmin;

    public ConfigurationAdminExporter(String managementDomain, ConfigurationAdmin configurationAdmin) {
        this.managementDomain = managementDomain;
        this.configurationAdmin = configurationAdmin;
    }

    public void init() {
        synchronized (this.monitor) {
            try {
                for (Configuration configuration : configurationAdmin.listConfigurations(null)) {
                    exportConfiguration(configuration.getPid());
                }
            } catch (Exception e) {
                logger.warn("Could not enumerate existing configurations");
            }
        }
    }

    public void configurationEvent(ConfigurationEvent configurationEvent) {
        String pid = configurationEvent.getPid();
        if (ConfigurationEvent.CM_UPDATED == configurationEvent.getType()) {
            exportConfiguration(pid);
        } else if (ConfigurationEvent.CM_DELETED == configurationEvent.getType()) {
            unexportConfiguration(pid);
        }
    }

    private ObjectName getObjectName(String pid) throws MalformedObjectNameException, NullPointerException {
        return new ObjectName(String.format(OBJECT_NAME_PATTERN, this.managementDomain, pid));
    }

    private void exportConfiguration(String pid) {
        synchronized (this.monitor) {
            if (!configurationInfos.containsKey(pid)) {
                try {
                    ConfigurationInfo configurationInfo = new ConfigurationAdminConfigurationInfo(this.configurationAdmin, pid);
                    ObjectInstance objectInstance = server.registerMBean(configurationInfo, getObjectName(pid));
                    this.configurationInfos.put(pid, objectInstance);
                } catch (JMException e) {
                    logger.warn("Unable to register MBean for configuration '{}'", pid);
                }
            }
        }
    }

    private void unexportConfiguration(String pid) {
        ObjectName objectName = null;
        
        synchronized (this.monitor) {
            if (configurationInfos.containsKey(pid)) {
                objectName = configurationInfos.remove(pid).getObjectName();
            }
        }
        
        if (objectName != null) {
            try {
                this.server.unregisterMBean(objectName);
            } catch (JMException e) {
                this.logger.warn("Unable to unregister MBean for configuration '{}'", pid);
            }
        }
    }

    /**
     * 
     */
    public void stop() {
        List<ObjectName> objectNames = new ArrayList<ObjectName>();
        
        synchronized (this.monitor) {
            Set<Entry<String, ObjectInstance>> entries = this.configurationInfos.entrySet();
            for (Entry<String, ObjectInstance> entry : entries) {
                objectNames.add(entry.getValue().getObjectName());
            }
            this.configurationInfos.clear();
        }
        
        for (ObjectName objectName : objectNames) {
            try {
                this.server.unregisterMBean(objectName);
            } catch (JMException jme) {
                logger.warn("Unable to unregister MBean '{}' during stop", objectName);
            }
        }   
    }
}
