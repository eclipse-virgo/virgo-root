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
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;

import java.io.File;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.virgo.kernel.userregion.internal.equinox.KernelBundleClassLoader;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.Bundle;


/**
 */
public class LoadTimeWeavingTests extends AbstractOsgiFrameworkLaunchingTests {

    private static final String DOMAIN_TYPE_NAME = "ltw.domain.DomainType";

    private static final String A_TYPE_NAME = "app.a.A";

    private static final String B_TYPE_NAME = "app.b.B";

    private Bundle emBundle;

    private Bundle includeBundle;
    
    @Override
    protected String getRepositoryConfigDirectory() {
        return new File("src/test/resources/config/LoadTimeWeavingTests").getAbsolutePath();
    }
    
    @Before
    public void setUp() throws Exception {
        super.setUp();        

        Bundle ltwDomainBundle = this.framework.getBundleContext().installBundle(new File("src/test/resources/ltw/ltw-domain.jar").toURI().toString());
        ltwDomainBundle.start();
        this.includeBundle = this.framework.getBundleContext().installBundle(new File("src/test/resources/ltw/ltw-include.jar").toURI().toString());
        this.includeBundle.start();
        this.emBundle = this.framework.getBundleContext().installBundle(new File("src/test/resources/ltw/ltw-em.jar").toURI().toString());
        this.emBundle.start();
    }

    @Test public void instrumentPackageIncludes() throws ClassNotFoundException {
        KernelBundleClassLoader incClassLoader = (KernelBundleClassLoader) this.framework.getBundleClassLoader(this.includeBundle);
        assertNotNull(incClassLoader);

        ClassLoader throwAway = incClassLoader.createThrowAway();
        assertEquals(throwAway, throwAway.loadClass(A_TYPE_NAME).getClassLoader());
        assertEquals(incClassLoader, throwAway.loadClass(B_TYPE_NAME).getClassLoader());
    }

    @Test public void throwawayAcrossBundles() throws ClassNotFoundException {
        KernelBundleClassLoader emClassLoader = (KernelBundleClassLoader) this.framework.getBundleClassLoader(this.emBundle);
        assertNotNull(emClassLoader);
        ClassLoader throwAway = emClassLoader.createThrowAway();
        Class<?> domainTypeClass = throwAway.loadClass(DOMAIN_TYPE_NAME);
        assertNotNull(domainTypeClass);
        assertNotSame(emClassLoader.loadClass(DOMAIN_TYPE_NAME), domainTypeClass);
        assertSame(domainTypeClass, throwAway.loadClass(DOMAIN_TYPE_NAME));
    }

    @Test public void weaveAcrossBundles() throws ClassNotFoundException {
        KernelBundleClassLoader emClassLoader = (KernelBundleClassLoader) this.framework.getBundleClassLoader(this.emBundle);
        assertNotNull(emClassLoader);
        final AtomicInteger counter = new AtomicInteger(0);
        emClassLoader.addClassFileTransformer(new ClassFileTransformer() {

            public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain,
                byte[] classfileBuffer) throws IllegalClassFormatException {
                counter.incrementAndGet();
                return null;
            }

        });
        Class<?> domainTypeClass = emClassLoader.loadClass(DOMAIN_TYPE_NAME);
        assertNotNull(domainTypeClass);
        assertEquals(1, counter.get());
    }

    @Test public void testRefreshWithNoPropagation() throws Exception {
        Class<?> before = this.emBundle.loadClass(DOMAIN_TYPE_NAME);
        this.framework.refresh(this.emBundle);        
        waitUntilResolved(3000);
        assertSame(this.emBundle.loadClass(DOMAIN_TYPE_NAME), before);
    }

    @Test public void testRefreshWithPropagation() throws Exception {
        KernelBundleClassLoader emClassLoader = (KernelBundleClassLoader) this.framework.getBundleClassLoader(this.emBundle);
        emClassLoader.addClassFileTransformer(new ClassFileTransformer() {

            public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain,
                byte[] classfileBuffer) throws IllegalClassFormatException {
                return null;
            }

        });
        Class<?> before = this.emBundle.loadClass(DOMAIN_TYPE_NAME);
        this.framework.refresh(this.emBundle);
        waitUntilResolved(3000);
        assertNotSame(this.emBundle.loadClass(DOMAIN_TYPE_NAME), before);
    }

    private void waitUntilResolved(int maxWaitInMillis) {
        boolean resolved = this.emBundle.getState() == Bundle.RESOLVED;
        while (!resolved && maxWaitInMillis>0) {
            try {
                Thread.sleep(50); maxWaitInMillis-=50;
            } catch (InterruptedException e) {
                continue;
            }
            resolved = this.emBundle.getState() == Bundle.RESOLVED;
        }
    }
}

