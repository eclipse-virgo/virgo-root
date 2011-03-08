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
import org.eclipse.virgo.kernel.serviceability.NonNull;

import org.eclipse.virgo.kernel.osgi.framework.PackageAdminUtil;
import org.eclipse.virgo.kernel.osgi.quasi.QuasiBundle;
import org.eclipse.virgo.kernel.osgi.quasi.QuasiExportPackage;
import org.eclipse.virgo.kernel.osgi.quasi.QuasiFramework;
import org.eclipse.virgo.kernel.osgi.quasi.QuasiFrameworkFactory;
import org.eclipse.virgo.kernel.osgi.quasi.QuasiImportPackage;

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

    private final PackageAdminUtil packageAdminUtil;

    public BundleDependencyDeterminer(@NonNull QuasiFrameworkFactory quasiFrameworkFactory, @NonNull RuntimeArtifactRepository artifactRepository, @NonNull PackageAdminUtil packageAdminUtil) {
        this.quasiFrameworkFactory = quasiFrameworkFactory;
        this.artifactRepository = artifactRepository;
        this.packageAdminUtil = packageAdminUtil;
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
                Artifact artifact = artifactRepository.getArtifact(BundleArtifact.TYPE, bundle.getSymbolicName(), bundle.getVersion());
                if (artifact == null) {
                    artifact = new QuasiBundleArtifact(bundle, this.packageAdminUtil);
                }
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
