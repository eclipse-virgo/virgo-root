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
        Bundle faultyBundle = this.framework.getBundleContext().installBundle(
            new File("src/test/resources/EquinoxOsgiFrameworkTests/faulty").toURI().toString());
        assertEquals(Bundle.INSTALLED, faultyBundle.getState());
        this.framework.getBundleClassLoader(faultyBundle);
    }

    @Test
    public void testEquinoxDeclarativServices() throws Exception {
        this.framework.getBundleContext().installBundle("file:///" + new File(System.getProperty("user.home")
            + "/.gradle/caches/modules-2/files-2.1/org.eclipse.virgo.mirrored/org.eclipse.osgi.services/3.5.0.v20150519-2006/f8b39d71416901549bf27cbff5d709d25a347632/org.eclipse.osgi.services-3.5.0.v20150519-2006.jar").getAbsolutePath());
        this.framework.getBundleContext().installBundle("file:///" + new File(System.getProperty("user.home")
            + "/.gradle/caches/modules-2/files-2.1/org.eclipse.virgo.mirrored/org.eclipse.equinox.util/1.0.500.v20130404-1337/ffedd440831050fce73a848a14104028759ff9fb/org.eclipse.equinox.util-1.0.500.v20130404-1337.jar").getAbsolutePath());
        Bundle dsBundle = this.framework.getBundleContext().installBundle("file:///" + new File(System.getProperty("user.home")
            + "/.gradle/caches/modules-2/files-2.1/org.eclipse.virgo.mirrored/org.eclipse.equinox.ds/1.4.200.v20131126-2331/8306727a3fe6b9d6c2aec60f229c04edcf8268f0/org.eclipse.equinox.ds-1.4.200.v20131126-2331.jar").getAbsolutePath());
        dsBundle.start();
    }

    @Test
    public void testLoadClassAndGetClassLoader() throws Exception {
        Bundle bundle = installSpringCore(this.framework);
        assertEquals("incorrect bundle loaded", "org.springframework.core", bundle.getSymbolicName());
        Class<?> cls = bundle.loadClass("org.springframework.core.JdkVersion");
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
        bundle.loadClass("org.springframework.core.JdkVersion");
        assertEquals(1, count.get());
    }

    /**
     * @param osgi
     * @return
     * @throws BundleException
     */
    private Bundle installSpringCore(EquinoxOsgiFramework osgi) throws BundleException {
        Bundle commonsLoggingBundle = osgi.getBundleContext().installBundle("file:///" + new File(System.getProperty("user.home")
            + "/.gradle/caches/modules-2/files-2.1/org.eclipse.virgo.mirrored/org.apache.commons.logging/1.2.0/16f574f7c054451477d7fc9d1f294e22b70a8eba/org.apache.commons.logging-1.2.0.jar").getAbsolutePath());
        commonsLoggingBundle.start();
        Bundle commonsCodecBundle = osgi.getBundleContext().installBundle("file:///" + new File(System.getProperty("user.home")
            + "/.gradle/caches/modules-2/files-2.1/org.eclipse.virgo.mirrored/org.apache.commons.codec/1.10.0/8aff50e99bd7e53f8c4f5fe45c2a63f1d47dd19c/org.apache.commons.codec-1.10.0.jar").getAbsolutePath());
        commonsCodecBundle.start();
        Bundle springframeworkCoreBundle = osgi.getBundleContext().installBundle("file:///" + new File(System.getProperty("user.home")
            + "/.gradle/caches/modules-2/files-2.1/org.eclipse.virgo.mirrored/org.springframework.core/4.2.1.RELEASE/57e9cdf473ba084c6ae2b9f166ab497f48334cd8/org.springframework.core-4.2.1.RELEASE.jar").getAbsolutePath());
        springframeworkCoreBundle.start();
        return springframeworkCoreBundle;
    }
}
