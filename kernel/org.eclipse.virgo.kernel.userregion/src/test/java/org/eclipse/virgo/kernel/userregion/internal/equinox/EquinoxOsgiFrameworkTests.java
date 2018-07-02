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

package org.eclipse.virgo.kernel.userregion.internal.equinox;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;
import org.eclipse.virgo.kernel.osgi.framework.BundleClassLoaderUnavailableException;
import org.eclipse.virgo.kernel.osgi.framework.InstrumentableClassLoader;
import org.eclipse.virgo.kernel.userregion.internal.equinox.EquinoxOsgiFramework;
import org.eclipse.virgo.kernel.userregion.internal.equinox.KernelBundleClassLoader;

/**
 */
public class EquinoxOsgiFrameworkTests extends AbstractOsgiFrameworkLaunchingTests {

    @Override
    protected String getRepositoryConfigDirectory() {
        return new File("src/test/resources/config/EquinoxOsgiFrameworkTests").getAbsolutePath();
    }

    @Test
    public void testStartAndStop() throws Exception {
        assertNotNull(this.framework.getBundleContext());
        assertEquals(Bundle.ACTIVE, this.framework.getBundleContext().getBundle().getState());
    }

    @Test
    public void testGetClassBundle() throws Exception {
        Class<?> c = this.framework.getBundleContext().getBundle().loadClass("org.osgi.framework.Bundle");
        assertNotNull(c);
        Bundle b = this.framework.getClassBundle(c);
        assertNotNull(b);
        assertEquals(0, b.getBundleId());
    }

    @Test(expected = BundleClassLoaderUnavailableException.class)
    public void testGetClassLoaderFromUnresolved() throws Exception {
        Bundle faultyBundle = this.framework.getBundleContext().installBundle(new File("src/test/resources/EquinoxOsgiFrameworkTests/faulty").toURI().toString());
        assertEquals(Bundle.INSTALLED, faultyBundle.getState());
        this.framework.getBundleClassLoader(faultyBundle);
    }

    @Test
    public void testLoadClassAndGetClassLoader() throws Exception {
        Bundle bundle = installSpringCore(this.framework);
        assertEquals("incorrect bundle loaded", "org.springframework.core", bundle.getSymbolicName());
        Class<?> cls = bundle.loadClass("org.springframework.core.SpringVersion");
        assertNotNull(cls);
        assertTrue(cls.getClassLoader() instanceof KernelBundleClassLoader);
        assertTrue("classloader is screwed", cls.getClassLoader().toString().contains("org.springframework.core"));
    }

    @Test
    public void testAddClassFileTransformer() throws Exception {
        Bundle bundle = installSpringCore(this.framework);
        ClassLoader bundleClassLoader = this.framework.getBundleClassLoader(bundle);
        assertNotNull(bundleClassLoader);
        InstrumentableClassLoader icl = (InstrumentableClassLoader) bundleClassLoader;
        final AtomicInteger count = new AtomicInteger(0);
        icl.addClassFileTransformer(new ClassFileTransformer() {

            public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain,
                byte[] classfileBuffer) throws IllegalClassFormatException {
                count.incrementAndGet();
                return null;
            }

        });
        bundle.loadClass("org.springframework.core.SpringVersion");
        assertEquals(1, count.get());
    }

    /**
     * @param osgi
     * @return
     * @throws BundleException
     */
    private Bundle installSpringCore(EquinoxOsgiFramework osgi) throws BundleException {
        osgi.getBundleContext().installBundle("file:///" + new File(System.getProperty("user.home")
            + "/.gradle/caches/modules-2/files-2.1/org.eclipse.virgo.mirrored/org.apache.commons.logging/1.2.0/"
            + "16f574f7c054451477d7fc9d1f294e22b70a8eba/org.apache.commons.logging-1.2.0.jar").getAbsolutePath());
        osgi.getBundleContext().installBundle("file:///" + new File(System.getProperty("user.home")
            + "/.gradle/caches/modules-2/files-2.1/org.eclipse.virgo.mirrored/org.apache.commons.codec/1.10.0/"
            + "8aff50e99bd7e53f8c4f5fe45c2a63f1d47dd19c/org.apache.commons.codec-1.10.0.jar").getAbsolutePath());
        return osgi.getBundleContext().installBundle("file:///" + new File(System.getProperty("user.home")
            + "/.gradle/caches/modules-2/files-2.1/org.eclipse.virgo.mirrored/org.springframework.core/5.0.7.RELEASE"
            + "/7dbe6456d86292b3ade3ad3e6b3c1220f16e526/org.springframework.core-5.0.7.RELEASE.jar").getAbsolutePath());
    }
}
