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

package org.eclipse.virgo.apps.admin.core.state;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.List;

import org.eclipse.virgo.apps.admin.core.BundleHolder;
import org.eclipse.virgo.apps.admin.core.FailedResolutionHolder;
import org.eclipse.virgo.apps.admin.core.PackagesCollection;
import org.eclipse.virgo.apps.admin.core.ServiceHolder;
import org.eclipse.virgo.apps.admin.core.state.StandardStateHolder;
import org.eclipse.virgo.apps.admin.core.stubs.StubDumpExtractor;
import org.eclipse.virgo.apps.admin.core.stubs.StubModuleContextAccessor;
import org.eclipse.virgo.apps.admin.core.stubs.StubQuasiFrameworkFactory;
import org.eclipse.virgo.apps.admin.core.stubs.StubQuasiLiveBundle;
import org.eclipse.virgo.apps.admin.core.stubs.StubStateService;
import org.junit.Before;
import org.junit.Test;


/**
 */
public class StandardStateHolderTests {

    private static final String TEST_DUMP_NAME = "1234";
    
    private static final long NOT_EXISTING_ID = 5;
    
    private static final long EXISTING_ID = 4;
    
    private StandardStateHolder standardStateHolder;

    private StubDumpExtractor stubDumpExtractor;

    private StubStateService stubStateService;

    @Before
    public void setUp() {
        this.stubStateService = new StubStateService();
        this.stubDumpExtractor = new StubDumpExtractor();
        this.standardStateHolder = new StandardStateHolder(this.stubStateService, this.stubDumpExtractor, new StubModuleContextAccessor(), new StubQuasiFrameworkFactory());
    }
    
    //ALL BUNDLES

    /**
     * Test method for {@link org.eclipse.virgo.apps.admin.core.state.StandardStateHolder#getAllBundles(String)}.
     */
    @Test
    public void testGetAllBundles() {
        this.stubStateService.setNotNullExpectations();
        List<BundleHolder> result = this.standardStateHolder.getAllBundles(TEST_DUMP_NAME);
        assertNotNull(result);
    }

    /**
     * Test method for {@link org.eclipse.virgo.apps.admin.core.state.StandardStateHolder#getAllBundles(String)}.
     */
    @Test
    public void testGetAllBundlesNull() {
        this.stubStateService.setNullExpectations();
        List<BundleHolder> result = this.standardStateHolder.getAllBundles(null);
        assertNotNull(result);
    }

    //BUNDLE
    
    /**
     * Test method for {@link org.eclipse.virgo.apps.admin.core.state.StandardStateHolder#getBundle(java.lang.String, long)}.
     */
    @Test
    public void testGetBundleExist() {
        this.stubStateService.setNotNullExpectations();
        BundleHolder result = this.standardStateHolder.getBundle(TEST_DUMP_NAME, EXISTING_ID);
        assertNotNull(result);
        assertEquals("fake.test.bundle", result.getSymbolicName());
    }

    /**
     * Test method for {@link org.eclipse.virgo.apps.admin.core.state.StandardStateHolder#getBundle(java.lang.String, long)}.
     */
    @Test
    public void testGetBundleNotExist() {
        this.stubStateService.setNotNullExpectations();
        BundleHolder result = this.standardStateHolder.getBundle(TEST_DUMP_NAME, NOT_EXISTING_ID);
        assertNull(result);
    }

    /**
     * Test method for {@link org.eclipse.virgo.apps.admin.core.state.StandardStateHolder#getBundle(java.lang.String, long)}.
     */
    @Test
    public void testGetBundleNullDumpExists() {
        this.stubStateService.setNullExpectations();
        BundleHolder result = this.standardStateHolder.getBundle(null, EXISTING_ID);
        assertNotNull(result);
        assertEquals("fake.test.bundle", result.getSymbolicName());
    }

    /**
     * Test method for {@link org.eclipse.virgo.apps.admin.core.state.StandardStateHolder#getBundle(java.lang.String, long)}.
     */
    @Test
    public void testGetBundleNullDumpNotExists() {
        this.stubStateService.setNullExpectations();
        BundleHolder result = this.standardStateHolder.getBundle(null, NOT_EXISTING_ID);
        assertNull(result);
    }
    
    /**
     * Test method for {@link org.eclipse.virgo.apps.admin.core.state.StandardStateHolder#getBundle(java.lang.String, long)}.
     */
    @Test
    public void testGetBundleByNameExist() {
        this.stubStateService.setNotNullExpectations();
        BundleHolder result = this.standardStateHolder.getBundle(TEST_DUMP_NAME, StubQuasiLiveBundle.TEST_NAME, StubQuasiLiveBundle.TEST_VERSION.toString());
        assertNotNull(result);
        assertEquals("fake.test.bundle", result.getSymbolicName());
    }

    /**
     * Test method for {@link org.eclipse.virgo.apps.admin.core.state.StandardStateHolder#getBundle(java.lang.String, long)}.
     */
    @Test
    public void testGetBundleByNameNotExist() {
        this.stubStateService.setNotNullExpectations();
        BundleHolder result = this.standardStateHolder.getBundle(TEST_DUMP_NAME, "nope", "nope");
        assertNull(result);
    }

    /**
     * Test method for {@link org.eclipse.virgo.apps.admin.core.state.StandardStateHolder#getBundle(java.lang.String, long)}.
     */
    @Test
    public void testGetBundleByNameNullDumpExists() {
        this.stubStateService.setNullExpectations();
        BundleHolder result = this.standardStateHolder.getBundle(null, StubQuasiLiveBundle.TEST_NAME, StubQuasiLiveBundle.TEST_VERSION.toString());
        assertNotNull(result);
        assertEquals(StubQuasiLiveBundle.TEST_NAME, result.getSymbolicName());
    }

    /**
     * Test method for {@link org.eclipse.virgo.apps.admin.core.state.StandardStateHolder#getBundle(java.lang.String, long)}.
     */
    @Test
    public void testGetBundleByNameNullDumpNotExists() {
        this.stubStateService.setNullExpectations();
        BundleHolder result = this.standardStateHolder.getBundle(null, "nope", "nope");
        assertNull(result);
    }

    //SERVICES
    
    @Test
    public void testGetAllServices(){
        this.stubStateService.setNotNullExpectations();
        List<ServiceHolder> allServices = this.standardStateHolder.getAllServices(TEST_DUMP_NAME);
        assertNotNull(allServices);
    }
    
    @Test
    public void testGetAllServicesNull(){
        this.stubStateService.setNullExpectations();
        List<ServiceHolder> allServices = this.standardStateHolder.getAllServices(null);
        assertNotNull(allServices);
    }

    @Test
    public void testGetServiceExists(){
        this.stubStateService.setNotNullExpectations();
        ServiceHolder service = this.standardStateHolder.getService(TEST_DUMP_NAME, EXISTING_ID);
        assertNotNull(service);
    }
    
    @Test
    public void testGetServiceNotExists(){
        this.stubStateService.setNotNullExpectations();
        ServiceHolder service = this.standardStateHolder.getService(TEST_DUMP_NAME, NOT_EXISTING_ID);
        assertNull(service);
    }

    @Test
    public void testGetServiceNullExists(){
        this.stubStateService.setNullExpectations();
        ServiceHolder service = this.standardStateHolder.getService(null, EXISTING_ID);
        assertNotNull(service);
    }
    
    @Test
    public void testGetServiceNullNotExists(){
        this.stubStateService.setNullExpectations();
        ServiceHolder service = this.standardStateHolder.getService(null, NOT_EXISTING_ID);
        assertNull(service);
    }
    
    //PACKAGES
    
    @Test
    public void testGetPackages(){
        this.stubStateService.setNotNullExpectations();
        PackagesCollection packages = this.standardStateHolder.getPackages(TEST_DUMP_NAME, StubStateService.TEST_PACKAGE_SEARCH);
        assertNotNull(packages);
        assertEquals(StubStateService.TEST_PACKAGE_SEARCH, packages.getPackageName());
    }
    
    @Test
    public void testGetPackagesNotExists(){
        this.stubStateService.setNotNullExpectations();
        PackagesCollection packages = this.standardStateHolder.getPackages(TEST_DUMP_NAME, "notExist");
        assertNotNull(packages);
        assertEquals(0, packages.getExported().size());
        assertEquals(0, packages.getImported().size());
        assertEquals("notExist", packages.getPackageName());
    }
    
    @Test
    public void testGetPackagesNull(){
        this.stubStateService.setNullExpectations();
        PackagesCollection packages = this.standardStateHolder.getPackages(null, StubStateService.TEST_PACKAGE_SEARCH);
        assertNotNull(packages);
        assertEquals(StubStateService.TEST_PACKAGE_SEARCH, packages.getPackageName());
    }
    
    @Test
    public void testGetPackagesNullNotExists(){
        this.stubStateService.setNullExpectations();
        PackagesCollection packages = this.standardStateHolder.getPackages(null, "notExist");
        assertNotNull(packages);
        assertEquals(0, packages.getExported().size());
        assertEquals(0, packages.getImported().size());
        assertEquals("notExist", packages.getPackageName());
    }
    
    //RESOLVER
    
    @Test
    public void testGetResolverReport(){
        this.stubStateService.setNotNullExpectations();
        List<FailedResolutionHolder> resolverReport = this.standardStateHolder.getResolverReport(TEST_DUMP_NAME, EXISTING_ID);
        assertNotNull(resolverReport);
    }
    
    @Test
    public void testGetResolverReportNotExists(){
        this.stubStateService.setNotNullExpectations();
        List<FailedResolutionHolder> resolverReport = this.standardStateHolder.getResolverReport(TEST_DUMP_NAME, NOT_EXISTING_ID);
        assertNotNull(resolverReport);
    }
    
    @Test
    public void testGetResolverReportNull(){
        this.stubStateService.setNullExpectations();
        List<FailedResolutionHolder> resolverReport = this.standardStateHolder.getResolverReport(null, EXISTING_ID);
        assertNotNull(resolverReport);
    }
    
    @Test
    public void testGetResolverReportNullNotExists(){
        this.stubStateService.setNullExpectations();
        List<FailedResolutionHolder> resolverReport = this.standardStateHolder.getResolverReport(null, NOT_EXISTING_ID);
        assertNotNull(resolverReport);
    }
    
    //SEARCH
    
    @Test
    public void testSearch(){
        this.stubStateService.setNotNullExpectations();
        assertNotNull(this.standardStateHolder.search(TEST_DUMP_NAME, "term"));
    }
    
    @Test
    public void testSearchNullTerm(){
        this.stubStateService.setNotNullExpectations();
        assertNotNull(this.standardStateHolder.search(TEST_DUMP_NAME, null));
    }
    
    @Test
    public void testSearchNull(){
        this.stubStateService.setNullExpectations();
        assertNotNull(this.standardStateHolder.search(null, "term"));
    }
    
    @Test
    public void testSearchNullNullTerm(){
        this.stubStateService.setNullExpectations();
        assertNotNull(this.standardStateHolder.search(null, null));
    }
    
}
