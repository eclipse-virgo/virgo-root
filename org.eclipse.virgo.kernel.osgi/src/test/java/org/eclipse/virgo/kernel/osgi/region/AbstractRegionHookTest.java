
package org.eclipse.virgo.kernel.osgi.region;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.virgo.teststubs.osgi.framework.StubBundle;
import org.junit.Assert;
import org.junit.Before;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Version;

abstract public class AbstractRegionHookTest {
    
    protected static final int SYSTEM_BUNDLE_INDEX = 0;
    
    protected static final int KERNEL_BUNDLE_INDEX = 1;
    
    protected static final int USER_REGION_BUNDLE_INDEX = 2;
    
    protected static final int NUM_BUNDLES = 3;

    private StubBundle[] bundle = new StubBundle[3];

    private StubRegionMembership regionMembership;

    @Before
    public void basicSetUp() {
        this.bundle[SYSTEM_BUNDLE_INDEX] = new StubBundle(0L, "system", new Version("0"), "system@0");
        this.bundle[KERNEL_BUNDLE_INDEX] = new StubBundle(1L, "one", new Version("1"), "kernel@1");
        this.bundle[USER_REGION_BUNDLE_INDEX] = new StubBundle(2L, "two", new Version("2"), "kernel@2");
        regionMembership = new StubRegionMembership(2L);
    }

    Bundle getBundle(int i) {
        return this.bundle[i];
    }

    BundleContext getBundleContext(int i) {
        return this.bundle[i].getBundleContext();
    }
    
    Long getBundleId(int i) {
        return this.bundle[i].getBundleId();
    }

    List<Bundle> getBundles() {
        List<Bundle> l = new ArrayList<Bundle>();
        for (Bundle b : this.bundle) {
            l.add(b);
        }
        return l;
    }
    
    List<BundleContext> getBundleContexts() {
        List<BundleContext> bundleContexts = new ArrayList<BundleContext>();
        for (Bundle bundle : getBundles()) {
            bundleContexts.add(bundle.getBundleContext());
        }
        return bundleContexts;
    }
    
    void assertContextPresent(Collection<BundleContext> bundleContexts, int... bundleIindices) {
        for (int bundleIndex : bundleIindices) {
            Assert.assertTrue(bundleContexts.contains(getBundleContext(bundleIndex)));
        }

    }

    void assertBundlePresent(Collection<Bundle> bundles, int... bundleIindices) {
        for (int bundleIndex : bundleIindices) {
            Assert.assertTrue(bundles.contains(getBundle(bundleIndex)));
        }

    }
    
    RegionMembership getRegionMembership() {
        return this.regionMembership;
    }
    
    Region getKernelRegion() {
        return this.regionMembership.getKernelRegion();
    }

}