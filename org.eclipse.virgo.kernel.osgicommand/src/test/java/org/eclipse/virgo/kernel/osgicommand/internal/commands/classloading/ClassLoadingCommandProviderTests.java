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
package org.eclipse.virgo.kernel.osgicommand.internal.commands.classloading;

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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Unit tests for class loading commands
 *
 * @author Hristo Iliev (hristo.iliev@sap.com)
 * @version 1.0
 */
public class ClassLoadingCommandProviderTests {

    private static final long BUNDLE_ID = 1234;
    private static final String BUNDLE_SYMBOLIC_NAME = "test";
    private static final String CLASS_NAME = ClassLoadingCommandProviderTests.class.getName();
    private static final String CLASS_PACKAGE = ClassLoadingCommandProviderTests.class.getPackage().getName();

    @Test
    public void testCommandsWithNoClass() throws Exception {
        Bundle bundle = createMock(Bundle.class);
        BundleContext bundleContext = createMock(BundleContext.class);
        StubCommandInterpreter commandInterpreter = new StubCommandInterpreter();

        commandInterpreter.setArguments(new String[]{null});

        replay(bundle, bundleContext);

        ClassLoadingCommandProvider provider = new ClassLoadingCommandProvider(bundleContext);
        provider._clhas(commandInterpreter);
        provider._clload(commandInterpreter);
        provider._clexport(commandInterpreter);
        String output = commandInterpreter.getOutput();

        assertFalse("Command output [" + output + "] contains class name [" + CLASS_NAME + "]",
                    output.contains("" + CLASS_NAME));
        assertFalse("Command output [" + output + "] contains class package [" + CLASS_PACKAGE + "]",
                    output.contains("" + CLASS_PACKAGE));
        assertFalse("Command output [" + output + "] contains bundle ID [" + BUNDLE_ID + "]",
                    output.contains("" + BUNDLE_ID));
        assertFalse("Command output [" + output + "] contains bundle symbolic name [" + BUNDLE_SYMBOLIC_NAME + "]",
                    output.contains(BUNDLE_SYMBOLIC_NAME));

        verify(bundle, bundleContext);
    }

    @SuppressWarnings("rawtypes")
    @Test
    public void testClHasWithExistingClass() throws Exception {
        Bundle bundle = createMock(Bundle.class);
        BundleContext bundleContext = createMock(BundleContext.class);
        StubCommandInterpreter commandInterpreter = new StubCommandInterpreter();

        expect((Class)bundle.loadClass(CLASS_NAME)).andReturn(ClassLoadingCommandProviderTests.class);
        expect(bundle.getBundleId()).andReturn(BUNDLE_ID);
        expect(bundle.getSymbolicName()).andReturn(BUNDLE_SYMBOLIC_NAME);
        expect(bundleContext.getBundles()).andReturn(new Bundle[]{bundle});
        expect(bundleContext.getBundle(0)).andReturn(bundle); // system bundle is also our mockup
        commandInterpreter.setArguments(new String[]{CLASS_NAME});

        replay(bundle, bundleContext);

        ClassLoadingCommandProvider provider = new ClassLoadingCommandProvider(bundleContext);
        provider._clhas(commandInterpreter);
        String output = commandInterpreter.getOutput();

        assertTrue("Command output [" + output + "] does not contain class name [" + CLASS_NAME + "]",
                   output.contains("" + CLASS_NAME));
        assertTrue("Command output [" + output + "] does not contain class package [" + CLASS_PACKAGE + "]",
                   output.contains("" + CLASS_PACKAGE));
        assertTrue("Command output [" + output + "] does not contain bundle ID [" + BUNDLE_ID + "]",
                   output.contains("" + BUNDLE_ID));
        assertTrue("Command output [" + output + "] does not contain bundle symbolic name [" + BUNDLE_SYMBOLIC_NAME + "]",
                   output.contains(BUNDLE_SYMBOLIC_NAME));

        verify(bundle, bundleContext);
    }

    @Test
    public void testClHasWithNonExistingClass() throws Exception {
        Bundle bundle = createMock(Bundle.class);
        BundleContext bundleContext = createMock(BundleContext.class);
        StubCommandInterpreter commandInterpreter = new StubCommandInterpreter();

        expect(bundle.loadClass(CLASS_NAME)).andReturn(null); // class does not exist
        expect(bundleContext.getBundles()).andReturn(new Bundle[]{bundle});
        commandInterpreter.setArguments(new String[]{CLASS_NAME});

        replay(bundle, bundleContext);

        ClassLoadingCommandProvider provider = new ClassLoadingCommandProvider(bundleContext);
        provider._clhas(commandInterpreter);
        String output = commandInterpreter.getOutput();

        assertTrue("Command output [" + output + "] does not contain class name [" + CLASS_NAME + "]",
                   output.contains("" + CLASS_NAME));
        assertTrue("Command output [" + output + "] does not contain class package [" + CLASS_PACKAGE + "]",
                   output.contains("" + CLASS_PACKAGE));
        assertFalse("Command output [" + output + "] contains bundle ID [" + BUNDLE_ID + "]",
                    output.contains("" + BUNDLE_ID));
        assertFalse("Command output [" + output + "] contains bundle symbolic name [" + BUNDLE_SYMBOLIC_NAME + "]",
                    output.contains(BUNDLE_SYMBOLIC_NAME));

        verify(bundle, bundleContext);
    }

    @SuppressWarnings("rawtypes")
    @Test
    public void testClLoadWithExistingClass() throws Exception {
        Bundle bundle = createMock(Bundle.class);
        BundleContext bundleContext = createMock(BundleContext.class);
        StubCommandInterpreter commandInterpreter = new StubCommandInterpreter();

        expect((Class)bundle.loadClass(CLASS_NAME)).andReturn(ClassLoadingCommandProviderTests.class);
        expect(bundle.getBundleId()).andReturn(BUNDLE_ID);
        expect(bundle.getSymbolicName()).andReturn(BUNDLE_SYMBOLIC_NAME);
        expect(bundleContext.getBundles()).andReturn(new Bundle[]{bundle});
        expect(bundleContext.getBundle(0)).andReturn(bundle); // system bundle is also our mockup
        commandInterpreter.setArguments(new String[]{CLASS_NAME});

        replay(bundle, bundleContext);

        ClassLoadingCommandProvider provider = new ClassLoadingCommandProvider(bundleContext);
        provider._clload(commandInterpreter);
        String output = commandInterpreter.getOutput();

        assertTrue("Command output [" + output + "] does not contain class name [" + CLASS_NAME + "]",
                   output.contains("" + CLASS_NAME));
        assertTrue("Command output [" + output + "] does not contain class package [" + CLASS_PACKAGE + "]",
                   output.contains("" + CLASS_PACKAGE));
        assertTrue("Command output [" + output + "] does not contain bundle ID [" + BUNDLE_ID + "]",
                   output.contains("" + BUNDLE_ID));
        assertTrue("Command output [" + output + "] does not contain bundle symbolic name [" + BUNDLE_SYMBOLIC_NAME + "]",
                   output.contains(BUNDLE_SYMBOLIC_NAME));

        verify(bundle, bundleContext);
    }

    @Test
    public void testClLoadWithNonExistingClass() throws Exception {
        Bundle bundle = createMock(Bundle.class);
        BundleContext bundleContext = createMock(BundleContext.class);
        StubCommandInterpreter commandInterpreter = new StubCommandInterpreter();

        expect(bundle.loadClass(CLASS_NAME)).andReturn(null);
        expect(bundleContext.getBundles()).andReturn(new Bundle[]{bundle});
        commandInterpreter.setArguments(new String[]{CLASS_NAME});

        replay(bundle, bundleContext);

        ClassLoadingCommandProvider provider = new ClassLoadingCommandProvider(bundleContext);
        provider._clload(commandInterpreter);
        String output = commandInterpreter.getOutput();

        assertTrue("Command output [" + output + "] does not contain class name [" + CLASS_NAME + "]",
                   output.contains("" + CLASS_NAME));
        assertTrue("Command output [" + output + "] does not contain class package [" + CLASS_PACKAGE + "]",
                   output.contains("" + CLASS_PACKAGE));
        assertFalse("Command output [" + output + "] contains bundle ID [" + BUNDLE_ID + "]",
                    output.contains("" + BUNDLE_ID));
        assertFalse("Command output [" + output + "] contains bundle symbolic name [" + BUNDLE_SYMBOLIC_NAME + "]",
                    output.contains(BUNDLE_SYMBOLIC_NAME));

        verify(bundle, bundleContext);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Test
    public void testClLoadWithBundleNameAndExistingClass() throws Exception {
        Bundle bundle = createMock(Bundle.class);
        BundleContext bundleContext = createMock(BundleContext.class);
        PackageAdmin packageAdmin = createMock(PackageAdmin.class);
        ServiceReference packageAdminServiceReference = createMock(ServiceReference.class);
        StubCommandInterpreter commandInterpreter = new StubCommandInterpreter();

        expect((Class)bundle.loadClass(CLASS_NAME)).andReturn(ClassLoadingCommandProviderTests.class);
        expect(bundle.getBundleId()).andReturn(BUNDLE_ID);
        expect(bundle.getSymbolicName()).andReturn(BUNDLE_SYMBOLIC_NAME);
        expect(bundleContext.getBundle(0)).andReturn(bundle); // system bundle is also our mockup
        expect(bundleContext.getServiceReference(PackageAdmin.class)).andReturn(packageAdminServiceReference);
        expect(bundleContext.getService(packageAdminServiceReference)).andReturn(packageAdmin);
        expect(packageAdmin.getBundles(BUNDLE_SYMBOLIC_NAME, null)).andReturn(new Bundle[]{bundle});
        commandInterpreter.setArguments(new String[]{CLASS_NAME, BUNDLE_SYMBOLIC_NAME});

        replay(bundle, bundleContext, packageAdmin, packageAdminServiceReference);

        ClassLoadingCommandProvider provider = new ClassLoadingCommandProvider(bundleContext);
        provider._clload(commandInterpreter);
        String output = commandInterpreter.getOutput();

        assertTrue("Command output [" + output + "] does not contain class name [" + CLASS_NAME + "]",
                   output.contains("" + CLASS_NAME));
        assertTrue("Command output [" + output + "] does not contain class package [" + CLASS_PACKAGE + "]",
                   output.contains("" + CLASS_PACKAGE));
        assertTrue("Command output [" + output + "] does not contain bundle ID [" + BUNDLE_ID + "]",
                   output.contains("" + BUNDLE_ID));
        assertTrue("Command output [" + output + "] does not contain bundle symbolic name [" + BUNDLE_SYMBOLIC_NAME + "]",
                   output.contains(BUNDLE_SYMBOLIC_NAME));

        verify(bundle, bundleContext, packageAdmin, packageAdminServiceReference);
    }

    @SuppressWarnings("rawtypes")
    @Test
    public void testClLoadWithBundleIdAndExistingClass() throws Exception {
        Bundle bundle = createMock(Bundle.class);
        BundleContext bundleContext = createMock(BundleContext.class);
        StubCommandInterpreter commandInterpreter = new StubCommandInterpreter();

        expect((Class)bundle.loadClass(CLASS_NAME)).andReturn(ClassLoadingCommandProviderTests.class);
        expect(bundle.getBundleId()).andReturn(BUNDLE_ID);
        expect(bundle.getSymbolicName()).andReturn(BUNDLE_SYMBOLIC_NAME);
        expect(bundleContext.getBundle(0)).andReturn(bundle); // system bundle is also our mockup
        expect(bundleContext.getBundle(BUNDLE_ID)).andReturn(bundle);
        commandInterpreter.setArguments(new String[]{CLASS_NAME, "" + BUNDLE_ID});

        replay(bundle, bundleContext);

        ClassLoadingCommandProvider provider = new ClassLoadingCommandProvider(bundleContext);
        provider._clload(commandInterpreter);
        String output = commandInterpreter.getOutput();

        assertTrue("Command output [" + output + "] does not contain class name [" + CLASS_NAME + "]",
                   output.contains("" + CLASS_NAME));
        assertTrue("Command output [" + output + "] does not contain class package [" + CLASS_PACKAGE + "]",
                   output.contains("" + CLASS_PACKAGE));
        assertTrue("Command output [" + output + "] does not contain bundle ID [" + BUNDLE_ID + "]",
                   output.contains("" + BUNDLE_ID));
        assertTrue("Command output [" + output + "] does not contain bundle symbolic name [" + BUNDLE_SYMBOLIC_NAME + "]",
                   output.contains(BUNDLE_SYMBOLIC_NAME));

        verify(bundle, bundleContext);
    }

    @Test
    public void testClLoadWithBundleIdAndMissingClass() throws Exception {
        Bundle bundle = createMock(Bundle.class);
        BundleContext bundleContext = createMock(BundleContext.class);
        StubCommandInterpreter commandInterpreter = new StubCommandInterpreter();

        expect(bundle.loadClass(CLASS_NAME)).andReturn(null);
        expect(bundleContext.getBundle(BUNDLE_ID)).andReturn(bundle);
        commandInterpreter.setArguments(new String[]{CLASS_NAME, "" + BUNDLE_ID});

        replay(bundle, bundleContext);

        ClassLoadingCommandProvider provider = new ClassLoadingCommandProvider(bundleContext);
        provider._clload(commandInterpreter);
        String output = commandInterpreter.getOutput();

        assertTrue("Command output [" + output + "] does not contain class name [" + CLASS_NAME + "]",
                   output.contains("" + CLASS_NAME));
        assertTrue("Command output [" + output + "] does not contain class package [" + CLASS_PACKAGE + "]",
                   output.contains("" + CLASS_PACKAGE));
        assertTrue("Command output [" + output + "] does not contain bundle ID [" + BUNDLE_ID + "]",
                   output.contains("" + BUNDLE_ID));
        assertFalse("Command output [" + output + "] contains bundle symbolic name [" + BUNDLE_SYMBOLIC_NAME + "]",
                    output.contains(BUNDLE_SYMBOLIC_NAME));

        verify(bundle, bundleContext);
    }

    @Test
    public void testClExportWithNoPackage() throws Exception {
        Bundle bundle = createMock(Bundle.class);
        BundleContext bundleContext = createMock(BundleContext.class);
        StubCommandInterpreter commandInterpreter = new StubCommandInterpreter();

        commandInterpreter.setArguments(new String[]{""});

        replay(bundle, bundleContext);

        ClassLoadingCommandProvider provider = new ClassLoadingCommandProvider(bundleContext);
        provider._clexport(commandInterpreter);
        String output = commandInterpreter.getOutput();

        assertFalse("Command output [" + output + "] does not contain class name [" + CLASS_NAME + "]",
                    output.contains("" + CLASS_NAME));
        assertFalse("Command output [" + output + "] does not contain class package [" + CLASS_PACKAGE + "]",
                    output.contains("" + CLASS_PACKAGE));
        assertFalse("Command output [" + output + "] does not contain bundle ID [" + BUNDLE_ID + "]",
                    output.contains("" + BUNDLE_ID));
        assertFalse("Command output [" + output + "] does not contain bundle symbolic name [" + BUNDLE_SYMBOLIC_NAME + "]",
                    output.contains(BUNDLE_SYMBOLIC_NAME));

        verify(bundle, bundleContext);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Test
    public void testClExportWithMissingPackage() throws Exception {
        PlatformAdmin platformAdmin = createMock(PlatformAdmin.class);
        ServiceReference platformAdminServiceReference = createMock(ServiceReference.class);
        Bundle bundle = createMock(Bundle.class);
        BundleContext bundleContext = createMock(BundleContext.class);
        State bundleState = createMock(State.class);
        BundleDescription bundleDescription = createMock(BundleDescription.class);
        StubCommandInterpreter commandInterpreter = new StubCommandInterpreter();

        expect(bundle.getBundleId()).andReturn(BUNDLE_ID);
        expect(bundleContext.getServiceReference(PlatformAdmin.class)).andReturn(platformAdminServiceReference);
        expect(bundleContext.getService(platformAdminServiceReference)).andReturn(platformAdmin);
        expect(bundleContext.getBundles()).andReturn(new Bundle[]{bundle});
        expect(bundleDescription.getSelectedExports()).andReturn(new ExportPackageDescription[]{}); // nothing exported
        expect(platformAdmin.getState(false)).andReturn(bundleState);
        expect(bundleState.getBundle(BUNDLE_ID)).andReturn(bundleDescription);
        commandInterpreter.setArguments(new String[]{CLASS_NAME});

        replay(platformAdmin, platformAdminServiceReference,
               bundle, bundleContext, bundleState, bundleDescription);

        ClassLoadingCommandProvider provider = new ClassLoadingCommandProvider(bundleContext);
        provider._clexport(commandInterpreter);
        String output = commandInterpreter.getOutput();

        assertTrue("Command output [" + output + "] does not contain class name [" + CLASS_NAME + "]",
                   output.contains("" + CLASS_NAME));
        assertTrue("Command output [" + output + "] does not contain class package [" + CLASS_PACKAGE + "]",
                   output.contains("" + CLASS_PACKAGE));
        assertFalse("Command output [" + output + "] contains bundle ID [" + BUNDLE_ID + "]",
                    output.contains("" + BUNDLE_ID));
        assertFalse("Command output [" + output + "] contains bundle symbolic name [" + BUNDLE_SYMBOLIC_NAME + "]",
                    output.contains(BUNDLE_SYMBOLIC_NAME));

        verify(platformAdmin, platformAdminServiceReference,
               bundle, bundleContext, bundleState, bundleDescription);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Test
    public void testClExportWithExportedPackage() throws Exception {
        PlatformAdmin platformAdmin = createMock(PlatformAdmin.class);
        ServiceReference platformAdminServiceReference = createMock(ServiceReference.class);
        Bundle bundle = createMock(Bundle.class);
        BundleContext bundleContext = createMock(BundleContext.class);
        State bundleState = createMock(State.class);
        BundleDescription bundleDescription = createMock(BundleDescription.class);
        ExportPackageDescription exportPackageDescription = createMock(ExportPackageDescription.class);
        StubCommandInterpreter commandInterpreter = new StubCommandInterpreter();

        expect(bundle.getBundleId()).andReturn(BUNDLE_ID).times(2);
        expect(bundle.getSymbolicName()).andReturn(BUNDLE_SYMBOLIC_NAME);
        expect((Class)bundle.loadClass(CLASS_NAME)).andReturn(ClassLoadingCommandProviderTests.class);
        expect(bundleContext.getServiceReference(PlatformAdmin.class)).andReturn(platformAdminServiceReference);
        expect(bundleContext.getService(platformAdminServiceReference)).andReturn(platformAdmin);
        expect(bundleContext.getBundles()).andReturn(new Bundle[]{bundle});
        expect(exportPackageDescription.getName()).andReturn(CLASS_PACKAGE);
        expect(bundleDescription.getSelectedExports()).andReturn(new ExportPackageDescription[]{exportPackageDescription});
        expect(platformAdmin.getState(false)).andReturn(bundleState);
        expect(bundleState.getBundle(BUNDLE_ID)).andReturn(bundleDescription);
        commandInterpreter.setArguments(new String[]{CLASS_NAME});

        replay(platformAdmin, platformAdminServiceReference,
               bundle, bundleContext, bundleState, bundleDescription,
               exportPackageDescription);

        ClassLoadingCommandProvider provider = new ClassLoadingCommandProvider(bundleContext);
        provider._clexport(commandInterpreter);
        String output = commandInterpreter.getOutput();

        assertTrue("Command output [" + output + "] does not contain class name [" + CLASS_NAME + "]",
                   output.contains("" + CLASS_NAME));
        assertTrue("Command output [" + output + "] does not contain class package [" + CLASS_PACKAGE + "]",
                   output.contains("" + CLASS_PACKAGE));
        assertTrue("Command output [" + output + "] does not contain bundle ID [" + BUNDLE_ID + "]",
                    output.contains("" + BUNDLE_ID));
        assertTrue("Command output [" + output + "] does not contain bundle symbolic name [" + BUNDLE_SYMBOLIC_NAME + "]",
                    output.contains(BUNDLE_SYMBOLIC_NAME));

        verify(platformAdmin, platformAdminServiceReference,
               bundle, bundleContext, bundleState, bundleDescription,
               exportPackageDescription);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Test
    public void testClExportWithExportedPackageMissingClass() throws Exception {
        PlatformAdmin platformAdmin = createMock(PlatformAdmin.class);
        ServiceReference platformAdminServiceReference = createMock(ServiceReference.class);
        Bundle bundle = createMock(Bundle.class);
        BundleContext bundleContext = createMock(BundleContext.class);
        State bundleState = createMock(State.class);
        BundleDescription bundleDescription = createMock(BundleDescription.class);
        ExportPackageDescription exportPackageDescription = createMock(ExportPackageDescription.class);
        StubCommandInterpreter commandInterpreter = new StubCommandInterpreter();

        expect(bundle.getBundleId()).andReturn(BUNDLE_ID).times(2);
        expect(bundle.getSymbolicName()).andReturn(BUNDLE_SYMBOLIC_NAME);
        expect(bundle.loadClass(CLASS_NAME)).andReturn(null); // class cannot be loaded
        expect(bundleContext.getServiceReference(PlatformAdmin.class)).andReturn(platformAdminServiceReference);
        expect(bundleContext.getService(platformAdminServiceReference)).andReturn(platformAdmin);
        expect(bundleContext.getBundles()).andReturn(new Bundle[]{bundle});
        expect(exportPackageDescription.getName()).andReturn(CLASS_PACKAGE);
        expect(bundleDescription.getSelectedExports()).andReturn(new ExportPackageDescription[]{exportPackageDescription});
        expect(platformAdmin.getState(false)).andReturn(bundleState);
        expect(bundleState.getBundle(BUNDLE_ID)).andReturn(bundleDescription);
        commandInterpreter.setArguments(new String[]{CLASS_NAME});

        replay(platformAdmin, platformAdminServiceReference,
               bundle, bundleContext, bundleState, bundleDescription,
               exportPackageDescription);

        ClassLoadingCommandProvider provider = new ClassLoadingCommandProvider(bundleContext);
        provider._clexport(commandInterpreter);
        String output = commandInterpreter.getOutput();

        assertTrue("Command output [" + output + "] does not contain class name [" + CLASS_NAME + "]",
                   output.contains("" + CLASS_NAME));
        assertTrue("Command output [" + output + "] does not contain class package [" + CLASS_PACKAGE + "]",
                   output.contains("" + CLASS_PACKAGE));
        assertTrue("Command output [" + output + "] does not contain bundle ID [" + BUNDLE_ID + "]",
                    output.contains("" + BUNDLE_ID));
        assertTrue("Command output [" + output + "] does not contain bundle symbolic name [" + BUNDLE_SYMBOLIC_NAME + "]",
                    output.contains(BUNDLE_SYMBOLIC_NAME));

        verify(platformAdmin, platformAdminServiceReference,
               bundle, bundleContext, bundleState, bundleDescription,
               exportPackageDescription);
    }

}
