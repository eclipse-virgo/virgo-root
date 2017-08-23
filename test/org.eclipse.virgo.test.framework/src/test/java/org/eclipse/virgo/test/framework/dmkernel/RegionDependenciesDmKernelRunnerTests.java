/*
 * This file is part of the Eclipse Virgo project.
 *
 * Copyright (c) 2011 copyright_holder
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    dsklyut - initial contribution
 */

package org.eclipse.virgo.test.framework.dmkernel;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;

import org.eclipse.virgo.test.framework.BundleEntry;
import org.eclipse.virgo.test.stubs.framework.StubBundle;
import org.eclipse.virgo.test.stubs.framework.StubBundleContext;
import org.eclipse.virgo.test.stubs.support.TrueFilter;
import org.junit.Before;
import org.junit.Test;
import org.junit.runners.model.InitializationError;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleListener;
import org.osgi.framework.Version;

/**
 * TODO Document RegionDependenciesDmKernelRunnerTests
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * TODO Document concurrent semantics of RegionDependenciesDmKernelRunnerTests
 */
public class RegionDependenciesDmKernelRunnerTests {

    @RegionBundleDependencies(entries = { @BundleEntry(value = "file:./src/test/resources/test-bundle2", autoStart = false) })
    public static class WithRegionDependenciesTest {

        @Test
        public void stubTest1() {
        }
    }

    @RegionBundleDependencies(entries = { @BundleEntry(value = "file:./src/test/resources/test-bundle1", autoStart = false) }, inheritDependencies = true)
    public static class WithRegionDependenciesAndInheritance extends WithRegionDependenciesTest {

        @Test
        public void stubTest3() {

        }
    }

    @RegionBundleDependencies(entries = { @BundleEntry("file:./src/test/resources/test-bundle1") }, inheritDependencies = false)
    public static class WithRegionDependenciesAndNoInheritance extends WithRegionDependenciesAndInheritance {

        @Test
        public void stubTest3() {

        }
    }

    private DmKernelTestRunner testRunner;

    private final StubBundleContext kernelBundleContext = new StubBundleContext();

    @Before
    public void setup() throws InitializationError {
        // this.testRunner = new DmKernelTestRunner(getClass(), 1000);
        this.kernelBundleContext.addFilter("(org.eclipse.virgo.kernel.regionContext=true)", new TrueFilter());

        // set-up user region bundle context
        StubBundleContext userRegionBundleContext = new StubBundleContext();
        
        StubBundle stubSystemBundle = new StubBundle(0L, "system-bundle", Version.emptyVersion, "system.bundle.location");
        userRegionBundleContext.addInstalledBundle(stubSystemBundle);

        Dictionary<String, Object> properties = new Hashtable<String, Object>();
        properties.put("org.eclipse.virgo.kernel.regionContext", true);
        this.kernelBundleContext.registerService(BundleContext.class.getName(), userRegionBundleContext, properties);

    }

    @Test
    public void testRegionBundleDependenciesUsage() {
        try {
            this.testRunner = new DmKernelTestRunner(WithRegionDependenciesTest.class, 1000);
        } catch (Exception ex) {
            ex.printStackTrace();
            fail(ex.getMessage());
        }
        BundleContext userRegionBundleContext = testRunner.getTargetBundleContext(kernelBundleContext);
        assertNotNull(userRegionBundleContext);
        TestBundleListener listener = new TestBundleListener();
        userRegionBundleContext.addBundleListener(listener);

        try {
            this.testRunner.postProcessTargetBundleContext(userRegionBundleContext, new Properties());
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }

        assertTrue(listener.getCalled());
        assertEquals(1, listener.getEvents().length);
        Bundle dep = listener.getEvents()[0].getBundle();
        assertEquals("file:./src/test/resources/test-bundle2", dep.getLocation());
        // autoStart == false
        assertEquals(Bundle.INSTALLED, dep.getState());
    }

    @Test
    public void testWithRegionDependenciesAndInheritance() {
        try {
            this.testRunner = new DmKernelTestRunner(WithRegionDependenciesAndInheritance.class, 1000);
        } catch (Exception ex) {
            ex.printStackTrace();
            fail(ex.getMessage());
        }
        BundleContext userRegionBundleContext = testRunner.getTargetBundleContext(kernelBundleContext);
        assertNotNull(userRegionBundleContext);
        TestBundleListener listener = new TestBundleListener();
        userRegionBundleContext.addBundleListener(listener);

        try {
            this.testRunner.postProcessTargetBundleContext(userRegionBundleContext, new Properties());
        } catch (Exception e) {
            fail(e.getMessage());
        }

        // should have 2 bundles. Both of them autoStart == false
        // provided
        assertTrue(listener.getCalled());
        assertEquals(2, listener.getEvents().length);

        for (BundleEvent e : listener.getEvents()) {
            assertEquals(Bundle.INSTALLED, e.getBundle().getState());
        }
    }

    @Test
    public void testWithRegionDependenciesAndNoInheritance() {
        try {
            this.testRunner = new DmKernelTestRunner(WithRegionDependenciesAndNoInheritance.class, 1000);
        } catch (Exception ex) {
            ex.printStackTrace();
            fail(ex.getMessage());
        }
        BundleContext userRegionBundleContext = testRunner.getTargetBundleContext(kernelBundleContext);
        assertNotNull(userRegionBundleContext);
        TestBundleListener listener = new TestBundleListener();
        userRegionBundleContext.addBundleListener(listener);

        try {
            this.testRunner.postProcessTargetBundleContext(userRegionBundleContext, new Properties());
        } catch (Exception e) {
            fail(e.getMessage());
        }

        // should have 1 bundle. and it should be started
        assertTrue(listener.getCalled());
        assertEquals(1, listener.getEvents().length);
        Bundle installedBundle = listener.getEvents()[0].getBundle();

        assertEquals(Bundle.ACTIVE, installedBundle.getState());
        assertEquals("file:./src/test/resources/test-bundle1", installedBundle.getLocation());

    }

    private static class TestBundleListener implements BundleListener {

        private boolean called = false;

        private List<BundleEvent> events = new ArrayList<BundleEvent>();

        public void bundleChanged(BundleEvent event) {
            this.called = true;
            this.events.add(event);
        }

        public boolean getCalled() {
            return called;
        }

        public BundleEvent[] getEvents() {
            return events.toArray(new BundleEvent[events.size()]);
        }

    }
}
