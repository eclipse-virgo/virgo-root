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

package org.eclipse.virgo.apps.admin.web.stubs;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.virgo.apps.admin.core.BundleHolder;
import org.eclipse.virgo.apps.admin.core.FailedResolutionHolder;
import org.eclipse.virgo.apps.admin.core.PackagesCollection;
import org.eclipse.virgo.apps.admin.core.ServiceHolder;
import org.eclipse.virgo.apps.admin.core.StateHolder;

/**
 */
public class StubStateHolder implements StateHolder {

    private final long EXISTING_BUNDLE_ID = 4;
    
    public List<BundleHolder> getAllBundles(String dumpName) {
      ArrayList<BundleHolder> overview = new ArrayList<BundleHolder>();
      overview.add(new StubBundleHolder());
      overview.add(new StubBundleHolder());
      return overview;
    }

    public List<ServiceHolder> getAllServices(String dumpName) {
        return new ArrayList<ServiceHolder>();
    }
    
    public BundleHolder getBundle(String dumpName, long bundleId) {
        if(bundleId == EXISTING_BUNDLE_ID){
            return new StubBundleHolder();
        }
        return null;
    }

    public BundleHolder getBundle(String source, String name, String version, String region) {
        return null;
    }

    public PackagesCollection getPackages(String dumpName, String packageName) {
        return new StubPackagesCollection(packageName);
    }

    public List<FailedResolutionHolder> getResolverReport(String dumpName, long bundleId) {
        if(bundleId == EXISTING_BUNDLE_ID){
            List<FailedResolutionHolder> result = new ArrayList<FailedResolutionHolder>();
            result.add(new FailedResolutionHolder() {
                
                public BundleHolder getUnresolvedBundle() {
                    return new StubBundleHolder();
                }
                
                public String getDescription() {
                    return "testDescription";
                }
            });
            return result;
        }
        return new ArrayList<FailedResolutionHolder>();
    }

    public ServiceHolder getService(String dumpName, long serviceId) {
        return null;
    }

    public List<BundleHolder> search(String dumpName, String term) {
        return new ArrayList<BundleHolder>();
    }
    
}
