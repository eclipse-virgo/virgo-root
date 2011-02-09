/*******************************************************************************
 * This file is part of the Virgo Web Server.
 *
 * Copyright (c) 2010 VMware Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SpringSource, a division of VMware - initial API and implementation and/or initial documentation
 *******************************************************************************/

package org.eclipse.virgo.kernel.osgi.region;

import java.util.Dictionary;
import java.util.Map;

import org.eclipse.virgo.util.osgi.VersionRange;
import org.osgi.framework.Filter;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.Version;

/**
 * A {@link RegionFilter} is associated with a connection from one region to another and determines the bundles,
 * packages, and services which are visible across the connection.
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Implementations must be thread safe.
 * 
 */
public interface RegionFilter {

    public static final RegionPackageImportPolicy ALL_PACKAGES = new RegionPackageImportPolicy() {

        @Override
        public boolean isImported(String packageName, Map<String, Object> attributes, Map<String, String> directives) {
            return true;
        }
    };

    public static final Filter ALL_SERVICES = new Filter() {

        @Override
        public boolean match(ServiceReference<?> reference) {
            return true;
        }

        @Override
        public boolean match(Dictionary<String, ?> dictionary) {
            return true;
        }

        @Override
        public boolean matchCase(Dictionary<String, ?> dictionary) {
            return true;
        }

        @Override
        public boolean matches(Map<String, ?> map) {
            return true;
        }
    };

    public static final RegionFilter TOP = new RegionFilter() {

        @Override
        public RegionFilter allowBundle(String bundleSymbolicName, VersionRange versionRange) {
            return this;
        }

        @Override
        public boolean isBundleAllowed(String bundleSymbolicName, Version bundleVersion) {
            return true;
        }

        @Override
        public RegionFilter setPackageImportPolicy(RegionPackageImportPolicy packageImportPolicy) {
            throw new UnsupportedOperationException("TOP is immutable");
        }

        @Override
        public RegionPackageImportPolicy getPackageImportPolicy() {
            return ALL_PACKAGES;
        }

        @Override
        public RegionFilter setServiceFilter(Filter serviceFilter) {
            throw new UnsupportedOperationException("TOP is immutable");
        }

        @Override
        public Filter getServiceFilter() {
            return ALL_SERVICES;
        }
    };

    /**
     * Allows bundles with the given bundle symbolic name and bundle version in the given range to be imported.
     * 
     * Note that the system bundle has the symbolic name "org.eclipse.osgi".
     * 
     * @param bundleSymbolicName
     * @param versionRange
     * @return this {@link RegionFilter} for chaining purposes
     */
    RegionFilter allowBundle(String bundleSymbolicName, VersionRange versionRange);

    /**
     * Determines whether this filter allows the bundle with the given symbolic name and version
     * 
     * @param bundleSymbolicName the symbolic name of the bundle
     * @param bundleVersion the {@link Version} of the bundle
     * @return <code>true</code> if the bundle is allowed and <code>false</code>otherwise
     */
    boolean isBundleAllowed(String bundleSymbolicName, Version bundleVersion);

    /**
     * Sets the package import policy of this filter.
     * 
     * @param packageImportPolicy
     * @return this {@link RegionFilter} for chaining purposes
     */
    RegionFilter setPackageImportPolicy(RegionPackageImportPolicy packageImportPolicy);

    /**
     * Gets the package import policy of this filter.
     * 
     * @return the package import policy or <code>null</code> if this has not been set
     */
    RegionPackageImportPolicy getPackageImportPolicy();

    /**
     * @param serviceFilter
     * @return this {@link RegionFilter} for chaining purposes
     * @see org.osgi.framework.Filter more information about service filters
     */
    RegionFilter setServiceFilter(Filter serviceFilter);

    /**
     * Gets the service filter of this filter.
     * 
     * @return the service filter or <code>null</code> if this has not been set
     */
    Filter getServiceFilter();

}
