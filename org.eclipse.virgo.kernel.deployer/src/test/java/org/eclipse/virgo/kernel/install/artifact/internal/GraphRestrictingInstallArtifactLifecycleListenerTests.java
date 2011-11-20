/*******************************************************************************
 * Copyright (c) 2008, 2010 VMware Inc. and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   VMware Inc. - initial contribution (TreeRestrictingInstallArtifactLifecycleListenerTests)
 *   EclipseSource - Bug 358442 Change InstallArtifact graph from a tree to a DAG
 *******************************************************************************/

package org.eclipse.virgo.kernel.install.artifact.internal;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;

import org.eclipse.virgo.kernel.deployer.core.DeploymentException;
import org.eclipse.virgo.kernel.install.artifact.InstallArtifact;
import org.eclipse.virgo.kernel.install.artifact.InstallArtifactLifecycleListener;
import org.eclipse.virgo.kernel.install.artifact.internal.GraphRestrictingInstallArtifactLifecycleListener;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.Version;


/**
 */
public class GraphRestrictingInstallArtifactLifecycleListenerTests {

    private StubEventLogger stubEventLogger;

    private InstallArtifactLifecycleListener treeRestrictingListener;

    private InstallArtifact installArtifact1;

    private InstallArtifact installArtifact2;

    @Before
    public void setUp() throws Exception {
        this.stubEventLogger = new StubEventLogger();
        this.treeRestrictingListener = new GraphRestrictingInstallArtifactLifecycleListener(this.stubEventLogger);
        
        this.installArtifact1 = createMock(InstallArtifact.class);
        expect(this.installArtifact1.getType()).andReturn("type1").anyTimes();
        expect(this.installArtifact1.getName()).andReturn("name1").anyTimes();
        expect(this.installArtifact1.getVersion()).andReturn(Version.emptyVersion).anyTimes();
        expect(this.installArtifact1.getScopeName()).andReturn(null).anyTimes();
        
        this.installArtifact2 = createMock(InstallArtifact.class);
        expect(this.installArtifact2.getType()).andReturn("type2").anyTimes();
        expect(this.installArtifact2.getName()).andReturn("name2").anyTimes();
        expect(this.installArtifact2.getVersion()).andReturn(Version.emptyVersion).anyTimes();
        expect(this.installArtifact2.getScopeName()).andReturn(null).anyTimes();
        
        replay(this.installArtifact1, this.installArtifact2);
    }

    @Test
    public void testOnInstalling() throws DeploymentException {
        this.treeRestrictingListener.onInstalling(this.installArtifact1);
        this.treeRestrictingListener.onInstalling(this.installArtifact2);
    }

    @Test
    public void testOnInstallFailed() throws DeploymentException {
        this.treeRestrictingListener.onInstalling(this.installArtifact1);
        this.treeRestrictingListener.onInstallFailed(this.installArtifact1);
        this.treeRestrictingListener.onInstalling(this.installArtifact1);
    }

    @Test
    public void testOnUninstalled() throws DeploymentException {
        this.treeRestrictingListener.onInstalling(this.installArtifact1);
        this.treeRestrictingListener.onUninstalled(this.installArtifact1);
        this.treeRestrictingListener.onInstalling(this.installArtifact1);
    }

}
