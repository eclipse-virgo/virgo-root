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

package org.eclipse.virgo.kernel.install.artifact.internal;

import java.io.File;

import org.eclipse.virgo.kernel.install.artifact.ArtifactIdentity;
import org.eclipse.virgo.kernel.install.artifact.ArtifactIdentityDeterminer;
import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;


/**
 * An {@link ArtifactIdentityDeterminer} that delegates to the <code>ArtifactTypeDeterminer</code>s available in the
 * OSGi service registry.
 * 
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * Thread-safe.
 * 
 */
public class DelegatingServiceRegistryBackedArtifactIdentityDeterminer implements ArtifactIdentityDeterminer {
    
    private final ServiceTracker<ArtifactIdentityDeterminer, ArtifactIdentityDeterminer> serviceTracker;

    /**
     * @param bundleContext
     */
    public DelegatingServiceRegistryBackedArtifactIdentityDeterminer(BundleContext bundleContext) {
        this.serviceTracker = new ServiceTracker<ArtifactIdentityDeterminer, ArtifactIdentityDeterminer>(bundleContext, ArtifactIdentityDeterminer.class.getName(), null);       
    }
    
    public void init() {
        this.serviceTracker.open();
    }
    
    public void destroy() {
        this.serviceTracker.close();
    }

    /** 
     * {@inheritDoc}
     */
    public ArtifactIdentity determineIdentity(File file, String scopeName) {
        Object[] services = this.serviceTracker.getServices();
        
        if (services != null) {
            for (Object service : services) {
                if (service != null) {
                    ArtifactIdentity identity = ((ArtifactIdentityDeterminer)service).determineIdentity(file, scopeName);
                    if (identity != null) {
                        return identity;
                    }
                }
            }
        }
        
        return null;
    }    
}
