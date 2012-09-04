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

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.reset;
import static org.junit.Assert.assertEquals;

import org.eclipse.equinox.region.RegionDigraph;
import org.eclipse.virgo.nano.deployer.api.core.DeploymentException;
import org.eclipse.virgo.kernel.install.artifact.InstallArtifact;
import org.eclipse.virgo.kernel.model.StubArtifactRepository;
import org.eclipse.virgo.kernel.model.StubSpringContextAccessor;
import org.eclipse.virgo.kernel.model.internal.DependencyDeterminer;
import org.eclipse.virgo.nano.serviceability.Assert.FatalAssertionException;
import org.eclipse.virgo.kernel.stubs.StubInstallArtifact;
import org.eclipse.virgo.kernel.stubs.StubPlanInstallArtifact;
import org.eclipse.virgo.test.stubs.framework.StubBundleContext;
import org.eclipse.virgo.test.stubs.region.StubRegion;
import org.eclipse.virgo.test.stubs.support.TrueFilter;
import org.junit.Before;
import org.junit.Test;

public class ModelInstallArtifactLifecycleListenerTests {

    private final StubArtifactRepository artifactRepository = new StubArtifactRepository();
    
    private final RegionDigraph regionDigraph = createMock(RegionDigraph.class);

    private final StubSpringContextAccessor springContextAccessor = new StubSpringContextAccessor();

    private final StubRegion region = new StubRegion("test-region", null);
    
    private final StubBundleContext bundleContext;
    {
        this.bundleContext = new StubBundleContext();
        String filterString1 = String.format("(&(objectClass=%s)(artifactType=bundle))", DependencyDeterminer.class.getCanonicalName());
        this.bundleContext.addFilter(filterString1, new TrueFilter(filterString1));
        String filterString2 = String.format("(&(objectClass=%s)(artifactType=plan))", DependencyDeterminer.class.getCanonicalName());
        this.bundleContext.addFilter(filterString2, new TrueFilter(filterString2));
    }

    private final ModelInstallArtifactLifecycleListener listener = new ModelInstallArtifactLifecycleListener(bundleContext, artifactRepository, regionDigraph, region, springContextAccessor);

    @Before
    public void setUp(){
        reset(this.regionDigraph);
        expect(this.regionDigraph.getRegion("global")).andReturn(region).anyTimes();
        replay(this.regionDigraph);
    }
    
    @Test(expected = FatalAssertionException.class)
    public void nullBundleContext() {
        new ModelInstallArtifactLifecycleListener(null, artifactRepository, regionDigraph, region, springContextAccessor);
    }

    @Test(expected = FatalAssertionException.class)
    public void nullArtifactRepository() {
        new ModelInstallArtifactLifecycleListener(bundleContext, null, regionDigraph, region, springContextAccessor);
    }
    
    @Test(expected = FatalAssertionException.class)
    public void nullRegionDigraph() {
        new ModelInstallArtifactLifecycleListener(bundleContext, artifactRepository, null, region, springContextAccessor);
    }
    
    @Test(expected = FatalAssertionException.class)
    public void nullRegion() {
        new ModelInstallArtifactLifecycleListener(bundleContext, artifactRepository, regionDigraph, null, springContextAccessor);
    }
    
    @Test(expected = FatalAssertionException.class)
    public void nullSpringContextAccessor() {
        new ModelInstallArtifactLifecycleListener(bundleContext, artifactRepository, regionDigraph, region, null);
    }

    @Test
    public void installing() throws DeploymentException {
        checkNumberOfArtifacts(0);
        
        StubInstallArtifact bundleInstallArtifact = new StubInstallArtifact("bundle");
        this.listener.onInstalling(bundleInstallArtifact);
        checkNumberOfArtifacts(1);
        
        this.listener.onInstalling(bundleInstallArtifact);
        checkNumberOfArtifacts(1);
        
        StubPlanInstallArtifact planInstallArtifact = new StubPlanInstallArtifact();
        this.listener.onInstalling(planInstallArtifact);
        checkNumberOfArtifacts(2);
        
        this.listener.onInstalling(planInstallArtifact);
        checkNumberOfArtifacts(2);
    }
    
    @Test
    public void installFailed() throws DeploymentException {
        checkNumberOfArtifacts(0);
        
        InstallArtifact bundleInstallArtifact = new StubInstallArtifact("bundle");
        this.listener.onInstalling(bundleInstallArtifact);
        checkNumberOfArtifacts(1);
        
        this.listener.onInstallFailed(bundleInstallArtifact);
        checkNumberOfArtifacts(0);
    }

    @Test
    public void uninstalled() throws DeploymentException {
        InstallArtifact bundleInstallArtifact = new StubInstallArtifact("bundle");
        this.listener.onInstalling(bundleInstallArtifact);
        checkNumberOfArtifacts(1);
        
        this.listener.onUninstalled(bundleInstallArtifact);
        checkNumberOfArtifacts(0);
        
        this.listener.onUninstalled(bundleInstallArtifact);
        checkNumberOfArtifacts(0);
    }
    
    private void checkNumberOfArtifacts(int expectedNumberOfArtifacts) {
        assertEquals(expectedNumberOfArtifacts, this.artifactRepository.getArtifacts().size());
    }
}
