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

import static org.junit.Assert.assertFalse;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.virgo.web.core.WebApplicationRegistry;


class StubWebApplicationRegistry implements WebApplicationRegistry {
    
    private final Map<String, String> registeredApplications = new HashMap<String, String>();
    
    private boolean stateChanged = false;

    /** 
     * {@inheritDoc}
     */
    public String getWebApplicationName(String contextPath) {
        return registeredApplications.get(contextPath);
    }

    /** 
     * {@inheritDoc}
     */
    public void registerWebApplication(String contextPath, String applicationName) {
        stateChanged = true;
        registeredApplications.put(contextPath, applicationName);
    }

    /** 
     * {@inheritDoc}
     */
    public void unregisterWebApplication(String contextPath) {
        stateChanged = true;
        registeredApplications.remove(contextPath);
    }
    
    void assertStateUnchanged() {
        assertFalse(stateChanged);
    }
}
