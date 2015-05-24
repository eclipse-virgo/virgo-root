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

package org.eclipse.virgo.shell.stubs;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.osgi.framework.BundleException;
import org.osgi.framework.Version;

import org.eclipse.equinox.region.Region;
import org.eclipse.virgo.kernel.osgi.quasi.QuasiBundle;
import org.eclipse.virgo.kernel.osgi.quasi.QuasiFramework;
import org.eclipse.virgo.kernel.osgi.quasi.QuasiResolutionFailure;
import org.eclipse.virgo.test.stubs.framework.StubBundle;
import org.eclipse.virgo.test.stubs.framework.StubBundleContext;
import org.eclipse.virgo.test.stubs.framework.StubServiceReference;
import org.eclipse.virgo.test.stubs.framework.StubServiceRegistration;
import org.eclipse.virgo.util.osgi.manifest.BundleManifest;

/**
 * <p>
 * This <code>StubQuasiFramework</code> is a pretend framework containing one bundle with an id of 4
 * <p />
 * 
 */
public class StubQuasiFramework implements QuasiFramework {
    
    private final Map<Long, StubBundle> bundles = new HashMap<Long, StubBundle>();

	public StubQuasiFramework(StubBundle... stubBundles) {
    	for (StubBundle stubBundle : stubBundles) {
            this.bundles.put(stubBundle.getBundleId(), stubBundle);
            StubBundleContext stubBundleContext = new StubBundleContext(stubBundle);
            stubBundleContext.addInstalledBundle(stubBundle);
            stubBundle.setBundleContext(stubBundleContext);
            stubBundle.addRegisteredService(new StubServiceReference<Object>(new StubServiceRegistration<Object>(stubBundleContext)));
		}
    }

    public void commit() throws BundleException {
    }

    public QuasiBundle getBundle(long bundleId) {
    	if(this.bundles.containsKey(bundleId)){
            return new StubQuasiBundle(bundleId, this.bundles.get(bundleId).getSymbolicName(), this.bundles.get(bundleId).getVersion());
    	} else {
    		return null;
    	}
    }

    public List<QuasiBundle> getBundles() {
        List<QuasiBundle> bundles = new ArrayList<QuasiBundle>();
        Collection<StubBundle> values = this.bundles.values();
        for (StubBundle stubBundle : values) {
			bundles.add(new StubQuasiBundle(stubBundle));
		}
        return bundles;
    }

    public QuasiBundle install(URI location, BundleManifest bundleManifest) throws BundleException {
        return new StubQuasiBundle(6l, null, null);
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

	@Override
	public Set<Region> getRegions() {
		return new HashSet<Region>();
	}

}
