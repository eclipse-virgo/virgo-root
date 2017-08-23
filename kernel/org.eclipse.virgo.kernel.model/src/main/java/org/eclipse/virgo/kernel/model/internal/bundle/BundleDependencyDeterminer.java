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
import java.util.List;
import java.util.Set;

import org.eclipse.equinox.region.RegionDigraph;
import org.eclipse.virgo.kernel.model.Artifact;
import org.eclipse.virgo.kernel.model.RuntimeArtifactRepository;
import org.eclipse.virgo.kernel.model.internal.DependencyDeterminer;
import org.eclipse.virgo.kernel.osgi.quasi.QuasiBundle;
import org.eclipse.virgo.kernel.osgi.quasi.QuasiExportPackage;
import org.eclipse.virgo.kernel.osgi.quasi.QuasiFramework;
import org.eclipse.virgo.kernel.osgi.quasi.QuasiFrameworkFactory;
import org.eclipse.virgo.kernel.osgi.quasi.QuasiImportPackage;
import org.eclipse.virgo.kernel.osgi.quasi.QuasiRequiredBundle;
import org.eclipse.virgo.nano.serviceability.NonNull;

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

    public BundleDependencyDeterminer(@NonNull QuasiFrameworkFactory quasiFrameworkFactory, @NonNull RuntimeArtifactRepository artifactRepository,
        @NonNull RegionDigraph regionDigraph) {
        this.quasiFrameworkFactory = quasiFrameworkFactory;
        this.artifactRepository = artifactRepository;
        this.regionDigraph = regionDigraph;
    }

    /**
     * {@inheritDoc}
     */
    public Set<Artifact> getDependents(Artifact rootArtifact) {
        if (!rootArtifact.getType().equalsIgnoreCase("bundle")) {
            return Collections.<Artifact> emptySet();
        }

        QuasiBundle rootBundle = getBundle(rootArtifact);
        if (rootBundle == null) {
            return Collections.<Artifact> emptySet();
        }

        Set<Artifact> artifacts = new HashSet<Artifact>();
        for (QuasiImportPackage importPackage : rootBundle.getImportPackages()) {
            QuasiExportPackage provider = importPackage.getProvider();
            if (provider != null) {
                QuasiBundle bundle = provider.getExportingBundle();
                addDependentBundle(artifacts, bundle);
            }
        }

        addDependents(artifacts, rootBundle.getHosts());
        
        List<QuasiRequiredBundle> requiredBundles = rootBundle.getRequiredBundles();
        if (requiredBundles != null) {
            for (QuasiRequiredBundle requiredBundle : requiredBundles) {
                addDependentBundle(artifacts, requiredBundle.getProvider());
            }
        }

        return artifacts;
    }

    public void addDependents(Set<Artifact> artifacts, List<QuasiBundle> dependents) {
        if (dependents != null) {
            for (QuasiBundle dependent : dependents) {
                addDependentBundle(artifacts, dependent);
            }
        }
    }

    public void addDependentBundle(Set<Artifact> artifacts, QuasiBundle bundle) {
        if (bundle != null) {
            Artifact artifact = artifactRepository.getArtifact(NativeBundleArtifact.TYPE, bundle.getSymbolicName(), bundle.getVersion(),
                this.regionDigraph.getRegion(bundle.getBundleId()));
            artifacts.add(artifact);
        }
    }

    private QuasiBundle getBundle(Artifact artifact) {
        QuasiFramework framework = quasiFrameworkFactory.create();
        for (QuasiBundle bundle : framework.getBundles()) {
            if (artifact.getName().equals(bundle.getSymbolicName()) && artifact.getVersion().equals(bundle.getVersion())
                && artifact.getRegion().getName().equals(this.regionDigraph.getRegion(bundle.getBundleId()).getName())) {
                return bundle;
            }
        }
        return null;
    }
}
