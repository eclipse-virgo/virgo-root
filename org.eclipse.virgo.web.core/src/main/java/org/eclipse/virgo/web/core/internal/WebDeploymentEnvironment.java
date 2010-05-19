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

import org.eclipse.gemini.web.core.WebBundleManifestTransformer;
import org.eclipse.gemini.web.core.WebContainer;
import org.eclipse.virgo.web.core.WebApplicationRegistry;

/**
 * Simple data structure that holds all the services needed from web deployment.
 * 
 * 
 */
final class WebDeploymentEnvironment {

    private final WebContainer webContainer;

    private final WebApplicationRegistry applicationRegistry;

    private final WebBundleManifestTransformer manifestTransformer;

    public WebDeploymentEnvironment(WebContainer webContainer, WebApplicationRegistry applicationRegistry,
        WebBundleManifestTransformer manifestTransformer) {
        this.webContainer = webContainer;
        this.applicationRegistry = applicationRegistry;
        this.manifestTransformer = manifestTransformer;
    }

    public WebContainer getWebContainer() {
        return webContainer;
    }

    public WebApplicationRegistry getApplicationRegistry() {
        return applicationRegistry;
    }

    public WebBundleManifestTransformer getManifestTransformer() {
        return manifestTransformer;
    }

}
