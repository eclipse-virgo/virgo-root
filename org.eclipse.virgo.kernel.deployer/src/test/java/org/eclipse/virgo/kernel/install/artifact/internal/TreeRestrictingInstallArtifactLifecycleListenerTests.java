/*******************************************************************************
 * Copyright (c) 2008, 2011 VMware Inc. and others
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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.virgo.kernel.deployer.core.DeploymentException;
import org.eclipse.virgo.kernel.install.artifact.InstallArtifact;
import org.eclipse.virgo.kernel.install.artifact.InstallArtifactLifecycleListener;
import org.eclipse.virgo.util.common.GraphNode;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.Version;


/**
 */
public class TreeRestrictingInstallArtifactLifecycleListenerTests {

    private StubEventLogger stubEventLogger;

    private InstallArtifactLifecycleListener treeRestrictingListener;

    private InstallArtifact installArtifact1;

    private InstallArtifact installArtifact2;

    @Before
    public void setUp() throws Exception {
        this.stubEventLogger = new StubEventLogger();
        this.treeRestrictingListener = new TreeRestrictingInstallArtifactLifecycleListener(this.stubEventLogger);
        
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
    
    /**
     * Add test for case where a restriction is needed. Keep this very basic as we will be deleting it before long.
     */
    @Test(expected=DeploymentException.class)
    public void testOnInstallingDAG() throws DeploymentException {
        
        GraphNode<InstallArtifact> graph = new GraphNode<InstallArtifact>() {

            @Override
            public InstallArtifact getValue() {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public List<GraphNode<InstallArtifact>> getChildren() {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public void addChild(GraphNode<InstallArtifact> child) {
                // TODO Auto-generated method stub
                
            }

            @Override
            public boolean removeChild(GraphNode<InstallArtifact> child) {
                // TODO Auto-generated method stub
                return false;
            }

            @Override
            public List<GraphNode<InstallArtifact>> getParents() {
                return new ArrayList<GraphNode<InstallArtifact>>();
            }

            @Override
            public void visit(org.eclipse.virgo.util.common.GraphNode.DirectedAcyclicGraphVisitor<InstallArtifact> visitor) {
                // TODO Auto-generated method stub
                
            }

            @Override
            public <E extends Exception> void visit(
                org.eclipse.virgo.util.common.GraphNode.ExceptionThrowingDirectedAcyclicGraphVisitor<InstallArtifact, E> visitor) throws E {
                // TODO Auto-generated method stub
                
            }

            @Override
            public int size() {
                // TODO Auto-generated method stub
                return 0;
            }

            @Override
            public boolean isRootNode() {
                // TODO Auto-generated method stub
                return false;
            }};
        InstallArtifact installArtifactA = createMock(InstallArtifact.class);
        expect(installArtifactA.getType()).andReturn("type1").anyTimes();
        expect(installArtifactA.getName()).andReturn("name1").anyTimes();
        expect(installArtifactA.getVersion()).andReturn(Version.emptyVersion).anyTimes();
        expect(installArtifactA.getScopeName()).andReturn(null).anyTimes();
        expect(installArtifactA.getGraph()).andReturn(graph).anyTimes();
        
        InstallArtifact installArtifactB = createMock(InstallArtifact.class);
        expect(installArtifactB.getType()).andReturn("type1").anyTimes();
        expect(installArtifactB.getName()).andReturn("name1").anyTimes();
        expect(installArtifactB.getVersion()).andReturn(Version.emptyVersion).anyTimes();
        expect(installArtifactB.getScopeName()).andReturn(null).anyTimes();
        
        replay(installArtifactA, installArtifactB);
        
        this.treeRestrictingListener.onInstalling(installArtifactA);
        this.treeRestrictingListener.onInstalling(installArtifactB);
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
