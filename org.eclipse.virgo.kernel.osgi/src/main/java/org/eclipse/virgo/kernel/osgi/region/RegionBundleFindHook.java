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

import java.util.Collection;
import java.util.Iterator;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.hooks.bundle.FindHook;

/**
 * TODO Document RegionBundleFindHook
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Thread safe.
 * 
 */
final class RegionBundleFindHook implements FindHook {

    private final RegionMembership regionMembership;

    public RegionBundleFindHook(RegionMembership regionMembership) {
        this.regionMembership = regionMembership;
    }

    @Override
    public void find(BundleContext context, Collection<Bundle> bundles) {
        boolean finderInRegion = this.regionMembership.contains(context.getBundle());
        Iterator<Bundle> i = bundles.iterator();
        while (i.hasNext()) {
            if (this.regionMembership.contains(i.next()) != finderInRegion) {
                i.remove();
            }
        }
    }

}
