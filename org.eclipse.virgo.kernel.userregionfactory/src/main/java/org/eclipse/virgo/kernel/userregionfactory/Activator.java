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

package org.eclipse.virgo.kernel.userregionfactory;

import java.net.URI;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.virgo.kernel.core.Shutdown;
import org.eclipse.virgo.kernel.osgi.framework.OsgiFrameworkLogEvents;
import org.eclipse.virgo.kernel.osgi.framework.OsgiFrameworkUtils;
import org.eclipse.virgo.kernel.osgi.framework.OsgiServiceHolder;
import org.eclipse.virgo.kernel.osgi.region.BundleIdBasedRegion;
import org.eclipse.virgo.kernel.osgi.region.Region;
import org.eclipse.virgo.kernel.osgi.region.RegionDigraph;
import org.eclipse.virgo.kernel.osgi.region.RegionFilter;
import org.eclipse.virgo.kernel.osgi.region.StandardRegionFilter;
import org.eclipse.virgo.medic.eventlog.EventLogger;
import org.eclipse.virgo.osgi.launcher.parser.ArgumentParser;
import org.eclipse.virgo.osgi.launcher.parser.BundleEntry;
import org.eclipse.virgo.util.osgi.ServiceRegistrationTracker;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.Filter;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;

/**
 * {@link Activator} initialises the user region factory bundle.
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Not thread safe.
 * 
 */
public final class Activator implements BundleActivator {

    private static final String CLASS_LIST_SEPARATOR = ",";

    private static final long MAX_SECONDS_WAIT_FOR_SERVICE = 30;

    private static final long MAX_MILLIS_WAIT_FOR_SERVICE = TimeUnit.SECONDS.toMillis(MAX_SECONDS_WAIT_FOR_SERVICE);

    private static final String USER_REGION_CONFIGURATION_PID = "org.eclipse.virgo.kernel.userregion";

    private static final String USER_REGION_BASE_BUNDLES_PROPERTY = "baseBundles";

    private static final String USER_REGION_PACKAGE_IMPORTS_PROPERTY = "packageImports";

    private static final String USER_REGION_SERVICE_IMPORTS_PROPERTY = "serviceImports";

    private static final String USER_REGION_SERVICE_EXPORTS_PROPERTY = "serviceExports";

    private static final String USER_REGION_BUNDLE_CONTEXT_SERVICE_PROPERTY = "org.eclipse.virgo.kernel.regionContext";

    private static final String REGION_USER = "org.eclipse.virgo.region.user";

    private static final String EVENT_REGION_STARTING = "org/eclipse/virgo/kernel/region/STARTING";

    private static final String EVENT_PROPERTY_REGION_BUNDLECONTEXT = "region.bundleContext";

    private EventAdmin eventAdmin;

    private String regionBundles;

    private String regionImports;

    private String regionServiceImports;

    private String regionServiceExports;

    private BundleContext bundleContext;

    private final ArgumentParser parser = new ArgumentParser();

    private final ServiceRegistrationTracker tracker = new ServiceRegistrationTracker();

    /**
     * {@inheritDoc}
     */
    @Override
    public void start(BundleContext bundleContext) throws Exception {
        this.bundleContext = bundleContext;
        RegionDigraph regionDigraph = getPotentiallyDelayedService(bundleContext, RegionDigraph.class);
        this.eventAdmin = getPotentiallyDelayedService(bundleContext, EventAdmin.class);
        ConfigurationAdmin configAdmin = getPotentiallyDelayedService(bundleContext, ConfigurationAdmin.class);
        EventLogger eventLogger = getPotentiallyDelayedService(bundleContext, EventLogger.class);
        Shutdown shutdown = getPotentiallyDelayedService(bundleContext, Shutdown.class);
        getRegionConfiguration(configAdmin, eventLogger, shutdown);

        createUserRegion(bundleContext, regionDigraph);
    }

    private void getRegionConfiguration(ConfigurationAdmin configAdmin, EventLogger eventLogger, Shutdown shutdown) {
        try {
            Configuration config = configAdmin.getConfiguration(USER_REGION_CONFIGURATION_PID, null);

            @SuppressWarnings("unchecked")
            Dictionary<String, String> properties = config.getProperties();

            if (properties != null) {
                this.regionBundles = properties.get(USER_REGION_BASE_BUNDLES_PROPERTY);
                this.regionImports = properties.get(USER_REGION_PACKAGE_IMPORTS_PROPERTY);
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

    private void createUserRegion(BundleContext userRegionBundleContext, RegionDigraph regionDigraph) throws BundleException {

        BundleContext systemBundleContext = getSystemBundleContext();

        Region userRegion = new BundleIdBasedRegion(REGION_USER, regionDigraph, systemBundleContext);

        Region kernelRegion = getKernelRegion(regionDigraph);

        RegionFilter kernelFilter = createKernelFilter(systemBundleContext);
        userRegion.connectRegion(kernelRegion, kernelFilter);

        RegionFilter userRegionFilter = createUserRegionFilter();
        kernelRegion.connectRegion(userRegion, userRegionFilter);

        notifyUserRegionStarting(userRegionBundleContext);

        initialiseUserRegionBundles(userRegion);

        registerRegionService(userRegion);
        publishUserRegionBundleContext(userRegionBundleContext);
    }

    private RegionFilter createUserRegionFilter() throws BundleException {
        RegionFilter userRegionFilter = new StandardRegionFilter();
        Filter serviceFilter;
        try {
            serviceFilter = this.bundleContext.createFilter(classesToFilter(this.regionServiceExports));
        } catch (InvalidSyntaxException e) {
            throw new BundleException("Invalid " + USER_REGION_SERVICE_EXPORTS_PROPERTY + "in user region configuration: '"
                + this.regionServiceExports + "'", e);
        }
        userRegionFilter.setServiceFilter(serviceFilter);

        return userRegionFilter;
    }

    private Region getKernelRegion(RegionDigraph regionDigraph) {
        return regionDigraph.iterator().next();
    }

    private RegionFilter createKernelFilter(BundleContext systemBundleContext) throws BundleException {
        RegionFilter kernelFilter = new StandardRegionFilter();
        Bundle systemBundle = systemBundleContext.getBundle();
        kernelFilter.allowBundle(systemBundle.getSymbolicName(), systemBundle.getVersion());
        kernelFilter.setPackageImportPolicy(createUserRegionPackageImportPolicy(systemBundleContext));
        Filter serviceFilter;
        try {
            serviceFilter = this.bundleContext.createFilter(classesToFilter(this.regionServiceImports));
        } catch (InvalidSyntaxException e) {
            throw new BundleException("Invalid " + USER_REGION_SERVICE_IMPORTS_PROPERTY + "in user region configuration: '"
                + this.regionServiceImports + "'", e);
        }
        kernelFilter.setServiceFilter(serviceFilter);
        return kernelFilter;
    }

    private String classesToFilter(String classList) {
        if (classList == null) {
            return "";
        }
        String[] classes = classList.split(CLASS_LIST_SEPARATOR);
        if (classes.length == 0) {
            return "";
        }
        StringBuffer filter = new StringBuffer();
        filter.append("(|");
        for (String className : classes) {
            filter.append("(objectClass=" + className + ")");
        }
        filter.append(")");
        return filter.toString();
    }

    private UserRegionPackageImportPolicy createUserRegionPackageImportPolicy(BundleContext systemBundleContext) {
        String userRegionImportsProperty = this.regionImports != null ? this.regionImports
            : this.bundleContext.getProperty(USER_REGION_PACKAGE_IMPORTS_PROPERTY);
        String expandedUserRegionImportsProperty = null;
        if (userRegionImportsProperty != null) {
            expandedUserRegionImportsProperty = PackageImportWildcardExpander.expandPackageImportsWildcards(userRegionImportsProperty,
                this.bundleContext);
        }
        
        //expandedUserRegionImportsProperty += getSystemBundleExports(systemBundleContext);

        UserRegionPackageImportPolicy userRegionPackageImportPolicy = new UserRegionPackageImportPolicy(expandedUserRegionImportsProperty);
        return userRegionPackageImportPolicy;
    }

    private String getSystemBundleExports(BundleContext systemBundleContext) {
        //systemBundleContext.getBundle().get
        // TODO Auto-generated method stub
        return ",org.osgi.framework,org.osgi.service.packageadmin,org.eclipse.osgi.baseadaptor,org.eclipse.osgi.baseadaptor.bundlefile";
    }

    private BundleContext getSystemBundleContext() {
        return this.bundleContext.getBundle(0L).getBundleContext();
    }

    private void notifyUserRegionStarting(BundleContext userRegionBundleContext) {
        Map<String, Object> properties = new HashMap<String, Object>();
        properties.put(EVENT_PROPERTY_REGION_BUNDLECONTEXT, userRegionBundleContext);
        this.eventAdmin.sendEvent(new Event(EVENT_REGION_STARTING, properties));
    }

    private void initialiseUserRegionBundles(Region userRegion) throws BundleException {

        String userRegionBundlesProperty = this.regionBundles != null ? this.regionBundles
            : this.bundleContext.getProperty(USER_REGION_BASE_BUNDLES_PROPERTY);

        if (userRegionBundlesProperty != null) {
            List<Bundle> bundlesToStart = new ArrayList<Bundle>();

            for (BundleEntry entry : this.parser.parseBundleEntries(userRegionBundlesProperty)) {
                URI uri = entry.getURI();
                Bundle bundle = userRegion.installBundle(uri.toString());

                if (entry.isAutoStart()) {
                    bundlesToStart.add(bundle);
                }
            }

            for (Bundle bundle : bundlesToStart) {
                try {
                    bundle.start();
                } catch (BundleException e) {
                    throw new BundleException("Failed to start bundle " + bundle.getSymbolicName() + " " + bundle.getVersion(), e);
                }
            }
        }
    }

    private void registerRegionService(Region region) {
        Dictionary<String, String> props = new Hashtable<String, String>();
        props.put("org.eclipse.virgo.kernel.region.name", region.getName());
        this.tracker.track(this.bundleContext.registerService(Region.class, region, props));
    }

    private void publishUserRegionBundleContext(BundleContext userRegionBundleContext) {
        Dictionary<String, String> properties = new Hashtable<String, String>();
        properties.put(USER_REGION_BUNDLE_CONTEXT_SERVICE_PROPERTY, "true");
        this.bundleContext.registerService(BundleContext.class, userRegionBundleContext, properties);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void stop(BundleContext context) throws Exception {
    }

    private static <T> T getPotentiallyDelayedService(BundleContext context, Class<T> serviceClass) throws TimeoutException, InterruptedException {
        T service = null;
        OsgiServiceHolder<T> serviceHolder;
        long millisWaited = 0;
        while (service == null && millisWaited <= MAX_MILLIS_WAIT_FOR_SERVICE) {
            try {
                serviceHolder = OsgiFrameworkUtils.getService(context, serviceClass);
                if (serviceHolder != null) {
                    service = serviceHolder.getService();
                } else {
                    millisWaited += sleepABitMore();
                }
            } catch (IllegalStateException e) {
            }
        }
        if (service == null) {
            throw new TimeoutException(serviceClass.getName());
        }
        return service;
    }

    private static long sleepABitMore() throws InterruptedException {
        long before = System.currentTimeMillis();
        Thread.sleep(100);
        return System.currentTimeMillis() - before;
    }
}
