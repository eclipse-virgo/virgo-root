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

package org.eclipse.virgo.kernel.install.artifact.internal;

import static org.easymock.EasyMock.createNiceMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.eclipse.virgo.kernel.install.artifact.BundleInstallArtifact;
import org.eclipse.virgo.kernel.install.artifact.InstallArtifact;
import org.eclipse.virgo.kernel.install.artifact.PlanInstallArtifact;
import org.eclipse.virgo.util.common.DirectedAcyclicGraph;
import org.eclipse.virgo.util.common.GraphNode;
import org.eclipse.virgo.util.common.ThreadSafeDirectedAcyclicGraph;
import org.junit.Test;


/**
 */
public class PlanMemberCollectorTests {

    private final PlanMemberCollector collector = new PlanMemberCollector();
    
    @Test
    public void rootPlanIsNotIncludedInTheCollection() {
        PlanInstallArtifact plan = createNiceMock(PlanInstallArtifact.class);

        DirectedAcyclicGraph<InstallArtifact> dag = new ThreadSafeDirectedAcyclicGraph<InstallArtifact>();
        GraphNode<InstallArtifact> graph = dag.createRootNode(plan);

        expect(plan.getGraph()).andReturn(graph);
        
        replay(plan);
        
        List<InstallArtifact> members = this.collector.collectPlanMembers(plan);
        assertNotNull(members);
        assertEquals(0, members.size());
    }
    
    @Test
    public void singleLevelPlan() {
        PlanInstallArtifact plan = createNiceMock(PlanInstallArtifact.class);
        DirectedAcyclicGraph<InstallArtifact> dag = new ThreadSafeDirectedAcyclicGraph<InstallArtifact>();
        GraphNode<InstallArtifact> tree = dag.createRootNode(plan);
        
        BundleInstallArtifact bundle1 = createNiceMock(BundleInstallArtifact.class);
        tree.addChild(dag.createRootNode(bundle1));
        
        BundleInstallArtifact bundle2 = createNiceMock(BundleInstallArtifact.class);
        tree.addChild(dag.createRootNode(bundle2));
        
        expect(plan.getGraph()).andReturn(tree);
        
        replay(plan);
        
        List<InstallArtifact> members = this.collector.collectPlanMembers(plan);
        assertNotNull(members);
        assertEquals(2, members.size());
        assertTrue(members.contains(bundle1));
        assertTrue(members.contains(bundle2));
    }
    
    @Test
    public void nestedPlan() {
        PlanInstallArtifact plan = createNiceMock(PlanInstallArtifact.class);
        DirectedAcyclicGraph<InstallArtifact> dag = new ThreadSafeDirectedAcyclicGraph<InstallArtifact>();
        GraphNode<InstallArtifact> tree = dag.createRootNode(plan);

        expect(plan.getGraph()).andReturn(tree);
        
        BundleInstallArtifact bundle1 = createNiceMock(BundleInstallArtifact.class);
        tree.addChild(dag.createRootNode(bundle1));
        
        BundleInstallArtifact bundle2 = createNiceMock(BundleInstallArtifact.class);
        tree.addChild(dag.createRootNode(bundle2));
        
        PlanInstallArtifact nestedPlan = createNiceMock(PlanInstallArtifact.class);
        GraphNode<InstallArtifact> nestedTree = dag.createRootNode(nestedPlan);
        expect(nestedPlan.getGraph()).andReturn(nestedTree);
        
        BundleInstallArtifact bundle3 = createNiceMock(BundleInstallArtifact.class);
        nestedTree.addChild(dag.createRootNode(bundle3));
        
        tree.addChild(nestedTree);
        
        replay(plan, nestedPlan);
        
        List<InstallArtifact> members = this.collector.collectPlanMembers(plan);
        assertNotNull(members);
        assertEquals(4, members.size());
        assertTrue(members.contains(bundle1));
        assertTrue(members.contains(bundle2));
        assertTrue(members.contains(nestedPlan));
        assertTrue(members.contains(bundle3));
    }
}
