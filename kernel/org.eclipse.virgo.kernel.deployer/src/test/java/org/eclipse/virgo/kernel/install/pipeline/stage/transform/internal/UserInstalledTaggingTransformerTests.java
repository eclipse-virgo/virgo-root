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

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import org.eclipse.virgo.nano.deployer.api.core.DeploymentException;
import org.eclipse.virgo.kernel.install.artifact.InstallArtifact;
import org.eclipse.virgo.kernel.install.environment.InstallEnvironment;
import org.eclipse.virgo.util.common.GraphNode;
import org.junit.Test;

public class UserInstalledTaggingTransformerTests {

    private final UserInstalledTaggingTransformer transformer = new UserInstalledTaggingTransformer();

    @SuppressWarnings("unchecked")
    @Test
    public void test() throws DeploymentException {
        InstallEnvironment installEnvironment = createMock(InstallEnvironment.class);
        GraphNode<InstallArtifact> installGraph = createMock(GraphNode.class);
        InstallArtifact installArtifact = createMock(InstallArtifact.class);

        expect(installGraph.getValue()).andReturn(installArtifact);
        expect(installArtifact.setProperty(eq("user.installed"), eq("true"))).andReturn(null);

        replay(installEnvironment, installGraph, installArtifact);

        this.transformer.transform(installGraph, installEnvironment);

        verify(installEnvironment, installGraph, installArtifact);
    }
}
