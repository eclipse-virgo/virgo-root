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

package org.eclipse.virgo.kernel.install.artifact.internal.bundle;

import org.eclipse.virgo.nano.core.BundleStarter;
import org.eclipse.virgo.kernel.install.artifact.ArtifactIdentity;
import org.eclipse.virgo.kernel.install.artifact.internal.ArtifactStateMonitor;
import org.eclipse.virgo.nano.shim.serviceability.TracingService;
import org.osgi.framework.BundleContext;

import org.eclipse.virgo.kernel.osgi.framework.OsgiFramework;
import org.eclipse.virgo.kernel.osgi.framework.PackageAdminUtil;


/**
 * A factory for creating {@link BundleDriver} instances.
 * 
 * <p />
 *
 * <strong>Concurrent Semantics</strong><br />
 *
 * Thread-safe
 *
 */
final class BundleDriverFactory {
    
    private final OsgiFramework osgiFramework;
    
    private final BundleContext regionBundleContext;
    
    private final BundleStarter bundleStarter;
    
    private final TracingService tracingService;
    
    private final PackageAdminUtil packageAdminUtil;
    
    public BundleDriverFactory(OsgiFramework osgiFramework, BundleContext regionBundleContext, BundleStarter bundleStarter,
        TracingService tracingService, PackageAdminUtil packageAdminUtil) {
        this.osgiFramework = osgiFramework;
        this.regionBundleContext = regionBundleContext;
        this.bundleStarter = bundleStarter;
        this.tracingService = tracingService;
        this.packageAdminUtil = packageAdminUtil;
    }

    StandardBundleDriver createBundleDriver(ArtifactIdentity identity, ArtifactStateMonitor artifactStateMonitor) {
        return new StandardBundleDriver(this.osgiFramework, this.regionBundleContext, this.bundleStarter, this.tracingService, this.packageAdminUtil, identity.getScopeName(), artifactStateMonitor);
    }
}
