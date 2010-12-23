/*******************************************************************************
 * This file is part of the Virgo Web Server.
 *
 * Copyright (c) 2010 Eclipse Foundation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SpringSource, a division of VMware - initial API and implementation and/or initial documentation
 *******************************************************************************/

package org.eclipse.virgo.kernel.osgi.region;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.virgo.util.osgi.manifest.ImportedPackage;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.Bundle;
import org.osgi.framework.Version;
import org.osgi.framework.wiring.BundleRevision;
import org.osgi.framework.wiring.Capability;

public class RegionResolverHookTests extends AbstractRegionHookTest {

    private RegionResolverHook regionResolverHook;

    @Before
    public void setUp() throws Exception {
        triggerFromUserRegion();
    }

    @Test
    public void testFilterResolvable() {
        Collection<BundleRevision> candidates = new ArrayList<BundleRevision>();
        for (int i = 0; i < 3; i++) {
            candidates.add(new TestBundleRevision(i));
        }
        this.regionResolverHook.filterResolvable(candidates);
        assertEquals(3, candidates.size());
    }

    @Test
    public void testFilterResolvableWhenTriggeredFromKernel() {
        triggerFromKernel();

        List<BundleRevision> candidates = new ArrayList<BundleRevision>();
        for (int i = 0; i < 3; i++) {
            candidates.add(new TestBundleRevision(i));
        }
        BundleRevision systemBundleCandidate = candidates.get(0);
        BundleRevision kernelCandidate = candidates.get(1);
        this.regionResolverHook.filterResolvable(candidates);
        assertEquals(2, candidates.size());
        assertTrue(candidates.contains(systemBundleCandidate));
        assertTrue(candidates.contains(kernelCandidate));
    }

    private void triggerFromUserRegion() {
        BundleRevision bundleRevision = new TestBundleRevision(2);
        Collection<BundleRevision> triggers = new ArrayList<BundleRevision>();
        triggers.add(bundleRevision);
        this.regionResolverHook = new RegionResolverHook(getRegionMembership(), new ArrayList<ImportedPackage>(), triggers);
    }

    private void triggerFromKernel() {
        BundleRevision bundleRevision = new TestBundleRevision(1);
        Collection<BundleRevision> triggers = new ArrayList<BundleRevision>();
        triggers.add(bundleRevision);
        this.regionResolverHook = new RegionResolverHook(getRegionMembership(), new ArrayList<ImportedPackage>(), triggers);
    }

    @Test
    public void testFilterSingletonCollisions() {
        this.regionResolverHook.filterResolvable(null);
    }

    /**
     * Test method for
     * {@link org.eclipse.virgo.kernel.osgi.region.RegionResolverHook#filterMatches(org.osgi.framework.wiring.BundleRevision, java.util.Collection)}
     * .
     */
    @Test
    public void testFilterMatches() {
        fail("Not yet implemented");
    }

    @Test
    public void testEnd() {
        this.regionResolverHook.end();
    }

    private final class TestBundleRevision implements BundleRevision {
        
        private int index;

        private Bundle bundle;

        public TestBundleRevision(int index) {
            this.index = index;
            this.bundle = RegionResolverHookTests.this.getBundle(index);
        }

        @Override
        public Bundle getBundle() {
            return this.bundle;
        }

        @Override
        public String getSymbolicName() {
            return this.bundle.getSymbolicName();
        }

        @Override
        public Version getVersion() {
            return this.bundle.getVersion();
        }

        @Override
        public List<Capability> getDeclaredCapabilities(String namespace) {
            return null;
        }

        @Override
        public int getTypes() {
            return 0;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + getOuterType().hashCode();
            result = prime * result + index;
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            TestBundleRevision other = (TestBundleRevision) obj;
            if (!getOuterType().equals(other.getOuterType()))
                return false;
            if (index != other.index)
                return false;
            return true;
        }

        private RegionResolverHookTests getOuterType() {
            return RegionResolverHookTests.this;
        }
        
    }

}
