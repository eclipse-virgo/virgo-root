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

package org.eclipse.virgo.web.core;


public interface WebApplicationRegistry {

    /**
     * Registers the web application with the supplied <code>contextPath</code> and <code>applicationName</code>.
     * @param contextPath to register web application with
     * @param applicationName of web application
     */
    void registerWebApplication(String contextPath, String applicationName);

    /**
     * Unregisters the web application with the supplied <code>contextPath</code>.
     * @param contextPath of web application to unregister
     */
    void unregisterWebApplication(String contextPath);

    /**
     * Gets the name of the web application with the supplied <code>contextPath</code>.
     * @param contextPath of registered web application
     * @return name of registered web application
     */
    String getWebApplicationName(String contextPath);
}
