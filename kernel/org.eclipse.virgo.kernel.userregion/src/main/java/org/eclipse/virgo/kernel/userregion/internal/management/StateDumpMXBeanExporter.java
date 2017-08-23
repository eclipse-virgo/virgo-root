/*******************************************************************************
 * Copyright (c) 2008, 2012 VMware Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   VMware Inc. - initial contribution
 *******************************************************************************/
package org.eclipse.virgo.kernel.userregion.internal.management;

import java.lang.management.ManagementFactory;

import javax.management.MBeanServer;
import javax.management.ObjectInstance;
import javax.management.ObjectName;

import org.eclipse.virgo.kernel.osgi.quasi.QuasiFrameworkFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 *
 */
public class StateDumpMXBeanExporter {
    private final Logger logger = LoggerFactory.getLogger(StateDumpMXBeanExporter.class);

    private static final String DOMAIN = "org.eclipse.virgo.kernel";
    
    private final MBeanServer server = ManagementFactory.getPlatformMBeanServer();

	private ObjectInstance registeredMBean;

    /**
     * 
     * @param serverHome
     */
	public StateDumpMXBeanExporter(QuasiFrameworkFactory quasiFrameworkFactory) {
		try {
			ObjectName dumpMBeanName = new ObjectName(String.format("%s:type=Medic,name=StateDumpInspector", DOMAIN));
			registeredMBean = this.server.registerMBean(new JMXQuasiStateDump(quasiFrameworkFactory), dumpMBeanName);
		} catch (Exception e) {
			logger.error("Unable to register the DumpInspectorMBean", e);
		} 
	}
	
	/**
	 * 
	 */
	public void close(){
		ObjectInstance localRegisteredMBean = this.registeredMBean;
		if(localRegisteredMBean != null){
			try {
				this.server.unregisterMBean(localRegisteredMBean.getObjectName());
				this.registeredMBean = null;
			} catch (Exception e) {
				logger.error("Unable to unregister MBean", e);
			} 
		}
	}
}
