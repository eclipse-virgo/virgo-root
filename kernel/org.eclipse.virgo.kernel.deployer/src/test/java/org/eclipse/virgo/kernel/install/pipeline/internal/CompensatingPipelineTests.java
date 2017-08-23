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

package org.eclipse.virgo.kernel.install.pipeline.internal;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.isA;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.reset;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.virgo.nano.deployer.api.core.DeploymentException;
import org.eclipse.virgo.kernel.install.artifact.InstallArtifact;
import org.eclipse.virgo.kernel.install.environment.InstallEnvironment;
import org.eclipse.virgo.kernel.install.environment.InstallLog;
import org.eclipse.virgo.kernel.install.pipeline.Pipeline;
import org.eclipse.virgo.kernel.install.pipeline.stage.AbstractPipelineStage;
import org.eclipse.virgo.kernel.install.pipeline.stage.PipelineStage;
import org.eclipse.virgo.kernel.osgi.framework.UnableToSatisfyBundleDependenciesException;
import org.eclipse.virgo.medic.eventlog.LogEvent;
import org.eclipse.virgo.util.common.DirectedAcyclicGraph;
import org.eclipse.virgo.util.common.GraphNode;
import org.eclipse.virgo.util.common.ThreadSafeDirectedAcyclicGraph;
import org.junit.Before;
import org.junit.Test;

/**
 */
public class CompensatingPipelineTests {

    private GraphNode<InstallArtifact> installGraph;

    private InstallEnvironment installEnvironment;

    private InstallLog installLog;

    private List<PipelineStage> stageTrace;

    private class GoodPipelineStage extends AbstractPipelineStage {

        @Override
        protected void doProcessGraph(GraphNode<InstallArtifact> installGraph, InstallEnvironment installEnvironment) {
            CompensatingPipelineTests.this.stageTrace.add(this);
        }

        public String toString() {
            return "stage";
        }

    }

    private class DeploymentExceptionPipelineStage extends AbstractPipelineStage {

        @Override
        protected void doProcessGraph(GraphNode<InstallArtifact> installGraph, InstallEnvironment installEnvironment) throws DeploymentException {
            CompensatingPipelineTests.this.stageTrace.add(this);
            throw new DeploymentException("failed");
        }

        public String toString() {
            return "DeploymentExceptionPipelineStage";
        }

    }

    private class UnableToSatisfyBundleDependenciesExceptionPipelineStage extends AbstractPipelineStage {

        @Override
        protected void doProcessGraph(GraphNode<InstallArtifact> installGraph, InstallEnvironment installEnvironment)
            throws UnableToSatisfyBundleDependenciesException {
            CompensatingPipelineTests.this.stageTrace.add(this);
            throw new UnableToSatisfyBundleDependenciesException("failed", null, null);
        }

        public String toString() {
            return "UnableToSatisfyBundleDependenciesException";
        }

    }

    private class RuntimeExceptionPipelineStage extends AbstractPipelineStage {

        @Override
        protected void doProcessGraph(GraphNode<InstallArtifact> installGraph, InstallEnvironment installEnvironment) {
            CompensatingPipelineTests.this.stageTrace.add(this);
            throw new RuntimeException("failed");
        }

        public String toString() {
            return "RuntimeException";
        }

    }

    private class CompensationStage extends AbstractPipelineStage {

        @Override
        protected void doProcessGraph(GraphNode<InstallArtifact> installGraph, InstallEnvironment installEnvironment) {
            CompensatingPipelineTests.this.stageTrace.add(this);
        }

        public String toString() {
            return "compensation";
        }

    }

    @Before
    public void setUp() {
    		DirectedAcyclicGraph<InstallArtifact> dag = new ThreadSafeDirectedAcyclicGraph<InstallArtifact>();
        this.installGraph = dag.createRootNode(null);
        this.installEnvironment = createMock(InstallEnvironment.class);
        this.installLog = createMock(InstallLog.class);
        this.stageTrace = new ArrayList<PipelineStage>();
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
    public void testEmptyPipeline() throws DeploymentException, UnableToSatisfyBundleDependenciesException {

        expect(this.installEnvironment.getInstallLog()).andReturn(this.installLog);
        this.installLog.log(isA(PipelineStage.class), isA(String.class), isA(String.class));
        expectLastCall().times(2);

        replayMocks();

        List<PipelineStage> expectedStageTrace = new ArrayList<PipelineStage>();

        Pipeline p = new CompensatingPipeline(new CompensationStage());
        p.process(installGraph, installEnvironment);

        assertEquals(expectedStageTrace, this.stageTrace);

        verifyMocks();
        resetMocks();

    }

    @Test
    public void testSimplePipeline() throws DeploymentException, UnableToSatisfyBundleDependenciesException {

        expect(this.installEnvironment.getInstallLog()).andReturn(this.installLog).anyTimes();
        this.installLog.log(isA(PipelineStage.class), isA(String.class), isA(String.class));
        expectLastCall().times(6);

        replayMocks();

        List<PipelineStage> expectedStageTrace = new ArrayList<PipelineStage>();

        Pipeline p = new CompensatingPipeline(new CompensationStage());

        PipelineStage ps1 = new GoodPipelineStage();
        p.appendStage(ps1);
        expectedStageTrace.add(ps1);

        PipelineStage ps2 = new GoodPipelineStage();
        p.appendStage(ps2);
        expectedStageTrace.add(ps2);

        p.process(installGraph, installEnvironment);

        assertEquals(expectedStageTrace, this.stageTrace);

        verifyMocks();
        resetMocks();

    }

    @Test
    public void testEarlyDeploymentException() throws Exception {

        expect(this.installEnvironment.getInstallLog()).andReturn(this.installLog).anyTimes();
        this.installLog.log(isA(PipelineStage.class), isA(String.class), isA(String.class));
        expectLastCall().anyTimes();
        this.installLog.log(isA(PipelineStage.class), isA(String.class), isA(String.class), isA(String.class));
        expectLastCall().anyTimes();
        this.installLog.logFailure(isA(LogEvent.class), isA(Throwable.class));
        expectLastCall().anyTimes();

        replayMocks();

        List<PipelineStage> expectedStageTrace = new ArrayList<PipelineStage>();

        CompensationStage compensation = new CompensationStage();
        Pipeline p = new CompensatingPipeline(compensation);

        PipelineStage ps1 = new DeploymentExceptionPipelineStage();
        p.appendStage(ps1);
        expectedStageTrace.add(ps1);

        PipelineStage ps2 = new GoodPipelineStage();
        p.appendStage(ps2);

        expectedStageTrace.add(compensation);

        try {
            p.process(installGraph, installEnvironment);
            assertTrue(false);
        } catch (DeploymentException e) {
        }

        assertEquals(expectedStageTrace, this.stageTrace);

        verifyMocks();
        resetMocks();

    }
    
    @Test
    public void testLateDeploymentException() throws Exception {

        expect(this.installEnvironment.getInstallLog()).andReturn(this.installLog).anyTimes();
        this.installLog.log(isA(PipelineStage.class), isA(String.class), isA(String.class));
        expectLastCall().anyTimes();
        this.installLog.log(isA(PipelineStage.class), isA(String.class), isA(String.class), isA(String.class));
        expectLastCall().anyTimes();
        this.installLog.logFailure(isA(LogEvent.class), isA(Throwable.class));
        expectLastCall().anyTimes();


        replayMocks();

        List<PipelineStage> expectedStageTrace = new ArrayList<PipelineStage>();

        CompensationStage compensation = new CompensationStage();
        Pipeline p = new CompensatingPipeline(compensation);

        PipelineStage ps1 = new GoodPipelineStage();
        p.appendStage(ps1);
        expectedStageTrace.add(ps1);

        PipelineStage ps2 = new DeploymentExceptionPipelineStage();
        p.appendStage(ps2);
        expectedStageTrace.add(ps2);

        expectedStageTrace.add(compensation);

        try {
            p.process(installGraph, installEnvironment);
            assertTrue(false);
        } catch (DeploymentException e) {
        }

        assertEquals(expectedStageTrace, this.stageTrace);

        verifyMocks();
        resetMocks();

    }
    
    @Test
    public void testUnableToSatisfyBundleDependenciesException() throws Exception {

        expect(this.installEnvironment.getInstallLog()).andReturn(this.installLog).anyTimes();
        this.installLog.log(isA(PipelineStage.class), isA(String.class), isA(String.class));
        expectLastCall().anyTimes();
        this.installLog.log(isA(PipelineStage.class), isA(String.class), isA(String.class), isA(String.class));
        expectLastCall().anyTimes();
        this.installLog.logFailure(isA(LogEvent.class), isA(Throwable.class));
        expectLastCall().anyTimes();

        replayMocks();

        List<PipelineStage> expectedStageTrace = new ArrayList<PipelineStage>();

        CompensationStage compensation = new CompensationStage();
        Pipeline p = new CompensatingPipeline(compensation);

        PipelineStage ps1 = new UnableToSatisfyBundleDependenciesExceptionPipelineStage();
        p.appendStage(ps1);
        expectedStageTrace.add(ps1);

        PipelineStage ps2 = new GoodPipelineStage();
        p.appendStage(ps2);

        expectedStageTrace.add(compensation);

        try {
            p.process(installGraph, installEnvironment);
            assertTrue(false);
        } catch (UnableToSatisfyBundleDependenciesException e) {
        }

        assertEquals(expectedStageTrace, this.stageTrace);

        verifyMocks();
        resetMocks();

    }
    
    @Test
    public void testRuntimeException() throws Exception {

        expect(this.installEnvironment.getInstallLog()).andReturn(this.installLog).anyTimes();
        this.installLog.log(isA(PipelineStage.class), isA(String.class), isA(String.class));
        expectLastCall().anyTimes();
        this.installLog.log(isA(PipelineStage.class), isA(String.class), isA(String.class), isA(String.class));
        expectLastCall().anyTimes();
        this.installLog.logFailure(isA(LogEvent.class), isA(Throwable.class));
        expectLastCall().anyTimes();

        replayMocks();

        List<PipelineStage> expectedStageTrace = new ArrayList<PipelineStage>();

        CompensationStage compensation = new CompensationStage();
        Pipeline p = new CompensatingPipeline(compensation);

        PipelineStage ps1 = new RuntimeExceptionPipelineStage();
        p.appendStage(ps1);
        expectedStageTrace.add(ps1);

        PipelineStage ps2 = new GoodPipelineStage();
        p.appendStage(ps2);

        expectedStageTrace.add(compensation);

        try {
            p.process(installGraph, installEnvironment);
            assertTrue(false);
        } catch (RuntimeException e) {
        }

        assertEquals(expectedStageTrace, this.stageTrace);

        verifyMocks();
        resetMocks();

    }

}
