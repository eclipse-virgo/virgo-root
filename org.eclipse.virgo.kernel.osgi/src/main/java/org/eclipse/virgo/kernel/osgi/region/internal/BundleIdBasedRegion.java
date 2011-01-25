/*******************************************************************************
 * Copyright (c) 2011 VMware Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   VMware Inc. - initial contribution
 *******************************************************************************/

package org.eclipse.virgo.kernel.osgi.region.internal;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Set;

import org.eclipse.virgo.kernel.osgi.region.Region;
import org.eclipse.virgo.kernel.osgi.region.RegionDigraph;
import org.eclipse.virgo.kernel.osgi.region.RegionDigraph.FilteredRegion;
import org.eclipse.virgo.kernel.osgi.region.RegionFilter;
import org.eclipse.virgo.kernel.osgi.region.RegionPackageImportPolicy;
import org.eclipse.virgo.kernel.serviceability.NonNull;
import org.eclipse.virgo.util.math.ConcurrentHashSet;
import org.eclipse.virgo.util.math.OrderedPair;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.Version;

/**
 * {@link BundleIdBasedRegion} is an implementation of {@link Region} which keeps a track of the bundles in the region
 * by recording their bundle identifiers.
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * Thread safe.
 */
public final class BundleIdBasedRegion implements Region {

    private static final String REGION_LOCATION_DELIMITER = "@";

    private static final String REFERENCE_SCHEME = "reference:";

    private static final String FILE_SCHEME = "file:";

    // A concurrent data structure ensures the contains method does not need synchronisation.
    private final Set<Long> bundleIds = new ConcurrentHashSet<Long>();

    // Updates do need synchronising to avoid races.
    private final Object updateMonitor = new Object();

    private final String regionName;

    private final RegionDigraph regionDigraph;

    private final BundleContext systemBundleContext;

    public BundleIdBasedRegion(@NonNull String regionName, @NonNull RegionDigraph regionDigraph, @NonNull BundleContext systemBundleContext) {
        this.regionName = regionName;
        this.regionDigraph = regionDigraph;
        this.systemBundleContext = systemBundleContext;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getName() {
        return this.regionName;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addBundle(Bundle bundle) throws BundleException {
        synchronized (this.updateMonitor) {
            checkBundleNotAssociatedWithAnotherRegion(bundle);

            String symbolicName = bundle.getSymbolicName();
            Version version = bundle.getVersion();

            checkDuplicateBundleInRegion(bundle, symbolicName, version);

            checkDuplicateBundleViaFilter(bundle, symbolicName, version);

            this.bundleIds.add(bundle.getBundleId());
        }
    }

    private void checkBundleNotAssociatedWithAnotherRegion(Bundle bundle) throws BundleException {
        for (Region r : this.regionDigraph) {
            if (!this.equals(r) && r.contains(bundle)) {
                throw new BundleException("Bundle '" + bundle + "' is already associated with region '" + r + "'", BundleException.INVALID_OPERATION);
            }
        }
    }

    private void checkDuplicateBundleInRegion(Bundle bundle, String symbolicName, Version version) throws BundleException {
        Bundle existingBundle = getBundle(symbolicName, version);
        if (existingBundle != null) {
            throw new BundleException("Cannot add bundle '" + bundle + "' to region '" + this
                + "' as its symbolic name and version conflict with those of bundle '" + existingBundle + "' which is already present in the region",
                BundleException.DUPLICATE_BUNDLE_ERROR);
        }
    }

    private void checkDuplicateBundleViaFilter(Bundle bundle, String symbolicName, Version version) throws BundleException {
        for (FilteredRegion filteredRegion : this.regionDigraph.getEdges(this)) {
            RegionFilter filter = filteredRegion.getFilter();
            if (filter.getAllowedBundles().contains(new OrderedPair<String, Version>(symbolicName, version))) {
                throw new BundleException("Cannot add bundle '" + bundle + "' to region '" + this + "' as filter '" + filter
                    + "' allows it to be seen from region '" + filteredRegion.getRegion() + "'", BundleException.DUPLICATE_BUNDLE_ERROR);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Bundle installBundle(String location, InputStream input) throws BundleException {
        /*
         * TODO: use bundle install hook to close the window between the bundle being installed and it belonging to the region.
         * See bug 333189.
         */
        Bundle bundle = this.systemBundleContext.installBundle(this.regionName + REGION_LOCATION_DELIMITER + location, input);
        addBundle(bundle);
        return bundle;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Bundle installBundle(String location) throws BundleException {
        /*
         * TODO: use bundle install hook to close the window between the bundle being installed and it belonging to the region.
         * See bug 333189.
         */
        Bundle bundle = this.systemBundleContext.installBundle(this.regionName + REGION_LOCATION_DELIMITER + location, openBundleStream(location));
        addBundle(bundle);
        return bundle;
    }

    private InputStream openBundleStream(String location) throws BundleException {
        String absoluteBundleUriString = getAbsoluteUriString(location);

        try {
            // Use the reference: scheme to obtain an InputStream for either a file or a directory.
            return new URL(REFERENCE_SCHEME + absoluteBundleUriString).openStream();

        } catch (MalformedURLException e) {
            throw new BundleException("Location '" + location + "' resulted in an invalid bundle URI '" + absoluteBundleUriString + "'", e);
        } catch (IOException e) {
            throw new BundleException("Location '" + location + "' referred to an invalid bundle at URI '" + absoluteBundleUriString + "'", e);
        }
    }

    private String getAbsoluteUriString(String location) throws BundleException {
        if (!location.startsWith(FILE_SCHEME)) {
            throw new BundleException("Cannot install from location '" + location + "' which did not start with '" + FILE_SCHEME + "'");
        }

        String filePath = location.substring(FILE_SCHEME.length());

        return FILE_SCHEME + new File(filePath).getAbsolutePath();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Bundle getBundle(@NonNull String symbolicName, @NonNull Version version) {

        // The following iteration is weakly consistent and will never throw ConcurrentModificationException.
        for (long bundleId : this.bundleIds) {
            Bundle bundle = this.systemBundleContext.getBundle(bundleId);
            if (bundle != null && symbolicName.equals(bundle.getSymbolicName()) && version.equals(bundle.getVersion())) {
                return bundle;
            }
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void connectRegion(Region tailRegion, RegionFilter filter) throws BundleException {
        synchronized (this.updateMonitor) {
            this.regionDigraph.connect(this, filter, tailRegion);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BundleContext getBundleContext() {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RegionPackageImportPolicy getRegionPackageImportPolicy() {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean contains(Bundle bundle) {
        return this.bundleIds.contains(bundle.getBundleId());
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + this.regionName.hashCode();
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof BundleIdBasedRegion)) {
            return false;
        }
        BundleIdBasedRegion other = (BundleIdBasedRegion) obj;
        return this.regionName.equals(other.regionName);
    }

}
