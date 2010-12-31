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

import org.eclipse.osgi.internal.module.ResolverBundle;
import org.eclipse.virgo.kernel.serviceability.Assert;
import org.osgi.framework.Bundle;
import org.osgi.framework.hooks.resolver.ResolverHook;
import org.osgi.framework.wiring.BundleRevision;
import org.osgi.framework.wiring.Capability;

/**
 * TODO Document RegionResolverHook
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Thread safe.
 * 
 */
final class RegionResolverHook extends RegionHookBase implements ResolverHook {

    private static final long INVALID_BUNDLE_ID = -1L;

    private final RegionPackageImportPolicy importedPackages;
    
    private final Region kernelRegion;

    RegionResolverHook(RegionMembership regionMembership, RegionPackageImportPolicy importedPackages, Collection<BundleRevision> triggers) {
        super(regionMembership);
        this.importedPackages = importedPackages;
        this.kernelRegion = getKernelRegion();
    }

    @Override
    public void filterResolvable(Collection<BundleRevision> candidates) {
    }

    private Region getRegion(BundleRevision bundleRevision) {
        Bundle bundle = bundleRevision.getBundle();
        if (bundle != null) {
            return getRegion(bundle);
        }
        Long bundleId = getBundleId(bundleRevision);
        return getRegion(bundleId);
    }

    private Long getBundleId(BundleRevision bundleRevision) {
        if (bundleRevision instanceof ResolverBundle) {
            ResolverBundle resolverBundle = (ResolverBundle) bundleRevision;
            return resolverBundle.getBundleDescription().getBundleId();
        }
        Assert.isTrue(false, "Cannot determine bundle id of BundleRevision '%s'", bundleRevision);
        return INVALID_BUNDLE_ID;
    }

    private boolean isSystemBundle(BundleRevision bundleRevision) {
        Bundle bundle = bundleRevision.getBundle();
        if (bundle != null) {
            return isSystemBundle(bundle);
        }
        return isSystemBundle(getBundleId(bundleRevision));
    }

    @Override
    public void filterSingletonCollisions(Capability singleton, Collection<Capability> collisionCandidates) {
        // Take the default behaviour.
    }

    @Override
    public void filterMatches(BundleRevision requirer, Collection<Capability> candidates) {
        Region requirerRegion = getRegion(requirer);
        if (requirerRegion != null) {
            if (!kernelRegion.equals(requirerRegion)) {
                // User region bundles can wire only to user region bundles and imported packages from the kernel
                // region. Note: the following code currently assumes there is only a single user region.
                Iterator<Capability> i = candidates.iterator();
                while (i.hasNext()) {
                    Capability c = i.next();
                    if (this.kernelRegion.equals(getRegion(c.getProviderRevision()))) {
                        String namespace = c.getNamespace();
                        if (Capability.PACKAGE_CAPABILITY.equals(namespace)) {
                            if (!this.importedPackages.isImported((String) c.getAttributes().get(Capability.PACKAGE_CAPABILITY), c.getAttributes(),
                                c.getDirectives())) {
                                i.remove();
                            }
                        } else {
                            // Filter out other capabilities such as osgi.bundle and osgi.host.
                            i.remove();
                        }
                    }
                }
            } else {
                // Kernel region bundles can wire only to kernel region bundles.
                Iterator<Capability> i = candidates.iterator();
                while (i.hasNext()) {
                    Capability c = i.next();
                    BundleRevision providerRevision = c.getProviderRevision();
                    if (!isSystemBundle(providerRevision) && !this.kernelRegion.equals(getRegion(providerRevision))) {
                        i.remove();
                    }
                }
            }
        }
    }

    @Override
    public void end() {
    }

}
