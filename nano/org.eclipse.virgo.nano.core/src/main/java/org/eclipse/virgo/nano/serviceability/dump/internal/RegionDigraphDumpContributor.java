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

package org.eclipse.virgo.nano.serviceability.dump.internal;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

import org.eclipse.equinox.region.RegionDigraph;
import org.eclipse.virgo.nano.serviceability.NonNull;
import org.eclipse.virgo.medic.dump.Dump;
import org.eclipse.virgo.medic.dump.DumpContributionFailedException;
import org.eclipse.virgo.medic.dump.DumpContributor;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

/**
 * {@link RegionDigraphDumpContributor} dumps the {@link RegionDigraph} to a file.
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * Thread safe.
 */
public final class RegionDigraphDumpContributor implements DumpContributor {

    private final BundleContext bundleContext;

    public RegionDigraphDumpContributor(@NonNull BundleContext bundleContext) {
        this.bundleContext = bundleContext;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void contribute(Dump dump) throws DumpContributionFailedException {
        File outputFile = dump.createFile("region.digraph");
        // TODO use potentially delayed
        ServiceReference<RegionDigraph> serviceReference = this.bundleContext.getServiceReference(RegionDigraph.class);
        RegionDigraph regionDigraph = this.bundleContext.getService(serviceReference);
        try {
            OutputStream output = new FileOutputStream(outputFile);
            try {
                regionDigraph.getRegionDigraphPersistence().save(regionDigraph, output);
            } finally {
                output.close();
            }
        } catch (Exception e) {
            throw new DumpContributionFailedException("Failed to dump region digraph", e);
        } finally {
            this.bundleContext.ungetService(serviceReference);
        }

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getName() {
        return "region digraph";
    }

}
