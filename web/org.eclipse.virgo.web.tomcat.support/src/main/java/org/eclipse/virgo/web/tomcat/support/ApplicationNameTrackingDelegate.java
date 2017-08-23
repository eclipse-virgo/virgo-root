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

package org.eclipse.virgo.web.tomcat.support;

import org.eclipse.virgo.nano.shim.serviceability.TracingService;
import org.eclipse.virgo.web.core.WebApplicationRegistry;

class ApplicationNameTrackingDelegate {

    private static final ApplicationNameTrackingDelegate INSTANCE = new ApplicationNameTrackingDelegate();
    
    private static final String ROOT_CONTEXT_PATH = "/";

    private final Object monitor = new Object();

    private TracingService tracingService;

    private WebApplicationRegistry registry;

    public static ApplicationNameTrackingDelegate getInstance() {
        return INSTANCE;
    }

    private ApplicationNameTrackingDelegate() {
    }

    public void setTracingService(TracingService tracingService) {
        synchronized (this.monitor) {
            this.tracingService = tracingService;
        }
    }

    public void setRegistry(WebApplicationRegistry registry) {
        synchronized (this.monitor) {
            this.registry = registry;
        }
    }

    public final void setApplicationNameForContextPath(String contextPath) {
        if (contextPath == null || contextPath.length() == 0) {
            contextPath = ROOT_CONTEXT_PATH;
        }
        TracingService service = null;
        WebApplicationRegistry registry = null;
        synchronized (this.monitor) {
            service = this.tracingService;
            registry = this.registry;
        }
        if (service != null && registry != null) {
            String appName = this.registry.getWebApplicationName(contextPath);
            service.setCurrentApplicationName(appName);
        }
    }

    public final void clearName() {
        TracingService service = null;
        synchronized (this.monitor) {
            service = this.tracingService;
        }
        if (service != null) {
            service.setCurrentApplicationName(null);
        }
    }

}
