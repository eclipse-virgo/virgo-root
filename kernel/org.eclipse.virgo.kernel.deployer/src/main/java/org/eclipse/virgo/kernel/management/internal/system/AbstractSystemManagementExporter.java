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

import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A base implementation of the {@SystemManagementExporter} interface that allows you to
 * export a single bean in an automated fashion with consistent naming.
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Threadsafe.  All sub-implementations should be threadsafe as well.
 * 
 * @param <T> Type of bean exported
 */
public abstract class AbstractSystemManagementExporter<T> implements SystemManagementExporter {

    private static final String OBJECT_NAME_PATTERN = "%s:category=System Information,type=%s";

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final MBeanServer server = ManagementFactory.getPlatformMBeanServer();

    public void register(String managementDomain) {
        try {
            server.registerMBean(getBean(), getObjectName(managementDomain));
        } catch (Exception e) {
            logger.warn("Unable to register system information {} for management", getName());
        }
    }

    public void unregister(String managementDomain) {
        try {
            server.unregisterMBean(getObjectName(managementDomain));
        } catch (Exception e) {
            logger.warn("Unable to unregister system information {} from management", getName());
        }
    }

    private ObjectName getObjectName(String managementDomain) throws MalformedObjectNameException, NullPointerException {
        return new ObjectName(String.format(OBJECT_NAME_PATTERN, managementDomain, getName()));
    }

    abstract T getBean();

    abstract String getName();

}
