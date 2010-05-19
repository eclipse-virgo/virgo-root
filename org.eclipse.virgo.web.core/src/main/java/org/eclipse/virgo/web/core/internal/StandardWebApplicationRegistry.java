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

package org.eclipse.virgo.web.core.internal;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.virgo.web.core.WebApplicationRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


final class StandardWebApplicationRegistry implements WebApplicationRegistry {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final Map<String, String> deployedWebAppNames = new ConcurrentHashMap<String, String>();

    /**
     * {@inheritDoc}
     */
    public String getWebApplicationName(String contextPath) {
        return this.deployedWebAppNames.get(contextPath);
    }

    /**
     * {@inheritDoc}
     */
    public void registerWebApplication(String contextPath, String applicationName) {
        logger.debug("Registering web application with context path [{}] and application name [{}].", contextPath, applicationName);
        this.deployedWebAppNames.put(contextPath, applicationName);
    }

    /**
     * {@inheritDoc}
     */
    public void unregisterWebApplication(String contextPath) {
        logger.debug("Unregistering web application with context path [{}].", contextPath);
        this.deployedWebAppNames.remove(contextPath);
    }

}
