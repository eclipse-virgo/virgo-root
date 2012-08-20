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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.virgo.kernel.osgi.quasi.QuasiBundle;
import org.eclipse.virgo.kernel.osgi.quasi.QuasiResolutionFailure;
import org.eclipse.virgo.kernel.shell.state.QuasiLiveService;
import org.eclipse.virgo.kernel.shell.state.QuasiPackage;
import org.eclipse.virgo.kernel.shell.state.StateService;
import org.osgi.framework.Version;

public class StubStateService implements StateService {

    public static final long STUB_STATE_BUNDLE_ID = 5;
    public static final long STUB_STATE_NON_BUNDLE_ID = 6;
    public static final long STUB_STATE_SERVICE_ID = 55;
    
    private StubQuasiBundle stubQuasiBundle = new StubQuasiBundle(STUB_STATE_BUNDLE_ID, "name", new Version("1.0.0"));

    /** 
     * {@inheritDoc}
     */
    @Override
    public List<QuasiBundle> getAllBundles() {
        return new ArrayList<QuasiBundle>();
    }

    /** 
     * {@inheritDoc}
     */
    @Override
    public List<QuasiLiveService> getAllServices() {
        return new ArrayList<QuasiLiveService>();
    }

    /** 
     * {@inheritDoc}
     */
    @Override
    public QuasiBundle getBundle(long bundleId) {
        if (bundleId == stubQuasiBundle.getBundleId()) {
            return this.stubQuasiBundle;
        } else {
            return null;
        }
    }

    /** 
     * {@inheritDoc}
     */
    public String getBundleRegionName(long bundleId) {
        if (bundleId == stubQuasiBundle.getBundleId()) {
            return "org.eclipse.virgo.region.user";
        } else {
            return null;
        }
    }
    
    /** 
     * {@inheritDoc}
     */
    @Override
    public List<QuasiResolutionFailure> getResolverReport(long bundleId) {
        return new ArrayList<QuasiResolutionFailure>();
    }

    /** 
     * {@inheritDoc}
     */
    @Override
    public QuasiLiveService getService(long serviceId) {
        return new StubQuasiLiveService(STUB_STATE_SERVICE_ID, this.stubQuasiBundle);
    }

    /** 
     * {@inheritDoc}
     */
    @Override
    public QuasiPackage getPackages(String packageName) {
        return new StubQuasiPackage(packageName);
    }

}
