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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.virgo.kernel.osgi.quasi.QuasiBundle;
import org.eclipse.virgo.kernel.osgi.quasi.QuasiResolutionFailure;
import org.eclipse.virgo.kernel.shell.state.QuasiLiveService;
import org.eclipse.virgo.kernel.shell.state.QuasiPackage;
import org.eclipse.virgo.kernel.shell.state.StateService;

/**
 */
final public class StubStateService implements StateService {

    public static final String TEST_PACKAGE_SEARCH = "com.foo.bar";

    public static final String TEST_INSTALL_LOCATION = "src/test/resources";
    
    private static final long EXISTING_ID = 4;

    private boolean expectNull;

    public void setNullExpectations(){
        this.expectNull = true;
    }
    
    public void setNotNullExpectations(){
        this.expectNull = false;
    }
   
    /**
     * {@inheritDoc}
     */
    public List<QuasiBundle> getAllBundles(File source) {
        this.checkNull(source);
        ArrayList<QuasiBundle> arrayList = new ArrayList<QuasiBundle>();
        arrayList.add(new StubQuasiLiveBundle(EXISTING_ID, null));
        return arrayList;
    }

    /**
     * {@inheritDoc}
     */
    public QuasiBundle getBundle(File source, long bundleId){ 
        this.checkNull(source);
        if(bundleId == EXISTING_ID){
            return new StubQuasiLiveBundle(EXISTING_ID, null);
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public List<QuasiLiveService> getAllServices(File source) {
        this.checkNull(source);
        return new ArrayList<QuasiLiveService>();
    }

    /**
     * {@inheritDoc}
     */
    public QuasiPackage getPackages(File source, String packageName) {
        this.checkNull(source);
        if(packageName.equals(TEST_PACKAGE_SEARCH)) {
            return new StubQuasiPackages(TEST_PACKAGE_SEARCH);
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public QuasiLiveService getService(File source, long serviceId) {
        this.checkNull(source);
        if(serviceId == EXISTING_ID){
            return new StubQuasiLiveService(serviceId, new StubQuasiLiveBundle(EXISTING_ID, null));
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public QuasiBundle installBundle(File source, String location) {
        this.checkNull(source);
        if(TEST_INSTALL_LOCATION.equals(location)) {
            return new StubQuasiLiveBundle(EXISTING_ID, null);
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public List<QuasiBundle> search(File source, String term) {
        this.checkNull(source);
        return new ArrayList<QuasiBundle>();
    }

    /**
     * {@inheritDoc}
     */
    public List<QuasiResolutionFailure> getResolverReport(File source, long bundleId) {
        this.checkNull(source);
        return new ArrayList<QuasiResolutionFailure>();
    }
    
    private void checkNull(File source){
        if(this.expectNull){
            assertNull(source);
        } else {
            assertNotNull(source);
        }
    }
    
}
