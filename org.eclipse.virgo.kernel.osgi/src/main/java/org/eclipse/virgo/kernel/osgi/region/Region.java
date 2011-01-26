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

package org.eclipse.virgo.kernel.osgi.region;

import java.io.InputStream;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.Version;

/**
 * A <i>region</i> is a subset of the bundles of an OSGi framework. A regions is "weakly" isolated from other regions
 * except that is has full visibility of certain (subject to a {@link RegionFilter}) bundles, packages, and services
 * from other regions to which it is connected. However a bundle running in a region is not protected from discovering
 * bundles in other regions, e.g. by following wires using Wire Admin or similar services, so this is why regions are
 * only weakly isolated from each other.
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Implementations must be thread safe.
 * 
 */
public interface Region {

    /**
     * Returns the name of the region.
     * 
     * @return the region name
     */
    String getName();

    /**
     * Associates a given bundle, which has therefore already been installed, with this region.
     * <p>
     * This method is typically used to associate the system bundle with a region. Note that the system bundle is not
     * treated specially and in order to be fully visible in a region, it must either be associated with the region or
     * imported from another region via a connection.
     * <p>
     * If the bundle is already associated with this region, takes no action and returns normally.
     * <p>
     * If the bundle is already associated with another region, throws BundleException with exception type
     * INVALID_OPERATION.
     * <p>
     * If the bundle has the same bundle symbolic name and version as a bundle already present in the region or as a
     * bundle import specified on a connection to another region, then BundleException with exception type
     * DUPLICATE_BUNDLE_ERROR is thrown.
     * 
     * @param bundle the bundle to be associated with this region
     * @throws BundleException if the bundle cannot be associated with the region
     */
    void addBundle(Bundle bundle) throws BundleException;

    /**
     * Installs a bundle and associates the bundle with this region. The bundle's location will have the region name
     * prepended to the given location to ensure the location is unique across regions.
     * <p>
     * If the bundle has the same bundle symbolic name and version as a bundle already present in the region or as a
     * bundle import specified on a connection to another region, then BundleException with exception type
     * DUPLICATE_BUNDLE_ERROR is thrown.
     * 
     * @param location the bundle location string
     * @param input a stream of the bundle's contents or <code>null</code>
     * @return the installed Bundle
     * @throws BundleException if the install fails
     * @see BundleContext#installBundle(String, InputStream)
     */
    Bundle installBundle(String location, InputStream input) throws BundleException;

    /**
     * Installs a bundle and associates the bundle with this region. The bundle's location will have the region name
     * prepended to the given location to ensure the location is unique across regions.
     * <p>
     * If the bundle has the same bundle symbolic name and version as a bundle already present in the region or as a
     * bundle import specified on a connection to another region, then BundleException with exception type
     * DUPLICATE_BUNDLE_ERROR is thrown.
     * 
     * 
     * @param location the bundle location string
     * @return the installed Bundle
     * @throws BundleException if the install fails
     * @see BundleContext#installBundle(String)
     */
    Bundle installBundle(String location) throws BundleException;
    
    /**
     * Returns <code>true</code> if and only if the given bundle belongs to this region.
     * 
     * @param bundle a {@link Bundle}
     * @return <code>true</code> if the given bundle belongs to this region and <code>false</code> otherwise
     */
    boolean contains(Bundle bundle);

    /**
     * Returns <code>true</code> if and only if a bundle with the given bundle id belongs to this region.
     * 
     * @param bundleId a bundle id
     * @return <code>true</code> if a bundle with the given bundle id belongs to this region and <code>false</code> otherwise
     */
    boolean contains(long bundleId);
    
    /**
     * Get the bundle in this region with the given symbolic name and version.
     * 
     * @param symbolicName
     * @param version
     * @return the bundle or <code>null</code> if there is no such bundle
     */
    Bundle getBundle(String symbolicName, Version version);

    /**
     * Connects this region to the given tail region and associates the given {@link RegionFilter} with the
     * connection. This region may then, subject to the region filter, see bundles, packages, and services visible in
     * the tail region.
     * <p>
     * If the filter allows the same bundle symbolic name and version as a bundle already present in this region or a
     * filter connecting this region to a region other than the tail region, then BundleException with exception type
     * DUPLICATE_BUNDLE_ERROR is thrown.
     * <p>
     * If the given source region is already connected to the given tail region, then BundleException with exception
     * type UNSUPPORTED_OPERATION is thrown.
     * 
     * @param tailRegion the region to connect this region to
     * @param filter a {@link RegionFilter} which controls what is visible across the connection
     * @throws BundleException if the connection was not created
     */
    void connectRegion(Region tailRegion, RegionFilter filter) throws BundleException;

    /**
     * Returns a {@link BundleContext} that can be used to access the contents of the region.
     * 
     * @return a <code>BundleContext</code>
     * @deprecated This method should not appear on the API as it is hard to guarantee the availability of a suitable
     *             bundle context
     */
    BundleContext getBundleContext();

}
