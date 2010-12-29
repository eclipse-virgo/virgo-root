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

import org.eclipse.virgo.kernel.serviceability.Assert;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

/**
 * {@link RegionHookBase} is a base hook for controlling isolation between the kernel and user regions.
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * This class is thread safe.
 * 
 */
abstract class RegionHookBase {

    private static final long SYSTEM_BUNDLE_ID = 0L;

    private final RegionMembership regionMembership;

    private final Region kernelRegion;

    RegionHookBase(RegionMembership regionMembership) {
        this.regionMembership = regionMembership;
        this.kernelRegion = regionMembership.getKernelRegion();
    }

    protected static boolean isSystemBundle(long bundleId) {
        return bundleId == SYSTEM_BUNDLE_ID;
    }

    protected static boolean isSystemBundle(Bundle bundle) {
        return isSystemBundle(bundle.getBundleId());
    }

    protected static boolean isSystemBundle(BundleContext bundleContext) {
        return isSystemBundle(bundleContext.getBundle());
    }

    protected final boolean isUserRegionBundle(long bundleId) {
        try {
            return isSystemBundle(bundleId) || !this.regionMembership.getRegion(bundleId).equals(this.kernelRegion);
        } catch (UserRegionNotInitialisedException _) {
            return true;
        } catch (IndeterminateRegionException e) {
            Assert.isTrue(false, "Unexpected exception " + e);
            return true;
        }
    }

    protected final boolean isUserRegionBundle(Bundle bundle) {
        return isUserRegionBundle(bundle.getBundleId());
    }

    protected final boolean isUserRegionBundle(BundleContext bundleContext) {
        return isUserRegionBundle(bundleContext.getBundle());
    }

}