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

package org.eclipse.virgo.kernel.shell.state.internal;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.Version;

import org.eclipse.virgo.kernel.osgi.quasi.QuasiBundle;
import org.eclipse.virgo.kernel.osgi.quasi.QuasiFramework;
import org.eclipse.virgo.kernel.osgi.quasi.QuasiResolutionFailure;
import org.eclipse.virgo.kernel.shell.state.QuasiLiveBundle;
import org.eclipse.virgo.util.osgi.manifest.BundleManifest;

/**
 * <p>
 * StandardQuasiLiveFramework is the Standard implementation of {@link QuasiFramework}. It will fall 
 * back to a regular QuasiFramework but use richer types with more information available from the 
 * live state opposed to a start from an equinox dump. This should be obtained by casting 
 * {@link QuasiBundle} references to a {@link QuasiLiveBundle}s.
 * </p>
 *
 * <strong>Concurrent Semantics</strong><br />
 *
 * StandardQuasiLiveFramework is threadsafe
 *
 */
final class StandardQuasiLiveFramework implements QuasiFramework {
    
    private final QuasiFramework quasiFramework;
    
    private final BundleContext systemBundleContext;

    /**
     * Takes in the {@link QuasiFramework} to be decorated with the extra functionality 
     * of providing live information on services and Spring etc..
     * 
     * @param quasiFramework
     * @param bundleContext 
     */
    public StandardQuasiLiveFramework(QuasiFramework quasiFramework, BundleContext bundleContext) {
        this.quasiFramework = quasiFramework;
        this.systemBundleContext = bundleContext.getBundle(0L).getBundleContext();
    }

    /** 
     * {@inheritDoc}
     */
    public QuasiBundle getBundle(long bundleId) {
        QuasiBundle quasiBundle = this.quasiFramework.getBundle(bundleId); 
        if(quasiBundle == null){
            return null;
        }
        Bundle rawBundle = this.systemBundleContext.getBundle(bundleId);
        return new StandardQuasiLiveBundle(this, quasiBundle, rawBundle);
    }

    /** 
     * {@inheritDoc}
     */
    public List<QuasiBundle> getBundles() {
        List<QuasiBundle> quasiBundles = this.quasiFramework.getBundles();
        List<QuasiBundle> quasiLiveBundles = new ArrayList<QuasiBundle>();
        Bundle rawBundle;
        
        for(QuasiBundle quasiBundle : quasiBundles){
            rawBundle = this.systemBundleContext.getBundle(quasiBundle.getBundleId());
            quasiLiveBundles.add(new StandardQuasiLiveBundle(this, quasiBundle, rawBundle));
        }
        
        return quasiLiveBundles;
    }
    
    /** 
     * {@inheritDoc}
     */
    public void commit() throws BundleException {
        this.quasiFramework.commit();
    }
    
    /** 
     * {@inheritDoc}
     */
    public QuasiBundle install(URI location, BundleManifest bundleManifest) throws BundleException {
        return this.quasiFramework.install(location, bundleManifest);
    }

    /** 
     * {@inheritDoc}
     */
    public List<QuasiResolutionFailure> resolve() {
        return this.quasiFramework.resolve();
    }

    /**
     * {@inheritDoc}
     */
    public List<QuasiResolutionFailure> diagnose(long bundleId) {
        return this.quasiFramework.diagnose(bundleId);
    }

    /**
     * {@inheritDoc}
     */
    public QuasiBundle getBundle(String name, Version version) {
        return this.quasiFramework.getBundle(name, version);
    }

    /** 
     * {@inheritDoc}
     */
    @Override
    public void destroy() {
    }

}
