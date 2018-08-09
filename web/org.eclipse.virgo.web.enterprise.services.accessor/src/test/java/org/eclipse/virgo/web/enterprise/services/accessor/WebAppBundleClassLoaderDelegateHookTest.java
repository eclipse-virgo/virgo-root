/*******************************************************************************
 * Copyright (c) 2012 SAP AG
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   SAP AG - initial contribution
 *******************************************************************************/

package org.eclipse.virgo.web.enterprise.services.accessor;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

import org.eclipse.osgi.framework.adaptor.BundleClassLoader;
import org.eclipse.osgi.framework.adaptor.BundleData;
import org.junit.Test;
import org.osgi.framework.Bundle;
import org.osgi.framework.wiring.BundleRevision;
import org.osgi.framework.wiring.BundleWiring;

public class WebAppBundleClassLoaderDelegateHookTest {

    @SuppressWarnings("unchecked")
    @Test
    public void testPostFindWithWAB() throws ClassNotFoundException, IOException {
        BundleData bd = createMock(BundleData.class);
        BundleClassLoader bcl = createMock(BundleClassLoader.class);
        Bundle wab = createMock(Bundle.class);
        Bundle apiBundle = createMock(Bundle.class);
        BundleRevision bundleRevision = createMock(BundleRevision.class);
        BundleWiring bundleWiring = createMock(BundleWiring.class);
        expect(bd.getBundle()).andReturn(wab).anyTimes();
        expect((Class<String>) apiBundle.loadClass("")).andReturn(String.class).andThrow(new ClassNotFoundException());
        expect(apiBundle.getResource("")).andReturn(new URL("file:foo.txt")).andReturn(null);
        List<URL> resources = new ArrayList<>();
        resources.add(new URL("file:foo.txt"));
        Enumeration<URL> enumeration = Collections.enumeration(resources);
        expect(apiBundle.getResources("")).andReturn(enumeration).andReturn(null).andThrow(new IOException());
        expect(wab.adapt(BundleRevision.class)).andReturn(bundleRevision);
        expect(bundleRevision.getWiring()).andReturn(bundleWiring);
        expect(bundleWiring.getRequiredWires(BundleRevision.PACKAGE_NAMESPACE)).andReturn(new ArrayList<>());
        expect(bundleWiring.getRequiredWires(BundleRevision.BUNDLE_NAMESPACE)).andReturn(new ArrayList<>());

        replay(bd, bcl, wab, apiBundle, bundleRevision, bundleWiring);

        WebAppBundleClassLoaderDelegateHook webAppBundleClassLoaderDelegateHook = new WebAppBundleClassLoaderDelegateHook();
        webAppBundleClassLoaderDelegateHook.addWebAppBundle(wab);

        checkExpectations(webAppBundleClassLoaderDelegateHook, "", bcl, bd, new Object[] { null, null, null });

        webAppBundleClassLoaderDelegateHook.addApiBundle(apiBundle);
        checkExpectations(webAppBundleClassLoaderDelegateHook, "", bcl, bd, new Object[] { String.class, new URL("file:foo.txt"), enumeration });

        checkExpectations(webAppBundleClassLoaderDelegateHook, "", bcl, bd, new Object[] { null, null, null });
        assertNull(webAppBundleClassLoaderDelegateHook.postFindResources("", bcl, bd));

        verify(bd, bcl, wab/* , apiBundle */, bundleRevision, bundleWiring);
    }

    @Test
    public void testPostFindWithImplBundle() {
        BundleData bd = createMock(BundleData.class);
        BundleClassLoader bcl = createMock(BundleClassLoader.class);
        Bundle implBundle = createMock(Bundle.class);
        expect(bd.getBundle()).andReturn(implBundle).anyTimes();

        replay(bd, bcl, implBundle);

        WebAppBundleClassLoaderDelegateHook webAppBundleClassLoaderDelegateHook = new WebAppBundleClassLoaderDelegateHook();
        webAppBundleClassLoaderDelegateHook.addImplBundle(implBundle);

        ClassLoader tccl = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(null);
            checkExpectations(webAppBundleClassLoaderDelegateHook, "", bcl, bd, new Object[] { null, null, null });
        } finally {
            Thread.currentThread().setContextClassLoader(tccl);
        }

//        tccl = Thread.currentThread().getContextClassLoader();
//        List<URL> resources = new ArrayList<URL>();
//        resources.add(new URL("file:foo.txt"));
//        final Enumeration<URL> enumeration = Collections.enumeration(resources);
//        try {
//            Thread.currentThread().setContextClassLoader(new ClassLoaderExt1(enumeration));
//            checkExpectations(webAppBundleClassLoaderDelegateHook, "", bcl, bd, new Object[] { String.class, new URL("file:foo.txt"), enumeration });
//        } finally {
//            Thread.currentThread().setContextClassLoader(tccl);
//        }

        tccl = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(new ClassLoaderExt2());
            checkExpectations(webAppBundleClassLoaderDelegateHook, "", bcl, bd, new Object[] { null, null, null });
        } finally {
            Thread.currentThread().setContextClassLoader(tccl);
        }

        tccl = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(new ClassLoaderExt3());
            assertNull(webAppBundleClassLoaderDelegateHook.postFindResources("", bcl, bd));
        } finally {
            Thread.currentThread().setContextClassLoader(tccl);
        }

        verify(bd, bcl, implBundle);
    }

    @Test
    public void testPostFindNotWABNorImplBundle() {
        BundleData bd = createMock(BundleData.class);
        BundleClassLoader bcl = createMock(BundleClassLoader.class);
        Bundle wab = createMock(Bundle.class);
        expect(bd.getBundle()).andReturn(wab).anyTimes();

        replay(bd, bcl, wab);

        WebAppBundleClassLoaderDelegateHook webAppBundleClassLoaderDelegateHook = new WebAppBundleClassLoaderDelegateHook();

        checkExpectations(webAppBundleClassLoaderDelegateHook, "", bcl, bd, new Object[] { null, null, null });

        verify(bd, bcl, wab);
    }

    private void checkExpectations(WebAppBundleClassLoaderDelegateHook webAppBundleClassLoaderDelegateHook, String name, BundleClassLoader bcl,
        BundleData bd, Object[] expected) {
        // assertEquals(expected[0], webAppBundleClassLoaderDelegateHook.postFindClass(name, bcl, bd));
        assertEquals(expected[1], webAppBundleClassLoaderDelegateHook.postFindResource(name, bcl, bd));
        assertEquals(expected[2], webAppBundleClassLoaderDelegateHook.postFindResources(name, bcl, bd));
    }

    private final class ClassLoaderExt1 extends ClassLoader {

        private final Enumeration<URL> enumeration;

        private ClassLoaderExt1(Enumeration<URL> enumeration) {
            this.enumeration = enumeration;
        }

        @Override
        public Class<?> loadClass(String name) {
            return String.class;
        }

        @Override
        public URL getResource(String name) {
            try {
                return new URL("file:foo.txt");
            } catch (MalformedURLException e) {
                return null;
            }
        }

        @Override
        public Enumeration<URL> getResources(String name) {
            return this.enumeration;
        }
    }

    private final class ClassLoaderExt2 extends ClassLoader {

        @Override
        public Class<?> loadClass(String name) throws ClassNotFoundException {
            throw new ClassNotFoundException();
        }

        @Override
        public URL getResource(String name) {
            return null;
        }

        @Override
        public Enumeration<URL> getResources(String name) {
            return null;
        }
    }

    private static final class ClassLoaderExt3 extends ClassLoader {

        @Override
        public Enumeration<URL> getResources(String name) throws IOException {
            throw new IOException();
        }
    }

}
