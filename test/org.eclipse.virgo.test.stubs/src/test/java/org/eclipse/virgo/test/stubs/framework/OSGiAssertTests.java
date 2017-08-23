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

import static org.eclipse.virgo.test.stubs.framework.OSGiAssert.assertBundleListenerCount;
import static org.eclipse.virgo.test.stubs.framework.OSGiAssert.assertCleanState;
import static org.eclipse.virgo.test.stubs.framework.OSGiAssert.assertFrameworkListenerCount;
import static org.eclipse.virgo.test.stubs.framework.OSGiAssert.assertServiceListenerCount;
import static org.eclipse.virgo.test.stubs.framework.OSGiAssert.assertServiceRegistrationCount;

import org.junit.Test;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleListener;
import org.osgi.framework.FrameworkEvent;
import org.osgi.framework.FrameworkListener;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;

import org.eclipse.virgo.test.stubs.framework.StubBundle;
import org.eclipse.virgo.test.stubs.framework.StubBundleContext;

public class OSGiAssertTests {

    private final StubBundleContext bundleContext = new StubBundleContext(new StubBundle());

    @Test
    public void testAssertCleanState() {
        assertCleanState(this.bundleContext);
    }

    @Test
    public void testAssertBundleListenerCount() {
        this.bundleContext.addBundleListener(new BundleListener() {

            public void bundleChanged(BundleEvent event) {

            }
        });
        assertBundleListenerCount(this.bundleContext, 1);
    }

    @Test
    public void testAssertFrameworkListenerCount() {
        this.bundleContext.addFrameworkListener(new FrameworkListener() {

            public void frameworkEvent(FrameworkEvent event) {
            }
        });
        assertFrameworkListenerCount(this.bundleContext, 1);
    }

    @Test
    public void testAssertServiceListenerCount() {
        this.bundleContext.addServiceListener(new ServiceListener() {

            public void serviceChanged(ServiceEvent event) {
            }
        });
        assertServiceListenerCount(this.bundleContext, 1);
    }

    @Test
    public void testAssertServiceRegistrationCount() {
        this.bundleContext.registerService(Object.class.getName(), new Object(), null);
        this.bundleContext.registerService(Exception.class.getName(), new Object(), null);
        assertServiceRegistrationCount(this.bundleContext, 2);
    }

    @Test
    public void testAssertServiceRegistrationCountType() {
        this.bundleContext.registerService(Object.class.getName(), new Object(), null);
        this.bundleContext.registerService(Exception.class.getName(), new Object(), null);
        assertServiceRegistrationCount(this.bundleContext, Object.class, 1);
    }

}
