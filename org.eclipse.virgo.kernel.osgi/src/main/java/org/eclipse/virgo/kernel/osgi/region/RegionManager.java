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

package org.eclipse.virgo.kernel.osgi.region;

import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Map;

import org.eclipse.virgo.kernel.core.Shutdown;
import org.eclipse.virgo.kernel.osgi.framework.OsgiFrameworkLogEvents;
import org.eclipse.virgo.kernel.serviceability.Assert;
import org.eclipse.virgo.medic.eventlog.EventLogger;
import org.eclipse.virgo.util.osgi.ServiceRegistrationTracker;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.hooks.bundle.EventHook;
import org.osgi.framework.hooks.bundle.FindHook;
import org.osgi.framework.hooks.resolver.ResolverHookFactory;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.event.EventAdmin;

/**
 * Creates and manages the user {@link Region regions}.
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Threadsafe.
 * 
 */
final class RegionManager {

    private static final String USER_REGION_CONFIGURATION_PID = "org.eclipse.virgo.kernel.userregion";

    private static final String USER_REGION_SERVICE_IMPORTS_PROPERTY = "serviceImports";

    private static final String USER_REGION_SERVICE_EXPORTS_PROPERTY = "serviceExports";

    private static final String REGION_KERNEL = "org.eclipse.virgo.region.kernel";

    private final ServiceRegistrationTracker tracker = new ServiceRegistrationTracker();

    private final BundleContext bundleContext;

    private String regionServiceImports;

    private String regionServiceExports;

    public RegionManager(BundleContext bundleContext, EventAdmin eventAdmin, ConfigurationAdmin configAdmin, EventLogger eventLogger,
        Shutdown shutdown) {
        this.bundleContext = bundleContext;
        getRegionConfiguration(configAdmin, eventLogger, shutdown);
    }

    private void getRegionConfiguration(ConfigurationAdmin configAdmin, EventLogger eventLogger, Shutdown shutdown) {
        try {
            Configuration config = configAdmin.getConfiguration(USER_REGION_CONFIGURATION_PID, null);

            @SuppressWarnings("unchecked")
            Dictionary<String, String> properties = config.getProperties();

            if (properties != null) {
                this.regionServiceImports = properties.get(USER_REGION_SERVICE_IMPORTS_PROPERTY);
                this.regionServiceExports = properties.get(USER_REGION_SERVICE_EXPORTS_PROPERTY);
            } else {
                eventLogger.log(OsgiFrameworkLogEvents.USER_REGION_CONFIGURATION_UNAVAILABLE);
                shutdown.immediateShutdown();
            }
        } catch (Exception e) {
            eventLogger.log(OsgiFrameworkLogEvents.USER_REGION_CONFIGURATION_UNAVAILABLE, e);
            shutdown.immediateShutdown();
        }
    }

    public void start() throws BundleException {
        createAndPublishUserRegion();
    }

    private void createAndPublishUserRegion() throws BundleException {

        ImmutableRegion kernelRegion = new ImmutableRegion(REGION_KERNEL, this.bundleContext, new RegionPackageImportPolicy() {

            @Override
            public boolean isImported(Region providerRegion, String packageName, Map<String, Object> attributes, Map<String, String> directives) {
                return providerRegion == null || this.equals(providerRegion);
            }
        });
        registerRegionService(kernelRegion);

        StandardRegionMembership regionMembership = new StandardRegionMembership(this.bundleContext.getBundle(), kernelRegion);
        registerRegionMembership(regionMembership, this.bundleContext);

        registerResolverHookFactory(new RegionResolverHookFactory(regionMembership));

        registerBundleEventHook(new RegionBundleEventHook(regionMembership));

        registerBundleFindHook(new RegionBundleFindHook(regionMembership));

        registerServiceEventHook(new RegionServiceEventHook(regionMembership, this.regionServiceImports, this.regionServiceExports));

        registerServiceFindHook(new RegionServiceFindHook(regionMembership, this.regionServiceImports, this.regionServiceExports));
    }

    private void registerRegionMembership(RegionMembership regionMembership, BundleContext userRegionBundleContext) {
        this.tracker.track(this.bundleContext.registerService(RegionMembership.class, regionMembership, null));
        if (userRegionBundleContext != null) {
            this.tracker.track(userRegionBundleContext.registerService(RegionMembership.class, regionMembership, null));
        }
    }

    private void registerServiceFindHook(org.osgi.framework.hooks.service.FindHook serviceFindHook) {
        this.tracker.track(this.bundleContext.registerService(org.osgi.framework.hooks.service.FindHook.class, serviceFindHook, null));
    }

    private void registerServiceEventHook(org.osgi.framework.hooks.service.EventHook serviceEventHook) {
        this.tracker.track(this.bundleContext.registerService(org.osgi.framework.hooks.service.EventHook.class, serviceEventHook, null));
    }

    private void registerBundleFindHook(FindHook findHook) {
        this.tracker.track(this.bundleContext.registerService(FindHook.class, findHook, null));
    }

    private void registerBundleEventHook(EventHook eventHook) {
        this.tracker.track(this.bundleContext.registerService(EventHook.class, eventHook, null));

    }

    private void registerResolverHookFactory(ResolverHookFactory resolverHookFactory) {
        this.tracker.track(this.bundleContext.registerService(ResolverHookFactory.class, resolverHookFactory, null));
    }

    private void registerRegionService(Region region) {
        Dictionary<String, String> props = new Hashtable<String, String>();
        props.put("org.eclipse.virgo.kernel.region.name", region.getName());
        this.tracker.track(this.bundleContext.registerService(Region.class, region, props));
    }

    public void stop() {
        this.tracker.unregisterAll();
    }

    private final class StandardRegionMembership implements RegionMembership {

        private final long highestKernelBundleId;

        private final Region kernelRegion;

        private Region userRegion;

        private final Object monitor = new Object();

        public StandardRegionMembership(Bundle lastBundleInKernel, Region kernelRegion) {
            this.highestKernelBundleId = lastBundleInKernel.getBundleId();
            this.kernelRegion = kernelRegion;
        }

        private boolean contains(Long bundleId) {
            // TODO implement a more robust membership scheme. See bug 333193.
            return bundleId > this.highestKernelBundleId || bundleId == 0L;
        }

        public void setUserRegion(Region userRegion) {
            synchronized (this.monitor) {
                if (this.userRegion == null) {
                    this.userRegion = userRegion;
                } else {
                    Assert.isTrue(this.userRegion == userRegion, "User region already set");
                }
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Region getRegion(Bundle bundle) throws IndeterminateRegionException {
            try {
                return getRegion(bundle.getBundleId());
            } catch (RegionSpanningException _) {
                throw new RegionSpanningException(bundle);
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Region getRegion(long bundleId) throws IndeterminateRegionException {
            if (bundleId == 0L) {
                throw new RegionSpanningException(bundleId);
            }
            if (contains(bundleId)) {
                synchronized (this.monitor) {
                    if (this.userRegion != null) {
                        return this.userRegion;
                    } else {
                        // Allow the user region factory bundle to start off in the kernel until it creates the user
                        // region
                        return this.kernelRegion;
                    }
                }
            } else {
                return this.kernelRegion;
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Region getKernelRegion() {
            return this.kernelRegion;
        }
    }
}
