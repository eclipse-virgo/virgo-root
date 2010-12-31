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

import org.osgi.framework.BundleContext;

/**
 * A region is a subset of an OSGi framework. Regions are isolated from each other from the perspective of package and
 * service wiring except by explicitly shared packages and services that are defined when the region is created. However
 * a bundle running in a region is not protected from discovering bundles in other regions, e.g. by following wires.
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Implementations must be thread safe.
 * 
 */
public interface Region {

    /**
     * @return the name of the region
     */
    String getName();

    /**
     * Returns a {@link BundleContext} that can be used to access the encapsulated OSGi framework.
     * 
     * @return a <code>BundleContext</code>
     */
    BundleContext getBundleContext();
    
    /**
     * Returns the {@link RegionPackageImportPolicy} associated with this region.
     * 
     * @return the {@link RegionPackageImportPolicy} of this region
     */
    RegionPackageImportPolicy getRegionPackageImportPolicy();

}
