/*******************************************************************************
 * This file is part of the Virgo Web Server.
 *
 * Copyright (c) 2010 Eclipse Foundation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SpringSource, a division of VMware - initial API and implementation and/or initial documentation
 *******************************************************************************/

package org.eclipse.virgo.kernel.osgi.region;

import org.osgi.framework.Bundle;

/**
 * {@link RegionMembership} provides a way of determining which region a bundle belongs to.
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Implementations of this interface must be thread safe.
 * 
 */
public interface RegionMembership {

    /**
     * Gets the region to which the specified bundle belongs. If there is no such unique region, throws
     * {@link RegionSpanningException}.
     * 
     * @param bundle the bundle whose region is to be gotten
     * @return the {@link Region} to which the bundle belongs
     * @throws IndeterminateRegionException if the region of the bundle cannot be determined
     */
    Region getRegion(Bundle bundle) throws IndeterminateRegionException;

    /**
     * Gets the region to which the bundle with the specified bundle id belongs. If there is no such unique region,
     * throws {@link RegionSpanningException}.
     * 
     * @param bundleId the if of the bundle whose region is to be gotten
     * @return the {@link Region} to which the bundle with the specified id belongs
     * @throws IndeterminateRegionException if the region of the bundle with the specified id cannot be determined
     */
    Region getRegion(long bundleId) throws IndeterminateRegionException;

    /**
     * Gets the kernel {@link Region}.
     * 
     * @return the kernel {@link Region}
     */
    Region getKernelRegion();

    void setUserRegion(Region userRegion);

}
