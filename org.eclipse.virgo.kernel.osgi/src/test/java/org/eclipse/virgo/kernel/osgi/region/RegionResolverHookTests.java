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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
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
        for (int i = 0; i < NUM_BUNDLES; i++) {
            candidates.add(new TestBundleRevision(i));
        }
        this.regionResolverHook.filterResolvable(candidates);
        assertEquals(3, candidates.size());
    }

    private void triggerFromUserRegion(String... importedPackages) {
        BundleRevision bundleRevision = new TestBundleRevision(USER_REGION_BUNDLE_INDEX);
        Collection<BundleRevision> triggers = new ArrayList<BundleRevision>();
        triggers.add(bundleRevision);
        this.regionResolverHook = new RegionResolverHook(getRegionMembership(), triggers);
        try {
            StubRegion userRegion = (StubRegion)getRegionMembership().getRegion(getBundleId(USER_REGION_BUNDLE_INDEX));
            RegionPackageImportPolicy userRegionPackageImportPolicy = createImportedPackages(importedPackages);
            userRegion.setRegionPackageImportPolicy(userRegionPackageImportPolicy);
        } catch (IndeterminateRegionException e) {
            Assert.fail("Unexpected exception" + e);
        }
    }

    private RegionPackageImportPolicy createImportedPackages(final String... importedPackages) {
        return new RegionPackageImportPolicy() {

            @Override
            public boolean isImported(String packageName, Map<String, Object> attributes, Map<String, String> directives) {
                for (String importedPackage : importedPackages) {
                    if (packageName.equals(importedPackage)) {
                        return true;
                    }
                }
                return false;
            }

            @Override
            public Region getUserRegion() {
              //XXX Temporary hack: return any region object that is not the kernel region.
                return new Region(){

                    @Override
                    public void addBundle(Bundle bundle) throws BundleException {
                        // TODO Auto-generated method stub
                        
                    }

                    @Override
                    public void connectRegion(Region tailRegion, RegionFilter filter) throws BundleException {
                        // TODO Auto-generated method stub
                        
                    }

                    @Override
                    public boolean contains(Bundle bundle) {
                        // TODO Auto-generated method stub
                        return false;
                    }

                    @Override
                    public Bundle getBundle(String symbolicName, Version version) {
                        // TODO Auto-generated method stub
                        return null;
                    }

                    @Override
                    public BundleContext getBundleContext() {
                        // TODO Auto-generated method stub
                        return null;
                    }

                    @Override
                    public String getName() {
                        // TODO Auto-generated method stub
                        return null;
                    }

                    @Override
                    public RegionPackageImportPolicy getRegionPackageImportPolicy() {
                        // TODO Auto-generated method stub
                        return null;
                    }

                    @Override
                    public Bundle installBundle(String location, InputStream input) throws BundleException {
                        // TODO Auto-generated method stub
                        return null;
                    }

                    @Override
                    public Bundle installBundle(String location) throws BundleException {
                        // TODO Auto-generated method stub
                        return null;
                    }};

            }};
    }

    private void triggerFromKernel() {
        BundleRevision bundleRevision = new TestBundleRevision(KERNEL_BUNDLE_INDEX);
        Collection<BundleRevision> triggers = new ArrayList<BundleRevision>();
        triggers.add(bundleRevision);
        this.regionResolverHook = new RegionResolverHook(getRegionMembership(), triggers);
    }

    @Test
    public void testFilterSingletonCollisions() {
        this.regionResolverHook.filterResolvable(null);
    }

    @Test
    public void testFilterMatchesUserRegionRequirer() {
        Collection<Capability> candidates = createCandidates("p", SYSTEM_BUNDLE_INDEX, KERNEL_BUNDLE_INDEX, USER_REGION_BUNDLE_INDEX);
        this.regionResolverHook.filterMatches(new TestBundleRevision(USER_REGION_BUNDLE_INDEX), candidates);
        assertCandidates(candidates, "p", SYSTEM_BUNDLE_INDEX, USER_REGION_BUNDLE_INDEX);
    }

    @Test
    public void testFilterMatchesUserRegionRequirerWithImport() {
        triggerFromUserRegion("p");
        Collection<Capability> candidates = createCandidates("p", SYSTEM_BUNDLE_INDEX, KERNEL_BUNDLE_INDEX, USER_REGION_BUNDLE_INDEX);
        this.regionResolverHook.filterMatches(new TestBundleRevision(USER_REGION_BUNDLE_INDEX), candidates);
        assertCandidates(candidates, "p", SYSTEM_BUNDLE_INDEX, KERNEL_BUNDLE_INDEX, USER_REGION_BUNDLE_INDEX);
    }

    @Test
    public void testFilterMatchesUserRegionRequirerWithImports() {
        triggerFromUserRegion("p", "q");
        Collection<Capability> candidates = createCandidates("p", SYSTEM_BUNDLE_INDEX, KERNEL_BUNDLE_INDEX, USER_REGION_BUNDLE_INDEX);
        candidates.addAll(createCandidates("q", SYSTEM_BUNDLE_INDEX, KERNEL_BUNDLE_INDEX, USER_REGION_BUNDLE_INDEX));
        this.regionResolverHook.filterMatches(new TestBundleRevision(USER_REGION_BUNDLE_INDEX), candidates);
        assertEquals(6, candidates.size());
    }
    
    @Test
    public void testFilterMatchesUserRegionRequirerWithImportsSomeFiltered() {
        triggerFromUserRegion("p");
        Collection<Capability> candidates = createCandidates("p", SYSTEM_BUNDLE_INDEX, KERNEL_BUNDLE_INDEX, USER_REGION_BUNDLE_INDEX);
        candidates.addAll(createCandidates("q", SYSTEM_BUNDLE_INDEX, KERNEL_BUNDLE_INDEX, USER_REGION_BUNDLE_INDEX));
        this.regionResolverHook.filterMatches(new TestBundleRevision(USER_REGION_BUNDLE_INDEX), candidates);
        assertEquals(5, candidates.size());
        assertFalse(candidates.contains(createCapability(KERNEL_BUNDLE_INDEX, "q")));
    }

    @Test
    public void testFilterMatchesKernelRequirer() {
        triggerFromKernel();
        Collection<Capability> candidates = createCandidates("p", SYSTEM_BUNDLE_INDEX, KERNEL_BUNDLE_INDEX, USER_REGION_BUNDLE_INDEX);
        this.regionResolverHook.filterMatches(new TestBundleRevision(KERNEL_BUNDLE_INDEX), candidates);
        assertCandidates(candidates, "p", SYSTEM_BUNDLE_INDEX, KERNEL_BUNDLE_INDEX);
    }
    
    @Test
    public void testFilterMatchesUserRegionRequirerBundleCapabilities() {
        Collection<Capability> candidates = createBundleCandidates(SYSTEM_BUNDLE_INDEX, KERNEL_BUNDLE_INDEX, USER_REGION_BUNDLE_INDEX);
        this.regionResolverHook.filterMatches(new TestBundleRevision(USER_REGION_BUNDLE_INDEX), candidates);
        assertBundleCandidates(candidates, SYSTEM_BUNDLE_INDEX, USER_REGION_BUNDLE_INDEX);
    }


    
    @Test
    public void testEnd() {
        this.regionResolverHook.end();
    }

    private void assertCandidates(Collection<Capability> candidates, String packageName, int... indices) {
        assertEquals(indices.length, candidates.size());
        for (int index : indices) {
            assertTrue(candidates.contains(createCapability(index, packageName)));
        }
    }
    
    private Collection<Capability> createCandidates(String packageName, int... indices) {
        Collection<Capability> candidates = new ArrayList<Capability>();
        for (int index : indices) {
            candidates.add(createCapability(index, packageName));
        }
        return candidates;
    }
    
    private TestPackageCapability createCapability(int index, String packageName) {
        return new TestPackageCapability(index, packageName);
    }
    
    private void assertBundleCandidates(Collection<Capability> candidates, int... indices) {
        assertEquals(indices.length, candidates.size());
        for (int index : indices) {
            assertTrue(candidates.contains(createBundleCapability(index)));
        }
    }
    
    private Collection<Capability> createBundleCandidates(int... indices) {
        Collection<Capability> candidates = new ArrayList<Capability>();
        for (int index : indices) {
            candidates.add(createBundleCapability(index));
        }
        return candidates;
    }
    
    private TestBundleCapability createBundleCapability(int index) {
        return new TestBundleCapability(index);
    }

    
    private final class TestPackageCapability implements Capability {

        private final int index;

        private final String packageName;
        
        private final Map<String, Object> attributes = new HashMap<String, Object>();

        private TestPackageCapability(int index, String packageName) {
            this.index = index;
            this.packageName = packageName;
            this.attributes.put(Capability.PACKAGE_CAPABILITY, packageName);
        }

        @Override
        public String getNamespace() {
            return PACKAGE_CAPABILITY;
        }

        @Override
        public Map<String, String> getDirectives() {
            return null;
        }

        @Override
        public Map<String, Object> getAttributes() {
            return this.attributes;
        }

        @Override
        public BundleRevision getProviderRevision() {
            return new TestBundleRevision(this.index);
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + getOuterType().hashCode();
            result = prime * result + index;
            result = prime * result + ((packageName == null) ? 0 : packageName.hashCode());
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
            TestPackageCapability other = (TestPackageCapability) obj;
            if (!getOuterType().equals(other.getOuterType()))
                return false;
            if (index != other.index)
                return false;
            if (packageName == null) {
                if (other.packageName != null)
                    return false;
            } else if (!packageName.equals(other.packageName))
                return false;
            return true;
        }

        private RegionResolverHookTests getOuterType() {
            return RegionResolverHookTests.this;
        }

    }
    
    private final class TestBundleCapability implements Capability {

        private final int index;

        private TestBundleCapability(int index) {
            this.index = index;
        }

        @Override
        public String getNamespace() {
            return BUNDLE_CAPABILITY;
        }

        @Override
        public Map<String, String> getDirectives() {
            return null;
        }

        @Override
        public Map<String, Object> getAttributes() {
            return null;
        }

        @Override
        public BundleRevision getProviderRevision() {
            return new TestBundleRevision(this.index);
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
            TestBundleCapability other = (TestBundleCapability) obj;
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
