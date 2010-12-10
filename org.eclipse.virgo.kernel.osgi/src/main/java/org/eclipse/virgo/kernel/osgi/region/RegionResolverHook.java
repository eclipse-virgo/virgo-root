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

    RegionResolverHook(RegionMembership regionMembership, List<ImportedPackage> importedPackages) {
        this.regionMembership = regionMembership;
        this.importedPackages = importedPackages;
    }

    @Override
    public void filterResolvable(Collection<BundleRevision> candidates) {
        // Remove all candidates not in the region. May be too limiting, but let's see.
        Iterator<BundleRevision> i = candidates.iterator();
        while (i.hasNext()) {
            if (!isMember(i.next().getBundle())) {
                i.remove();
            }
        }
    }

    private boolean isMember(Bundle bundle) {
        return this.regionMembership.isMember(bundle);
    }

    @Override
    public void filterSingletonCollisions(Capability singleton, Collection<Capability> collisionCandidates) {
        // Take the default behaviour.
    }

    @Override
    public void filterMatches(BundleRevision requirer, Collection<Capability> candidates) {
        if (isMember(requirer.getBundle())) {
            Iterator<Capability> i = candidates.iterator();
            while (i.hasNext()) {
                Capability c = i.next();
                Bundle providerBundle = c.getProviderRevision().getBundle();
                if (!isMember(providerBundle)) {
                    String namespace = c.getNamespace();
                    // Filter out bundles that are not members of the region
                    if (Capability.BUNDLE_CAPABILITY.equals(namespace)) {
                        i.remove();
                    }
                    if (Capability.PACKAGE_CAPABILITY.equals(namespace)) {
                        if (!imported((String)c.getAttributes().get(Capability.PACKAGE_CAPABILITY), c.getAttributes(), c.getDirectives())) {
                            i.remove();
                        }
                    }
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
