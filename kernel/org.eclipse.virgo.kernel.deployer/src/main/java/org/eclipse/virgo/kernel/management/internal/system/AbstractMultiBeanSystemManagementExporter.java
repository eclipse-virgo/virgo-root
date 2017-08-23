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

package org.eclipse.virgo.kernel.management.internal.system;

import java.lang.management.ManagementFactory;
import java.util.List;

import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A base implementation of the {@SystemManagementExporter} interface that allows you to
 * export multiple beans in an automated fashion with consistent naming.
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Threadsafe. All sub-implementations should be threadsafe as well.
 * 
 * @param <T> Type of beans exported
 */
public abstract class AbstractMultiBeanSystemManagementExporter<T> implements SystemManagementExporter {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final MBeanServer server = ManagementFactory.getPlatformMBeanServer();

    public void register(String managementDomain) {
        for (T bean : getBeans()) {
            try {
                server.registerMBean(bean, getObjectName(managementDomain, bean));
            } catch (Exception e) {
                logger.warn("Unable to register system information {} for management", getName(bean));
            }
        }
    }

    public void unregister(String managementDomain) {
        for (T bean : getBeans()) {
            try {
                server.unregisterMBean(getObjectName(managementDomain, bean));
            } catch (Exception e) {
                logger.warn("Unable to unregister system information {} from management", getName(bean));
            }
        }
    }

    abstract List<T> getBeans();

    abstract ObjectName getObjectName(String managementDomain, T bean) throws MalformedObjectNameException, NullPointerException;

    abstract String getName(T bean);
}
