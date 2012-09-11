/*******************************************************************************
 * Copyright (c) 2011 SAP AG
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Hristo Iliev, SAP AG - initial contribution
 ******************************************************************************/

package org.eclipse.virgo.shell.osgicommand.management;

import org.eclipse.osgi.service.resolver.BundleDescription;
import org.eclipse.osgi.service.resolver.ExportPackageDescription;
import org.eclipse.osgi.service.resolver.PlatformAdmin;
import org.eclipse.osgi.service.resolver.State;
import org.eclipse.virgo.shell.osgicommand.helper.ClassLoadingHelperTests;
import org.junit.Test;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import java.net.URL;
import java.util.*;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

/**
 * Tests for {@link ClassLoadingSupport}
 *
 */
public class ClassLoadingSupportTests {

    private static final long BUNDLE_ID = 1234;
    private static final String BUNDLE_SYMBOLIC_NAME = "test";

    private static final String CLASS_NAME = ClassLoadingSupportTests.class.getName();
    private static final String CLASS_PACKAGE = ClassLoadingSupportTests.class.getPackage().getName();

    private static final String SHORT_CLASS_NAME = CLASS_NAME.substring(CLASS_NAME.lastIndexOf(".") + 1) + ".class";
    private static final String CLASS_NAME_PATH = CLASS_NAME.replace(".", "/") + ".class";

    private static final Map<List<String>, List<String>> RESULT_ORIGIN_LOAD_MAP = new HashMap<List<String>, List<String>>(2);
    private static final List<List<String>> RESULT_EXPORT_ARRAY = new ArrayList<List<String>>(1);

    static {
        List<String> loadingBundle = new ArrayList<String>();
        loadingBundle.add("" + BUNDLE_ID);
        loadingBundle.add(BUNDLE_SYMBOLIC_NAME);

        List<String> originBundle = new ArrayList<String>();
        originBundle.add("" + BUNDLE_ID);
        originBundle.add(BUNDLE_SYMBOLIC_NAME);

        RESULT_ORIGIN_LOAD_MAP.put(loadingBundle, originBundle);
        RESULT_EXPORT_ARRAY.add(loadingBundle);
    }

    @Test
    public void testGetBundlesContainingResource() throws Exception {
        Bundle bundle = createMock(Bundle.class);
        BundleContext bundleContext = createMock(BundleContext.class);
        Enumeration<URL> urlEnum = this.getClass().getClassLoader().getResources(CLASS_NAME_PATH);

        expect(bundle.findEntries("/", SHORT_CLASS_NAME, true)).andReturn(urlEnum);
        expect(bundle.findEntries("/", CLASS_NAME_PATH, true)).andReturn(null); // not found in the root
        expect(bundle.getBundleId()).andReturn(BUNDLE_ID);
        expect(bundle.getSymbolicName()).andReturn(BUNDLE_SYMBOLIC_NAME);
        expect(bundleContext.getBundles()).andReturn(new Bundle[]{bundle});

        replay(bundle, bundleContext);

        ClassLoadingSupport support = new ClassLoadingSupport(bundleContext);

        Map<List<String>, List<String>> map = support.getBundlesContainingResource(CLASS_NAME_PATH);
        assertEquals("More than one test URL found in the result " + map, 1, map.size());
        assertTrue("Test URL not found in the result: " + map, map.toString().contains(CLASS_NAME_PATH));

        verify(bundle, bundleContext);
    }

    @SuppressWarnings("rawtypes")
    @Test
    public void testGetBundlesLoadingClass() throws Exception {
        Bundle bundle = createMock(Bundle.class);
        BundleContext bundleContext = createMock(BundleContext.class);

        expect((Class) bundle.loadClass(CLASS_NAME)).andReturn(ClassLoadingHelperTests.class);
        expect(bundle.getBundleId()).andReturn(BUNDLE_ID).times(2);
        expect(bundle.getSymbolicName()).andReturn(BUNDLE_SYMBOLIC_NAME).times(2);
        expect(bundleContext.getBundles()).andReturn(new Bundle[]{bundle});
        expect(bundleContext.getBundle(0)).andReturn(bundle);

        replay(bundle, bundleContext);

        ClassLoadingSupport support = new ClassLoadingSupport(bundleContext);

        Map<List<String>, List<String>> map = support.getBundlesLoadingClass(CLASS_NAME);
        assertEquals("Unexpected result length for map " + map, RESULT_ORIGIN_LOAD_MAP.size(), map.size());
        assertEquals("Unexpected result array: " + map, RESULT_ORIGIN_LOAD_MAP, map);

        verify(bundle, bundleContext);
    }

    @Test
    public void testGetBundlesExportingPackage() throws Exception {
        PlatformAdmin platformAdmin = createMock(PlatformAdmin.class);
        @SuppressWarnings("unchecked")
        ServiceReference<PlatformAdmin> platformAdminServiceReference = createMock(ServiceReference.class);
        Bundle bundle = createMock(Bundle.class);
        BundleContext bundleContext = createMock(BundleContext.class);
        State bundleState = createMock(State.class);
        BundleDescription bundleDescription = createMock(BundleDescription.class);
        ExportPackageDescription exportPackageDescription = createMock(ExportPackageDescription.class);

        expect(bundle.getBundleId()).andReturn(BUNDLE_ID).times(2);
        expect(bundle.getSymbolicName()).andReturn(BUNDLE_SYMBOLIC_NAME);
        expect(bundleContext.getServiceReference(PlatformAdmin.class)).andReturn(platformAdminServiceReference);
        expect(bundleContext.getService(platformAdminServiceReference)).andReturn(platformAdmin);
        expect(bundleContext.getBundles()).andReturn(new Bundle[]{bundle});
        expect(exportPackageDescription.getName()).andReturn(CLASS_PACKAGE);
        expect(bundleDescription.getSelectedExports()).andReturn(new ExportPackageDescription[]{exportPackageDescription});
        expect(platformAdmin.getState(false)).andReturn(bundleState);
        expect(bundleState.getBundle(BUNDLE_ID)).andReturn(bundleDescription);

        replay(platformAdmin, platformAdminServiceReference,
               bundle, bundleContext, bundleState, bundleDescription,
               exportPackageDescription);

        ClassLoadingSupport support = new ClassLoadingSupport(bundleContext);

        List<List<String>> list = support.getBundlesExportingPackage(CLASS_PACKAGE);
        assertEquals("Unexpected result length for list " + list, RESULT_EXPORT_ARRAY.size(), list.size());
        assertEquals("Unexpected result list " + list, RESULT_EXPORT_ARRAY, list);

        verify(platformAdmin, platformAdminServiceReference, bundle, bundleContext, bundleState, bundleDescription);
    }

    @SuppressWarnings("rawtypes")
    @Test
    public void testTryToLoadClassFromBundle() throws Exception {
        Bundle bundle = createMock(Bundle.class);
        expect((Class) bundle.loadClass(CLASS_NAME)).andReturn(ClassLoadingHelperTests.class);
        expect((Class<?>) bundle.loadClass(CLASS_NAME + CLASS_NAME)).andReturn(null);
        BundleContext bundleContext = createMock(BundleContext.class);
        expect(bundleContext.getBundle(BUNDLE_ID)).andReturn(bundle).times(2);

        replay(bundle, bundleContext);

        ClassLoadingSupport support = new ClassLoadingSupport(bundleContext);

        assertTrue("Class [" + CLASS_NAME + "] not found", support.tryToLoadClassFromBundle(CLASS_NAME, BUNDLE_ID));
        assertFalse("Class [" + CLASS_NAME + CLASS_NAME + "] found", support.tryToLoadClassFromBundle(CLASS_NAME + CLASS_NAME, BUNDLE_ID));

        verify(bundle, bundleContext);
    }
}
