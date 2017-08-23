/*******************************************************************************
 * Copyright (c) 2008, 2011 VMware Inc. and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   VMware Inc. - initial contribution
 *   EclipseSource - Bug 358442 Change InstallArtifact graph from a tree to a DAG
 *******************************************************************************/

package org.eclipse.virgo.kernel.model.internal.deployer;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.equinox.region.Region;
import org.eclipse.virgo.kernel.install.artifact.InstallArtifact;
import org.eclipse.virgo.kernel.model.Artifact;
import org.eclipse.virgo.kernel.model.RuntimeArtifactRepository;
import org.eclipse.virgo.kernel.model.internal.DependencyDeterminer;
import org.eclipse.virgo.nano.serviceability.NonNull;
import org.eclipse.virgo.util.common.GraphNode;

/**
 * Implementation of {@link DependencyDeterminer} that returns the dependent of a <code>Plan</code>. The dependents
 * consist of the artifacts specified in the plan.
 * <p />
 * This class makes the assumption that the children of a composite artifact will always be in the same region as the parent.
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Threadsafe
 * 
 */
public final class DeployerCompositeArtifactDependencyDeterminer implements DependencyDeterminer {

    private final RuntimeArtifactRepository artifactRepository;
    
    private final Region globalRegion;
    
    private final Region userRegion;

    public DeployerCompositeArtifactDependencyDeterminer(@NonNull RuntimeArtifactRepository artifactRepository, @NonNull Region userRegion, @NonNull Region globalRegion) {
        this.artifactRepository = artifactRepository;
        this.userRegion = userRegion;
        this.globalRegion = globalRegion;
    }

    /**
     * {@inheritDoc}
     */
    public Set<Artifact> getDependents(Artifact rootArtifact) {
        if (!(rootArtifact instanceof DeployerCompositeArtifact)) {
            return Collections.<Artifact> emptySet();
        }

        final Set<Artifact> dependents = new HashSet<Artifact>();
        List<GraphNode<InstallArtifact>> children = ((DeployerCompositeArtifact) rootArtifact).getInstallArtifact().getGraph().getChildren();
        for (GraphNode<InstallArtifact> child : children) {
            InstallArtifact artifact = child.getValue();
            if(artifact.getType().equalsIgnoreCase("bundle")){
                dependents.add(this.artifactRepository.getArtifact(artifact.getType(), artifact.getName(), artifact.getVersion(), this.userRegion));
            }else{
                dependents.add(this.artifactRepository.getArtifact(artifact.getType(), artifact.getName(), artifact.getVersion(), this.globalRegion));
            }
        }

        return dependents;
    }

}
