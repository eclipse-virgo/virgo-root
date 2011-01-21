/*******************************************************************************
 * Copyright (c) 2011 VMware Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   VMware Inc. - initial contribution
 *******************************************************************************/

package org.eclipse.virgo.kernel.osgi.region.hook;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.eclipse.osgi.internal.module.ResolverBundle;
import org.eclipse.virgo.kernel.osgi.region.Region;
import org.eclipse.virgo.kernel.osgi.region.RegionDigraph;
import org.eclipse.virgo.kernel.osgi.region.RegionFilter;
import org.eclipse.virgo.kernel.osgi.region.RegionPackageImportPolicy;
import org.eclipse.virgo.kernel.osgi.region.RegionDigraph.FilteredRegion;
import org.eclipse.virgo.kernel.serviceability.Assert;
import org.eclipse.virgo.util.math.OrderedPair;
import org.osgi.framework.Bundle;
import org.osgi.framework.Version;
import org.osgi.framework.hooks.resolver.ResolverHook;
import org.osgi.framework.wiring.BundleRevision;
import org.osgi.framework.wiring.Capability;

/**
 * {@link RegionResolverHook} manages the visibility of bundles across regions according to the {@link RegionDigraph}.
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * Thread safe.
 */
final class RegionResolverHook implements ResolverHook {

    private static final long INVALID_BUNDLE_ID = -1L;

    private final RegionDigraph regionDigraph;

    RegionResolverHook(RegionDigraph regionDigraph) {
        this.regionDigraph = regionDigraph;
    }

    @Override
    public void filterMatches(BundleRevision requirer, Collection<Capability> candidates) {
        if (getBundleId(requirer) == 0L) {
            return;
        }

        Region requirerRegion = getRegion(requirer);
        if (requirerRegion == null) {
            candidates.clear();
            return;
        }

        Set<Capability> allowed = getAllowed(requirerRegion, candidates, new HashSet<Region>());

        candidates.retainAll(allowed);
    }

    private Region getRegion(BundleRevision bundleRevision) {
        Bundle bundle = bundleRevision.getBundle();
        if (bundle != null) {
            return getRegion(bundle);
        }
        Long bundleId = getBundleId(bundleRevision);
        return getRegion(bundleId);
    }

    private Region getRegion(Long bundleId) {
        // TODO Auto-generated method stub
        return null;
    }

    private Long getBundleId(BundleRevision bundleRevision) {
        if (bundleRevision instanceof ResolverBundle) {
            ResolverBundle resolverBundle = (ResolverBundle) bundleRevision;
            return resolverBundle.getBundleDescription().getBundleId();
        }
        Assert.isTrue(false, "Cannot determine bundle id of BundleRevision '%s'", bundleRevision);
        return INVALID_BUNDLE_ID;
    }

    private Set<Capability> getAllowed(Region r, Collection<Capability> candidates, Set<Region> path) {
        Set<Capability> allowed = new HashSet<Capability>();

        if (!path.contains(r)) {
            allowPackagesInRegion(allowed, r, candidates);
            allowImportedPackages(allowed, r, candidates, path);
        }

        return allowed;
    }

    private void allowImportedPackages(Set<Capability> allowed, Region r, Collection<Capability> candidates, Set<Region> path) {
        for (FilteredRegion fr : this.regionDigraph.getEdges(r)) {
            Set<Capability> a = getAllowed(fr.getRegion(), candidates, extendPath(r, path));
            filter(a, fr.getFilter());
            allowed.addAll(a);
        }
    }

    private void allowPackagesInRegion(Set<Capability> allowed, Region r, Collection<Capability> candidates) {
        for (Capability b : candidates) {
            if (r.equals(getRegion(b.getProviderRevision()))) {
                allowed.add(b);
            }
        }
    }

    private Set<Region> extendPath(Region r, Set<Region> path) {
        Set<Region> newPath = new HashSet<Region>(path);
        newPath.add(r);
        return newPath;
    }

    private void filter(Set<Capability> capabilities, RegionFilter filter) {
        RegionPackageImportPolicy packageImportPolicy = filter.getPackageImportPolicy();
        Set<OrderedPair<String, Version>> allowedBundles = filter.getAllowedBundles();
        Iterator<Capability> i = capabilities.iterator();
        while (i.hasNext()) {
            Capability c = i.next();
            String namespace = c.getNamespace();
            if (Capability.PACKAGE_CAPABILITY.equals(namespace)) {
                if (!packageImportPolicy.isImported((String) c.getAttributes().get(Capability.PACKAGE_CAPABILITY), c.getAttributes(),
                    c.getDirectives())) {
                    i.remove();
                }
            } else {
                if (!allowedBundle(c.getProviderRevision(), allowedBundles)) {
                    i.remove();
                }
            }
        }
    }

    private boolean allowedBundle(BundleRevision bundleRevision, Set<OrderedPair<String, Version>> allowedBundles) {
        String symbolicName = bundleRevision.getSymbolicName();
        Version version = bundleRevision.getVersion();
        for (OrderedPair<String, Version> allowedBundle : allowedBundles) {
            if (symbolicName.equals(allowedBundle.getFirst()) && version.equals(allowedBundle.getSecond())) {
                return true;
            }
        }
        return false;
    }

    private Region getRegion(Bundle bundle) {
        for (Region r : this.regionDigraph) {
            if (r.contains(bundle)) {
                return r;
            }
        }
        return null;
    }

    @Override
    public void end() {
        // TODO Auto-generated method stub

    }

    @Override
    public void filterResolvable(Collection<BundleRevision> candidates) {
    }

    @Override
    public void filterSingletonCollisions(Capability singleton, Collection<Capability> collisionCandidates) {
    }

}
