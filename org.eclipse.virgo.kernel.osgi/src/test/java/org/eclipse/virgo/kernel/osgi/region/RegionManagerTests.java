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

package org.eclipse.virgo.kernel.osgi.region;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.eclipse.virgo.teststubs.osgi.framework.StubBundle;
import org.eclipse.virgo.teststubs.osgi.framework.StubBundleContext;
import org.eclipse.virgo.teststubs.osgi.framework.StubServiceRegistration;
import org.junit.Test;
import org.osgi.framework.Version;

public class RegionManagerTests {

    @Test
    public void testStartAndStop() throws Exception {
        StubBundleContext bundleContext = new StubBundleContext();
        
        StubBundle stubUserRegionFactoryBundle = new StubBundle("org.eclipse.virgo.kernel.userregionfactory", new Version("2.2.0"));
        bundleContext.addInstalledBundle(stubUserRegionFactoryBundle);
        
        StubBundle stubSystemBundle = new StubBundle(0L, "org.osgi.framework", new Version("0"), "");
        bundleContext.addInstalledBundle(stubSystemBundle);
                              
       
        RegionManager manager = new RegionManager(bundleContext, "test.domain");
        manager.start();
        
        List<StubServiceRegistration<Object>> serviceRegistrations = bundleContext.getServiceRegistrations();
        assertEquals("Region services not registered", 9, serviceRegistrations.size());
        
        manager.stop();
        
    }
}
