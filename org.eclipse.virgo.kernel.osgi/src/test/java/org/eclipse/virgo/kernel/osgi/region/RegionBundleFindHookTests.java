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

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.Bundle;

public class RegionBundleFindHookTests extends AbstractRegionHookTest {

    private RegionBundleFindHook regionBundleFindHook;

    private List<Bundle> bundles;

    @Before
    public void setUp() throws Exception {
        this.regionBundleFindHook = new RegionBundleFindHook(getRegionMembership());
    }

    @Test
    public void testFindBySystemBundle() {
        this.bundles = getBundles();
        this.regionBundleFindHook.find(getBundleContext(SYSTEM_BUNDLE_INDEX), this.bundles);
        assertBundlePresent(SYSTEM_BUNDLE_INDEX, KERNEL_BUNDLE_INDEX, USER_REGION_BUNDLE_INDEX);
    }

    @Test
    public void testFindByKernelBundle() {
        this.bundles = getBundles();
        this.regionBundleFindHook.find(getBundleContext(KERNEL_BUNDLE_INDEX), this.bundles);
        assertBundlePresent(SYSTEM_BUNDLE_INDEX, KERNEL_BUNDLE_INDEX);
    }

    @Test
    public void testFindByUserRegionBundle() {
        this.bundles = getBundles();
        this.regionBundleFindHook.find(getBundleContext(USER_REGION_BUNDLE_INDEX), this.bundles);
        assertBundlePresent(SYSTEM_BUNDLE_INDEX, USER_REGION_BUNDLE_INDEX);
    }

    private void assertBundlePresent(int... indices) {
        assertBundlePresent(this.bundles, indices);
    }

}
