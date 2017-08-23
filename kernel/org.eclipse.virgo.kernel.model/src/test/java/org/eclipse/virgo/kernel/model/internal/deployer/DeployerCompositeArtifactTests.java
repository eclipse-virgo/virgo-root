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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import org.eclipse.virgo.kernel.model.internal.DependencyDeterminer;
import org.eclipse.virgo.kernel.model.internal.deployer.DeployerArtifact;
import org.eclipse.virgo.kernel.model.internal.deployer.DeployerCompositeArtifact;
import org.eclipse.virgo.nano.serviceability.Assert.FatalAssertionException;
import org.eclipse.virgo.kernel.stubs.StubPlanInstallArtifact;
import org.eclipse.virgo.test.stubs.framework.StubBundleContext;
import org.eclipse.virgo.test.stubs.region.StubRegion;
import org.eclipse.virgo.test.stubs.support.TrueFilter;

public class DeployerCompositeArtifactTests {

    private final StubBundleContext bundleContext = new StubBundleContext();
    {
        String filterString = String.format("(&(objectClass=%s)(artifactType=plan))", DependencyDeterminer.class.getCanonicalName());
        bundleContext.addFilter(filterString, new TrueFilter(filterString));
    }

    private final StubRegion region = new StubRegion("test-region", null);
    
    private final DeployerCompositeArtifact artifact = new DeployerCompositeArtifact(bundleContext, new StubPlanInstallArtifact(), region);

    @Test(expected = FatalAssertionException.class)
    public void testNullBundleContext() {
        new DeployerArtifact(null, new StubPlanInstallArtifact(), region);
    }

    @Test(expected = FatalAssertionException.class)
    public void testNullInstallArtifact() {
        new DeployerArtifact(this.bundleContext, null, region);
    }

    @Test(expected = FatalAssertionException.class)
    public void testRegion() {
        new DeployerArtifact(this.bundleContext, new StubPlanInstallArtifact(), null);
    }

    @Test
    public void isAtomic() {
        assertFalse(this.artifact.isAtomic());
    }

    @Test
    public void isScoped() {
        assertFalse(this.artifact.isScoped());
    }

    @Test
    public void getInstallArtifact() {
        assertNotNull(this.artifact.getInstallArtifact());
    }
}
