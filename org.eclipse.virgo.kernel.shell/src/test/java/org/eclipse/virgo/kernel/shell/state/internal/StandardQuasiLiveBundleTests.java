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

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.Bundle;
import org.springframework.context.ApplicationContext;


import org.eclipse.virgo.kernel.shell.state.QuasiLiveService;
import org.eclipse.virgo.kernel.shell.state.internal.StandardQuasiLiveBundle;
import org.eclipse.virgo.kernel.shell.stubs.StubQuasiFramework;
import org.eclipse.virgo.kernel.shell.stubs.StubQuasiLiveBundle;
import org.eclipse.virgo.test.stubs.framework.StubBundle;
import org.eclipse.virgo.test.stubs.framework.StubBundleContext;
import org.eclipse.virgo.test.stubs.framework.StubServiceReference;
import org.eclipse.virgo.test.stubs.framework.StubServiceRegistration;

/**
 */
public class StandardQuasiLiveBundleTests {

    private StandardQuasiLiveBundle standardQuasiLiveBundle;

    private StubBundle stubOsgiBundle;

    private StubQuasiLiveBundle stubQuasiBundle;

    private StubQuasiFramework stubQuasiFramework;

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
        this.stubOsgiBundle = new StubBundle();
        this.stubQuasiBundle = new StubQuasiLiveBundle(5, stubOsgiBundle);
        this.stubQuasiFramework = new StubQuasiFramework();
        this.standardQuasiLiveBundle = new StandardQuasiLiveBundle(stubQuasiFramework, stubQuasiBundle, stubOsgiBundle);
    }

    /**
     * Test method for
     * {@link org.eclipse.virgo.kernel.shell.state.internal.StandardQuasiLiveBundle#getExportedServices()}.
     */
    @Test
    public void testGetExportedServices() {
        this.registerFakeAppContext();
        List<QuasiLiveService> exportedServices = this.standardQuasiLiveBundle.getExportedServices();
        assertNotNull(exportedServices);
        assertEquals(1, exportedServices.size());
    }

    /**
     * Test method for
     * {@link org.eclipse.virgo.kernel.shell.state.internal.StandardQuasiLiveBundle#getImportedServices()}.
     */
    @Test
    public void testGetImportedServices() {
        this.registerFakeAppContext();
        List<QuasiLiveService> importedServices = this.standardQuasiLiveBundle.getImportedServices();
        assertNotNull(importedServices);
        assertEquals(0, importedServices.size());
    }

    /**
     * Test method for {@link org.eclipse.virgo.kernel.shell.state.internal.StandardQuasiLiveBundle#getState()}.
     */
    @Test
    public void testGetState() {
        assertEquals("Starting", this.standardQuasiLiveBundle.getState());
        this.stubOsgiBundle.setState(Bundle.RESOLVED);
        assertEquals("Resolved", this.standardQuasiLiveBundle.getState());
    }

    /**
     * Test method for {@link org.eclipse.virgo.kernel.shell.state.internal.StandardQuasiLiveBundle#getBundle()}.
     */
    @Test
    public void testGetBundle() {
        assertEquals(this.stubOsgiBundle, this.standardQuasiLiveBundle.getBundle());
    }

    /**
     * Test method for {@link org.eclipse.virgo.kernel.shell.state.internal.StandardQuasiLiveBundle#getBundleId()}.
     */
    @Test
    public void testGetBundleId() {
        assertEquals(this.stubQuasiBundle.getBundleId(), this.standardQuasiLiveBundle.getBundleId());
    }

    /**
     * Test method for {@link org.eclipse.virgo.kernel.shell.state.internal.StandardQuasiLiveBundle#getDependents()}.
     */
    @Test
    public void testGetDependents() {
        assertEquals(this.stubQuasiBundle.getDependents(), this.standardQuasiLiveBundle.getDependents());
    }

    /**
     * Test method for {@link org.eclipse.virgo.kernel.shell.state.internal.StandardQuasiLiveBundle#getExportPackages()}.
     */
    @Test
    public void testGetExportPackages() {
        assertEquals(this.stubQuasiBundle.getExportPackages(), this.standardQuasiLiveBundle.getExportPackages());
    }

    /**
     * Test method for {@link org.eclipse.virgo.kernel.shell.state.internal.StandardQuasiLiveBundle#getFragments()}.
     */
    @Test
    public void testGetFragments() {
        assertEquals(this.stubQuasiBundle.getFragments(), this.standardQuasiLiveBundle.getFragments());
    }

    /**
     * Test method for {@link org.eclipse.virgo.kernel.shell.state.internal.StandardQuasiLiveBundle#getHosts()}.
     */
    @Test
    public void testGetHosts() {
        assertEquals(this.stubQuasiBundle.getHosts(), this.standardQuasiLiveBundle.getHosts());
    }

    /**
     * Test method for {@link org.eclipse.virgo.kernel.shell.state.internal.StandardQuasiLiveBundle#getImportPackages()}.
     */
    @Test
    public void testGetImportPackages() {
        assertEquals(this.stubQuasiBundle.getImportPackages(), this.standardQuasiLiveBundle.getImportPackages());
    }

    /**
     * Test method for {@link org.eclipse.virgo.kernel.shell.state.internal.StandardQuasiLiveBundle#getRequiredBundles()}
     * .
     */
    @Test
    public void testGetRequiredBundles() {
        assertEquals(this.stubQuasiBundle.getRequiredBundles(), this.standardQuasiLiveBundle.getRequiredBundles());
    }

    /**
     * Test method for {@link org.eclipse.virgo.kernel.shell.state.internal.StandardQuasiLiveBundle#getSymbolicName()}.
     */
    @Test
    public void testGetSymbolicName() {
        assertEquals(this.stubQuasiBundle.getSymbolicName(), this.standardQuasiLiveBundle.getSymbolicName());
    }

    /**
     * Test method for {@link org.eclipse.virgo.kernel.shell.state.internal.StandardQuasiLiveBundle#getVersion()}.
     */
    @Test
    public void testGetVersion() {
        assertEquals(this.stubQuasiBundle.getVersion(), this.standardQuasiLiveBundle.getVersion());
    }

    /**
     * Test method for {@link org.eclipse.virgo.kernel.shell.state.internal.StandardQuasiLiveBundle#isResolved()}.
     */
    @Test
    public void testIsResolved() {
        assertEquals(this.stubQuasiBundle.isResolved(), this.standardQuasiLiveBundle.isResolved());
    }

    @SuppressWarnings("unchecked")
    private void registerFakeAppContext() {
        this.stubOsgiBundle.addRegisteredService(new StubServiceReference<Object>(new StubServiceRegistration<Object>(new StubBundleContext(),
            ApplicationContext.class.getName())));
    }

}
