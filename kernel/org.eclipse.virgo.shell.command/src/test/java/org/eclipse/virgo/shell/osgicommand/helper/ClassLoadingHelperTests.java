/*******************************************************************************
 * Copyright (c) 2010 SAP AG
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Hristo Iliev, SAP AG - initial contribution
 ******************************************************************************/
package org.eclipse.virgo.shell.osgicommand.helper;

import org.eclipse.osgi.service.resolver.BundleDescription;
import org.eclipse.osgi.service.resolver.ExportPackageDescription;
import org.eclipse.osgi.service.resolver.PlatformAdmin;
import org.eclipse.osgi.service.resolver.State;
import org.junit.Test;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.packageadmin.PackageAdmin;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

/**
 * Class for unit testing {@link ClassLoadingHelper}
 */
@SuppressWarnings("deprecation")
public class ClassLoadingHelperTests {

    private static final long BUNDLE_ID = 1234;
    private static final String BUNDLE_SYMBOLIC_NAME = "test";
    private static final String CLASS_NAME = ClassLoadingHelperTests.class.getName();
    private static final String CLASS_NAME_PATH = CLASS_NAME.replace(".", "/") + ".class";
    private static final String CLASS_PACKAGE = ClassLoadingHelperTests.class.getPackage().getName();

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Test
    public void testIsMissingPackageExported() throws Exception {
        PlatformAdmin platformAdmin = createMock(PlatformAdmin.class);
        ServiceReference platformAdminServiceReference = createMock(ServiceReference.class);
        Bundle bundle = createMock(Bundle.class);
        BundleContext bundleContext = createMock(BundleContext.class);
        State bundleState = createMock(State.class);
        BundleDescription bundleDescription = createMock(BundleDescription.class);

        expect(bundle.getBundleId()).andReturn(BUNDLE_ID);
        expect(bundleContext.getServiceReference(PlatformAdmin.class)).andReturn(platformAdminServiceReference);
        expect(bundleContext.getService(platformAdminServiceReference)).andReturn(platformAdmin);
        expect(bundleDescription.getSelectedExports()).andReturn(new ExportPackageDescription[0]);
        expect(platformAdmin.getState(false)).andReturn(bundleState);
        expect(bundleState.getBundle(BUNDLE_ID)).andReturn(bundleDescription);

        replay(platformAdmin, platformAdminServiceReference, bundle, bundleContext, bundleState, bundleDescription);

        assertFalse("Class [" + CLASS_NAME + "] is reported as exported, while it is NOT",
                    ClassLoadingHelper.isPackageExported(bundleContext, CLASS_NAME, bundle));

        verify(platformAdmin, platformAdminServiceReference, bundle, bundleContext, bundleState, bundleDescription);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Test
    public void testIsExistingPackageExported() throws Exception {
        PlatformAdmin platformAdmin = createMock(PlatformAdmin.class);
        ServiceReference platformAdminServiceReference = createMock(ServiceReference.class);
        Bundle bundle = createMock(Bundle.class);
        BundleContext bundleContext = createMock(BundleContext.class);
        State bundleState = createMock(State.class);
        BundleDescription bundleDescription = createMock(BundleDescription.class);
        ExportPackageDescription exportPackageDescription = createMock(ExportPackageDescription.class);

        expect(bundle.getBundleId()).andReturn(BUNDLE_ID);
        expect(bundleContext.getServiceReference(PlatformAdmin.class)).andReturn(platformAdminServiceReference);
        expect(bundleContext.getService(platformAdminServiceReference)).andReturn(platformAdmin);
        expect(exportPackageDescription.getName()).andReturn(CLASS_PACKAGE);
        expect(bundleDescription.getSelectedExports()).andReturn(new ExportPackageDescription[]{exportPackageDescription});
        expect(platformAdmin.getState(false)).andReturn(bundleState);
        expect(bundleState.getBundle(BUNDLE_ID)).andReturn(bundleDescription);

        replay(platformAdmin, platformAdminServiceReference,
               bundle, bundleContext, bundleState, bundleDescription,
               exportPackageDescription);

        assertTrue("Class [" + CLASS_NAME + "] is reported as NOT exported, while it is",
                   ClassLoadingHelper.isPackageExported(bundleContext, CLASS_PACKAGE, bundle));

        verify(platformAdmin, platformAdminServiceReference, bundle, bundleContext, bundleState, bundleDescription);
    }

    @Test
    public void testTryToLoadMissingClass() throws Exception {
        Bundle bundle = createMock(Bundle.class);

        expect(bundle.loadClass(CLASS_NAME)).andReturn(null); // missing class

        replay(bundle);

        assertNull("Class [" + CLASS_NAME + "] found, while it is not existing",
                   ClassLoadingHelper.tryToLoadClass(CLASS_NAME, bundle));

        verify(bundle);
    }

    @Test
    public void testTryToLoadMissingBundle() throws Exception {
        assertNull("Class [" + CLASS_NAME + "] found, while no bundle is specified",
                   ClassLoadingHelper.tryToLoadClass(CLASS_NAME, null));
    }

    @Test
    public void testTryToLoadMissingClassWithException() throws Exception {
        Bundle bundle = createMock(Bundle.class);

        expect(bundle.loadClass(CLASS_NAME)).andThrow(new ClassNotFoundException("not found")); // missing class

        replay(bundle);

        assertNull("Class [" + CLASS_NAME + "] found, while it is not existing",
                   ClassLoadingHelper.tryToLoadClass(CLASS_NAME, bundle));

        verify(bundle);
    }

    @SuppressWarnings("rawtypes")
    @Test
    public void testTryToLoadExistingClass() throws Exception {
        Bundle bundle = createMock(Bundle.class);

        expect((Class) bundle.loadClass(CLASS_NAME)).andReturn(ClassLoadingHelperTests.class);

        replay(bundle);

        assertNotNull("Class [" + CLASS_NAME + "] not found",
                      ClassLoadingHelper.tryToLoadClass(CLASS_NAME, bundle));

        verify(bundle);
    }

    @Test
    public void testGetBundlesLoadingMissingClass() throws Exception {
        Bundle bundle = createMock(Bundle.class);
        BundleContext bundleContext = createMock(BundleContext.class);

        expect(bundle.loadClass(CLASS_NAME)).andReturn(null); // missing class
        expect(bundleContext.getBundles()).andReturn(new Bundle[]{bundle});

        replay(bundle, bundleContext);

        assertTrue("The bundle [" + BUNDLE_SYMBOLIC_NAME + "] should NOT be able to load class [" + CLASS_NAME + "]",
                   ClassLoadingHelper.getBundlesLoadingClass(bundleContext, CLASS_NAME).size() == 0);

        verify(bundle, bundleContext);
    }

    @SuppressWarnings("rawtypes")
    @Test
    public void testGetBundlesLoadingExistingClass() throws Exception {
        Bundle bundle = createMock(Bundle.class);
        BundleContext bundleContext = createMock(BundleContext.class);

        expect((Class) bundle.loadClass(CLASS_NAME)).andReturn(ClassLoadingHelperTests.class);
        expect(bundleContext.getBundles()).andReturn(new Bundle[]{bundle});
        expect(bundleContext.getBundle(0)).andReturn(bundle);

        replay(bundle, bundleContext);

        assertFalse("The bundle [" + BUNDLE_SYMBOLIC_NAME + "] should be able to load class [" + CLASS_NAME + "]",
                    ClassLoadingHelper.getBundlesLoadingClass(bundleContext, CLASS_NAME).size() == 0);

        verify(bundle, bundleContext);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Test
    public void testGetBundleLoadingMissingClass() throws Exception {
        Bundle bundle = createMock(Bundle.class);
        BundleContext bundleContext = createMock(BundleContext.class);
        PackageAdmin packageAdmin = createMock(PackageAdmin.class);
        ServiceReference packageAdminServiceReference = createMock(ServiceReference.class);

        expect(bundle.loadClass(CLASS_NAME)).andReturn(null); // missing class
        expect(bundleContext.getServiceReference(PackageAdmin.class)).andReturn(packageAdminServiceReference);
        expect(bundleContext.getService(packageAdminServiceReference)).andReturn(packageAdmin);
        expect(packageAdmin.getBundles(BUNDLE_SYMBOLIC_NAME, null)).andReturn(new Bundle[]{bundle});

        replay(bundle, bundleContext, packageAdmin, packageAdminServiceReference);

        assertTrue("No bundle should be able to load class [" + CLASS_NAME + "]",
                   ClassLoadingHelper.getBundlesLoadingClass(bundleContext, CLASS_NAME, BUNDLE_SYMBOLIC_NAME).size() == 0);

        verify(bundle, bundleContext, packageAdmin, packageAdminServiceReference);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Test
    public void testGetBundleByNameLoadingExistingClass() throws Exception {
        Bundle bundle = createMock(Bundle.class);
        BundleContext bundleContext = createMock(BundleContext.class);
        PackageAdmin packageAdmin = createMock(PackageAdmin.class);
        ServiceReference packageAdminServiceReference = createMock(ServiceReference.class);

        expect((Class) bundle.loadClass(CLASS_NAME)).andReturn(ClassLoadingHelperTests.class);
        expect(bundleContext.getBundle(0)).andReturn(bundle);
        expect(bundleContext.getServiceReference(PackageAdmin.class)).andReturn(packageAdminServiceReference);
        expect(bundleContext.getService(packageAdminServiceReference)).andReturn(packageAdmin);
        expect(packageAdmin.getBundles(BUNDLE_SYMBOLIC_NAME, null)).andReturn(new Bundle[]{bundle});

        replay(bundle, bundleContext, packageAdmin, packageAdminServiceReference);

        assertTrue("The class [" + CLASS_NAME + "] should be successfully loaded",
                   ClassLoadingHelper.getBundlesLoadingClass(bundleContext, CLASS_NAME, BUNDLE_SYMBOLIC_NAME).size() != 0);

        verify(bundle, bundleContext, packageAdmin, packageAdminServiceReference);
    }

    @SuppressWarnings("rawtypes")
    @Test
    public void testGetBundleByIdLoadingExistingClass() throws Exception {
        Bundle bundle = createMock(Bundle.class);
        BundleContext bundleContext = createMock(BundleContext.class);

        expect((Class) bundle.loadClass(CLASS_NAME)).andReturn(ClassLoadingHelperTests.class);
        expect(bundleContext.getBundle(BUNDLE_ID)).andReturn(bundle);
        expect(bundleContext.getBundle(0)).andReturn(bundle);

        replay(bundle, bundleContext);

        assertTrue("The class [" + CLASS_NAME + "] should be successfully loaded",
                   ClassLoadingHelper.getBundlesLoadingClass(bundleContext, CLASS_NAME, "" + BUNDLE_ID).size() != 0);

        verify(bundle, bundleContext);
    }

    @Test
    public void testConvertToClassName() throws Exception {
        assertEquals("Path to resource [" + CLASS_NAME_PATH + "] not converted properly", CLASS_NAME, ClassLoadingHelper.convertToClassName(CLASS_NAME_PATH));
        assertEquals("Path to resource [" + CLASS_NAME + "] not converted properly", CLASS_NAME, ClassLoadingHelper.convertToClassName(CLASS_NAME));
    }

    @Test
    public void testConvertToResourcePath() throws Exception {
        assertEquals("Class name [" + CLASS_NAME + "] not converted properly", CLASS_NAME_PATH, ClassLoadingHelper.convertToResourcePath(CLASS_NAME));
        assertEquals("Class name [" + CLASS_NAME_PATH + "] not converted properly", CLASS_NAME_PATH, ClassLoadingHelper.convertToResourcePath(CLASS_NAME_PATH));
    }

}
