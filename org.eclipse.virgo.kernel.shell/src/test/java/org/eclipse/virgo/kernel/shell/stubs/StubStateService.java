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

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.virgo.kernel.osgi.quasi.QuasiBundle;
import org.eclipse.virgo.kernel.osgi.quasi.QuasiResolutionFailure;
import org.eclipse.virgo.kernel.shell.state.QuasiLiveService;
import org.eclipse.virgo.kernel.shell.state.QuasiPackage;
import org.eclipse.virgo.kernel.shell.state.StateService;
import org.eclipse.virgo.teststubs.osgi.framework.StubBundle;

public class StubStateService implements StateService {

    public static final long STUB_STATE_BUNDLE_ID = 5;
    public static final long STUB_STATE_NON_BUNDLE_ID = 6;
    public static final long STUB_STATE_SERVICE_ID = 55;
    
    private StubQuasiLiveBundle stubQuasiBundle = new StubQuasiLiveBundle(STUB_STATE_BUNDLE_ID, new StubBundle());

    public List<QuasiBundle> getAllBundles(File source) {
        return new ArrayList<QuasiBundle>();
    }

    public List<QuasiLiveService> getAllServices(File source) {
        return new ArrayList<QuasiLiveService>();
    }

    public QuasiBundle getBundle(File source, long bundleId) {
        if (bundleId == stubQuasiBundle.getBundleId()) {
            return this.stubQuasiBundle;
        } else {
            return null;
        }
    }

    public List<QuasiResolutionFailure> getResolverReport(File source, long bundleId) {
        return new ArrayList<QuasiResolutionFailure>();
    }

    public QuasiLiveService getService(File source, long serviceId) {
        return new StubQuasiLiveService(STUB_STATE_SERVICE_ID, this.stubQuasiBundle);
    }

    public QuasiBundle installBundle(File source, String location) {
        return this.stubQuasiBundle;
    }

    public QuasiPackage getPackages(File source, String packageName) {
        return new StubQuasiPackage(packageName);
    }

    public List<QuasiBundle> search(File source, String term) {
        ArrayList<QuasiBundle> arrayList = new ArrayList<QuasiBundle>();
        if (term.contains("*")) {
            arrayList.add(this.stubQuasiBundle);
        }
        return arrayList;
    }

}
