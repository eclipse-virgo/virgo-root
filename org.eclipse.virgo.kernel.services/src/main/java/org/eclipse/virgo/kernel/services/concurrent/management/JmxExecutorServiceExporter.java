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

package org.eclipse.virgo.kernel.services.concurrent.management;

import java.lang.management.ManagementFactory;
import java.util.HashSet;
import java.util.Set;

import javax.management.JMException;
import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.eclipse.virgo.kernel.services.concurrent.ExecutorServiceInfo;
import org.eclipse.virgo.kernel.services.concurrent.ExecutorServiceStatistics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Implementation of {@link ExecutorServiceExporter} that exports to JMX
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Threasafe
 * 
 */
public class JmxExecutorServiceExporter implements ExecutorServiceExporter {

    private static final String OBJECT_NAME_PATTERN = "%s:type=Executor Service,name=%s";

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final MBeanServer server = ManagementFactory.getPlatformMBeanServer();

    private final Set<ObjectName> exportedExecutorServices = new HashSet<ObjectName>();

    private final String managementDomain;

    public JmxExecutorServiceExporter(String managementDomain) {
        this.managementDomain = managementDomain;
    }

    public void export(ExecutorServiceStatistics executorService) {
        try {
            ObjectName name = new ObjectName(String.format(OBJECT_NAME_PATTERN, this.managementDomain, executorService.getPoolName()));
            ExecutorServiceInfo info = new StandardExecutorServiceInfo(executorService);
            this.server.registerMBean(info, name);

            synchronized (this.exportedExecutorServices) {
                this.exportedExecutorServices.add(name);
            }
        } catch (JMException e) {
            this.logger.warn(String.format("Unable to register executor service %s for management", executorService), e);
        }
    }

    public void destroy() {
        synchronized (this.exportedExecutorServices) {
            for (ObjectName exportedExecutorService : this.exportedExecutorServices) {
                try {
                    this.server.unregisterMBean(exportedExecutorService);
                } catch (JMException e) {
                    this.logger.warn(String.format("Unable to unregister executor service %s from management", exportedExecutorService), e);
                }
            }
        }
    }

}
