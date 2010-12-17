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
import java.util.List;
import java.util.Map;

import org.eclipse.osgi.internal.module.ResolverBundle;
import org.eclipse.virgo.kernel.serviceability.Assert;
import org.eclipse.virgo.util.osgi.manifest.ImportedPackage;
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
final class RegionResolverHook implements ResolverHook {

    private final RegionMembership regionMembership;

    private final List<ImportedPackage> importedPackages;

    private final boolean triggerInRegion;

    RegionResolverHook(RegionMembership regionMembership, List<ImportedPackage> importedPackages, Collection<BundleRevision> triggers) {
        this.regionMembership = regionMembership;
        this.importedPackages = importedPackages;
        this.triggerInRegion = triggerInRegion(triggers);
    }

    private boolean triggerInRegion(Collection<BundleRevision> triggers) {
        // If there are no triggers, assume the resolution is occurring in the user region.
        if (triggers.isEmpty()) {
            return true;
        }
        Iterator<BundleRevision> i = triggers.iterator();
        while (i.hasNext()) {
            if (isMember(i.next())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void filterResolvable(Collection<BundleRevision> candidates) {
        if (!this.triggerInRegion) {
            // The trigger is in the kernel regions, so remove all candidates in the user region.
            Iterator<BundleRevision> i = candidates.iterator();
            while (i.hasNext()) {
                if (isMember(i.next())) {
                    i.remove();
                }
            }
        }
    }

    private boolean isMember(BundleRevision bundleRevision) {
        Bundle bundle = bundleRevision.getBundle();
        if (bundle != null) {
            return this.regionMembership.contains(bundle);
        }
        if (bundleRevision instanceof ResolverBundle) {
            ResolverBundle resolverBundle = (ResolverBundle)bundleRevision;
            return this.regionMembership.contains(resolverBundle.getBundleDescription().getBundleId());
        }
        Assert.isTrue(false, "Cannot determine region membership of BundleRevision '%s'", bundleRevision);
        // Assume that bundle revisions with no bundles belong to the user region.
        return true;
    }

    // private boolean isMember(Bundle bundle) {
    // return this.regionMembership.contains(bundle);
    // }

    @Override
    public void filterSingletonCollisions(Capability singleton, Collection<Capability> collisionCandidates) {
        // Take the default behaviour.
    }

    @Override
    public void filterMatches(BundleRevision requirer, Collection<Capability> candidates) {
        if (isMember(requirer)) {
            // XXX DEBUG
            // if ("org.springframework.web.portlet".equals(requirer.getSymbolicName())) {
            // Iterator<Capability> i = candidates.iterator();
            // while (i.hasNext()) {
            // Capability c = i.next();
            // if ("org.springframework.beans".equals(c.getProviderRevision().getSymbolicName())) {
            // System.out.println("here");
            // }
            // }
            // }

            // User region bundles can wire only to user region bundles and imported packages from the kernel region.
            Iterator<Capability> i = candidates.iterator();
            while (i.hasNext()) {
                Capability c = i.next();
                if (!isMember(c.getProviderRevision())) {
                    String namespace = c.getNamespace();
                    // Filter out bundles that are not members of the region
                    if (Capability.BUNDLE_CAPABILITY.equals(namespace)) {
                        i.remove();
                    }
                    if (Capability.PACKAGE_CAPABILITY.equals(namespace)) {
                        if (!imported((String) c.getAttributes().get(Capability.PACKAGE_CAPABILITY), c.getAttributes(), c.getDirectives())) {
                            i.remove();
                        }
                    }
                }
            }
        } else {
            // Kernel region bundles can wire only to kernel region bundles.
            Iterator<Capability> i = candidates.iterator();
            while (i.hasNext()) {
                Capability c = i.next();
                if (!isMember(c.getProviderRevision())) {
                    i.remove();
                }
            }
        }
    }

    private boolean imported(String packageName, Map<String, Object> attributes, Map<String, String> directives) {
        // TODO: prototype implementation - needs expanding to do complete match
        for (ImportedPackage importedPackage : this.importedPackages) {
            if (importedPackage.getPackageName().equals(packageName)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void end() {
    }

}
