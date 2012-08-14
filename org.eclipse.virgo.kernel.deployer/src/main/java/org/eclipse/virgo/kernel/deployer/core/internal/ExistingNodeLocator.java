/*******************************************************************************
 * Copyright (c) 2012 VMware Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   VMware Inc. - initial contribution
 *******************************************************************************/

package org.eclipse.virgo.kernel.deployer.core.internal;

import org.eclipse.virgo.kernel.deployer.model.GCRoots;
import org.eclipse.virgo.kernel.install.artifact.ArtifactIdentity;
import org.eclipse.virgo.kernel.install.artifact.InstallArtifact;
import org.eclipse.virgo.nano.serviceability.NonNull;
import org.eclipse.virgo.util.common.GraphNode;
import org.eclipse.virgo.util.common.GraphNode.DirectedAcyclicGraphVisitor;
import org.eclipse.virgo.util.osgi.manifest.VersionRange;

public final class ExistingNodeLocator implements DirectedAcyclicGraphVisitor<InstallArtifact> {

    private final String scopeName;

    private GraphNode<InstallArtifact> foundNode = null;

    private final String type;

    private final String name;

    private final VersionRange versionRange;

    /**
     * Searches the DAG from the given GC roots looking for an install artifact with the given identity and returns the
     * first one it finds or <code>null</code> if none are found.
     */
    public static GraphNode<InstallArtifact> findSharedNode(GCRoots gcRoots, ArtifactIdentity artifactIdentity) {
        ExistingNodeLocator visitor = new ExistingNodeLocator(artifactIdentity.getType(), artifactIdentity.getName(),
            VersionRange.createExactRange(artifactIdentity.getVersion()), artifactIdentity.getScopeName());
        for (InstallArtifact gcRoot : gcRoots) {
            gcRoot.getGraph().visit(visitor);
        }
        return visitor.getFoundNode();
    }

    /**
     * Searches the DAG from the given GC roots looking for an install artifact with the given type, name, and scope
     * name and with version in the given version range and returns the first one it finds or <code>null</code> if none
     * are found.
     */
    public static GraphNode<InstallArtifact> findSharedNode(GCRoots gcRoots, String type, String name, VersionRange versionRange, String scopeName) {
        ExistingNodeLocator visitor = new ExistingNodeLocator(type, name, versionRange, scopeName);
        for (InstallArtifact gcRoot : gcRoots) {
            gcRoot.getGraph().visit(visitor);
        }
        return visitor.getFoundNode();
    }

    /**
     * Searches the DAG from the given GC roots looking for an install artifact that matches the given graph node and
     * returns the first one it finds or <code>null</code> if none are found.
     */
    public static GraphNode<InstallArtifact> findSharedNode(GCRoots gcRoots, GraphNode<InstallArtifact> installGraph) {
        InstallArtifact installArtifact = installGraph.getValue();
        ArtifactIdentity artifactIdentity = new ArtifactIdentity(installArtifact.getType(), installArtifact.getName(), installArtifact.getVersion(),
            installArtifact.getScopeName());
        return findSharedNode(gcRoots, artifactIdentity);
    }

    ExistingNodeLocator(@NonNull String type, @NonNull String name, @NonNull VersionRange versionRange, String scopeName) {
        this.type = type;
        this.name = name;
        this.versionRange = versionRange;
        this.scopeName = scopeName;
    }

    @Override
    public boolean visit(GraphNode<InstallArtifact> node) {
        InstallArtifact candidate = node.getValue();
        if (this.foundNode == null && matches(candidate)) {
            this.foundNode = node;
        }
        return this.foundNode == null;
    }

    private boolean matches(InstallArtifact candidate) {
        return candidate.getType().equals(this.type)
            && candidate.getName().equals(this.name)
            && (this.scopeName == null && candidate.getScopeName() == null || this.scopeName != null
                && this.scopeName.equals(candidate.getScopeName())) && this.versionRange.includes(candidate.getVersion());
    }

    public GraphNode<InstallArtifact> getFoundNode() {
        return this.foundNode;
    }
}