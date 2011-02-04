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

package org.eclipse.virgo.kernel.shell.stubs;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.osgi.framework.BundleException;
import org.osgi.framework.Version;

import org.eclipse.virgo.kernel.osgi.quasi.QuasiBundle;
import org.eclipse.virgo.kernel.osgi.quasi.QuasiFramework;
import org.eclipse.virgo.kernel.osgi.quasi.QuasiResolutionFailure;
import org.eclipse.virgo.teststubs.osgi.framework.StubBundle;
import org.eclipse.virgo.teststubs.osgi.framework.StubBundleContext;
import org.eclipse.virgo.teststubs.osgi.framework.StubServiceReference;
import org.eclipse.virgo.teststubs.osgi.framework.StubServiceRegistration;
import org.eclipse.virgo.util.osgi.manifest.BundleManifest;


/**
 * <p>
 * This <code>StubQuasiFramework</code> is a pretend framework containing 
 * one bundle with an id of 4
 * <p />
 *
 */
public class StubQuasiFramework implements QuasiFramework{
    
    private final StubBundle stubBundle; 
    private final StubBundleContext stubBundleContext;
    
    @SuppressWarnings("unchecked")
    public StubQuasiFramework() {
        this.stubBundle = new StubBundle(4L, "test.symbolic.name", new Version("0"), "");
        this.stubBundleContext = new StubBundleContext(stubBundle);
        this.stubBundleContext.addInstalledBundle(stubBundle);
        this.stubBundle.addRegisteredService(new StubServiceReference<Object>(new StubServiceRegistration<Object>(this.stubBundleContext)));
    }

    public void commit() throws BundleException {
    }

    public QuasiBundle getBundle(long bundleId) {
        if(bundleId == 4){
            return new StubQuasiLiveBundle(bundleId, this.stubBundle);
        }else{
            return null;
        }
    }

    public List<QuasiBundle> getBundles() {
        List<QuasiBundle> bundles = new ArrayList<QuasiBundle>();
        
        
        bundles.add(new StubQuasiLiveBundle(4, this.stubBundle));
        return bundles;
    }

    public QuasiBundle install(URI location, BundleManifest bundleManifest) throws BundleException {
        return new StubQuasiLiveBundle(6, null);
    }

    public List<QuasiResolutionFailure> resolve() {
        return new ArrayList<QuasiResolutionFailure>();
    }

    public List<QuasiResolutionFailure> diagnose(long bundleId) {
        return new ArrayList<QuasiResolutionFailure>();
    }

    public QuasiBundle getBundle(String name, Version version) {
        return null;
    }

    @Override
    public void destroy() {
    }

}
