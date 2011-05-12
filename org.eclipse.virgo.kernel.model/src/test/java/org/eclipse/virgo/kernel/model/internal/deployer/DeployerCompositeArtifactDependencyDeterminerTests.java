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

package org.eclipse.virgo.kernel.model.internal.deployer;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import org.eclipse.virgo.kernel.install.artifact.InstallArtifact;
import org.eclipse.virgo.kernel.model.RuntimeArtifactRepository;
import org.eclipse.virgo.kernel.model.StubArtifactRepository;
import org.eclipse.virgo.kernel.model.StubCompositeArtifact;
import org.eclipse.virgo.kernel.model.StubRegion;
import org.eclipse.virgo.kernel.model.internal.DependencyDeterminer;
import org.eclipse.virgo.kernel.model.internal.deployer.DeployerCompositeArtifact;
import org.eclipse.virgo.kernel.model.internal.deployer.DeployerCompositeArtifactDependencyDeterminer;


import org.eclipse.virgo.kernel.serviceability.Assert.FatalAssertionException;
import org.eclipse.virgo.kernel.stubs.StubInstallArtifact;
import org.eclipse.virgo.kernel.stubs.StubPlanInstallArtifact;
import org.eclipse.virgo.teststubs.osgi.framework.StubBundleContext;
import org.eclipse.virgo.teststubs.osgi.support.TrueFilter;
import org.eclipse.virgo.util.common.ThreadSafeArrayListTree;

public class DeployerCompositeArtifactDependencyDeterminerTests {

    private final RuntimeArtifactRepository artifactRepository = new StubArtifactRepository();

    private final StubBundleContext bundleContext;
    {
        this.bundleContext = new StubBundleContext();
        String filterString = String.format("(&(objectClass=%s)(artifactType=plan))", DependencyDeterminer.class.getCanonicalName());
        this.bundleContext.addFilter(filterString, new TrueFilter(filterString));
    }

    private final StubRegion region1 = new StubRegion("test-region1");
    
    private final StubRegion region2 = new StubRegion("test-region2");
    
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
        StubPlanInstallArtifact installArtifact = new StubPlanInstallArtifact();
        installArtifact.getTree().addChild(new ThreadSafeArrayListTree<InstallArtifact>(new StubInstallArtifact()));
		DeployerCompositeArtifact artifact = new DeployerCompositeArtifact(bundleContext, installArtifact, region1);
        assertEquals(1, this.determiner.getDependents(artifact).size());
    }

}
