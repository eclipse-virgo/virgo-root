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

package org.eclipse.virgo.kernel.model.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.InputStream;
import java.util.Set;

import org.eclipse.virgo.kernel.model.Artifact;
import org.eclipse.virgo.kernel.model.StubCompositeArtifact;
import org.eclipse.virgo.kernel.osgi.region.Region;
import org.eclipse.virgo.kernel.osgi.region.RegionDigraph.FilteredRegion;
import org.eclipse.virgo.kernel.osgi.region.RegionDigraphVisitor;
import org.eclipse.virgo.kernel.osgi.region.RegionFilter;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;
import org.osgi.framework.Version;
import org.junit.Test;


public class NotifyingArtifactRepositoryTests {

    private final NotifyingRuntimeArtifactRepository artifactRepository = new NotifyingRuntimeArtifactRepository();

    @Test
    public void add() {
        StubCompositeArtifact artifact = new StubCompositeArtifact();
        assertTrue(this.artifactRepository.add(artifact));
        assertFalse(this.artifactRepository.add(artifact));
    }

    @Test
    public void remove() {
        StubCompositeArtifact artifact = new StubCompositeArtifact();
        assertTrue(this.artifactRepository.add(artifact));
        assertTrue(this.artifactRepository.remove(artifact.getType(), artifact.getName(), artifact.getVersion(), artifact.getRegion()));
        assertFalse(this.artifactRepository.remove(artifact.getType(), artifact.getName(), artifact.getVersion(), artifact.getRegion()));
    }

    @Test
    public void listeners() {
        StubArtifactRepositoryListener listener1 = new StubArtifactRepositoryListener(true);
        StubArtifactRepositoryListener listener2 = new StubArtifactRepositoryListener(false);
        NotifyingRuntimeArtifactRepository artifactRepository = new NotifyingRuntimeArtifactRepository(listener1, listener2);
        StubCompositeArtifact artifact = new StubCompositeArtifact();
        artifactRepository.add(artifact);
        artifactRepository.remove(artifact.getType(), artifact.getName(), artifact.getVersion(), artifact.getRegion());
        assertTrue(listener1.getAdded());
        assertTrue(listener2.getAdded());
        assertTrue(listener1.getRemoved());
        assertTrue(listener2.getRemoved());
    }

    @Test
    public void getArtifacts() {
        this.artifactRepository.add(new StubCompositeArtifact());
        Set<Artifact> artifacts1 = this.artifactRepository.getArtifacts();
        assertEquals(1, artifacts1.size());
        Set<Artifact> artifacts2 = this.artifactRepository.getArtifacts();
        assertNotSame(artifacts1, artifacts2);
    }
    
    @Test 
    public void getArtifact(){
        Region stubRegion = new StubRegion();
        Artifact stubArtifact = new StubCompositeArtifact("foo", "bar", null);
        Artifact stubArtifact2 = new StubCompositeArtifact("foo2", "bar2", stubRegion);
        this.artifactRepository.add(stubArtifact);
        this.artifactRepository.add(stubArtifact2);
        Artifact retrieved = this.artifactRepository.getArtifact("foo", "bar", new Version("0.1.0"), null);
        assertNull(retrieved);
        retrieved = this.artifactRepository.getArtifact("foo", "bar", Version.emptyVersion, null);
        assertEquals(stubArtifact, retrieved);
        retrieved = this.artifactRepository.getArtifact("foo", "bar", Version.emptyVersion, stubRegion);
        assertNull(retrieved);
        retrieved = this.artifactRepository.getArtifact("foo2", "bar2", Version.emptyVersion, stubRegion);
        assertEquals(stubArtifact2, retrieved);
    }

    private static class StubRegion implements Region{

        public String getName() {
            return "region";
        }

        public void addBundle(Bundle bundle) throws BundleException {
            
        }

        public void addBundle(long bundleId) {
            
        }

        public Bundle installBundle(String location, InputStream input) throws BundleException {
            return null;
        }

        public Bundle installBundle(String location) throws BundleException {
            return null;
        }

        public Set<Long> getBundleIds() {
            return null;
        }

        public boolean contains(Bundle bundle) {
            return false;
        }

        public boolean contains(long bundleId) {
            return false;
        }

        public Bundle getBundle(String symbolicName, Version version) {
            return null;
        }

        public void connectRegion(Region headRegion, RegionFilter filter) throws BundleException {
        }

        public void removeBundle(Bundle bundle) {
        }

        public void removeBundle(long bundleId) {
        }

        public Set<FilteredRegion> getEdges() {
            return null;
        }

        public void visitSubgraph(RegionDigraphVisitor visitor) {
        }
        
    }
    
    private static class StubArtifactRepositoryListener implements ArtifactRepositoryListener {

        private final boolean throwException;

        private volatile boolean added = false;

        private volatile boolean removed = false;

        public StubArtifactRepositoryListener(boolean throwException) {
            this.throwException = throwException;
        }

        public void added(Artifact artifact) {
            this.added = true;
            if (throwException) {
                throw new RuntimeException();
            }
        }

        public void removed(Artifact artifact) {
            this.removed = true;
            if (throwException) {
                throw new RuntimeException();
            }
        }

        public boolean getAdded() {
            return added;
        }

        public boolean getRemoved() {
            return removed;
        }
    }
}
