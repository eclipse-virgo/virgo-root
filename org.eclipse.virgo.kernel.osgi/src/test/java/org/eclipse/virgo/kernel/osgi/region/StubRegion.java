package org.eclipse.virgo.kernel.osgi.region;

import org.junit.Assert;
import org.osgi.framework.BundleContext;

final class StubRegion implements Region {
    
    private final String regionName;
    
    private RegionPackageImportPolicy regionPackageImportPolicy;

    StubRegion(String regionName) {
        this.regionName = regionName;
    }

    @Override
    public String getName() {
        return this.regionName;
    }

    @Override
    public BundleContext getBundleContext() {
        Assert.fail("method not implemented");
        return null;
    }

    void setRegionPackageImportPolicy(RegionPackageImportPolicy regionPackageImportPolicy) {
        this.regionPackageImportPolicy = regionPackageImportPolicy;
    }
    
    @Override
    public RegionPackageImportPolicy getRegionPackageImportPolicy() {
        return this.regionPackageImportPolicy;
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((regionName == null) ? 0 : regionName.hashCode());
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
        StubRegion other = (StubRegion) obj;
        if (regionName == null) {
            if (other.regionName != null)
                return false;
        } else if (!regionName.equals(other.regionName))
            return false;
        return true;
    }

}