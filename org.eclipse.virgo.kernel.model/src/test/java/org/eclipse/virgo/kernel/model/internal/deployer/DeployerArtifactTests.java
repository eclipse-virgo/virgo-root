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
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.osgi.framework.Version;

import org.eclipse.virgo.kernel.install.artifact.InstallArtifact;
import org.eclipse.virgo.kernel.install.artifact.InstallArtifact.State;
import org.eclipse.virgo.kernel.model.ArtifactState;
import org.eclipse.virgo.kernel.model.internal.DependencyDeterminer;
import org.eclipse.virgo.kernel.model.internal.deployer.DeployerArtifact;


import org.eclipse.virgo.kernel.core.KernelException;
import org.eclipse.virgo.kernel.deployer.core.DeploymentException;
import org.eclipse.virgo.kernel.serviceability.Assert.FatalAssertionException;
import org.eclipse.virgo.kernel.stubs.StubInstallArtifact;
import org.eclipse.virgo.teststubs.osgi.framework.StubBundleContext;
import org.eclipse.virgo.teststubs.osgi.support.TrueFilter;

public class DeployerArtifactTests {

    private final StubBundleContext bundleContext = new StubBundleContext();
    {
        String filterString = String.format("(&(objectClass=%s)(artifactType=bundle))", DependencyDeterminer.class.getCanonicalName());
        bundleContext.addFilter(filterString, new TrueFilter(filterString));
    }

    private final DeployerArtifact artifact = new DeployerArtifact(bundleContext, new StubInstallArtifact("bundle"));

    @Test(expected = FatalAssertionException.class)
    public void testNullBundleContext() {
        new DeployerArtifact(null, new StubInstallArtifact("bundle"));
    }

    @Test(expected = FatalAssertionException.class)
    public void testNullInstallArtifact() {
        new DeployerArtifact(this.bundleContext, null);
    }

    @Test
    public void getState() {
        InstallArtifact installArtifact = createMock(InstallArtifact.class);

        expect(installArtifact.getType()).andReturn("bundle");
        expect(installArtifact.getName()).andReturn("test");
        expect(installArtifact.getVersion()).andReturn(Version.emptyVersion);

        expect(installArtifact.getState()).andReturn(State.INITIAL);
        expect(installArtifact.getState()).andReturn(State.INSTALLING);
        expect(installArtifact.getState()).andReturn(State.INSTALLED);
        expect(installArtifact.getState()).andReturn(State.RESOLVING);
        expect(installArtifact.getState()).andReturn(State.RESOLVED);
        expect(installArtifact.getState()).andReturn(State.STARTING);
        expect(installArtifact.getState()).andReturn(State.ACTIVE);
        expect(installArtifact.getState()).andReturn(State.STOPPING);
        expect(installArtifact.getState()).andReturn(State.UNINSTALLING);
        expect(installArtifact.getState()).andReturn(State.UNINSTALLED);
        replay(installArtifact);

        DeployerArtifact artifact1 = new DeployerArtifact(bundleContext, installArtifact);

        assertEquals(ArtifactState.INITIAL, artifact1.getState());
        assertEquals(ArtifactState.INSTALLING, artifact1.getState());
        assertEquals(ArtifactState.INSTALLED, artifact1.getState());
        assertEquals(ArtifactState.RESOLVING, artifact1.getState());
        assertEquals(ArtifactState.RESOLVED, artifact1.getState());
        assertEquals(ArtifactState.STARTING, artifact1.getState());
        assertEquals(ArtifactState.ACTIVE, artifact1.getState());
        assertEquals(ArtifactState.STOPPING, artifact1.getState());
        assertEquals(ArtifactState.UNINSTALLING, artifact1.getState());
        assertEquals(ArtifactState.UNINSTALLED, artifact1.getState());

        verify(installArtifact);
    }

    @Test
    public void start() throws KernelException {
        this.artifact.start();
    }

    @Test
    public void stop() throws DeploymentException {
        this.artifact.stop();
    }

    @Test
    public void uninstall() throws DeploymentException {
        this.artifact.uninstall();
    }

    @Test
    public void updateAndRefresh() throws DeploymentException {
        this.artifact.refresh();
    }

    @Test
    public void getProperties() throws DeploymentException {
        this.artifact.getProperties();
    }
}
