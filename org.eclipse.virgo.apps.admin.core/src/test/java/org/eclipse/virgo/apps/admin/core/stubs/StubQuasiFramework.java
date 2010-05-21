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

package org.eclipse.virgo.apps.admin.core.stubs;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.osgi.framework.BundleException;
import org.osgi.framework.Version;

import org.eclipse.virgo.kernel.osgi.quasi.QuasiBundle;
import org.eclipse.virgo.kernel.osgi.quasi.QuasiFramework;
import org.eclipse.virgo.kernel.osgi.quasi.QuasiResolutionFailure;
import org.eclipse.virgo.util.osgi.manifest.BundleManifest;

/**
 */
final class StubQuasiFramework implements QuasiFramework {

    public static final String TEST_PACKAGE_SEARCH = "com.foo.bar";

    public static final String TEST_INSTALL_LOCATION = "src/test/resources";
    
    public static final long EXISTING_ID = 4;
    
    public static final String EXISTING_NAME = "bundleName";
    
    public static final Version EXISTING_VERSION = new Version("2.0");
    
    /** 
     * {@inheritDoc}
     */
    public void commit() throws BundleException {
    }

    /** 
     * {@inheritDoc}
     */
    public List<QuasiResolutionFailure> diagnose(long bundleId) {
        return null;
    }

    /** 
     * {@inheritDoc}
     */
    public QuasiBundle getBundle(long bundleId) {
        if(bundleId == EXISTING_ID){
            return new StubQuasiLiveBundle(EXISTING_ID, null);
        }
        return null;
    }

    /** 
     * {@inheritDoc}
     */
    public QuasiBundle getBundle(String name, Version version) {
        if(EXISTING_NAME.equals(name) && EXISTING_VERSION.equals(version)) {
            return new StubQuasiLiveBundle(EXISTING_ID, null);
        }
        return null;
    }

    /** 
     * {@inheritDoc}
     */
    public List<QuasiBundle> getBundles() {
        ArrayList<QuasiBundle> arrayList = new ArrayList<QuasiBundle>();
        arrayList.add(new StubQuasiLiveBundle(EXISTING_ID, null));
        return arrayList;
    }

    /** 
     * {@inheritDoc}
     */
    public QuasiBundle install(URI location, BundleManifest bundleManifest) throws BundleException {
        if(TEST_INSTALL_LOCATION.equals(location)) {
            return new StubQuasiLiveBundle(EXISTING_ID, null);
        }
        return null;
    }

    /** 
     * {@inheritDoc}
     */
    public List<QuasiResolutionFailure> resolve() {
        return new ArrayList<QuasiResolutionFailure>();
    }

}
