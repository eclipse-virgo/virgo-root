
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

    private StubBundle[] bundle = new StubBundle[3];

    private StubRegionMembership regionMembership;

    @Before
    public void basicSetUp() {
        this.bundle[0] = new StubBundle(0L, "system", new Version("0"), "system@0");
        this.bundle[1] = new StubBundle(1L, "one", new Version("1"), "kernel@1");
        this.bundle[2] = new StubBundle(2L, "two", new Version("2"), "kernel@2");
        regionMembership = new StubRegionMembership(2L);
    }

    Bundle getBundle(int i) {
        return this.bundle[i];
    }

    BundleContext getBundleContext(int i) {
        return this.bundle[i].getBundleContext();
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
            Assert.assertTrue(bundleContexts.contains(getBundle(bundleIndex).getBundleContext()));
        }

    }

    RegionMembership getRegionMembership() {
        return this.regionMembership;
    }

}