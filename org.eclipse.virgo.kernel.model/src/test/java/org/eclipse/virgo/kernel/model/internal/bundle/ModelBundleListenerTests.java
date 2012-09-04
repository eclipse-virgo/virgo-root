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

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.isA;
import static org.easymock.EasyMock.replay;
import static org.junit.Assert.assertEquals;

import org.eclipse.equinox.region.RegionDigraph;
import org.eclipse.virgo.kernel.model.StubArtifactRepository;
import org.eclipse.virgo.kernel.model.StubSpringContextAccessor;
import org.eclipse.virgo.kernel.model.internal.DependencyDeterminer;
import org.eclipse.virgo.kernel.osgi.framework.PackageAdminUtil;
import org.eclipse.virgo.nano.serviceability.Assert.FatalAssertionException;
import org.eclipse.virgo.test.stubs.framework.StubBundle;
import org.eclipse.virgo.test.stubs.framework.StubBundleContext;
import org.eclipse.virgo.test.stubs.region.StubRegion;
import org.eclipse.virgo.test.stubs.support.TrueFilter;
import org.junit.Test;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleEvent;

public class ModelBundleListenerTests {

    private final StubArtifactRepository artifactRepository = new StubArtifactRepository();

    private final PackageAdminUtil packageAdminUtil = createMock(PackageAdminUtil.class);
    
    private final StubSpringContextAccessor springContextAccessor = new StubSpringContextAccessor();

    private final StubBundleContext bundleContext;
    
    private final RegionDigraph regionDigraph = createMock(RegionDigraph.class);
    
    private final StubRegion region = new StubRegion("test-region", null);

    {
        this.bundleContext = new StubBundleContext();
        String filterString = String.format("(&(objectClass=%s)(artifactType=bundle))", DependencyDeterminer.class.getCanonicalName());
        this.bundleContext.addFilter(filterString, new TrueFilter(filterString));
        expect(regionDigraph.getRegion(isA(Bundle.class))).andReturn(region).anyTimes();
    }

    private final ModelBundleListener listener = new ModelBundleListener(this.bundleContext, this.artifactRepository, this.packageAdminUtil, this.regionDigraph, this.springContextAccessor);

    @Test(expected = FatalAssertionException.class)
    public void nullBundleContext() {
        new ModelBundleListener(null, artifactRepository, packageAdminUtil, regionDigraph, this.springContextAccessor);
    }

    @Test(expected = FatalAssertionException.class)
    public void nullArtifactRepository() {
        new ModelBundleListener(bundleContext, null, packageAdminUtil, regionDigraph, this.springContextAccessor);
    }

    @Test(expected = FatalAssertionException.class)
    public void nullPackageAdminUtil() {
        new ModelBundleListener(bundleContext, artifactRepository, null, regionDigraph, this.springContextAccessor);
    }
    
    @Test(expected = FatalAssertionException.class)
    public void nullRegionDigraph() {
        new ModelBundleListener(bundleContext, artifactRepository, packageAdminUtil, null, this.springContextAccessor);
    }
    
    @Test(expected = FatalAssertionException.class)
    public void nullSpringContextAccessor() {
        new ModelBundleListener(bundleContext, artifactRepository, packageAdminUtil, regionDigraph, null);
    }

    @Test
    public void installed() {
        replay(regionDigraph);
        assertEquals(0, this.artifactRepository.getArtifacts().size());
        BundleEvent event1 = new BundleEvent(BundleEvent.INSTALLED, new StubBundle().setBundleContext(this.bundleContext));
        this.listener.bundleChanged(event1);
        assertEquals(1, this.artifactRepository.getArtifacts().size());
        BundleEvent event2 = new BundleEvent(BundleEvent.INSTALLED, new StubBundle().setBundleContext(this.bundleContext));
        this.listener.bundleChanged(event2);
        assertEquals(1, this.artifactRepository.getArtifacts().size());
    }

    @Test
    public void uninstalled() {
        replay(regionDigraph);
        StubBundle myBundle = new StubBundle().setBundleContext(this.bundleContext);
        BundleEvent event1 = new BundleEvent(BundleEvent.INSTALLED, myBundle);
        this.listener.bundleChanged(event1);
        assertEquals(1, this.artifactRepository.getArtifacts().size());
        myBundle.setState(Bundle.UNINSTALLED);
        BundleEvent event2 = new BundleEvent(BundleEvent.UNINSTALLED, myBundle);
        this.listener.bundleChanged(event2);
        assertEquals(0, this.artifactRepository.getArtifacts().size());
        BundleEvent event3 = new BundleEvent(BundleEvent.UNINSTALLED, myBundle);
        this.listener.bundleChanged(event3);
        assertEquals(0, this.artifactRepository.getArtifacts().size());
    }

    @Test
    public void unknownEventType() {
        replay(regionDigraph);
        BundleEvent event = new BundleEvent(-1, new StubBundle().setBundleContext(this.bundleContext));
        this.listener.bundleChanged(event);
    }
}
