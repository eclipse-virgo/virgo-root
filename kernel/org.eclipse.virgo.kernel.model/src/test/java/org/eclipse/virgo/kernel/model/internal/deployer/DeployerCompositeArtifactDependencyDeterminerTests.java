/*******************************************************************************
 * Copyright (c) 2008, 2010 VMware Inc. and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   VMware Inc. - initial contribution
 *   EclipseSource - Bug 358442 Change InstallArtifact graph from a tree to a DAG
 *******************************************************************************/

package org.eclipse.virgo.kernel.model.internal.deployer;

import static org.junit.Assert.assertEquals;

import org.eclipse.virgo.kernel.install.artifact.InstallArtifact;
import org.eclipse.virgo.kernel.model.RuntimeArtifactRepository;
import org.eclipse.virgo.kernel.model.StubArtifactRepository;
import org.eclipse.virgo.kernel.model.StubCompositeArtifact;
import org.eclipse.virgo.kernel.model.internal.DependencyDeterminer;
import org.eclipse.virgo.kernel.stubs.StubInstallArtifact;
import org.eclipse.virgo.kernel.stubs.StubPlanInstallArtifact;
import org.eclipse.virgo.nano.serviceability.Assert.FatalAssertionException;
import org.eclipse.virgo.test.stubs.framework.StubBundleContext;
import org.eclipse.virgo.test.stubs.region.StubRegion;
import org.eclipse.virgo.test.stubs.support.TrueFilter;
import org.eclipse.virgo.util.common.DirectedAcyclicGraph;
import org.eclipse.virgo.util.common.ThreadSafeDirectedAcyclicGraph;
import org.junit.Test;

public class DeployerCompositeArtifactDependencyDeterminerTests {

    private final RuntimeArtifactRepository artifactRepository = new StubArtifactRepository();

    private final StubBundleContext bundleContext;
    {
        this.bundleContext = new StubBundleContext();
        String filterString = String.format("(&(objectClass=%s)(artifactType=plan))", DependencyDeterminer.class.getCanonicalName());
        this.bundleContext.addFilter(filterString, new TrueFilter(filterString));
    }

    private final StubRegion region1 = new StubRegion("test-region1", null);
    
    private final StubRegion region2 = new StubRegion("test-region2", null);
    
    private final DeployerCompositeArtifactDependencyDeterminer determiner = new DeployerCompositeArtifactDependencyDeterminer(artifactRepository, region1, region2);

    @Test(expected = FatalAssertionException.class)
    public void nullRepository() {
        new DeployerCompositeArtifactDependencyDeterminer(null, region1, region2);
    }

    @Test(expected = FatalAssertionException.class)
    public void nullUserRegion() {
        new DeployerCompositeArtifactDependencyDeterminer(artifactRepository, null, region2);
    }

    @Test(expected = FatalAssertionException.class)
    public void nullGlobalRegion() {
        new DeployerCompositeArtifactDependencyDeterminer(artifactRepository, region1, null);
    }

    @Test
    public void nonCompositeArtifact() {
        assertEquals(0, this.determiner.getDependents(new StubCompositeArtifact()).size());
    }

    @Test
    public void success() {
    	DirectedAcyclicGraph<InstallArtifact> dag = new ThreadSafeDirectedAcyclicGraph<InstallArtifact>();
        StubPlanInstallArtifact installArtifact = new StubPlanInstallArtifact(dag);
        installArtifact.getGraph().addChild(dag.createRootNode(new StubInstallArtifact()));
		DeployerCompositeArtifact artifact = new DeployerCompositeArtifact(bundleContext, installArtifact, region1);
        assertEquals(1, this.determiner.getDependents(artifact).size());
    }

}
