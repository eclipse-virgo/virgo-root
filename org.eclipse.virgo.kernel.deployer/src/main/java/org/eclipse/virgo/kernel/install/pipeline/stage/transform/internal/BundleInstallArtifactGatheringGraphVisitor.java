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

package org.eclipse.virgo.kernel.install.pipeline.stage.transform.internal;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.virgo.kernel.install.artifact.BundleInstallArtifact;
import org.eclipse.virgo.kernel.install.artifact.InstallArtifact;
import org.eclipse.virgo.util.common.GraphNode;
import org.eclipse.virgo.util.common.GraphNode.DirectedAcyclicGraphVisitor;

public final class BundleInstallArtifactGatheringGraphVisitor implements DirectedAcyclicGraphVisitor<InstallArtifact> {

    private final Set<BundleInstallArtifact> childBundles = new HashSet<BundleInstallArtifact>();

    public boolean visit(GraphNode<InstallArtifact> graph) {
        InstallArtifact artifact = graph.getValue();

        if (artifact instanceof BundleInstallArtifact) {
            this.childBundles.add((BundleInstallArtifact) artifact);
        }

        return true;
    }

    public Set<BundleInstallArtifact> getChildBundles() {
        return this.childBundles;
    }

}
