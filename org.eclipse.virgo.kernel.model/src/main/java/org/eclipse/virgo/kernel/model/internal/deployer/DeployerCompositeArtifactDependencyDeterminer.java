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

package org.eclipse.virgo.kernel.model.internal.deployer;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.virgo.kernel.install.artifact.InstallArtifact;
import org.eclipse.virgo.kernel.model.Artifact;
import org.eclipse.virgo.kernel.model.RuntimeArtifactRepository;
import org.eclipse.virgo.kernel.model.internal.DependencyDeterminer;

import org.eclipse.virgo.kernel.serviceability.NonNull;
import org.eclipse.virgo.util.common.Tree;

/**
 * Implementation of {@link DependencyDeterminer} that reutns the dependent of a <code>Plan</code>. The dependents
 * consist of the artifacts specified in the plan.
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Threadsafe
 * 
 */
public final class DeployerCompositeArtifactDependencyDeterminer implements DependencyDeterminer {

    private final RuntimeArtifactRepository artifactRepository;

    public DeployerCompositeArtifactDependencyDeterminer(@NonNull RuntimeArtifactRepository artifactRepository) {
        this.artifactRepository = artifactRepository;
    }

    /**
     * {@inheritDoc}
     */
    public Set<Artifact> getDependents(Artifact rootArtifact) {
        if (!(rootArtifact instanceof DeployerCompositeArtifact)) {
            return Collections.<Artifact> emptySet();
        }

        final Set<Artifact> dependents = new HashSet<Artifact>();
        List<Tree<InstallArtifact>> children = ((DeployerCompositeArtifact) rootArtifact).getInstallArtifact().getTree().getChildren();
        for (Tree<InstallArtifact> child : children) {
            InstallArtifact artifact = child.getValue();
            dependents.add(artifactRepository.getArtifact(artifact.getType(), artifact.getName(), artifact.getVersion(), rootArtifact.getRegion()));
        }

        return dependents;
    }

}
