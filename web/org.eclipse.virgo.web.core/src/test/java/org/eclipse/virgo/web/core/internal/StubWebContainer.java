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

import java.util.HashMap;
import java.util.Map;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;

import org.eclipse.gemini.web.core.WebApplication;
import org.eclipse.gemini.web.core.WebContainer;

public class StubWebContainer implements WebContainer {
    
    private Map<Bundle, WebApplication> webApplications = new HashMap<Bundle, WebApplication>();

    /** 
     * {@inheritDoc}
     */
    public WebApplication createWebApplication(Bundle bundle) throws BundleException {
        WebApplication webApplication = this.webApplications.get(bundle);
        if (webApplication == null) {
            throw new BundleException("Failed to create web application for bundle " + bundle);
        }
        return webApplication;
    }

    /** 
     * {@inheritDoc}
     */
    public WebApplication createWebApplication(Bundle bundle, Bundle extender) throws BundleException {
        return createWebApplication(bundle);
    }
    
    /** 
     * {@inheritDoc}
     */
    public boolean isWebBundle(Bundle bundle) {
        return this.webApplications.keySet().contains(bundle);
    }
    
    void addWebApplication(Bundle bundle, WebApplication webApplication) {
        this.webApplications.put(bundle, webApplication);
    }

	public void halt() {
	}

}
