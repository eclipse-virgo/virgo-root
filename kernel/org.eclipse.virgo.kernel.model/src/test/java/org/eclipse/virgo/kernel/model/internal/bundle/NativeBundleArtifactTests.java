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
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;

import org.eclipse.equinox.region.Region;
import org.eclipse.virgo.kernel.model.ArtifactState;
import org.eclipse.virgo.kernel.model.StubSpringContextAccessor;
import org.eclipse.virgo.kernel.model.internal.AbstractArtifact;
import org.eclipse.virgo.kernel.model.internal.DependencyDeterminer;
import org.eclipse.virgo.kernel.osgi.framework.PackageAdminUtil;
import org.eclipse.virgo.nano.serviceability.Assert.FatalAssertionException;
import org.eclipse.virgo.test.stubs.framework.StubBundle;
import org.eclipse.virgo.test.stubs.framework.StubBundleContext;
import org.eclipse.virgo.test.stubs.support.TrueFilter;
import org.junit.Test;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;

public class NativeBundleArtifactTests {

    private final PackageAdminUtil packageAdminUtil = createMock(PackageAdminUtil.class);

    private final StubBundle bundle = new StubBundle();

    private final StubBundleContext bundleContext;
    
    private final StubSpringContextAccessor springContextAccessor = new StubSpringContextAccessor();
    
    private final Region region = createMock(Region.class);

    {
        this.bundleContext = (StubBundleContext) bundle.getBundleContext();
        String filterString = String.format("(&(objectClass=%s)(artifactType=bundle))", DependencyDeterminer.class.getCanonicalName());
        bundleContext.addFilter(filterString, new TrueFilter(filterString));
        expect(this.region.getName()).andReturn("test.region").anyTimes();
    }

    private final AbstractArtifact artifact = new NativeBundleArtifact(this.bundleContext, this.packageAdminUtil, this.bundle, this.region, this.springContextAccessor);

    @Test(expected = FatalAssertionException.class)
    public void nullBundleContext() {
        new NativeBundleArtifact(null, packageAdminUtil, bundle, this.region, this.springContextAccessor);
    }

    @Test(expected = FatalAssertionException.class)
    public void nullPackageAdminUtil() {
        new NativeBundleArtifact(bundleContext, null, bundle, this.region, this.springContextAccessor);
    }

    @Test(expected = FatalAssertionException.class)
    public void nullBundle() {
        new NativeBundleArtifact(bundleContext, packageAdminUtil, null, this.region, this.springContextAccessor);
    }

    @Test(expected = FatalAssertionException.class)
    public void nullRegion() {
        new NativeBundleArtifact(bundleContext, packageAdminUtil, bundle, null, this.springContextAccessor);
    }

    @Test(expected = FatalAssertionException.class)
    public void nullSpringContextAccessor() {
        new NativeBundleArtifact(bundleContext, packageAdminUtil, bundle, this.region, null);
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
