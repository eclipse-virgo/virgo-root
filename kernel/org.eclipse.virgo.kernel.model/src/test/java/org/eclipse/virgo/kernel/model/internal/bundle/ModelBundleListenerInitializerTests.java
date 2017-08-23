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
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;

import java.io.IOException;

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
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.Version;

public class ModelBundleListenerInitializerTests {

    private final StubArtifactRepository artifactRepository = new StubArtifactRepository();
    
    private final StubRegion region = new StubRegion("test-region", null);

    private final PackageAdminUtil packageAdminUtil = createMock(PackageAdminUtil.class);

    private final StubSpringContextAccessor springContextAccessor = new StubSpringContextAccessor();
    
    private final StubBundleContext bundleContext;

    private final StubBundleContext systemBundleContext;

    private final RegionDigraph regionDigraph = createMock(RegionDigraph.class);

    {
        StubBundle bundle = new StubBundle();
        this.bundleContext = (StubBundleContext) bundle.getBundleContext();
        StubBundle stubSystemBundle = new StubBundle(0L, "org.osgi.framework", new Version("0"), "loc");
        this.systemBundleContext = (StubBundleContext) stubSystemBundle.getBundleContext();
        this.bundleContext.addInstalledBundle(stubSystemBundle);
        this.systemBundleContext.addInstalledBundle(bundle);
        String filterString = String.format("(&(objectClass=%s)(artifactType=bundle))", DependencyDeterminer.class.getCanonicalName());
        this.bundleContext.addFilter(filterString, new TrueFilter(filterString));
        expect(regionDigraph.getRegion(bundle)).andReturn(region).anyTimes();
    }

    private final ModelBundleListenerInitializer initializer = new ModelBundleListenerInitializer(this.artifactRepository, this.packageAdminUtil, this.bundleContext, this.regionDigraph, this.springContextAccessor);

    @Test(expected = FatalAssertionException.class)
    public void nullArtifactRepository() {
        new ModelBundleListenerInitializer(null, packageAdminUtil, bundleContext, regionDigraph, this.springContextAccessor);
    }

    @Test(expected = FatalAssertionException.class)
    public void nullPackageAdminUtil() {
        new ModelBundleListenerInitializer(artifactRepository, null, bundleContext, regionDigraph, this.springContextAccessor);
    }

    @Test(expected = FatalAssertionException.class)
    public void nullKernelBundleContext() {
        new ModelBundleListenerInitializer(artifactRepository, packageAdminUtil, null, regionDigraph, this.springContextAccessor);
    }

    @Test(expected = FatalAssertionException.class)
    public void nullRegionDigraph() {
        new ModelBundleListenerInitializer(artifactRepository, packageAdminUtil, bundleContext, null, this.springContextAccessor);
    }

    @Test(expected = FatalAssertionException.class)
    public void nullSpringContextAccessor() {
        new ModelBundleListenerInitializer(artifactRepository, packageAdminUtil, bundleContext, regionDigraph, null);
    }

    @Test
    public void initialize() throws IOException, InvalidSyntaxException {
        replay(this.regionDigraph);
        assertEquals(0, this.systemBundleContext.getBundleListeners().size());
        this.initializer.initialize();
        assertEquals(1, this.systemBundleContext.getBundleListeners().size());
        assertEquals(1, this.artifactRepository.getArtifacts().size());
        verify(this.regionDigraph);
    }

    @Test
    public void destroy() throws IOException, InvalidSyntaxException {
        replay(this.regionDigraph);
        this.initializer.initialize();
        assertEquals(1, this.systemBundleContext.getBundleListeners().size());
        this.initializer.destroy();
        assertEquals(0, this.systemBundleContext.getBundleListeners().size());
        verify(this.regionDigraph);
    }
}
