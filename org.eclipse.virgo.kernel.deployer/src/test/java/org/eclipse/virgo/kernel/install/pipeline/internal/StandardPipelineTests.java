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

package org.eclipse.virgo.kernel.install.pipeline.internal;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.isA;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.reset;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import org.eclipse.virgo.kernel.osgi.framework.UnableToSatisfyBundleDependenciesException;

import org.eclipse.virgo.kernel.deployer.core.DeploymentException;
import org.eclipse.virgo.kernel.install.artifact.InstallArtifact;
import org.eclipse.virgo.kernel.install.environment.InstallEnvironment;
import org.eclipse.virgo.kernel.install.environment.InstallLog;
import org.eclipse.virgo.kernel.install.pipeline.Pipeline;
import org.eclipse.virgo.kernel.install.pipeline.internal.StandardPipeline;
import org.eclipse.virgo.kernel.install.pipeline.stage.AbstractPipelineStage;
import org.eclipse.virgo.kernel.install.pipeline.stage.PipelineStage;
import org.eclipse.virgo.util.common.ThreadSafeArrayListTree;
import org.eclipse.virgo.util.common.Tree;

/**
 */
public class StandardPipelineTests {

    private Tree<InstallArtifact> installTree;

    private InstallEnvironment installEnvironment;

    private InstallLog installLog;

    private List<PipelineStage> stageTrace;

    private class TestPipelineStage extends AbstractPipelineStage {

        @Override
        protected void doProcessTree(Tree<InstallArtifact> installTree, InstallEnvironment installEnvironment) {
            StandardPipelineTests.this.stageTrace.add(this);
        }

        public String toString() {
            return "stage";
        }

    }

    private class SelfModifyingPipelineStage extends TestPipelineStage {

        private final Pipeline pipeline;

        private final PipelineStage extraStage;

        public SelfModifyingPipelineStage(Pipeline pipeline, PipelineStage extraStage) {
            this.pipeline = pipeline;
            this.extraStage = extraStage;
        }

        @Override
        protected void doProcessTree(Tree<InstallArtifact> installTree, InstallEnvironment installEnvironment) {
            super.doProcessTree(installTree, installEnvironment);
            this.pipeline.appendStage(this.extraStage);
        }

    }

    @Before
    public void setUp() {
        this.installTree = new ThreadSafeArrayListTree<InstallArtifact>(null);;
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

        Pipeline p = new StandardPipeline();
        p.process(installTree, installEnvironment);

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

        Pipeline p = new StandardPipeline();

        PipelineStage ps1 = new TestPipelineStage();
        p.appendStage(ps1);
        expectedStageTrace.add(ps1);

        PipelineStage ps2 = new TestPipelineStage();
        p.appendStage(ps2);
        expectedStageTrace.add(ps2);

        p.process(installTree, installEnvironment);

        assertEquals(expectedStageTrace, this.stageTrace);

        verifyMocks();
        resetMocks();

    }

    @Test
    public void testPipelineWithDuplicatedStage() throws DeploymentException, UnableToSatisfyBundleDependenciesException {

        expect(this.installEnvironment.getInstallLog()).andReturn(this.installLog).anyTimes();
        this.installLog.log(isA(PipelineStage.class), isA(String.class), isA(String.class));
        expectLastCall().times(8);

        replayMocks();

        List<PipelineStage> expectedStageTrace = new ArrayList<PipelineStage>();

        Pipeline p = new StandardPipeline();

        PipelineStage ps1 = new TestPipelineStage();
        p.appendStage(ps1);
        expectedStageTrace.add(ps1);

        PipelineStage ps2 = new TestPipelineStage();
        p.appendStage(ps2);
        expectedStageTrace.add(ps2);

        p.appendStage(ps1);
        expectedStageTrace.add(ps1);

        p.process(installTree, installEnvironment);

        assertEquals(expectedStageTrace, this.stageTrace);

        verifyMocks();
        resetMocks();

    }

    @Test
    public void testNestedPipeline() throws DeploymentException, UnableToSatisfyBundleDependenciesException {

        expect(this.installEnvironment.getInstallLog()).andReturn(this.installLog).anyTimes();
        this.installLog.log(isA(PipelineStage.class), isA(String.class), isA(String.class));
        expectLastCall().times(14);

        replayMocks();

        List<PipelineStage> expectedStageTrace = new ArrayList<PipelineStage>();

        Pipeline child1 = new StandardPipeline();

        PipelineStage ps1 = new TestPipelineStage();
        child1.appendStage(ps1);
        expectedStageTrace.add(ps1);

        PipelineStage ps2 = new TestPipelineStage();
        child1.appendStage(ps2);
        expectedStageTrace.add(ps2);

        Pipeline child2 = new StandardPipeline();

        PipelineStage ps3 = new TestPipelineStage();
        child2.appendStage(ps3);
        expectedStageTrace.add(ps3);

        PipelineStage ps4 = new TestPipelineStage();
        child2.appendStage(ps4);
        expectedStageTrace.add(ps4);
        
        Pipeline parent = new StandardPipeline();
        parent.appendStage(child1);
        parent.appendStage(child2);

        parent.process(installTree, installEnvironment);

        assertEquals(expectedStageTrace, this.stageTrace);

        verifyMocks();
        resetMocks();

    }

    @Test
    public void testSelfModifyingPipeline() throws DeploymentException, UnableToSatisfyBundleDependenciesException {

        expect(this.installEnvironment.getInstallLog()).andReturn(this.installLog).anyTimes();
        this.installLog.log(isA(PipelineStage.class), isA(String.class), isA(String.class));
        expectLastCall().times(12);

        replayMocks();

        List<PipelineStage> expectedStageTrace = new ArrayList<PipelineStage>();

        Pipeline p = new StandardPipeline();

        PipelineStage ps1 = new TestPipelineStage();
        p.appendStage(ps1);
        expectedStageTrace.add(ps1);

        PipelineStage ps4 = new TestPipelineStage();
        PipelineStage ps2 = new SelfModifyingPipelineStage(p, ps4);
        p.appendStage(ps2);
        expectedStageTrace.add(ps2);

        PipelineStage ps5 = new TestPipelineStage();
        PipelineStage ps3 = new SelfModifyingPipelineStage(p, ps5);
        p.appendStage(ps3);
        expectedStageTrace.add(ps3);

        expectedStageTrace.add(ps4);
        expectedStageTrace.add(ps5);

        p.process(installTree, installEnvironment);

        assertEquals(expectedStageTrace, this.stageTrace);

        verifyMocks();
        resetMocks();

    }

}
