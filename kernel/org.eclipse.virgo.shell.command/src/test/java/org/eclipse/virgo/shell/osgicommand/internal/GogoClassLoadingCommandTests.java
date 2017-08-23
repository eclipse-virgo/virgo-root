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

package org.eclipse.virgo.shell.osgicommand.internal;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.Enumeration;

import org.eclipse.osgi.service.resolver.BundleDescription;
import org.eclipse.osgi.service.resolver.ExportPackageDescription;
import org.eclipse.osgi.service.resolver.PlatformAdmin;
import org.eclipse.osgi.service.resolver.State;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.packageadmin.PackageAdmin;

/**
 * Unit tests for class loading commands
 */
@SuppressWarnings("deprecation")
public class GogoClassLoadingCommandTests {

    private static final long BUNDLE_ID = 1234;

    private static final String BUNDLE_SYMBOLIC_NAME = "test";

    private static final String CLASS_NAME = GogoClassLoadingCommandTests.class.getName();

    private static final String CLASS_PACKAGE = GogoClassLoadingCommandTests.class.getPackage().getName();

    private static final String SHORT_CLASS_NAME = CLASS_NAME.substring(CLASS_NAME.lastIndexOf(".") + 1) + ".class";

    private static final String CLASS_NAME_PATH = CLASS_NAME.replace(".", "/") + ".class";

    private static final String CLASS_PACKAGE_PATH = CLASS_PACKAGE.replace(".", "/");

    private ByteArrayOutputStream baos;

    private PrintStream oldOut;

    private PrintStream output;

    @Before
    public void before() throws UnsupportedEncodingException {
        this.oldOut = System.out;
        this.baos = new ByteArrayOutputStream();
        this.output = new PrintStream(baos, true, UTF_8.name());
        System.setOut(this.output);
    }

    private String getOutput() {
        this.output.flush();
        String output = new String(baos.toByteArray(), UTF_8);
        return output;
    }

    @After
    public void after() {
        System.setOut(this.oldOut);
    }

    @Test
    public void testClHasWithExistingClass() throws Exception {
        Bundle bundle = createMock(Bundle.class);
        BundleContext bundleContext = createMock(BundleContext.class);
        Enumeration<URL> urlEnum = this.getClass().getClassLoader().getResources(CLASS_NAME_PATH);

        expect(bundle.findEntries("/", SHORT_CLASS_NAME, true)).andReturn(urlEnum);
        expect(bundle.findEntries("/", CLASS_NAME_PATH, true)).andReturn(null); // class not found in root
        expect(bundle.getBundleId()).andReturn(BUNDLE_ID);
        expect(bundle.getSymbolicName()).andReturn(BUNDLE_SYMBOLIC_NAME);
        expect(bundleContext.getBundles()).andReturn(new Bundle[] { bundle });

        replay(bundle, bundleContext);

        GogoClassLoadingCommand command = new GogoClassLoadingCommand(bundleContext);
        command.clhas(CLASS_NAME);
        String output = getOutput();

        assertTrue("Command output [" + output + "] does not contain class name [" + CLASS_NAME + "]", output.contains("" + CLASS_NAME_PATH));
        assertTrue("Command output [" + output + "] does not contain class package [" + CLASS_PACKAGE_PATH + "]",
            output.contains("" + CLASS_PACKAGE_PATH));
        assertTrue("Command output [" + output + "] does not contain bundle ID [" + BUNDLE_ID + "]", output.contains("" + BUNDLE_ID));
        assertTrue("Command output [" + output + "] does not contain bundle symbolic name [" + BUNDLE_SYMBOLIC_NAME + "]",
            output.contains(BUNDLE_SYMBOLIC_NAME));

        verify(bundle, bundleContext);

    }

    @Test
    public void testClHasWithNonExistingClass() throws Exception {
        Bundle bundle = createMock(Bundle.class);
        BundleContext bundleContext = createMock(BundleContext.class);

        expect(bundle.findEntries("/", SHORT_CLASS_NAME, true)).andReturn(null); // class does not exist
        expect(bundle.findEntries("/", CLASS_NAME_PATH, true)).andReturn(null); // class does not exist
        expect(bundleContext.getBundles()).andReturn(new Bundle[] { bundle });

        replay(bundle, bundleContext);

        GogoClassLoadingCommand command = new GogoClassLoadingCommand(bundleContext);
        command.clhas(CLASS_NAME);
        String output = getOutput();

        assertTrue("Command output [" + output + "] does not contain class name [" + CLASS_NAME + "]", output.contains("" + CLASS_NAME_PATH));
        assertTrue("Command output [" + output + "] does not contain class package [" + CLASS_PACKAGE + "]", output.contains("" + CLASS_PACKAGE_PATH));
        assertFalse("Command output [" + output + "] contains bundle ID [" + BUNDLE_ID + "]", output.contains("" + BUNDLE_ID));
        assertFalse("Command output [" + output + "] contains bundle symbolic name [" + BUNDLE_SYMBOLIC_NAME + "]",
            output.contains(BUNDLE_SYMBOLIC_NAME));

        verify(bundle, bundleContext);
    }

    @SuppressWarnings("rawtypes")
    @Test
    public void testClLoadWithExistingClass() throws Exception {
        Bundle bundle = createMock(Bundle.class);
        BundleContext bundleContext = createMock(BundleContext.class);

        expect((Class) bundle.loadClass(CLASS_NAME)).andReturn(GogoClassLoadingCommandTests.class);
        expect(bundle.getBundleId()).andReturn(BUNDLE_ID);
        expect(bundle.getSymbolicName()).andReturn(BUNDLE_SYMBOLIC_NAME);
        expect(bundleContext.getBundles()).andReturn(new Bundle[] { bundle });
        expect(bundleContext.getBundle(0)).andReturn(bundle); // system bundle is also our mockup

        replay(bundle, bundleContext);

        GogoClassLoadingCommand command = new GogoClassLoadingCommand(bundleContext);
        command.clload(CLASS_NAME);
        String output = getOutput();

        assertTrue("Command output [" + output + "] does not contain class name [" + CLASS_NAME + "]", output.contains("" + CLASS_NAME));
        assertTrue("Command output [" + output + "] does not contain class package [" + CLASS_PACKAGE + "]", output.contains("" + CLASS_PACKAGE));
        assertTrue("Command output [" + output + "] does not contain bundle ID [" + BUNDLE_ID + "]", output.contains("" + BUNDLE_ID));
        assertTrue("Command output [" + output + "] does not contain bundle symbolic name [" + BUNDLE_SYMBOLIC_NAME + "]",
            output.contains(BUNDLE_SYMBOLIC_NAME));

        verify(bundle, bundleContext);
    }

    @Test
    public void testClLoadWithNonExistingClass() throws Exception {
        Bundle bundle = createMock(Bundle.class);
        BundleContext bundleContext = createMock(BundleContext.class);

        expect(bundle.loadClass(CLASS_NAME)).andReturn(null);
        expect(bundleContext.getBundles()).andReturn(new Bundle[] { bundle });

        replay(bundle, bundleContext);

        GogoClassLoadingCommand command = new GogoClassLoadingCommand(bundleContext);
        command.clload(CLASS_NAME);
        String output = getOutput();

        assertTrue("Command output [" + output + "] does not contain class name [" + CLASS_NAME + "]", output.contains("" + CLASS_NAME));
        assertTrue("Command output [" + output + "] does not contain class package [" + CLASS_PACKAGE + "]", output.contains("" + CLASS_PACKAGE));
        assertFalse("Command output [" + output + "] contains bundle ID [" + BUNDLE_ID + "]", output.contains("" + BUNDLE_ID));
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

        expect((Class) bundle.loadClass(CLASS_NAME)).andReturn(GogoClassLoadingCommandTests.class);
        expect(bundle.getBundleId()).andReturn(BUNDLE_ID);
        expect(bundle.getSymbolicName()).andReturn(BUNDLE_SYMBOLIC_NAME);
        expect(bundleContext.getBundle(0)).andReturn(bundle); // system bundle is also our mockup
        expect(bundleContext.getServiceReference(PackageAdmin.class)).andReturn(packageAdminServiceReference);
        expect(bundleContext.getService(packageAdminServiceReference)).andReturn(packageAdmin);
        expect(packageAdmin.getBundles(BUNDLE_SYMBOLIC_NAME, null)).andReturn(new Bundle[] { bundle });

        replay(bundle, bundleContext, packageAdmin, packageAdminServiceReference);

        GogoClassLoadingCommand command = new GogoClassLoadingCommand(bundleContext);
        command.clload(CLASS_NAME, BUNDLE_SYMBOLIC_NAME);
        String output = getOutput();

        assertTrue("Command output [" + output + "] does not contain class name [" + CLASS_NAME + "]", output.contains("" + CLASS_NAME));
        assertTrue("Command output [" + output + "] does not contain class package [" + CLASS_PACKAGE + "]", output.contains("" + CLASS_PACKAGE));
        assertTrue("Command output [" + output + "] does not contain bundle ID [" + BUNDLE_ID + "]", output.contains("" + BUNDLE_ID));
        assertTrue("Command output [" + output + "] does not contain bundle symbolic name [" + BUNDLE_SYMBOLIC_NAME + "]",
            output.contains(BUNDLE_SYMBOLIC_NAME));

        verify(bundle, bundleContext, packageAdmin, packageAdminServiceReference);
    }

    @SuppressWarnings("rawtypes")
    @Test
    public void testClLoadWithBundleIdAndExistingClass() throws Exception {
        Bundle bundle = createMock(Bundle.class);
        BundleContext bundleContext = createMock(BundleContext.class);

        expect((Class) bundle.loadClass(CLASS_NAME)).andReturn(GogoClassLoadingCommandTests.class);
        expect(bundle.getBundleId()).andReturn(BUNDLE_ID);
        expect(bundle.getSymbolicName()).andReturn(BUNDLE_SYMBOLIC_NAME);
        expect(bundleContext.getBundle(0)).andReturn(bundle); // system bundle is also our mockup
        expect(bundleContext.getBundle(BUNDLE_ID)).andReturn(bundle);

        replay(bundle, bundleContext);

        GogoClassLoadingCommand command = new GogoClassLoadingCommand(bundleContext);
        command.clload(CLASS_NAME, BUNDLE_ID);
        String output = getOutput();

        assertTrue("Command output [" + output + "] does not contain class name [" + CLASS_NAME + "]", output.contains("" + CLASS_NAME));
        assertTrue("Command output [" + output + "] does not contain class package [" + CLASS_PACKAGE + "]", output.contains("" + CLASS_PACKAGE));
        assertTrue("Command output [" + output + "] does not contain bundle ID [" + BUNDLE_ID + "]", output.contains("" + BUNDLE_ID));
        assertTrue("Command output [" + output + "] does not contain bundle symbolic name [" + BUNDLE_SYMBOLIC_NAME + "]",
            output.contains(BUNDLE_SYMBOLIC_NAME));

        verify(bundle, bundleContext);
    }

    @Test
    public void testClLoadWithBundleIdAndMissingClass() throws Exception {
        Bundle bundle = createMock(Bundle.class);
        BundleContext bundleContext = createMock(BundleContext.class);

        expect(bundle.loadClass(CLASS_NAME)).andReturn(null);
        expect(bundleContext.getBundle(BUNDLE_ID)).andReturn(bundle);

        replay(bundle, bundleContext);

        GogoClassLoadingCommand command = new GogoClassLoadingCommand(bundleContext);
        command.clload(CLASS_NAME, BUNDLE_ID);
        String output = getOutput();

        assertTrue("Command output [" + output + "] does not contain class name [" + CLASS_NAME + "]", output.contains("" + CLASS_NAME));
        assertTrue("Command output [" + output + "] does not contain class package [" + CLASS_PACKAGE + "]", output.contains("" + CLASS_PACKAGE));
        assertTrue("Command output [" + output + "] does not contain bundle ID [" + BUNDLE_ID + "]", output.contains("" + BUNDLE_ID));
        assertFalse("Command output [" + output + "] contains bundle symbolic name [" + BUNDLE_SYMBOLIC_NAME + "]",
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

        expect(bundle.getBundleId()).andReturn(BUNDLE_ID);
        expect(bundleContext.getServiceReference(PlatformAdmin.class)).andReturn(platformAdminServiceReference);
        expect(bundleContext.getService(platformAdminServiceReference)).andReturn(platformAdmin);
        expect(bundleContext.getBundles()).andReturn(new Bundle[] { bundle });
        expect(bundleDescription.getSelectedExports()).andReturn(new ExportPackageDescription[] {}); // nothing exported
        expect(platformAdmin.getState(false)).andReturn(bundleState);
        expect(bundleState.getBundle(BUNDLE_ID)).andReturn(bundleDescription);

        replay(platformAdmin, platformAdminServiceReference, bundle, bundleContext, bundleState, bundleDescription);

        GogoClassLoadingCommand command = new GogoClassLoadingCommand(bundleContext);
        command.clexport(CLASS_NAME);
        String output = getOutput();

        assertTrue("Command output [" + output + "] does not contain class name [" + CLASS_NAME + "]", output.contains("" + CLASS_NAME));
        assertTrue("Command output [" + output + "] does not contain class package [" + CLASS_PACKAGE + "]", output.contains("" + CLASS_PACKAGE));
        assertFalse("Command output [" + output + "] contains bundle ID [" + BUNDLE_ID + "]", output.contains("" + BUNDLE_ID));
        assertFalse("Command output [" + output + "] contains bundle symbolic name [" + BUNDLE_SYMBOLIC_NAME + "]",
            output.contains(BUNDLE_SYMBOLIC_NAME));

        verify(platformAdmin, platformAdminServiceReference, bundle, bundleContext, bundleState, bundleDescription);
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

        expect(bundle.getBundleId()).andReturn(BUNDLE_ID).times(2);
        expect(bundle.getSymbolicName()).andReturn(BUNDLE_SYMBOLIC_NAME);
        expect((Class) bundle.loadClass(CLASS_NAME)).andReturn(GogoClassLoadingCommandTests.class);
        expect(bundleContext.getServiceReference(PlatformAdmin.class)).andReturn(platformAdminServiceReference);
        expect(bundleContext.getService(platformAdminServiceReference)).andReturn(platformAdmin);
        expect(bundleContext.getBundles()).andReturn(new Bundle[] { bundle });
        expect(exportPackageDescription.getName()).andReturn(CLASS_PACKAGE);
        expect(bundleDescription.getSelectedExports()).andReturn(new ExportPackageDescription[] { exportPackageDescription });
        expect(platformAdmin.getState(false)).andReturn(bundleState);
        expect(bundleState.getBundle(BUNDLE_ID)).andReturn(bundleDescription);

        replay(platformAdmin, platformAdminServiceReference, bundle, bundleContext, bundleState, bundleDescription, exportPackageDescription);

        GogoClassLoadingCommand command = new GogoClassLoadingCommand(bundleContext);
        command.clexport(CLASS_NAME);
        String output = getOutput();

        assertTrue("Command output [" + output + "] does not contain class name [" + CLASS_NAME + "]", output.contains("" + CLASS_NAME));
        assertTrue("Command output [" + output + "] does not contain class package [" + CLASS_PACKAGE + "]", output.contains("" + CLASS_PACKAGE));
        assertTrue("Command output [" + output + "] does not contain bundle ID [" + BUNDLE_ID + "]", output.contains("" + BUNDLE_ID));
        assertTrue("Command output [" + output + "] does not contain bundle symbolic name [" + BUNDLE_SYMBOLIC_NAME + "]",
            output.contains(BUNDLE_SYMBOLIC_NAME));

        verify(platformAdmin, platformAdminServiceReference, bundle, bundleContext, bundleState, bundleDescription, exportPackageDescription);
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

        expect(bundle.getBundleId()).andReturn(BUNDLE_ID).times(2);
        expect(bundle.getSymbolicName()).andReturn(BUNDLE_SYMBOLIC_NAME);
        expect(bundle.loadClass(CLASS_NAME)).andReturn(null); // class cannot be loaded
        expect(bundleContext.getServiceReference(PlatformAdmin.class)).andReturn(platformAdminServiceReference);
        expect(bundleContext.getService(platformAdminServiceReference)).andReturn(platformAdmin);
        expect(bundleContext.getBundles()).andReturn(new Bundle[] { bundle });
        expect(exportPackageDescription.getName()).andReturn(CLASS_PACKAGE);
        expect(bundleDescription.getSelectedExports()).andReturn(new ExportPackageDescription[] { exportPackageDescription });
        expect(platformAdmin.getState(false)).andReturn(bundleState);
        expect(bundleState.getBundle(BUNDLE_ID)).andReturn(bundleDescription);

        replay(platformAdmin, platformAdminServiceReference, bundle, bundleContext, bundleState, bundleDescription, exportPackageDescription);

        GogoClassLoadingCommand command = new GogoClassLoadingCommand(bundleContext);
        command.clexport(CLASS_NAME);
        String output = getOutput();

        assertTrue("Command output [" + output + "] does not contain class name [" + CLASS_NAME + "]", output.contains("" + CLASS_NAME));
        assertTrue("Command output [" + output + "] does not contain class package [" + CLASS_PACKAGE + "]", output.contains("" + CLASS_PACKAGE));
        assertTrue("Command output [" + output + "] does not contain bundle ID [" + BUNDLE_ID + "]", output.contains("" + BUNDLE_ID));
        assertTrue("Command output [" + output + "] does not contain bundle symbolic name [" + BUNDLE_SYMBOLIC_NAME + "]",
            output.contains(BUNDLE_SYMBOLIC_NAME));

        verify(platformAdmin, platformAdminServiceReference, bundle, bundleContext, bundleState, bundleDescription, exportPackageDescription);
    }

}
