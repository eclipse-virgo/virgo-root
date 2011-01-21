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

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.virgo.kernel.osgi.region.RegionFilter;
import org.eclipse.virgo.kernel.osgi.region.RegionPackageImportPolicy;
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

    private RegionPackageImportPolicy packageImportPolicy;

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
    public RegionFilter setServiceFilter(Filter serviceFilter) {
        synchronized (this.monitor) {
            this.serviceFilter = serviceFilter;
        }
        return this;
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

    @Override
    public RegionPackageImportPolicy getPackageImportPolicy() {
        synchronized (this.monitor) {
            return this.packageImportPolicy;
        }
    }

    @Override
    public RegionFilter setPackageImportPolicy(RegionPackageImportPolicy packageImportPolicy) {
        synchronized (this.monitor) {
            this.packageImportPolicy = packageImportPolicy;
        }
        return this;
    }

}
