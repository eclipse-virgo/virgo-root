/*******************************************************************************
 * Copyright (c) 2011 VMware Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   VMware Inc. - initial contribution
 *******************************************************************************/

package org.eclipse.virgo.kernel.stubs;

import org.eclipse.virgo.kernel.artifact.fs.ArtifactFS;
import org.eclipse.virgo.nano.deployer.api.core.DeploymentException;
import org.eclipse.virgo.kernel.install.artifact.GraphAssociableInstallArtifact;
import org.eclipse.virgo.kernel.install.artifact.InstallArtifact;
import org.eclipse.virgo.util.common.DirectedAcyclicGraph;
import org.eclipse.virgo.util.common.GraphNode;
import org.osgi.framework.Version;

public class StubGraphAssociableInstallArtifact extends StubInstallArtifact implements GraphAssociableInstallArtifact {

    public StubGraphAssociableInstallArtifact() {
        super();
    }

    public StubGraphAssociableInstallArtifact(ArtifactFS artifactFS, String type, String name, Version version, InstallArtifact... children) {
        super(artifactFS, type, name, version, children);
    }

    public StubGraphAssociableInstallArtifact(ArtifactFS artifactFS, String type, String name, Version version, String scopeName,
        DirectedAcyclicGraph<InstallArtifact> dag, InstallArtifact... children) {
        super(artifactFS, type, name, version, scopeName, dag, children);
    }

    public StubGraphAssociableInstallArtifact(ArtifactFS artifactFS, String type, String name, Version version, String scopeName,
        InstallArtifact... children) {
        super(artifactFS, type, name, version, scopeName, children);
    }

    public StubGraphAssociableInstallArtifact(ArtifactFS artifactFS, String type, String name, Version version, String scopeName) {
        super(artifactFS, type, name, version, scopeName);
    }

    public StubGraphAssociableInstallArtifact(ArtifactFS artifactFS, String type, String name, Version version) {
        super(artifactFS, type, name, version);
    }

    public StubGraphAssociableInstallArtifact(ArtifactFS artifactFS, String name, Version version) {
        super(artifactFS, name, version);
    }

    public StubGraphAssociableInstallArtifact(String type) {
        super(type);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setGraph(GraphNode<InstallArtifact> graph) throws DeploymentException {
        throw new UnsupportedOperationException();
    }

}
