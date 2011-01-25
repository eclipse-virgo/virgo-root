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

import org.osgi.framework.Bundle;

public class StubRegionMembership implements RegionMembership {

    private final Region kernelRegion = new StubRegion("stubKernelRegion");

    private final Region userRegion = new StubRegion("stubUserRegion");

    private final long minimumUserRegionBundleId;

    public StubRegionMembership(long minimumUserRegionBundleId) {
        this.minimumUserRegionBundleId = minimumUserRegionBundleId;
    }

    @Override
    public Region getRegion(Bundle bundle) throws IndeterminateRegionException {
        return getRegion(bundle.getBundleId());
    }

    @Override
    public Region getRegion(long bundleId) throws IndeterminateRegionException {
        if (bundleId == 0L) {
            throw new RegionSpanningException(bundleId);
        }
        return bundleId >= this.minimumUserRegionBundleId ? this.userRegion : this.kernelRegion;
    }

    @Override
    public Region getKernelRegion() {
        return this.kernelRegion;
    }

    @Override
    public void setUserRegion(Region userRegion) {
    }

}
