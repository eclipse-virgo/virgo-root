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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;

/**
 * A stub testing implementation of {@link ServiceReference} as defined in section 6.1.23 of the OSGi Service Platform Core
 * Specification.
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Threadsafe
 * 
 * @param <S> type of ServiceReference
 * 
 */
public final class StubServiceReference<S> implements ServiceReference<S> {

    private static final Long DEFAULT_SERVICE_ID = Long.valueOf(1);

    private static final Integer DEFAULT_SERVICE_RANKING = Integer.valueOf(0);

    private final Long serviceId;

    private final Integer serviceRanking;

    private final StubServiceRegistration<S> serviceRegistration;

    private volatile StubBundle bundle;

    private final Object bundleMonitor = new Object();

    private final List<StubBundle> usingBundles = new ArrayList<StubBundle>();

    private final Object usingBundlesMonitor = new Object();

    private final Map<Bundle, List<String>> assignableTo = new HashMap<Bundle, List<String>>();

    private final Object assignableToMonitor = new Object();

    /**
     * Creates a new {@link StubServiceRegistration} and sets its initial state. This constructor sets
     * <code>service.id</code> to <code>1</code> and <code>service.ranking</code> to <code>0</code>.
     * 
     * @param serviceRegistration The service registration behind this {@link ServiceReference}
     */
    public StubServiceReference(StubServiceRegistration<S> serviceRegistration) {
        this(DEFAULT_SERVICE_ID, DEFAULT_SERVICE_RANKING, serviceRegistration);
    }

    /**
     * Creates a new {@link StubServiceRegistration} and sets its initial state
     * 
     * @param serviceId The service id to use
     * @param serviceRanking The service ranking to use
     * @param serviceRegistration The service registration behind this {@link ServiceReference}
     */
    public StubServiceReference(Long serviceId, Integer serviceRanking, StubServiceRegistration<S> serviceRegistration) {
        assertNotNull(serviceId, "serviceId");
        assertNotNull(serviceRanking, "serviceRanking");
        assertNotNull(serviceRegistration, "serviceRegistration");

        this.serviceId = serviceId;
        this.serviceRanking = serviceRanking;
        this.serviceRegistration = serviceRegistration;
        this.serviceRegistration.setServiceReference(this);
        this.bundle = serviceRegistration.getBundleContext().getContextBundle();
    }

    /**
     * Gets this {@link ServiceReference}'s <code>service.id</code>
     * 
     * @return This {@link ServiceReference}'s <code>service.id</code>
     */
    public Long getServiceId() {
        return this.serviceId;
    }

    /**
     * Gets this {@link ServiceReference}'s <code>service.ranking</code>
     * 
     * @return This {@link ServiceReference}'s <code>service.ranking</code>
     */
    public Integer getServiceRanking() {
        return this.serviceRanking;
    }

    /**
     * Gets this {@link ServiceReference}'s {@link ServiceRegistration}
     * 
     * @return This {@link ServiceReference}'s {@link ServiceRegistration}
     */
    public StubServiceRegistration<S> getServiceRegistration() {
        return this.serviceRegistration;
    }

    /**
     * {@inheritDoc}
     */
    public int compareTo(Object reference) {
        if (reference == null) {
            throw new IllegalArgumentException("input cannot be null");
        }

        if (!(reference instanceof StubServiceReference<?>)) {
            throw new IllegalArgumentException("input must be StubServiceReference");
        }

        StubServiceReference<?> other = (StubServiceReference<?>) reference;
        int idComparison = serviceId.compareTo(other.serviceId);
        int rankingComparison = serviceRanking.compareTo(other.serviceRanking);

        if (serviceId.equals(other.serviceId)) {
            return 0;
        } else if (rankingComparison != 0) {
            return rankingComparison;
        } else {
            return idComparison;
        }
    }

    /**
     * {@inheritDoc}
     */
    public Bundle getBundle() {
        synchronized (this.bundleMonitor) {
            return this.bundle;
        }
    }

    /**
     * Sets the {@link Bundle} to return for all subsequent calls to {@link #getBundle()}.
     * 
     * @param bundle The bundle to return
     * 
     * @return <code>this</code> instance of the {@link StubServiceReference}
     */
    public StubServiceReference<S> setBundle(StubBundle bundle) {
        synchronized (this.bundleMonitor) {
            this.bundle = bundle;
            return this;
        }
    }

    /**
     * {@inheritDoc}
     */
    public Object getProperty(String key) {
        return this.serviceRegistration.getProperties().get(key);
    }

    /**
     * {@inheritDoc}
     */
    public String[] getPropertyKeys() {
        List<String> properties = new ArrayList<String>();
        Enumeration<String> keys = this.serviceRegistration.getProperties().keys();
        while (keys.hasMoreElements()) {
            properties.add((String) keys.nextElement());
        }

        return properties.toArray(new String[properties.size()]);
    }

    /**
     * {@inheritDoc}
     */
    public Bundle[] getUsingBundles() {
        synchronized (this.usingBundlesMonitor) {
            if (this.usingBundles.isEmpty()) {
                return null;
            }
            return this.usingBundles.toArray(new Bundle[this.usingBundles.size()]);
        }
    }

    /**
     * Adds a collection of {@link Bundle}s to this {@link ServiceReference} to be returned for all subsequent calls to
     * {@link #getUsingBundles()}.
     * 
     * @param bundles The bundles to add
     * @return <code>this</code> instance of the {@link StubServiceReference}
     */
    public StubServiceReference<S> addUsingBundles(StubBundle... bundles) {
        synchronized (this.usingBundlesMonitor) {
            this.usingBundles.addAll(Arrays.asList(bundles));
            return this;
        }
    }

    /**
     * Removes a collection of {@link Bundle}s from this {@link ServiceReference} to be returned for all subsequent
     * calls to {@link #getUsingBundles()}.
     * 
     * @param bundles The bundles to remove
     * @return <code>this</code> instance of the {@link StubServiceReference}
     */
    public StubServiceReference<S> removeUsingBundles(StubBundle... bundles) {
        synchronized (this.usingBundlesMonitor) {
            this.usingBundles.removeAll(Arrays.asList(bundles));
            return this;
        }
    }

    /**
     * {@inheritDoc}
     */
    public boolean isAssignableTo(Bundle bundle, String className) {
        synchronized (this.assignableToMonitor) {
            if (!this.assignableTo.containsKey(bundle)) {
                return false;
            }
            return this.assignableTo.get(bundle).contains(className);
        }
    }

    /**
     * Adds a mapping from a {@link Bundle} to a collection of class names that the bundle is assignable to for all
     * subsequent calls to {@link #isAssignableTo(Bundle, String)}.
     * 
     * @param bundle The bundle that the class names will be assignable to
     * @param classNames The class names that this bundle is assignable from
     * @return <code>this</code> instance of the {@link StubServiceReference}
     */
    public StubServiceReference<S> putAssignableTo(Bundle bundle, String... classNames) {
        synchronized (this.assignableToMonitor) {
            this.assignableTo.put(bundle, Arrays.asList(classNames));
            return this;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + serviceRegistration.hashCode();
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        StubServiceReference<?> other = (StubServiceReference<?>) obj;

        if (!serviceRegistration.equals(other.serviceRegistration)) {
            return false;
        }
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return String.format("id: %d, ranking: %d", this.serviceId, this.serviceRanking);
    }
}
