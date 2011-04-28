/*******************************************************************************
 * Copyright (c) 2008, 2010 VMware Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   VMware Inc. - initial contribution
 *******************************************************************************/

package org.eclipse.virgo.kernel.model.internal.bundle;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.virgo.kernel.model.Artifact;
import org.eclipse.virgo.kernel.model.RuntimeArtifactRepository;
import org.eclipse.virgo.kernel.model.internal.DependencyDeterminer;
import org.eclipse.virgo.kernel.osgi.quasi.QuasiBundle;
import org.eclipse.virgo.kernel.osgi.quasi.QuasiExportPackage;
import org.eclipse.virgo.kernel.osgi.quasi.QuasiFramework;
import org.eclipse.virgo.kernel.osgi.quasi.QuasiFrameworkFactory;
import org.eclipse.virgo.kernel.osgi.quasi.QuasiImportPackage;
import org.eclipse.virgo.kernel.osgi.region.RegionDigraph;
import org.eclipse.virgo.kernel.serviceability.NonNull;

/**
 * Implementation of {@link DependencyDeterminer} that returns the dependents of a {@link org.osgi.framework.Bundle
 * Bundle}. The dependents consist of any bundle that has been wired to as a result of <code>Import-Package</code>.
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Threadsafe
 * 
 */
public final class BundleDependencyDeterminer implements DependencyDeterminer {

    private final QuasiFrameworkFactory quasiFrameworkFactory;

    private final RuntimeArtifactRepository artifactRepository;

    private final RegionDigraph regionDigraph;

    public BundleDependencyDeterminer(@NonNull QuasiFrameworkFactory quasiFrameworkFactory, @NonNull RuntimeArtifactRepository artifactRepository, @NonNull RegionDigraph regionDigraph) {
        this.quasiFrameworkFactory = quasiFrameworkFactory;
        this.artifactRepository = artifactRepository;
        this.regionDigraph = regionDigraph;
    }

    /**
     * {@inheritDoc}
     */
    public Set<Artifact> getDependents(Artifact rootArtifact) {
        Set<Artifact> artifacts = new HashSet<Artifact>();
        QuasiBundle rootBundle = getBundle(rootArtifact);

        if (rootBundle == null) {
            return Collections.<Artifact> emptySet();
        }

        for (QuasiImportPackage importPackage : rootBundle.getImportPackages()) {
            QuasiExportPackage provider = importPackage.getProvider();
            if (provider != null) {
                QuasiBundle bundle = provider.getExportingBundle();
                Artifact artifact = artifactRepository.getArtifact(BundleArtifact.TYPE, bundle.getSymbolicName(), bundle.getVersion(), this.regionDigraph.getRegion(bundle.getBundleId()));
//                if (artifact == null) {
//                    // If there is no matching artifact in the user region, try the kernel region.
//                    for (Artifact a : this.artifactRepository.getArtifacts()) {
//                        if (BundleArtifact.TYPE.equals(a.getType()) && bundle.getSymbolicName().equals(a.getName()) && bundle.getVersion().equals(a.getVersion())) {
//                            artifact = a;
//                            break;
//                        }
//                    }
//                }
                artifacts.add(artifact);
            }
        }

        return artifacts;
    }

    private QuasiBundle getBundle(Artifact artifact) {
        QuasiFramework framework = quasiFrameworkFactory.create();
        for (QuasiBundle bundle : framework.getBundles()) {
            if (artifact.getName().equals(bundle.getSymbolicName()) && artifact.getVersion().equals(bundle.getVersion())) {
                return bundle;
            }
        }
        return null;
    }
}
