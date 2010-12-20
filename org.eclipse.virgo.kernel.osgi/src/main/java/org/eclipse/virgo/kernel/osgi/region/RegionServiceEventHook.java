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

import java.util.Collection;
import java.util.Iterator;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.hooks.service.EventHook;

/**
 * {@link RegionServiceEventHook} is a service {@link EventHook} that controls service isolation between the kernel and
 * user regions.
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * This class is thread safe.
 * 
 */
final class RegionServiceEventHook extends RegionServiceHookBase implements EventHook {

    /**
     * Constructs a {@link RegionServiceEventHook} which prevents service events for kernel region services which are
     * not imported from being delivered to bundles in the user region and prevents service events for user region
     * services which are not exported from being delivered to bundles in the kernel region.
     * <p>
     * The services imported from the kernel region into the user region and exported from the user region into the
     * kernel region are defined by class names, each of which names a service type.
     * 
     * @param regionMembership a way of determining whether a given bundle resides in the user region
     * @param regionServiceImports a comma-separated list of class names
     * @param regionServiceExports a comma-separated list of class names
     */
    RegionServiceEventHook(RegionMembership regionMembership, String regionServiceImports, String regionServiceExports) {
        super(regionMembership, regionServiceImports, regionServiceExports);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void event(ServiceEvent event, Collection<BundleContext> contexts) {

        ServiceReference<?> serviceReference = event.getServiceReference();

        if (!isSystemBundleService(serviceReference)) {
            if (isUserRegionService(serviceReference)) {
                if (!serviceExported(serviceReference)) {
                    Iterator<BundleContext> i = contexts.iterator();
                    while (i.hasNext()) {
                        BundleContext targetBundleContext = i.next();
                        // Do not deliver user region services which are not exported to bundles in the kernel region.
                        if (!isUserRegionBundle(targetBundleContext) && !isSystemBundle(targetBundleContext)) {
                            i.remove();
                        }
                    }
                }
            } else {
                if (!serviceImported(serviceReference)) {
                    Iterator<BundleContext> i = contexts.iterator();
                    while (i.hasNext()) {
                        BundleContext targetBundleContext = i.next();
                        // Do not deliver kernel region services which are not imported to bundles in the user region.
                        if (isUserRegionBundle(targetBundleContext) &&  !isSystemBundle(targetBundleContext)) {
                            i.remove();
                        }
                    }
                }
            }
        }

    }

}
