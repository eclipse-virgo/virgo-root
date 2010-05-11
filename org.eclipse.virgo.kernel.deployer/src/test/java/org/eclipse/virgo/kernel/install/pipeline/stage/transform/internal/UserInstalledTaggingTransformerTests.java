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

package org.eclipse.virgo.kernel.install.pipeline.stage.transform.internal;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import org.junit.Test;


import org.eclipse.virgo.kernel.deployer.core.DeploymentException;
import org.eclipse.virgo.kernel.install.artifact.InstallArtifact;
import org.eclipse.virgo.kernel.install.environment.InstallEnvironment;
import org.eclipse.virgo.kernel.install.pipeline.stage.transform.internal.UserInstalledTaggingTransformer;
import org.eclipse.virgo.util.common.Tree;

public class UserInstalledTaggingTransformerTests {

    private final UserInstalledTaggingTransformer transformer = new UserInstalledTaggingTransformer();

    @SuppressWarnings("unchecked")
    @Test
    public void test() throws DeploymentException {
        InstallEnvironment installEnvironment = createMock(InstallEnvironment.class);
        Tree<InstallArtifact> installTree = createMock(Tree.class);
        InstallArtifact installArtifact = createMock(InstallArtifact.class);

        expect(installTree.getValue()).andReturn(installArtifact);
        expect(installArtifact.setProperty(eq("user.installed"), eq("true"))).andReturn(null);

        replay(installEnvironment, installTree, installArtifact);

        this.transformer.transform(installTree, installEnvironment);

        verify(installEnvironment, installTree, installArtifact);
    }
}
