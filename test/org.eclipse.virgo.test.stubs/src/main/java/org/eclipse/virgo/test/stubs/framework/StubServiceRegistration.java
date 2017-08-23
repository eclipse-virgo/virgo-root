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

import static org.eclipse.virgo.test.stubs.internal.Assert.assertNotNull;
import static org.eclipse.virgo.test.stubs.internal.Duplicator.shallowCopy;

import java.util.Arrays;
import java.util.Dictionary;
import java.util.Hashtable;

import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;

/**
 * A stub testing implementation of {@link ServiceRegistration} as defined in section 6.1.24 of the OSGi Service Platform Core
 * Specification.
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Threadsafe
 * 
 * @param <S> ServiceRegistration type
 * 
 */
public final class StubServiceRegistration<S> implements ServiceRegistration<S> {

    private volatile Dictionary<String, Object> properties = new Hashtable<String, Object>();

    private final Object propertiesMonitor = new Object();

    private volatile boolean unregistered = false;

    private final Object unregisteredMonitor = new Object();

    private final StubBundleContext bundleContext;

    private final String[] objectClasses;

    private volatile StubServiceReference<S> serviceReference;

    private final Object serviceReferenceMonitor = new Object();

    /**
     * Creates a new {@link StubServiceRegistration} and sets its initial state
     * 
     * @param bundleContext The bundle context that created this {@link ServiceRegistration}
     * @param objectClasses The classes that this service is registered under
     */
    public StubServiceRegistration(StubBundleContext bundleContext, String... objectClasses) {
        assertNotNull(bundleContext, "bundleContext");

        this.bundleContext = bundleContext;
        this.objectClasses = objectClasses;
        this.serviceReference = new StubServiceReference<S>(this);
        populateDefaultProperties(this.properties);
    }

    /**
     * {@inheritDoc}
     */
    public ServiceReference<S> getReference() {
        synchronized (this.serviceReferenceMonitor) {
            return this.serviceReference;
        }
    }

    /**
     * Sets the service reference to return for all subsequent calls to {@link #getReference()}.
     * 
     * @param serviceReference The service reference to return
     * 
     * @return <code>this</code> instance of the {@link StubServiceRegistration}
     */
    public StubServiceRegistration<S> setServiceReference(StubServiceReference<S> serviceReference) {
        assertNotNull(serviceReference, "serviceReference");
        synchronized (this.serviceReferenceMonitor) {
            this.serviceReference = serviceReference;

            synchronized (this.propertiesMonitor) {
                populateDefaultProperties(this.properties);
            }

            return this;
        }
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    public void setProperties(Dictionary<String, ?> properties) {
        if (properties == null) {
            return;
        }

        synchronized (this.propertiesMonitor) {
            this.properties = (Dictionary<String, Object>) properties;
            populateDefaultProperties(this.properties);
        }
    }

    /**
     * Gets the properties that were last set with a call to {@link #setProperties(Dictionary)}.
     * 
     * @return The properties last passed in with a call to {@link #setProperties(Dictionary)} or <code>null</code> if
     *         {@link #setProperties(Dictionary)} has not been called
     */
    public Dictionary<String, Object> getProperties() {
        synchronized (this.propertiesMonitor) {
            return shallowCopy(this.properties);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void unregister() {
        synchronized (this.unregisteredMonitor) {
            this.bundleContext.removeRegisteredService(this);
            this.unregistered = true;

            synchronized (this.serviceReferenceMonitor) {
                this.serviceReference.setBundle(null);
            }
        }
    }

    /**
     * Gets whether this {@link ServiceRegistration} has been unregistered with a call to {@link #unregister()}.
     * 
     * @return Whether or not this {@link StubServiceRegistration} has been unregistered
     */
    public boolean getUnregistered() {
        synchronized (this.unregisteredMonitor) {
            return this.unregistered;
        }
    }

    /**
     * Gets the {@link BundleContext} that created this registration
     * 
     * @return The {@link BundleContext} that created this registration
     */
    public StubBundleContext getBundleContext() {
        return this.bundleContext;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return String.format("object classes: %s, unregistered: %b, properties: %s", Arrays.toString(this.objectClasses), this.unregistered,
            this.properties);
    }

    private void populateDefaultProperties(Dictionary<String, Object> properties) {
        synchronized (this.serviceReferenceMonitor) {
            properties.put(Constants.SERVICE_ID, this.serviceReference.getServiceId());
            properties.put(Constants.SERVICE_RANKING, this.serviceReference.getServiceRanking());
            properties.put(Constants.OBJECTCLASS, this.objectClasses);
        }
    }
}
