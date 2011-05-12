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
import static org.easymock.EasyMock.reset;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.eclipse.equinox.region.Region;
import org.eclipse.equinox.region.RegionDigraph;
import org.eclipse.virgo.kernel.model.Artifact;
import org.eclipse.virgo.kernel.model.RuntimeArtifactRepository;
import org.eclipse.virgo.kernel.model.StubArtifactRepository;
import org.eclipse.virgo.kernel.model.StubCompositeArtifact;
import org.eclipse.virgo.kernel.osgi.quasi.QuasiBundle;
import org.eclipse.virgo.kernel.osgi.quasi.QuasiFramework;
import org.eclipse.virgo.kernel.osgi.quasi.QuasiFrameworkFactory;
import org.eclipse.virgo.kernel.osgi.quasi.QuasiImportPackage;
import org.eclipse.virgo.kernel.serviceability.Assert.FatalAssertionException;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.Version;

public class BundleDependencyDeterminerTests {

    private final QuasiFrameworkFactory quasiFrameworkFactory = createMock(QuasiFrameworkFactory.class);

    private final RuntimeArtifactRepository artifactRepository = new StubArtifactRepository();
    
    private final RegionDigraph regionDigraph = createMock(RegionDigraph.class);
    
    private final Region regionA = createMock(Region.class);
    
    private final Region regionB = createMock(Region.class);
    
    private final BundleDependencyDeterminer determiner = new BundleDependencyDeterminer(quasiFrameworkFactory, artifactRepository, regionDigraph);

    private QuasiBundle bundle1 = createMock(QuasiBundle.class);
    
    private QuasiBundle bundle2 = createMock(QuasiBundle.class);
    
    @Before
    public void setUp(){
        reset(quasiFrameworkFactory, regionDigraph);
    }
    
    @Test(expected = FatalAssertionException.class)
    public void nullFactory() {
        new BundleDependencyDeterminer(null, artifactRepository, regionDigraph);
    }

    @Test(expected = FatalAssertionException.class)
    public void nullRepository() {
        new BundleDependencyDeterminer(quasiFrameworkFactory, null, regionDigraph);
    }

    @Test(expected = FatalAssertionException.class)
    public void nullRegionDigraph() {
        new BundleDependencyDeterminer(quasiFrameworkFactory, artifactRepository, null);
    }
    
    @Test
    public void unknownBundle() {
        QuasiFramework framework = createMock(QuasiFramework.class);
        replay(quasiFrameworkFactory, regionDigraph, framework);

        Set<Artifact> dependents = this.determiner.getDependents(new StubCompositeArtifact("bar", "foo", regionB));
        assertEquals(Collections.<Artifact> emptySet(), dependents);

        verify(quasiFrameworkFactory, regionDigraph, framework);
    }
    
    @Test
    public void bundleFromDifferentRegion() {
        QuasiFramework framework = createMock(QuasiFramework.class);
        expect(quasiFrameworkFactory.create()).andReturn(framework);
        expect(framework.getBundles()).andReturn(getTestBundleSet());

        expect(bundle1.getSymbolicName()).andReturn("bundle");
        expect(bundle1.getVersion()).andReturn(Version.emptyVersion);
        expect(bundle1.getBundleId()).andReturn(10l);
        expect(regionDigraph.getRegion(10l)).andReturn(regionA);
        expect(bundle2.getSymbolicName()).andReturn("bundle");
        expect(bundle2.getVersion()).andReturn(Version.emptyVersion);
        expect(bundle2.getBundleId()).andReturn(100l);
        expect(regionDigraph.getRegion(100l)).andReturn(regionB);
        
        expect(bundle2.getImportPackages()).andReturn(new ArrayList<QuasiImportPackage>());
        
        replay(quasiFrameworkFactory, regionDigraph, framework, bundle1, bundle2);

        Set<Artifact> dependents = this.determiner.getDependents(new StubCompositeArtifact("bundle", "bundle", regionB));
        assertEquals(Collections.<Artifact> emptySet(), dependents);

        verify(quasiFrameworkFactory, regionDigraph, framework, bundle1, bundle2);
    }
    
    private List<QuasiBundle> getTestBundleSet(){
        List<QuasiBundle> bundles = new ArrayList<QuasiBundle>();
        bundles.add(bundle1);
        bundles.add(bundle2);
        return bundles;
    }

}
