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

import static org.junit.Assert.assertEquals;

import org.osgi.framework.BundleListener;
import org.osgi.framework.Constants;
import org.osgi.framework.FrameworkListener;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceRegistration;

/**
 * A set of assertion methods for testing the state of the Stub OSGi Framework classes. This class makes its assertions
 * using JUnit4 assertion classes.
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Threadsafe
 * 
 */
public final class OSGiAssert {

    /**
     * Asserts that this {@link StubBundleContext} in is a clean state. A clean consists of the following
     * 
     * <ul>
     * <li>0 {@link BundleListener}s</li>
     * <li>0 {@link FrameworkListener}s</li>
     * <li>0 {@link ServiceListener}s</li>
     * <li>0 {@link ServiceRegistration}s</li>
     * </ul>
     * 
     * @param bundleContext The context to assert against
     * @see #assertBundleListenerCount(StubBundleContext, int)
     * @see #assertFrameworkListenerCount(StubBundleContext, int)
     * @see #assertServiceListenerCount(StubBundleContext, int)
     * @see #assertServiceRegistrationCount(StubBundleContext, int)
     */
    public static void assertCleanState(StubBundleContext bundleContext) {
        assertBundleListenerCount(bundleContext, 0);
        assertFrameworkListenerCount(bundleContext, 0);
        assertServiceListenerCount(bundleContext, 0);
        assertServiceRegistrationCount(bundleContext, 0);
    }

    /**
     * Asserts that a number of {@link BundleListener}s are currently registered
     * 
     * @param bundleContext The context to assert against
     * @param count The number of listeners to assert
     */
    public static void assertBundleListenerCount(StubBundleContext bundleContext, int count) {
        assertEquals("Invalid number of BundleListeners", count, bundleContext.getBundleListeners().size());
    }

    /**
     * Asserts that a number of {@link FrameworkListener}s are currently registered
     * 
     * @param bundleContext The context to assert against
     * @param count The number of listeners to assert
     */
    public static void assertFrameworkListenerCount(StubBundleContext bundleContext, int count) {
        assertEquals("Invalid number of FrameworkListeners", count, bundleContext.getFrameworkListeners().size());
    }

    /**
     * Asserts that a number of {@link ServiceListener}s are currently registered
     * 
     * @param bundleContext The context to assert against
     * @param count The number of listeners to assert
     */
    public static void assertServiceListenerCount(StubBundleContext bundleContext, int count) {
        assertEquals("Invalid number of ServiceListeners", count, bundleContext.getServiceListeners().size());
    }

    /**
     * Asserts that a number of {@link ServiceRegistration}s of are currently registered
     * 
     * @param bundleContext The context to assert against
     * @param count The number of registered services to assert
     */
    public static void assertServiceRegistrationCount(StubBundleContext bundleContext, int count) {
        assertEquals("Invalid number of ServiceRegistrations", count, bundleContext.getServiceRegistrations().size());
    }

    /**
     * Asserts that a number of {@link ServiceRegistration}s of are currently registered
     * 
     * @param bundleContext The context to assert against
     * @param type The {@link Constants#OBJECTCLASS} of the services to assert
     * @param count The number of registered services to assert
     */
    public static void assertServiceRegistrationCount(StubBundleContext bundleContext, Class<?> type, int count) {
        String typeName = type.getName();
        int found = 0;
        for (ServiceRegistration<?> serviceRegistration : bundleContext.getServiceRegistrations()) {
            String[] objectClasses = (String[]) serviceRegistration.getReference().getProperty(Constants.OBJECTCLASS);
            for (String objectClass : objectClasses) {
                if (typeName.equals(objectClass)) {
                    found++;
                    break;
                }
            }
        }
        assertEquals(String.format("Invalid number of ServiceRegistrations of type %s", typeName), count, found);
    }
}
