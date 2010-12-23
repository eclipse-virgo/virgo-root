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


public class StubRegionMembership implements RegionMembership {
    
    private final long minimumBundleId;

    public StubRegionMembership(long minimumBundleId) {
        this.minimumBundleId = minimumBundleId;
    }

    @Override
    public boolean contains(Bundle bundle) {
        return contains(bundle.getBundleId());
    }

    @Override
    public boolean contains(Long bundleId) {
        return bundleId >= this.minimumBundleId || bundleId == 0L;
    }

}
