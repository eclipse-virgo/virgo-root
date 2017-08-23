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
package org.eclipse.virgo.test.stubs.framework;

import static org.eclipse.virgo.test.stubs.AdditionalAsserts.assertContains;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

import org.junit.Test;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleException;
import org.osgi.framework.BundleListener;
import org.osgi.framework.Version;
import org.osgi.framework.startlevel.BundleStartLevel;

public class StubBundleTests {

    private static final Long DEFAULT_BUNDLE_ID = Long.valueOf(1);

    private static final String DEFAULT_SYMBOLIC_NAME = "org.eclipse.virgo.test.stubs.testbundle";

    private static final Version DEFAULT_VERSION = Version.emptyVersion;

    private static final String DEFAULT_LOCATION = "/";

    private StubBundle bundle = new StubBundle();

    @Test
    public void initialState() throws IOException {
        assertEquals(Bundle.STARTING, this.bundle.getState());
        assertNotNull(this.bundle.getBundleContext());
        assertEquals(0, this.bundle.getHeaders().size());
        assertEquals(0, this.bundle.getHeaders("testLocale").size());
        assertNull(this.bundle.getEntry("testPath"));
        assertNull(this.bundle.getEntryPaths("testPath"));
        assertTrue(this.bundle.hasPermission("testPermission"));
        assertNull(this.bundle.getResource("testName"));
        assertNull(this.bundle.getResources("testName"));
        assertNull(this.bundle.findEntries("testPath", "testFilePattern", true));
        assertNull(this.bundle.getRegisteredServices());
        assertNull(this.bundle.getServicesInUse());

        try {
            this.bundle.loadClass("testClass");
            fail();
        } catch (ClassNotFoundException e) {

        }
    }

    @Test
    public void defaultIdNameVersionLocation() {
        assertEquals(DEFAULT_BUNDLE_ID.longValue(), this.bundle.getBundleId());
        assertEquals(DEFAULT_SYMBOLIC_NAME, this.bundle.getSymbolicName());
        assertEquals(DEFAULT_VERSION, this.bundle.getVersion());
        assertEquals(DEFAULT_LOCATION, this.bundle.getLocation());
    }

    @Test
    public void nameVersion() {
        StubBundle b1 = new StubBundle("testSymbolicName", new Version(1, 0, 0));
        assertEquals(DEFAULT_BUNDLE_ID.longValue(), this.bundle.getBundleId());
        assertEquals("testSymbolicName", b1.getSymbolicName());
        assertEquals(new Version(1, 0, 0), b1.getVersion());
        assertEquals(DEFAULT_LOCATION, this.bundle.getLocation());
    }

    @Test
    public void idNameVersionLocation() {
        StubBundle b1 = new StubBundle(2L, "testSymbolicName", new Version(1, 0, 0), "testLocation");
        assertEquals(2L, b1.getBundleId());
        assertEquals("testSymbolicName", b1.getSymbolicName());
        assertEquals(new Version(1, 0, 0), b1.getVersion());
        assertEquals("testLocation", b1.getLocation());
    }

    @Test
    public void getCustomHeader() {
        this.bundle.addHeader("testKey", "testValue");
        assertEquals("testValue", this.bundle.getHeaders().get("testKey"));
    }

    @Test
    public void getCustomLocalizedHeaders() {
        Dictionary<String, String> testHeaders = new Hashtable<>();
        this.bundle.setLocalizedHeaders(testHeaders);
        assertEquals(testHeaders, this.bundle.getHeaders("testLocale"));
    }

    @Test
    public void getLastModified() throws BundleException {
        Long lastModified = this.bundle.getLastModified();
        this.bundle.update();
        assertTrue(lastModified < this.bundle.getLastModified());
    }

    @Test(expected = ClassNotFoundException.class)
    public void loadClassNonExistent() throws ClassNotFoundException {
        this.bundle.loadClass("testClass");
    }

    @Test(expected = IllegalStateException.class)
    public void loadClassUninstalled() throws BundleException, ClassNotFoundException {
        this.bundle.addLoadClass("testClass", this.getClass());
        this.bundle.uninstall();
        this.bundle.loadClass("testClass");
    }

    @Test
    public void loadClass() throws ClassNotFoundException {
        this.bundle.addLoadClass("testClass", this.getClass());
        assertNotNull(this.bundle.loadClass("testClass"));
    }

    @Test(expected = IllegalStateException.class)
    public void getEntryUninstalled() throws BundleException, MalformedURLException {
        this.bundle.addEntry("testPath", new URL("http://localhost"));
        this.bundle.uninstall();
        this.bundle.getEntry("testPath");
    }

    @Test
    public void getEntry() throws MalformedURLException {
        assertNull(this.bundle.getEntry("testPath"));
        this.bundle.addEntry("testPath", new URL("http://localhost"));
        assertNotNull(this.bundle.getEntry("testPath"));
    }

    @Test(expected = IllegalStateException.class)
    public void getEntryPathsUninstalled() throws BundleException, MalformedURLException {
        this.bundle.addEntryPaths("testPath", new TestEnumeration<String>());
        this.bundle.uninstall();
        this.bundle.getEntryPaths("testPath");
    }

    @Test
    public void getEntryPaths() throws MalformedURLException {
        assertNull(this.bundle.getEntryPaths("testPath"));
        this.bundle.addEntryPaths("testPath", new TestEnumeration<String>());
        assertNotNull(this.bundle.getEntryPaths("testPath"));
    }

    @Test(expected = IllegalStateException.class)
    public void hasPermissionUninstalled() throws BundleException, MalformedURLException {
        this.bundle.addPermission("testPermission", false);
        this.bundle.uninstall();
        this.bundle.hasPermission("testPermission");
    }

    @Test
    public void hasPermission() throws MalformedURLException {
        assertTrue(this.bundle.hasPermission("testPermission"));
        this.bundle.addPermission("testPermission", false);
        assertFalse(this.bundle.hasPermission("testPermission"));
    }

    @Test(expected = IllegalStateException.class)
    public void getResourceUninstalled() throws BundleException, MalformedURLException {
        this.bundle.addResource("testName", new URL("http://localhost"));
        this.bundle.uninstall();
        this.bundle.getEntry("testName");
    }

    @Test
    public void getResource() throws MalformedURLException {
        assertNull(this.bundle.getResource("testName"));
        this.bundle.addEntry("testName", new URL("http://localhost"));
        assertNotNull(this.bundle.getEntry("testName"));
    }

    @Test(expected = IllegalStateException.class)
    public void getResourcesUninstalled() throws BundleException, IOException {
        this.bundle.addResources("testName", new TestEnumeration<URL>());
        this.bundle.uninstall();
        this.bundle.getResources("testName");
    }

    @Test
    public void getResources() throws IOException {
        assertNull(this.bundle.getResources("testName"));
        this.bundle.addResources("testName", new TestEnumeration<URL>());
        assertNotNull(this.bundle.getResources("testName"));
    }

    @Test
    public void findEntriesNoDelegate() {
        assertNull(this.bundle.findEntries("testPath", "testFilePattern", true));
    }

    @Test
    public void findEntries() {
        this.bundle.setFindEntriesDelegate(new TestFindEntriesDelegate());
        assertNotNull(this.bundle.findEntries("testPath", "testFilePattern", true));
    }

    @Test()
    public void getRegisteredServicesEmpty() {
        assertNull(this.bundle.getRegisteredServices());
    }

    @Test(expected = IllegalStateException.class)
    public void getRegisteredServicesUninstalled() throws BundleException {
        this.bundle.addRegisteredService(new StubServiceReference<Object>(new StubServiceRegistration<Object>(new StubBundleContext(this.bundle))));
        this.bundle.uninstall();
        this.bundle.getRegisteredServices();
    }

    @Test
    public void getRegisteredServices() {
        this.bundle.addRegisteredService(new StubServiceReference<Object>(new StubServiceRegistration<Object>(new StubBundleContext(this.bundle))));
        assertNotNull(this.bundle.getRegisteredServices());
    }

    @Test()
    public void getServicesInUseEmpty() {
        assertNull(this.bundle.getServicesInUse());
    }

    @Test(expected = IllegalStateException.class)
    public void getServicesInUseUninstalled() throws BundleException {
        this.bundle.addServiceInUse(new StubServiceReference<Object>(new StubServiceRegistration<Object>(new StubBundleContext(this.bundle))));
        this.bundle.uninstall();
        this.bundle.getServicesInUse();
    }

    @Test
    public void getServicesInUse() {
        this.bundle.addServiceInUse(new StubServiceReference<Object>(new StubServiceRegistration<Object>(new StubBundleContext(this.bundle))));
        assertNotNull(this.bundle.getServicesInUse());
    }

    @Test(expected = IllegalStateException.class)
    public void updateUninstall() throws BundleException {
        this.bundle.uninstall();
        this.bundle.update();
    }

    @Test
    public void updateNoDelegate() throws BundleException {
        this.bundle.setState(Bundle.STARTING);
        this.bundle.update();
        assertEquals(Bundle.INSTALLED, this.bundle.getState());
    }

    @Test
    public void updateInputStreamNoDelegate() throws BundleException {
        this.bundle.setState(Bundle.STARTING);
        this.bundle.update(new TestInputStream());
        assertEquals(Bundle.INSTALLED, this.bundle.getState());
    }

    @Test
    public void updateActive() throws BundleException {
        this.bundle.setState(Bundle.ACTIVE);
        this.bundle.update();
        assertEquals(Bundle.ACTIVE, this.bundle.getState());
    }

    @Test
    public void updateStarting() throws BundleException {
        this.bundle.setState(Bundle.STARTING);
        this.bundle.update();
        assertEquals(Bundle.INSTALLED, this.bundle.getState());
    }

    @Test
    public void updateStopping() throws BundleException {
        this.bundle.setState(Bundle.STOPPING);
        this.bundle.update();
        assertEquals(Bundle.INSTALLED, this.bundle.getState());

    }

    @Test
    public void updateInstalled() throws BundleException {
        this.bundle.setState(Bundle.INSTALLED);
        this.bundle.update();
        assertEquals(Bundle.INSTALLED, this.bundle.getState());
    }

    @Test
    public void updateResolved() throws BundleException {
        this.bundle.setState(Bundle.RESOLVED);
        this.bundle.update();
        assertEquals(Bundle.INSTALLED, this.bundle.getState());
    }

    @Test
    public void update() throws BundleException {
        TestBundleListener listener = new TestBundleListener();
        this.bundle.getBundleContext().addBundleListener(listener);
        TestUpdateDelegate delegate = new TestUpdateDelegate();
        this.bundle.setUpdateDelegate(delegate);
        this.bundle.update();
        assertTrue(listener.getCalled());
        assertEquals(BundleEvent.UPDATED, listener.getEvents().get(0).getType());
        assertTrue(delegate.updateCalled);
    }

    @Test(expected = IllegalStateException.class)
    public void uninstallUninstall() throws BundleException {
        this.bundle.uninstall();
        assertEquals(Bundle.UNINSTALLED, this.bundle.getState());
        this.bundle.uninstall();
    }

    @Test
    public void uninstallActive() throws BundleException {
        this.bundle.setState(Bundle.ACTIVE);
        this.bundle.uninstall();
        assertEquals(Bundle.UNINSTALLED, this.bundle.getState());
    }

    @Test
    public void uninstallStarting() throws BundleException {
        this.bundle.setState(Bundle.STARTING);
        this.bundle.uninstall();
        assertEquals(Bundle.UNINSTALLED, this.bundle.getState());
    }

    @Test
    public void uninstallStopping() throws BundleException {
        this.bundle.setState(Bundle.STOPPING);
        this.bundle.uninstall();
        assertEquals(Bundle.UNINSTALLED, this.bundle.getState());
    }

    @Test
    public void uninstallInstalled() throws BundleException {
        this.bundle.setState(Bundle.INSTALLED);
        this.bundle.uninstall();
        assertEquals(Bundle.UNINSTALLED, this.bundle.getState());
    }

    @Test
    public void uninstallResolved() throws BundleException {
        this.bundle.setState(Bundle.RESOLVED);
        this.bundle.uninstall();
        assertEquals(Bundle.UNINSTALLED, this.bundle.getState());
    }

    @Test
    public void uninstall() throws BundleException {
        TestBundleListener listener = new TestBundleListener();
        this.bundle.getBundleContext().addBundleListener(listener);
        this.bundle.uninstall();
        assertTrue(listener.getCalled());
        assertEquals(BundleEvent.UNINSTALLED, listener.getEvents().get(0).getType());
    }

    @Test
    public void stopNotActive() throws BundleException {
        TestBundleListener listener = new TestBundleListener();
        this.bundle.getBundleContext().addBundleListener(listener);
        this.bundle.stop();
        assertEquals(Bundle.STARTING, this.bundle.getState());
        assertFalse(listener.getCalled());
    }

    @Test
    public void stop() throws BundleException {
        TestBundleListener listener = new TestBundleListener();
        this.bundle.getBundleContext().addBundleListener(listener);
        StubServiceReference<Object> reference = new StubServiceReference<Object>(new StubServiceRegistration<Object>(new StubBundleContext(this.bundle)));
        reference.setBundle(this.bundle);
        this.bundle.addRegisteredService(reference);
        reference.addUsingBundles(this.bundle);
        this.bundle.addServiceInUse(reference);
        this.bundle.setState(Bundle.ACTIVE);

        this.bundle.stop();

        assertEquals(Bundle.RESOLVED, this.bundle.getState());
        assertNull(reference.getBundle());
        assertNull(this.bundle.getRegisteredServices());
        assertNull(reference.getUsingBundles());
        assertNull(this.bundle.getServicesInUse());
        assertTrue(listener.getCalled());
        assertEquals(BundleEvent.STOPPING, listener.getEvents().get(0).getType());
        assertEquals(BundleEvent.STOPPED, listener.getEvents().get(1).getType());
    }

    @Test
    public void startActive() throws BundleException {
        TestBundleListener listener = new TestBundleListener();
        this.bundle.getBundleContext().addBundleListener(listener);
        this.bundle.setState(Bundle.ACTIVE);
        this.bundle.start();
        assertEquals(Bundle.ACTIVE, this.bundle.getState());
        assertFalse(listener.getCalled());
    }

    @Test
    public void start() throws BundleException {
        TestBundleListener listener = new TestBundleListener();
        this.bundle.getBundleContext().addBundleListener(listener);

        this.bundle.start();

        assertEquals(Bundle.ACTIVE, this.bundle.getState());
        assertTrue(listener.getCalled());
        assertEquals(BundleEvent.STARTING, listener.getEvents().get(0).getType());
        assertEquals(BundleEvent.STARTED, listener.getEvents().get(1).getType());
    }

    @Test
    public void customBundleContext() {
        StubBundleContext bundleContext = new StubBundleContext(this.bundle);
        this.bundle.setBundleContext(bundleContext);
        assertSame(bundleContext, this.bundle.getBundleContext());
    }

    @Test
    public void testHashCode() {
        StubBundle b2 = new StubBundle();
        assertFalse(31 == b2.hashCode());
    }

    @Test
    public void testEquals() {
        assertTrue(this.bundle.equals(this.bundle));
        assertFalse(this.bundle.equals(null));
        assertFalse(this.bundle.equals(new Object()));

        assertFalse(this.bundle.equals(new StubBundle(2L, DEFAULT_SYMBOLIC_NAME, DEFAULT_VERSION, DEFAULT_LOCATION)));
        assertTrue(this.bundle.equals(new StubBundle()));
    }

    @Test
    public void testToString() {
        String toString = bundle.toString();
        assertContains("id", toString);
        assertContains("symbolic name", toString);
        assertContains("version", toString);
        assertContains("state", toString);
    }

    @Test
    public void compareTo() {
        assertEquals(0, this.bundle.compareTo(this.bundle));
        assertEquals(0, this.bundle.compareTo(new StubBundle(DEFAULT_BUNDLE_ID, DEFAULT_SYMBOLIC_NAME, DEFAULT_VERSION, DEFAULT_LOCATION)));
        assertTrue(0 != this.bundle.compareTo(new StubBundle(2L, DEFAULT_SYMBOLIC_NAME, DEFAULT_VERSION, DEFAULT_LOCATION)));
        assertTrue(0 != this.bundle.compareTo(new StubBundle(DEFAULT_BUNDLE_ID, "testName", DEFAULT_VERSION, DEFAULT_LOCATION)));
        assertTrue(0 != this.bundle.compareTo(new StubBundle(DEFAULT_BUNDLE_ID, DEFAULT_SYMBOLIC_NAME, new Version(1, 0, 0), DEFAULT_LOCATION)));
        assertTrue(0 != this.bundle.compareTo(new StubBundle(DEFAULT_BUNDLE_ID, DEFAULT_SYMBOLIC_NAME, DEFAULT_VERSION, "testLocation")));
        assertEquals(0, this.bundle.compareTo(new StubBundle()));
    }

    @Test(expected = NullPointerException.class)
    public void compareToNull() {
        this.bundle.compareTo(null);
    }

    @Test
    public void testGetSignerCertificates() {
        assertEquals(0, this.bundle.getSignerCertificates(0).size());
        assertEquals(0, this.bundle.getSignerCertificates(7).size());
    }

    @Test
    public void adapt() {
        assertNull(this.bundle.adapt(null));
        assertNull(this.bundle.adapt(BundleStartLevel.class));
    }

    @Test
    public void getDataFile() {
        assertNull(this.bundle.getDataFile(null));
        assertNull(this.bundle.getDataFile("testFile"));
    }

    private static final class TestInputStream extends InputStream {

        @Override
        public int read() throws IOException {
            throw new UnsupportedOperationException();
        }
    }

    private static final class TestFindEntriesDelegate implements FindEntriesDelegate {

        public Enumeration<URL> findEntries(String path, String filePattern, boolean recurse) {
            return new TestEnumeration<URL>();
        }
    }

    private static class TestUpdateDelegate implements UpdateDelegate {

        private volatile boolean updateCalled = false;

        public void update(StubBundle bundle) throws BundleException {
            this.updateCalled = true;
        }
    }

    private static class TestEnumeration<S> implements Enumeration<S> {

        public boolean hasMoreElements() {
            throw new UnsupportedOperationException();
        }

        public S nextElement() {
            throw new UnsupportedOperationException();
        }
    }

    private static class TestBundleListener implements BundleListener {

        private List<BundleEvent> events = new ArrayList<BundleEvent>();

        public void bundleChanged(BundleEvent event) {
            this.events.add(event);
        }

        public List<BundleEvent> getEvents() {
            return this.events;
        }

        public boolean getCalled() {
            return this.events.size() != 0;
        }
    }

}
