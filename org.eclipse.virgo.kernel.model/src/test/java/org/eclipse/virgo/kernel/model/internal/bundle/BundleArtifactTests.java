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

package org.eclipse.virgo.kernel.model.internal.bundle;

import static org.easymock.EasyMock.aryEq;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;

import org.eclipse.virgo.kernel.osgi.framework.PackageAdminUtil;

import org.eclipse.virgo.kernel.model.ArtifactState;
import org.eclipse.virgo.kernel.model.internal.AbstractArtifact;
import org.eclipse.virgo.kernel.model.internal.DependencyDeterminer;
import org.eclipse.virgo.kernel.model.internal.bundle.BundleArtifact;
import org.eclipse.virgo.kernel.serviceability.Assert.FatalAssertionException;
import org.eclipse.virgo.teststubs.osgi.framework.StubBundle;
import org.eclipse.virgo.teststubs.osgi.framework.StubBundleContext;
import org.eclipse.virgo.teststubs.osgi.support.TrueFilter;

public class BundleArtifactTests {

    private final PackageAdminUtil packageAdminUtil = createMock(PackageAdminUtil.class);

    private final StubBundle bundle = new StubBundle();

    private final StubBundleContext bundleContext;

    {
        this.bundleContext = (StubBundleContext) bundle.getBundleContext();
        String filterString = String.format("(&(objectClass=%s)(artifactType=bundle))", DependencyDeterminer.class.getCanonicalName());
        bundleContext.addFilter(filterString, new TrueFilter(filterString));
    }

    private final AbstractArtifact artifact = new BundleArtifact(bundleContext, packageAdminUtil, bundle);

    @Test(expected = FatalAssertionException.class)
    public void nullBundleContext() {
        new BundleArtifact(null, packageAdminUtil, bundle);
    }

    @Test(expected = FatalAssertionException.class)
    public void nullPackageAdminUtil() {
        new BundleArtifact(bundleContext, null, bundle);
    }

    @Test(expected = FatalAssertionException.class)
    public void nullBundle() {
        new BundleArtifact(bundleContext, packageAdminUtil, null);
    }

    @Test
    public void getState() {
        this.bundle.setState(Bundle.UNINSTALLED);
        assertEquals(ArtifactState.UNINSTALLED, this.artifact.getState());
        this.bundle.setState(Bundle.INSTALLED);
        assertEquals(ArtifactState.INSTALLED, this.artifact.getState());
        this.bundle.setState(Bundle.RESOLVED);
        assertEquals(ArtifactState.RESOLVED, this.artifact.getState());
        this.bundle.setState(Bundle.STARTING);
        assertEquals(ArtifactState.STARTING, this.artifact.getState());
        this.bundle.setState(Bundle.STOPPING);
        assertEquals(ArtifactState.STOPPING, this.artifact.getState());
        this.bundle.setState(Bundle.ACTIVE);
        assertEquals(ArtifactState.ACTIVE, this.artifact.getState());
    }

    @Test(expected = IllegalArgumentException.class)
    public void illegalState() {
        this.bundle.setState(10000);
        this.artifact.getState();
    }

    @Test
    public void updateAndRefresh() {
        this.packageAdminUtil.synchronouslyRefreshPackages(aryEq(new Bundle[] { this.bundle }));
        replay(this.packageAdminUtil);
        this.artifact.refresh();
        verify(this.packageAdminUtil);
    }

    @Test
    public void start() throws BundleException {
        this.bundle.setState(Bundle.RESOLVED);
        this.artifact.start();
        assertEquals(Bundle.ACTIVE, this.bundle.getState());
    }

    @Test
    public void stop() throws BundleException {
        this.bundle.setState(Bundle.ACTIVE);
        this.artifact.stop();
        assertEquals(Bundle.RESOLVED, this.bundle.getState());

    }

    @Test
    public void uninstall() throws BundleException {
        this.bundle.setState(Bundle.ACTIVE);
        this.artifact.uninstall();
        assertEquals(Bundle.UNINSTALLED, this.bundle.getState());
    }
}
