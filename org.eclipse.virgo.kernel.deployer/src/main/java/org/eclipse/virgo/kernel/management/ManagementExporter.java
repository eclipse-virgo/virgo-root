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

package org.eclipse.virgo.kernel.management;

import java.lang.management.ManagementFactory;

import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;

/**
 * Exports an object for management. It is useful to keep a handle to this exporter in order to {@link #unExport()} the
 * object passed in. In the case where this class is created as part of a Spring bean definition, the object will be
 * automatically exported and unExported in line with the lifecycle of the {@link ApplicationContext};
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Thread-safe.
 * 
 * @param <T> The type of the object to export for management
 * 
 */
public class ManagementExporter<T> implements InitializingBean, DisposableBean {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final MBeanServer server = ManagementFactory.getPlatformMBeanServer();

    private final T object;

    private final ObjectName name;

    /**
     * Create a {@link ManagementExporter}
     * 
     * @param object The object to export
     * @param name The name to export the object to
     */
    public ManagementExporter(T object, String name) {
        this.object = object;
        this.name = createObjectName(name);
    }

    private ObjectName createObjectName(String name) {
        try {
            return new ObjectName(name);
        } catch (MalformedObjectNameException e) {
            throw new RuntimeException("Could not convert String '" + name + "' to ObjectName", e);
        }
    }

    /**
     * Export the object
     */
    public void export() {
        afterPropertiesSet();
    }

    /**
     * Unexport the object
     */
    public void unExport() {
        destroy();
    }

    public void afterPropertiesSet() {
        try {
            logger.debug("Registering object {} with name {} for management", object, name);
            server.registerMBean(object, name);
        } catch (Exception e) {
            logger.error("Unable to register object {} with name {} for management", object, name);
            throw new RuntimeException(e);
        }
    }

    public void destroy() {
        try {
            logger.debug("Unregistering bean {} with name {} from management", object, name);
            server.unregisterMBean(name);
        } catch (Exception e) {
            logger.warn("Unable to unregister bean {} with name {} from management", object, name);
        }
    }

}
