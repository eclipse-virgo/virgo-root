/*
 * This file is part of the Eclipse Virgo project.
 *
 * Copyright (c) 2011 VMware Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    VMware Inc. - initial contribution
 */

package org.eclipse.virgo.kernel.osgi.region.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.easymock.EasyMock;
import org.eclipse.virgo.kernel.osgi.region.RegionFilter;
import org.eclipse.virgo.teststubs.osgi.framework.StubBundle;
import org.eclipse.virgo.teststubs.osgi.framework.StubBundleContext;
import org.eclipse.virgo.teststubs.osgi.framework.StubServiceRegistration;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.Constants;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceRegistration;
import org.osgi.framework.Version;
import org.osgi.framework.wiring.BundleCapability;
import org.osgi.framework.wiring.BundleRevision;

public class StandardRegionFilterTests {

    private static final String BUNDLE_SYMBOLIC_NAME = "A";

    private static final Version BUNDLE_VERSION = new Version("0");

    private StubBundle stubBundle;

    private String packageImportPolicy = "(" + BundleRevision.PACKAGE_NAMESPACE + "=foo)";

    private String serviceImportPolicy = "(" + Constants.OBJECTCLASS + "=foo.Service)";

    @Before
    public void setUp() throws Exception {
        this.stubBundle = new StubBundle(BUNDLE_SYMBOLIC_NAME, BUNDLE_VERSION);
    }

    @After
    public void tearDown() throws Exception {
    }

    private RegionFilter createBundleFilter(String bundleSymbolicName, Version bundleVersion) throws InvalidSyntaxException {
        String filter = "(&(" + RegionFilter.VISIBLE_BUNDLE_NAMESPACE + "=" + bundleSymbolicName + ")(" + Constants.BUNDLE_VERSION_ATTRIBUTE + ">="
            + bundleVersion + "))";
        return new StandardRegionFilterBuilder().allow(RegionFilter.VISIBLE_BUNDLE_NAMESPACE, filter).build();
    }

    private RegionFilter createRegionFilter(String namespace, Collection<String> filters) throws InvalidSyntaxException {
        StandardRegionFilterBuilder builder = new StandardRegionFilterBuilder();
        for (String filter : filters) {
            builder.allow(namespace, filter);
        }
        return builder.build();
    }

    @Test
    public void testAllowBundle() throws InvalidSyntaxException {
        RegionFilter regionFilter = createBundleFilter(BUNDLE_SYMBOLIC_NAME, BUNDLE_VERSION);
        assertTrue(regionFilter.isAllowed(stubBundle));
    }

    @Test
    public void testBundleNotAllowed() throws InvalidSyntaxException {
        RegionFilter regionFilter = new StandardRegionFilter(Collections.EMPTY_MAP);
        assertFalse(regionFilter.isAllowed(stubBundle));
    }

    @Test
    public void testBundleNotAllowedInRange() throws InvalidSyntaxException {
        RegionFilter regionFilter = createBundleFilter(BUNDLE_SYMBOLIC_NAME, new Version(1, 0, 0));
        assertFalse(regionFilter.isAllowed(stubBundle));
    }

    @Test
    public void testPackageImportAllowed() throws InvalidSyntaxException {
        RegionFilter regionFilter = createRegionFilter(RegionFilter.VISIBLE_PACKAGE_NAMESPACE, Arrays.asList(packageImportPolicy));
        BundleCapability packageCapability = EasyMock.createMock(BundleCapability.class);
        Map<String, Object> attrs = new HashMap<String, Object>();
        attrs.put(BundleRevision.PACKAGE_NAMESPACE, "foo");
        EasyMock.expect(packageCapability.getNamespace()).andReturn(BundleRevision.PACKAGE_NAMESPACE).anyTimes();
        EasyMock.expect(packageCapability.getAttributes()).andReturn(attrs).anyTimes();
        EasyMock.replay(packageCapability);
        assertTrue(regionFilter.isAllowed(packageCapability));
        assertEquals(Arrays.asList(this.packageImportPolicy), regionFilter.getSharingPolicy().get(RegionFilter.VISIBLE_PACKAGE_NAMESPACE));
    }

    @Test
    public void testPackageImportNotAllowed() throws InvalidSyntaxException {
        RegionFilter regionFilter = createRegionFilter(RegionFilter.VISIBLE_PACKAGE_NAMESPACE, Arrays.asList(packageImportPolicy));
        BundleCapability packageCapability = EasyMock.createMock(BundleCapability.class);
        Map<String, Object> attrs = new HashMap<String, Object>();
        attrs.put(BundleRevision.PACKAGE_NAMESPACE, "bar");
        EasyMock.expect(packageCapability.getNamespace()).andReturn(BundleRevision.PACKAGE_NAMESPACE).anyTimes();
        EasyMock.expect(packageCapability.getAttributes()).andReturn(attrs).anyTimes();
        EasyMock.replay(packageCapability);
        assertFalse(regionFilter.isAllowed(packageCapability));
        assertEquals(Arrays.asList(this.packageImportPolicy), regionFilter.getSharingPolicy().get(RegionFilter.VISIBLE_PACKAGE_NAMESPACE));
    }

    @Test
    public void testServiceImportAllowed() throws InvalidSyntaxException {
        RegionFilter regionFilter = createRegionFilter(RegionFilter.VISIBLE_SERVICE_NAMESPACE, Arrays.asList(serviceImportPolicy));
        ServiceRegistration<?> reg = new StubServiceRegistration<Object>(new StubBundleContext(), "foo.Service");
        assertTrue(regionFilter.isAllowed(reg.getReference()));
        assertEquals(Arrays.asList(serviceImportPolicy), regionFilter.getSharingPolicy().get(RegionFilter.VISIBLE_SERVICE_NAMESPACE));
    }

    @Test
    public void testServiceImportNotAllowed() throws InvalidSyntaxException {
        RegionFilter regionFilter = createRegionFilter(RegionFilter.VISIBLE_SERVICE_NAMESPACE, Arrays.asList(serviceImportPolicy));
        ServiceRegistration<?> reg = new StubServiceRegistration<Object>(new StubBundleContext(), "bar.Service");
        assertFalse(regionFilter.isAllowed(reg.getReference()));
        assertEquals(Arrays.asList(serviceImportPolicy), regionFilter.getSharingPolicy().get(RegionFilter.VISIBLE_SERVICE_NAMESPACE));
    }
}
