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

import org.eclipse.virgo.kernel.osgi.framework.BundleClassLoaderUnavailableException;
import org.eclipse.virgo.kernel.osgi.framework.InstrumentableClassLoader;
import org.eclipse.virgo.util.osgi.BundleUtils;
import org.junit.Test;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;

import java.io.File;
import java.io.IOException;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import static org.eclipse.virgo.kernel.userregion.internal.TestUtils.*;
import static org.junit.Assert.*;

public class EquinoxOsgiFrameworkTests extends AbstractOsgiFrameworkLaunchingTests {

    @Override
    protected String getRepositoryConfigDirectory() {
        return new File("src/test/resources/config/EquinoxOsgiFrameworkTests").getAbsolutePath();
    }

    @Test
    public void bundleUtilsShouldGetPackagesExportedBySystemBundle() {
        Set<String> exportedBundles = BundleUtils.getPackagesExportedBySystemBundle(this.framework.getBundleContext().getBundle());

        assertTrue("System bundle should export 'org.osgi.framework'", exportedBundles.contains("org.osgi.framework"));
    }

    @Test
    public void testStartAndStop() {
        assertNotNull(this.framework.getBundleContext());
        assertEquals(Bundle.ACTIVE, this.framework.getBundleContext().getBundle().getState());
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
        assertEquals("incorrect bundle loaded", "oevm.org.springframework.core", bundle.getSymbolicName());
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
        icl.addClassFileTransformer((loader, className, classBeingRedefined, protectionDomain, classfileBuffer) -> {
            count.incrementAndGet();
            return null;
        });
        bundle.loadClass("org.springframework.core.SpringVersion");
        assertEquals(1, count.get());
    }

    private Bundle installSpringCore(EquinoxOsgiFramework osgi) throws BundleException, IOException {
        osgi.getBundleContext().installBundle("file:///" + fromGradleCache("slf4j-api", "slf4jVersion").getAbsolutePath());
        osgi.getBundleContext().installBundle("file:///" + fromGradleCache("slf4j-nop", "slf4jVersion").getAbsolutePath());
        osgi.getBundleContext().installBundle("file:///" + fromGradleCache("jcl-over-slf4j", "slf4jVersion").getAbsolutePath()).start();

        return osgi.getBundleContext().installBundle("file:///" + fromBndPlatform("oevm.org.springframework.core").getAbsolutePath());
    }
}
