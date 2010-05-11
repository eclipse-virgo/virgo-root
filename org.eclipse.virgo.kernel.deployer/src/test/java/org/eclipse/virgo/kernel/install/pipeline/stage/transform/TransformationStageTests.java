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

package org.eclipse.virgo.kernel.install.pipeline.stage.transform;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.isA;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.ServiceRegistration;

import org.eclipse.virgo.kernel.osgi.framework.UnableToSatisfyBundleDependenciesException;

import org.eclipse.virgo.kernel.deployer.core.DeploymentException;
import org.eclipse.virgo.kernel.install.artifact.InstallArtifact;
import org.eclipse.virgo.kernel.install.environment.InstallEnvironment;
import org.eclipse.virgo.kernel.install.environment.InstallLog;
import org.eclipse.virgo.kernel.install.pipeline.stage.PipelineStage;
import org.eclipse.virgo.kernel.install.pipeline.stage.transform.Transformer;
import org.eclipse.virgo.kernel.install.pipeline.stage.transform.internal.TransformationStage;
import org.eclipse.virgo.teststubs.osgi.framework.StubBundleContext;
import org.eclipse.virgo.util.common.Tree;

/**
 * TODO Document TransformationStageTests
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * TODO Document concurrent semantics of TransformationStageTests
 * 
 */
public class TransformationStageTests {

    private Tree<InstallArtifact> installTree;

    private InstallEnvironment installEnvironment;

    private Transformer transformer1;
    
    private Transformer transformer2;

    private InstallLog installLog;

    private PipelineStage transformationStage;

    private StubBundleContext bundleContext = new StubBundleContext();

    @SuppressWarnings("unchecked")
    @Before
    public void setUp() {        
        this.installTree = createMock(Tree.class);
        this.transformer1 = createMock(Transformer.class);
        this.transformer2 = createMock(Transformer.class);
        this.transformationStage = new TransformationStage(bundleContext);
        this.installEnvironment = createMock(InstallEnvironment.class);
        this.installLog = createMock(InstallLog.class);

        expect(this.installEnvironment.getInstallLog()).andReturn(this.installLog).anyTimes();
        this.installLog.log(isA(Object.class), isA(String.class), isA(String.class));
        expectLastCall().anyTimes();
    }

    @Test
    public void transformation() throws DeploymentException, UnableToSatisfyBundleDependenciesException {
        this.transformer1.transform(this.installTree, this.installEnvironment);
        this.transformer2.transform(this.installTree, this.installEnvironment);
        this.transformer2.transform(this.installTree, this.installEnvironment);

        replay(this.transformer1, this.transformer2, this.installEnvironment, this.installLog);
        
        this.transformationStage.process(this.installTree, this.installEnvironment);
        
        ServiceRegistration registration1 = this.bundleContext.registerService(Transformer.class.getName(), this.transformer1, null);
        ServiceRegistration registration2= this.bundleContext.registerService(Transformer.class.getName(), this.transformer2, null);
        
        this.transformationStage.process(this.installTree, this.installEnvironment);
        
        registration1.unregister();
        
        this.transformationStage.process(this.installTree, this.installEnvironment);
        
        registration2.unregister();
        
        this.transformationStage.process(this.installTree, this.installEnvironment);
        
        verify(this.transformer1, this.transformer2, this.installEnvironment, this.installLog);        
    }
}
