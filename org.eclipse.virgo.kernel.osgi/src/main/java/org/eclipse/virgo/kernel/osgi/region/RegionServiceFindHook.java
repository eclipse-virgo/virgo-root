/*******************************************************************************
 * This file is part of the Virgo Web Server.
 *
 * Copyright (c) 2010 VMware Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SpringSource, a division of VMware - initial API and implementation and/or initial documentation
 *******************************************************************************/

package org.eclipse.virgo.kernel.osgi.region;

import java.util.Collection;
import java.util.Iterator;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.hooks.service.FindHook;

/**
 * {@link RegionServiceFindHook} is a service {@link FindHook} that controls service isolation between the kernel and
 * user regions.
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Thread safe.
 * 
 */
final class RegionServiceFindHook extends RegionServiceHookBase implements FindHook {

    private final Region kernelRegion;

    /**
     * Constructs a {@link RegionServiceFindHook} which prevents kernel region services which are not imported being
     * found by bundles in the user region and prevents user region services which are not exported from being found by
     * bundles in the kernel region.
     * <p>
     * The services imported from the kernel region into the user region and exported from the user region into the
     * kernel region are defined by class names, each of which names a service type.
     * 
     * @param regionMembership a way of determining whether a given bundle resides in the user region
     * @param regionServiceImports a comma-separated list of class names
     * @param regionServiceExports a comma-separated list of class names
     */
    RegionServiceFindHook(RegionMembership regionMembership, String regionServiceImports, String regionServiceExports) {
        super(regionMembership, regionServiceImports, regionServiceExports);
        this.kernelRegion = getKernelRegion();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void find(BundleContext finderBundleContext, String name, String filter, boolean allServices,
        Collection<ServiceReference<?>> foundReferences) {
        if (!isSystemBundle(finderBundleContext)) {
            Region finderRegion = getRegion(finderBundleContext);
            if (!this.kernelRegion.equals(finderRegion)) {
                Iterator<ServiceReference<?>> i = foundReferences.iterator();
                while (i.hasNext()) {
                    ServiceReference<?> foundServiceReference = i.next();
                    // Prevent kernel region services which are not imported from being found by user region bundles.
                    if (!isSystemBundleService(foundServiceReference) && this.kernelRegion.equals(getRegion(foundServiceReference))
                        && !serviceImported(foundServiceReference)) {
                        System.out.println("RSFH removing " + foundServiceReference);
                        i.remove();
                    }
                }
            } else {
                Iterator<ServiceReference<?>> i = foundReferences.iterator();
                while (i.hasNext()) {
                    ServiceReference<?> foundServiceReference = i.next();
                    // Prevent user region services which are not exported from being found by kernel region bundles.
                    if (!isSystemBundleService(foundServiceReference) && !this.kernelRegion.equals(getRegion(foundServiceReference))
                        && !serviceExported(foundServiceReference)) {
                        i.remove();
                    }
                }
            }
        }
    }

}
