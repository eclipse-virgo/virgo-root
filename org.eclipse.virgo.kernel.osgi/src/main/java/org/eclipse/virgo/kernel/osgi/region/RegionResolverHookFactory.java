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

import org.osgi.framework.hooks.resolver.ResolverHook;
import org.osgi.framework.hooks.resolver.ResolverHookFactory;
import org.osgi.framework.wiring.BundleRevision;

/**
 * TODO Document RegionResolverHook
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Thread safe.
 */
final class RegionResolverHookFactory implements ResolverHookFactory {

    private final RegionMembership regionMembership;

    private final RegionPackageImportPolicy regionPackageImportPolicy;

    RegionResolverHookFactory(RegionMembership regionMembership, String regionImports) {
        this.regionMembership = regionMembership;
        this.regionPackageImportPolicy = new UserRegionPackageImportPolicy(regionImports);
    }

    

    @Override
    public ResolverHook begin(Collection<BundleRevision> triggers) {
        return new RegionResolverHook(this.regionMembership, this.regionPackageImportPolicy, triggers);
    }

}
