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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.List;

import org.eclipse.virgo.kernel.osgi.quasi.QuasiBundle;
import org.eclipse.virgo.kernel.osgi.quasi.QuasiFrameworkFactory;
import org.eclipse.virgo.kernel.shell.state.QuasiLiveService;
import org.eclipse.virgo.kernel.shell.stubs.StubQuasiFrameworkFactory;
import org.eclipse.virgo.test.stubs.framework.StubBundle;
import org.eclipse.virgo.test.stubs.framework.StubBundleContext;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.Version;

/**
 * <p>
 * Tests for {@link StandardStateService}
 * </p>
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Tests
 * 
 */
public class StandardStateServiceTests {

    private StandardStateService standardStateService;

    private StubBundleContext stubBundleContext;

    private QuasiFrameworkFactory stubQuasiFrameworkFactory;

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
        this.stubBundleContext = new StubBundleContext();
        
        StubBundle stubSystemBundle = new StubBundle(0L, "stub.system.bundle", Version.emptyVersion, "stubLocation");
        this.stubBundleContext.addInstalledBundle(stubSystemBundle);
        
        this.stubQuasiFrameworkFactory = new StubQuasiFrameworkFactory();
        this.standardStateService = new StandardStateService(this.stubQuasiFrameworkFactory, this.stubBundleContext);
    }

    @Test
    public void getAllBundlesNullDump() {
        List<QuasiBundle> result = this.standardStateService.getAllBundles();
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    public void getBundleNullDumpExists() {
        QuasiBundle quasiBundle = this.standardStateService.getBundle(4);
        assertNotNull(quasiBundle);
        assertEquals("test.symbolic.name", quasiBundle.getSymbolicName());
    }

    @Test
    public void getBundleNullDumpNoExists() {
        QuasiBundle quasiBundle = this.standardStateService.getBundle(5);
        assertNull(quasiBundle);
    }

    @Test
    public void getAllServices() {
        List<QuasiLiveService> allServices = this.standardStateService.getAllServices();
        assertNotNull(allServices);
    }

}
