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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceFactory;
import org.osgi.framework.launch.Framework;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import org.osgi.service.framework.CompositeBundle;
import org.osgi.service.framework.CompositeBundleFactory;
import org.osgi.service.framework.SurrogateBundle;

import org.eclipse.virgo.osgi.launcher.parser.ArgumentParser;
import org.eclipse.virgo.osgi.launcher.parser.BundleEntry;
import org.eclipse.virgo.medic.eventlog.EventLogger;
import org.eclipse.virgo.util.osgi.ServiceRegistrationTracker;
import org.eclipse.virgo.kernel.core.Shutdown;
import org.eclipse.virgo.kernel.osgi.framework.OsgiFrameworkLogEvents;

/**
 * Creates and manages the user {@link Region regions}.
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Threadsafe.
 * 
 */
@SuppressWarnings("deprecation")
final class RegionManager {

    private static final String USER_REGION_CONFIGURATION_PID = "org.eclipse.virgo.kernel.userregion";

    private static final String PLUGGABLE_CLASS_LOADING_HOOK_CLASS_NAME = "org.eclipse.virgo.osgi.extensions.equinox.hooks.PluggableClassLoadingHook";

    private static final String USER_REGION_BASE_BUNDLES_PROPERTY = "baseBundles";

    private static final String USER_REGION_PACKAGE_IMPORTS_PROPERTY = "packageImports";

    private static final String USER_REGION_SERVICE_IMPORTS_PROPERTY = "serviceImports";

    private static final String USER_REGION_SERVICE_EXPORTS_PROPERTY = "serviceExports";

    private static final String USER_REGION_PROPERTIES_PROPERTY = "inheritedFrameworkProperties";

    private static final String REGION_USER = "org.eclipse.virgo.region.user";

    private static final String EVENT_REGION_STARTING = "org/eclipse/virgo/kernel/region/STARTING";

    private static final Object EVENT_PROPERTY_REGION_BUNDLECONTEXT = "region.bundleContext";

    private final ServiceRegistrationTracker tracker = new ServiceRegistrationTracker();

    private final BundleContext bundleContext;

    private final CompositeBundleFactory compositeBundleFactory;

    private final ArgumentParser parser = new ArgumentParser();

    private final EventAdmin eventAdmin;

    private final ServiceFactory eventLoggerServiceFactory;

    private volatile Framework childFramework;

    private Dictionary<String, String> userRegionProperties;

    private String regionBundles;

    private String regionImports;

    private String regionServiceImports;

    private String regionServiceExports;

    private String regionInheritedProperties;

    public RegionManager(BundleContext bundleContext, CompositeBundleFactory compositeBundleFactory, EventAdmin eventAdmin,
        ServiceFactory eventLoggerServiceFactory, ConfigurationAdmin configAdmin, EventLogger eventLogger, Shutdown shutdown) {
        this.bundleContext = bundleContext;
        this.compositeBundleFactory = compositeBundleFactory;
        this.eventAdmin = eventAdmin;
        this.eventLoggerServiceFactory = eventLoggerServiceFactory;
        getRegionConfiguration(configAdmin, eventLogger, shutdown);
    }

    private void getRegionConfiguration(ConfigurationAdmin configAdmin, EventLogger eventLogger, Shutdown shutdown) {
        try {
            Configuration config = configAdmin.getConfiguration(USER_REGION_CONFIGURATION_PID, null);
            
            @SuppressWarnings("unchecked")
            Dictionary<String, String> properties = (Dictionary<String, String>) config.getProperties();
            
            if (properties != null) {
                this.userRegionProperties = properties;
                this.regionBundles = properties.get(USER_REGION_BASE_BUNDLES_PROPERTY);
                this.regionImports = properties.get(USER_REGION_PACKAGE_IMPORTS_PROPERTY);
                this.regionServiceImports = properties.get(USER_REGION_SERVICE_IMPORTS_PROPERTY);
                this.regionServiceExports = properties.get(USER_REGION_SERVICE_EXPORTS_PROPERTY);
                this.regionInheritedProperties = properties.get(USER_REGION_PROPERTIES_PROPERTY);
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

        CompositeBundle compositeBundle = this.compositeBundleFactory.installCompositeBundle(createChildFrameworkConfig(), REGION_USER,
            createCompositeBundleManifest());

        childFramework = compositeBundle.getCompositeFramework();
        compositeBundle.start();
        childFramework.start();

        SurrogateBundle surrogateBundle = compositeBundle.getSurrogateBundle();
        BundleContext surrogateBundleContext = surrogateBundle.getBundleContext();

        Properties properties = new Properties();
        properties.put(EVENT_PROPERTY_REGION_BUNDLECONTEXT, surrogateBundleContext);
        this.eventAdmin.sendEvent(new Event(EVENT_REGION_STARTING, properties));

        setUserRegionBundleParentClassLoader(surrogateBundleContext);

        registerEventLoggerServiceFactory(surrogateBundleContext);

        initialiseUserRegionBundles(surrogateBundleContext);

        registerRegionService(new ImmutableRegion(REGION_USER, surrogateBundleContext));
        publishUserRegionsBundleContext(surrogateBundleContext);
    }

    private void registerEventLoggerServiceFactory(BundleContext surrogateBundleContext) {
        surrogateBundleContext.registerService(EventLogger.class.getName(), this.eventLoggerServiceFactory, null);
    }

    /**
     * @param surrogateBundleContext
     */
    private void publishUserRegionsBundleContext(BundleContext surrogateBundleContext) {
        Properties properties = new Properties();
        properties.put("org.eclipse.virgo.kernel.regionContext", "true");
        this.bundleContext.registerService(BundleContext.class.getName(), surrogateBundleContext, properties);
    }

    private void setUserRegionBundleParentClassLoader(BundleContext surrogateBundleContext) throws BundleException {
        ClassLoader surrogateClassLoader = surrogateBundleContext.getClass().getClassLoader();
        try {
            setUserRegionHookBundleParentClassLoader(surrogateClassLoader);
        } catch (Exception e) {
            throw new BundleException("Error setting user region hook bundle parent class loader", e);
        }
    }

    private void setUserRegionHookBundleParentClassLoader(ClassLoader parentClassLoader) throws ClassNotFoundException, SecurityException,
        NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        Class<?> pluggableClassLoadingHookClass = parentClassLoader.loadClass(PLUGGABLE_CLASS_LOADING_HOOK_CLASS_NAME);

        Object pluggableClassLoadingHookInstance = invokeGetInstance(pluggableClassLoadingHookClass);

        invokeSetParent(pluggableClassLoadingHookClass, pluggableClassLoadingHookInstance, parentClassLoader);
    }

    private Object invokeGetInstance(Class<?> pluggableClassLoadingHookClass) throws SecurityException, NoSuchMethodException,
        IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        Class<?>[] parmTypes = {};
        Method getInstanceMethod = pluggableClassLoadingHookClass.getDeclaredMethod("getInstance", parmTypes);
        Object[] args = {};
        return getInstanceMethod.invoke(null, args);
    }

    private void invokeSetParent(Class<?> pluggableClassLoadingHookClass, Object pluggableClassLoadingHookInstance, ClassLoader parentClassLoader)
        throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        Class<?>[] parmTypes = { ClassLoader.class };
        Method setParentMethod = pluggableClassLoadingHookClass.getDeclaredMethod("setBundleClassLoaderParent", parmTypes);
        Object[] args = { parentClassLoader };
        setParentMethod.invoke(pluggableClassLoadingHookInstance, args);
    }

    private Map<String, String> createChildFrameworkConfig() {
        HashMap<String, String> frameworkConfig = new HashMap<String, String>();

        setUserConfiguredUserRegionProperties(frameworkConfig);

        if (this.regionInheritedProperties != null) {
            String[] inheritedProperties = this.regionInheritedProperties.split(",");
            for (String property : inheritedProperties) {
                propagatePropertyToUserRegion(frameworkConfig, property);
            }
        }

        String userRegionImportsProperty = this.bundleContext.getProperty(USER_REGION_PROPERTIES_PROPERTY);
        if (userRegionImportsProperty != null) {
            for (String property : userRegionImportsProperty.split(",")) {
                propagatePropertyToUserRegion(frameworkConfig, property);
            }
        }

        return frameworkConfig;
    }

    private void setUserConfiguredUserRegionProperties(HashMap<String, String> frameworkConfig) {
        if (this.userRegionProperties != null) {
            Enumeration<String> keys = this.userRegionProperties.keys();
            while (keys.hasMoreElements()) {
                String propertyName = keys.nextElement();
                String propertyValue = this.userRegionProperties.get(propertyName);
                frameworkConfig.put(propertyName, propertyValue);
            }
        }
    }

    private void propagatePropertyToUserRegion(HashMap<String, String> frameworkConfig, String propertyName) {
        String propertyValue = this.bundleContext.getProperty(propertyName);
        if (propertyValue != null) {
            frameworkConfig.put(propertyName, propertyValue);
        }
    }

    private Map<String, String> createCompositeBundleManifest() {
        Map<String, String> compositeManifest = new HashMap<String, String>();

        compositeManifest.put(Constants.BUNDLE_SYMBOLICNAME, REGION_USER);

        String userRegionImportsProperty = this.regionImports != null ? this.regionImports
            : this.bundleContext.getProperty(USER_REGION_PACKAGE_IMPORTS_PROPERTY);
        if (userRegionImportsProperty != null) {
            String expandedUserRegionImportsProperty = PackageImportWildcardExpander.expandPackageImportsWildcards(userRegionImportsProperty,
                this.bundleContext);
            compositeManifest.put(Constants.IMPORT_PACKAGE, expandedUserRegionImportsProperty);
        }

        configureServiceImportFilter(compositeManifest);
        configureServiceExportFilter(compositeManifest);

        return compositeManifest;
    }

    private void configureServiceImportFilter(Map<String, String> compositeManifest) {
        String[] serviceImports = splitServices(this.regionServiceImports);
        if (serviceImports != null) {
            compositeManifest.put(CompositeBundleFactory.COMPOSITE_SERVICE_FILTER_IMPORT, createObjectClassesServiceFilter(serviceImports));
        }
    }

    private void configureServiceExportFilter(Map<String, String> compositeManifest) {
        String[] serviceExports = splitServices(this.regionServiceExports);
        if (serviceExports != null) {
            compositeManifest.put(CompositeBundleFactory.COMPOSITE_SERVICE_FILTER_EXPORT, createObjectClassesServiceFilter(serviceExports));
        }
    }

    private String[] splitServices(String serviceString) {
        String[] services = null;
        if (serviceString != null) {
            services = serviceString.split(",");
        }
        return services;
    }

    private String createObjectClassesServiceFilter(String[] serviceClassNames) {
        StringBuffer importFilter = new StringBuffer();
        importFilter.append("(|");
        for (String className : serviceClassNames) {
            importFilter.append(createObjectClassFilter(className));
        }
        importFilter.append(")");
        return importFilter.toString();
    }

    private String createObjectClassFilter(String className) {
        return "(objectClass=" + className + ")";
    }

    private void initialiseUserRegionBundles(BundleContext surrogateBundleContext) throws BundleException {
        String userRegionBundlesProperty = this.regionBundles != null ? this.regionBundles
            : this.bundleContext.getProperty(USER_REGION_BASE_BUNDLES_PROPERTY);

        if (userRegionBundlesProperty != null) {
            List<Bundle> bundlesToStart = new ArrayList<Bundle>();

            for (BundleEntry entry : this.parser.parseBundleEntries(userRegionBundlesProperty)) {
                Bundle bundle = surrogateBundleContext.installBundle(entry.getURI().toString());

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
        Properties props = new Properties();
        props.setProperty("org.eclipse.virgo.kernel.region.name", region.getName());
        this.tracker.track(this.bundleContext.registerService(Region.class.getName(), region, props));
    }

    public void stop() {
        this.tracker.unregisterAll();
    }

    private static class ImmutableRegion implements Region {

        private final String name;

        private final BundleContext bundleContext;

        public ImmutableRegion(String name, BundleContext bundleContext) {
            this.name = name;
            this.bundleContext = bundleContext;
        }

        public String getName() {
            return name;
        }

        public BundleContext getBundleContext() {
            return this.bundleContext;
        }

    }
}
