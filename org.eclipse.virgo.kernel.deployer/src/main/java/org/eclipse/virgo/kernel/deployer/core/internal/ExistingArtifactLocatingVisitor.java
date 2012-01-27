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

import org.eclipse.virgo.kernel.install.artifact.InstallArtifact;
import org.eclipse.virgo.kernel.serviceability.NonNull;
import org.eclipse.virgo.util.common.GraphNode;
import org.eclipse.virgo.util.common.GraphNode.DirectedAcyclicGraphVisitor;
import org.eclipse.virgo.util.osgi.manifest.VersionRange;

final class ExistingArtifactLocatingVisitor implements DirectedAcyclicGraphVisitor<InstallArtifact> {

    private final String scopeName;

    private GraphNode<InstallArtifact> foundNode = null;

    private final String type;

    private final String name;

    private final VersionRange versionRange;

    ExistingArtifactLocatingVisitor(@NonNull String type, @NonNull String name, @NonNull VersionRange versionRange, String scopeName) {
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