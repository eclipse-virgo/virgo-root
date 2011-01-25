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

import java.util.Set;

import org.eclipse.virgo.util.math.OrderedPair;
import org.osgi.framework.Filter;
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

    /**
     * Allows a bundle with the given bundle symbolic name and version to be imported.
     * 
     * Note that the system bundle has the OSGi defined symbolic name "system.bundle".
     * 
     * @param bundleSymbolicName
     * @param bundleVersion
     * @return
     */
    RegionFilter allowBundle(String bundleSymbolicName, Version bundleVersion);

    /**
     * Gets the bundles allowed by this filter.
     * 
     * @return a collection of (bundle symbolic name, bundle version) pairs
     */
    Set<OrderedPair<String, Version>> getAllowedBundles();

    /**
     * Gets the package import policy of this filter.
     * 
     * @return the package import policy or <code>null</code> if this has not been set
     */
    RegionPackageImportPolicy getPackageImportPolicy();

    /**
     * Sets the package import policy of this filter.
     * 
     * @param packageImportPolicy
     * @return
     */
    RegionFilter setPackageImportPolicy(RegionPackageImportPolicy packageImportPolicy);

    /**
     * @param serviceFilter
     * @return
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
