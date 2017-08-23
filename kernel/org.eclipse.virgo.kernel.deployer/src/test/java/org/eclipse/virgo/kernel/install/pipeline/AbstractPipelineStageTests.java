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

package org.eclipse.virgo.kernel.install.pipeline;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.isA;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.reset;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;

import org.eclipse.virgo.nano.deployer.api.core.DeploymentException;
import org.eclipse.virgo.kernel.install.artifact.InstallArtifact;
import org.eclipse.virgo.kernel.install.environment.InstallEnvironment;
import org.eclipse.virgo.kernel.install.environment.InstallLog;
import org.eclipse.virgo.kernel.install.pipeline.stage.AbstractPipelineStage;
import org.eclipse.virgo.kernel.install.pipeline.stage.PipelineStage;
import org.eclipse.virgo.kernel.osgi.framework.UnableToSatisfyBundleDependenciesException;
import org.eclipse.virgo.util.common.DirectedAcyclicGraph;
import org.eclipse.virgo.util.common.GraphNode;
import org.eclipse.virgo.util.common.ThreadSafeDirectedAcyclicGraph;
import org.junit.Before;
import org.junit.Test;

/**
 */
public class AbstractPipelineStageTests {
    
    private boolean processed;
    
    private GraphNode<InstallArtifact> installGraph;
    
    private InstallEnvironment installEnvironment;
    
    private InstallLog installLog;
    
    private final class TestPipelineStage extends AbstractPipelineStage {

        @Override
        protected void doProcessGraph(GraphNode<InstallArtifact> installGraph, InstallEnvironment installEnvironment) {
            AbstractPipelineStageTests.this.processed = true;
        }
        
        public String toString() {
            return "stage";
        }
        
    }
    
    @Before
    public void setUp() {
        this.processed = false;
        DirectedAcyclicGraph<InstallArtifact> dag = new ThreadSafeDirectedAcyclicGraph<InstallArtifact>();
        this.installGraph = dag.createRootNode(null);
        this.installEnvironment = createMock(InstallEnvironment.class);
        this.installLog = createMock(InstallLog.class);
    }
    
    private void replayMocks() {
        replay(this.installEnvironment, this.installLog);
    }
    
    private void verifyMocks() {
        verify(this.installEnvironment, this.installLog);
    }
    
    private void resetMocks() {
        reset(this.installEnvironment, this.installLog);
    }

    @Test
    public void testAbstractPipelineStage() throws DeploymentException, UnableToSatisfyBundleDependenciesException {
        
        expect(this.installEnvironment.getInstallLog()).andReturn(this.installLog);
        this.installLog.log(isA(PipelineStage.class), isA(String.class), isA(String.class));
        expectLastCall().times(2);
        
        replayMocks();
        
        PipelineStage ps = new TestPipelineStage();
        assertEquals(false, this.processed);
        ps.process(installGraph, installEnvironment);
        assertEquals(true, this.processed);
        
        verifyMocks();
        resetMocks();
    }
    
    

}
