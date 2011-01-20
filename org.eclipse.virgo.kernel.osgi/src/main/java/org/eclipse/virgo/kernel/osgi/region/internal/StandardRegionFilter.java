/*******************************************************************************
 * Copyright (c) 2011 VMware Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   VMware Inc. - initial contribution
 *******************************************************************************/

package org.eclipse.virgo.kernel.osgi.region.internal;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.virgo.kernel.osgi.region.RegionFilter;
import org.eclipse.virgo.util.math.OrderedPair;
import org.osgi.framework.Filter;
import org.osgi.framework.Version;

/**
 * {@link StandardRegionFilter} is the default implementation of {@link RegionFilter}.
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * Thread safe.
 */
public final class StandardRegionFilter implements RegionFilter {

    private final Object monitor = new Object();

    private final Set<OrderedPair<String, Version>> allowedBundles = new HashSet<OrderedPair<String, Version>>();

    private final Set<String> allowedPackages = new HashSet<String>();

    private final Set<String> allowedPackageStems = new HashSet<String>();

    private Filter serviceFilter;

    /**
     * {@inheritDoc}
     */
    @Override
    public RegionFilter allowBundle(String bundleSymbolicName, Version bundleVersion) {
        synchronized (this.monitor) {
            this.allowedBundles.add(new OrderedPair<String, Version>(bundleSymbolicName, bundleVersion));
        }
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<OrderedPair<String, Version>> getAllowedBundles() {
        synchronized (this.monitor) {
            return Collections.unmodifiableSet(this.allowedBundles);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RegionFilter allowPackage(String packageName) {
        synchronized (this.monitor) {
            this.allowedPackages.add(packageName);
        }
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<String> getAllowedPackages() {
        synchronized (this.monitor) {
            return Collections.unmodifiableSet(this.allowedPackages);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RegionFilter allowPackageStem(String packageStem) {
        synchronized (this.monitor) {
            this.allowedPackageStems.add(packageStem);
        }
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<String> getAllowedPackageStems() {
        synchronized (this.monitor) {
            return Collections.unmodifiableSet(this.allowedPackageStems);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RegionFilter setServiceFilter(Filter serviceFilter) {
        synchronized (this.monitor) {
            this.serviceFilter = serviceFilter;
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Filter getServiceFilter() {
        synchronized (this.monitor) {
            return this.serviceFilter;
        }
    }

}
