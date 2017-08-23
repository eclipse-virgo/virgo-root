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

import static org.eclipse.virgo.kernel.osgi.framework.ServiceUtils.getPotentiallyDelayedService;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.eclipse.equinox.region.Region;
import org.eclipse.equinox.region.RegionDigraph;
import org.eclipse.equinox.region.RegionFilter;
import org.eclipse.equinox.region.RegionFilterBuilder;
import org.eclipse.virgo.nano.core.Shutdown;
import org.eclipse.virgo.medic.dump.DumpGenerator;
import org.eclipse.virgo.medic.eventlog.EventLogger;
import org.eclipse.virgo.util.parser.launcher.ArgumentParser;
import org.eclipse.virgo.util.parser.launcher.BundleEntry;
import org.eclipse.virgo.util.osgi.ServiceRegistrationTracker;
import org.eclipse.virgo.util.osgi.manifest.BundleManifest;
import org.eclipse.virgo.util.osgi.manifest.BundleManifestFactory;
import org.eclipse.virgo.util.osgi.manifest.ImportedPackage;
import org.eclipse.virgo.util.osgi.manifest.RequireBundle;
import org.eclipse.virgo.util.osgi.manifest.RequiredBundle;
import org.eclipse.virgo.util.osgi.manifest.VersionRange;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.Constants;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.Version;
import org.osgi.framework.startlevel.FrameworkStartLevel;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.component.ComponentContext;
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
public final class Activator {

    private static final int DEFAULT_BUNDLE_START_LEVEL = 4;

    private static final String KERNEL_REGION_NAME = "org.eclipse.equinox.region.kernel";

    private static final String CLASS_LIST_SEPARATOR = ",";

    private static final String USER_REGION_CONFIGURATION_PID = "org.eclipse.virgo.kernel.userregion";

    private static final String USER_REGION_BASE_BUNDLES_PROPERTY = "baseBundles";

    private static final String USER_REGION_PACKAGE_IMPORTS_PROPERTY = "packageImports";

    private static final String USER_REGION_SERVICE_IMPORTS_PROPERTY = "serviceImports";

    private static final String USER_REGION_BUNDLE_IMPORTS_PROPERTY = "bundleImports";

    private static final String USER_REGION_SERVICE_EXPORTS_PROPERTY = "serviceExports";

    private static final String USER_REGION_BUNDLE_CONTEXT_SERVICE_PROPERTY = "org.eclipse.virgo.kernel.regionContext";

    private static final String REGION_USER = "org.eclipse.virgo.region.user";

    private static final String EVENT_REGION_STARTING = "org/eclipse/virgo/kernel/region/STARTING";

    private static final String EVENT_PROPERTY_REGION_BUNDLECONTEXT = "region.bundleContext";

    private static final String WILDCARD = "*";
    
    private EventAdmin eventAdmin;

    private String regionBundles;

    private String regionPackageImports;

    private String regionServiceImports;

    private String regionBundleImports;

    private String regionServiceExports;

    private BundleContext bundleContext;

    private final ArgumentParser parser = new ArgumentParser();

    private final ServiceRegistrationTracker tracker = new ServiceRegistrationTracker();

    private DumpGenerator dumpGenerator;

    public void activate(ComponentContext componentContext) throws Exception {
        this.bundleContext = componentContext.getBundleContext();
        
        FrameworkStartLevel frameworkStartLevel = (FrameworkStartLevel)componentContext.getBundleContext().getBundle(0).adapt(FrameworkStartLevel.class);
        frameworkStartLevel.setInitialBundleStartLevel(DEFAULT_BUNDLE_START_LEVEL);
        
        this.dumpGenerator = getPotentiallyDelayedService(bundleContext, DumpGenerator.class);
        RegionDigraph regionDigraph = getPotentiallyDelayedService(bundleContext, RegionDigraph.class);
        this.eventAdmin = getPotentiallyDelayedService(bundleContext, EventAdmin.class);
        ConfigurationAdmin configAdmin = getPotentiallyDelayedService(bundleContext, ConfigurationAdmin.class);
        EventLogger eventLogger = getPotentiallyDelayedService(bundleContext, EventLogger.class);
        Shutdown shutdown = getPotentiallyDelayedService(bundleContext, Shutdown.class);
        getRegionConfiguration(configAdmin, eventLogger, shutdown);

        createUserRegion(regionDigraph, eventLogger);
    }
    
    private void getRegionConfiguration(ConfigurationAdmin configAdmin, EventLogger eventLogger, Shutdown shutdown) {
        try {
            Configuration config = configAdmin.getConfiguration(USER_REGION_CONFIGURATION_PID, null);

            Dictionary<String, Object> properties = config.getProperties();

            if (properties != null) {
                this.regionBundles = properties.get(USER_REGION_BASE_BUNDLES_PROPERTY).toString();
                this.regionPackageImports = properties.get(USER_REGION_PACKAGE_IMPORTS_PROPERTY).toString();
                this.regionServiceImports = properties.get(USER_REGION_SERVICE_IMPORTS_PROPERTY).toString();
                this.regionBundleImports = properties.get(USER_REGION_BUNDLE_IMPORTS_PROPERTY).toString();
                this.regionServiceExports = properties.get(USER_REGION_SERVICE_EXPORTS_PROPERTY).toString();
            } else {
                eventLogger.log(UserRegionFactoryLogEvents.USER_REGION_CONFIGURATION_UNAVAILABLE);
                shutdown.immediateShutdown();
            }
        } catch (Exception e) {
            eventLogger.log(UserRegionFactoryLogEvents.USER_REGION_CONFIGURATION_UNAVAILABLE, e);
            shutdown.immediateShutdown();
        }
    }

    private void createUserRegion(RegionDigraph regionDigraph, EventLogger eventLogger) throws BundleException, InvalidSyntaxException {

        BundleContext systemBundleContext = getSystemBundleContext();
        Bundle userRegionFactoryBundle = this.bundleContext.getBundle();

        Region kernelRegion = getKernelRegion(regionDigraph);
        kernelRegion.removeBundle(userRegionFactoryBundle);

        Region userRegion = regionDigraph.createRegion(REGION_USER);
        userRegion.addBundle(userRegionFactoryBundle);

        RegionFilter kernelFilter = createKernelFilter(regionDigraph, systemBundleContext, eventLogger);
        userRegion.connectRegion(kernelRegion, kernelFilter);

        RegionFilter userRegionFilter = createUserRegionFilter(regionDigraph);
        kernelRegion.connectRegion(userRegion, userRegionFilter);

        notifyUserRegionStarting(this.bundleContext);

        initialiseUserRegionBundles(userRegion);

        registerRegionService(userRegion);
        publishUserRegionBundleContext(this.bundleContext);
    }

    private RegionFilter createUserRegionFilter(RegionDigraph digraph) throws InvalidSyntaxException {
        Collection<String> serviceFilters = classesToFilter(this.regionServiceExports);
        RegionFilterBuilder builder = digraph.createRegionFilterBuilder();
        for (String filter : serviceFilters) {
            builder.allow(RegionFilter.VISIBLE_SERVICE_NAMESPACE, filter);
        }
        return builder.build();
    }

    private Region getKernelRegion(RegionDigraph regionDigraph) {
        return regionDigraph.getRegion(KERNEL_REGION_NAME);
    }

    private RegionFilter createKernelFilter(RegionDigraph digraph, BundleContext systemBundleContext, EventLogger eventLogger)
        throws BundleException, InvalidSyntaxException {
        RegionFilterBuilder builder = digraph.createRegionFilterBuilder();
        Collection<String> allowedBundles = allowImportedBundles(eventLogger);
        for (String filter : allowedBundles) {
            builder.allow(RegionFilter.VISIBLE_BUNDLE_NAMESPACE, filter);
        }
        Collection<String> allowedPackages = createUserRegionPackageImportPolicy(systemBundleContext, eventLogger);
        for (String filter : allowedPackages) {
            builder.allow(RegionFilter.VISIBLE_PACKAGE_NAMESPACE, filter);
        }
        Collection<String> allowedServices = classesToFilter(this.regionServiceImports);
        for (String filter : allowedServices) {
            builder.allow(RegionFilter.VISIBLE_SERVICE_NAMESPACE, filter);
        }
        return builder.build();
    }

    private Collection<String> allowImportedBundles(EventLogger eventLogger) {
        String userRegionBundleImports = this.regionBundleImports != null ? this.regionBundleImports
            : this.bundleContext.getProperty(USER_REGION_BUNDLE_IMPORTS_PROPERTY);

        RequireBundle bundleImportsAsRequireBundle = representBundleImportsAsRequireBundle(userRegionBundleImports, eventLogger);
        return importBundleToFilter(bundleImportsAsRequireBundle.getRequiredBundles());
    }

    private RequireBundle representBundleImportsAsRequireBundle(String userRegionBundleImportsProperty, EventLogger eventLogger) {
        Dictionary<String, String> headers = new Hashtable<String, String>();
        headers.put("Require-Bundle", userRegionBundleImportsProperty);
        BundleManifest manifest = BundleManifestFactory.createBundleManifest(headers, new UserRegionFactoryParserLogger(eventLogger));
        return manifest.getRequireBundle();
    }

    private static Collection<String> importBundleToFilter(List<RequiredBundle> importedBundles) {
        if (importedBundles == null || importedBundles.isEmpty())
            return Collections.emptyList();
        Collection<String> result = new ArrayList<String>(importedBundles.size());
        for (RequiredBundle importedBundle : importedBundles) {
            StringBuilder f = new StringBuilder();
            f.append("(&(").append(RegionFilter.VISIBLE_BUNDLE_NAMESPACE).append('=').append(importedBundle.getBundleSymbolicName()).append(')');
            addRange(Constants.BUNDLE_VERSION_ATTRIBUTE, importedBundle.getBundleVersion(), f);
            f.append(')');
            result.add(f.toString());
        }
        return result;
    }

    private static Collection<String> importPackageToFilter(String importList) {
        if (importList == null || importList.isEmpty()) {
            return Collections.emptyList();
        }
        if (importList.contains(WILDCARD)) {
            throw new IllegalArgumentException("Wildcards not supported in region imports: '" + importList + "'");
        }
        BundleManifest manifest = BundleManifestFactory.createBundleManifest();
        manifest.setHeader("Import-Package", importList);
        List<ImportedPackage> list = manifest.getImportPackage().getImportedPackages();
        if (list.isEmpty())
            return Collections.emptyList();
        Collection<String> filters = new ArrayList<String>(list.size());
        for (ImportedPackage importedPackage : list) {
            StringBuilder f = new StringBuilder();
            f.append("(&(").append(RegionFilter.VISIBLE_PACKAGE_NAMESPACE).append('=').append(importedPackage.getPackageName()).append(')');
            Map<String, String> attrs = importedPackage.getAttributes();
            for (Map.Entry<String, String> attr : attrs.entrySet()) {
                if (Constants.VERSION_ATTRIBUTE.equals(attr.getKey()) || Constants.BUNDLE_VERSION_ATTRIBUTE.equals(attr.getKey())) {
                    addRange(attr.getKey(), new VersionRange(attr.getValue()), f);
                } else {
                    f.append('(').append(attr.getKey()).append('=').append(attr.getValue()).append(')');
                }
            }
            f.append(')');
            filters.add(f.toString());
        }
        return filters;
    }

    private static void addRange(String key, VersionRange range, StringBuilder f) {
        if (range.isFloorInclusive()) {
            f.append('(' + key + ">=" + range.getFloor() + ')');
        } else {
            f.append("(!(" + key + "<=" + range.getFloor() + "))");
        }
        Version ceiling = range.getCeiling();
        if (ceiling != null) {
            if (range.isCeilingInclusive()) {
                f.append('(' + key + "<=" + ceiling + ')');
            } else {
                f.append("(!(" + key + ">=" + ceiling + "))");
            }
        }
    }

    private static Collection<String> classesToFilter(String classList) {
        if (classList == null) {
            return Collections.emptyList();
        }
        String[] classes = classList.split(CLASS_LIST_SEPARATOR);
        if (classes.length == 0) {
            return Collections.emptyList();
        }
        Collection<String> result = new ArrayList<String>(classes.length);
        for (String className : classes) {
            result.add("(objectClass=" + className + ")");
        }
        return result;
    }

    private Collection<String> createUserRegionPackageImportPolicy(BundleContext systemBundleContext, EventLogger eventLogger) {
        String userRegionImportsProperty = this.regionPackageImports != null ? this.regionPackageImports
            : this.bundleContext.getProperty(USER_REGION_PACKAGE_IMPORTS_PROPERTY);
        String expandedUserRegionImportsProperty = null;
        if (userRegionImportsProperty != null) {
            expandedUserRegionImportsProperty = PackageImportWildcardExpander.expandPackageImportsWildcards(userRegionImportsProperty,
                systemBundleContext, eventLogger);
        }
        return importPackageToFilter(expandedUserRegionImportsProperty);
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
                    // Take state dump for diagnosis of resolution failures
                    this.dumpGenerator.generateDump("User region bundle failed to start", e);
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

    public void deactivate(ComponentContext context) throws Exception {
    }

}
