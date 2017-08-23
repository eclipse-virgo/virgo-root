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

package org.eclipse.virgo.test.framework.dmkernel;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

import org.eclipse.virgo.test.framework.BundleEntry;
import org.eclipse.virgo.test.framework.OsgiTestRunner;
import org.eclipse.virgo.test.framework.TestFrameworkUtils;
import org.eclipse.virgo.util.common.CollectionUtils;
import org.eclipse.virgo.util.common.PropertyPlaceholderResolver;
import org.junit.runners.model.InitializationError;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.startlevel.FrameworkStartLevel;

import javax.management.MalformedObjectNameException;

/**
 * JUnit TestRunner for running OSGi integration tests on the dm Kernel.
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * As thread-safe as OsgiTestRunner
 * 
 */
public class DmKernelTestRunner extends OsgiTestRunner {

    private static final int DEFAULT_BUNDLE_START_LEVEL = 4;

    private static final long DEFAULT_USER_REGION_START_WAIT_TIME = 60000;

    private final long userRegionStartWaitTime;

    public DmKernelTestRunner(Class<?> klass) throws InitializationError, MalformedObjectNameException {
        this(klass, DEFAULT_USER_REGION_START_WAIT_TIME);
    }

    protected DmKernelTestRunner(Class<?> klass, long userRegionStartWaitTime) throws InitializationError, MalformedObjectNameException {
        super(klass);
        this.userRegionStartWaitTime = userRegionStartWaitTime;
    }

    @Override
    protected BundleContext getTargetBundleContext(BundleContext bundleContext) {
        Collection<ServiceReference<BundleContext>> serviceReferences = getUserRegionBundleContextServiceReferences(bundleContext);

        if (serviceReferences != null) {
            if (serviceReferences.size() != 1) {
                throw new IllegalStateException("There must be exactly one user region bundle context in the service registry. "
                    + serviceReferences.size() + " were found.");
            } else {
                BundleContext targetBundleContext = (BundleContext) bundleContext.getService(serviceReferences.iterator().next());
                if (targetBundleContext != null) {
                    return targetBundleContext;
                }
            }
        }
        throw new IllegalStateException("User region's bundle context was not available from the service registry within "
            + (this.userRegionStartWaitTime / 1000) + " seconds.");
    }

    /**
     * Installs additional user region bundles based on {@link RegionBundleDependencies}
     */
    @Override
    protected void postProcessTargetBundleContext(BundleContext bundleContext, Properties frameworkConfigurationProperties) throws Exception {

        /*
         * Use the same default start level for user region bundles as the user region factory. Program the framework
         * start level instance defensively to allow for stubs which don't understand adapt.
         */
        FrameworkStartLevel frameworkStartLevel = (FrameworkStartLevel) bundleContext.getBundle(0).adapt(FrameworkStartLevel.class);
        if (frameworkStartLevel != null) {
            frameworkStartLevel.setInitialBundleStartLevel(DEFAULT_BUNDLE_START_LEVEL);
        }

        final Properties configurationProperties = new Properties(frameworkConfigurationProperties);

        // This list is installed post installation of user region bundle that includes bundles listed in the
        // user region configuration file as implemented in kernel RegionManager
        Class<RegionBundleDependencies> annotationType = RegionBundleDependencies.class;
        Class<?> annotationDeclaringClazz = TestFrameworkUtils.findAnnotationDeclaringClass(annotationType, getTestClass().getJavaClass());

        if (annotationDeclaringClazz == null) {
            // could not find an 'annotation declaring class' for annotation + annotationType + and targetType +
            // startFromClazz
            return;
        }

        final List<Bundle> bundlesToStart = new ArrayList<Bundle>();

        List<BundleEntry> bundleEntries = new ArrayList<BundleEntry>();

        while (annotationDeclaringClazz != null) {
            RegionBundleDependencies dependencies = annotationDeclaringClazz.getAnnotation(annotationType);
            BundleEntry[] entries = dependencies.entries();

            bundleEntries.addAll(0, Arrays.<BundleEntry> asList(entries));
            annotationDeclaringClazz = dependencies.inheritDependencies() ? TestFrameworkUtils.findAnnotationDeclaringClass(annotationType,
                annotationDeclaringClazz.getSuperclass()) : null;
        }
        PropertyPlaceholderResolver resolver = new PropertyPlaceholderResolver();

        if (!CollectionUtils.isEmpty(bundleEntries)) {

            for (BundleEntry bundleEntry : bundleEntries) {

                String path = bundleEntry.value();
                boolean autoStart = bundleEntry.autoStart();

                String formattedPath = resolver.resolve(path, configurationProperties);
                Bundle bundle = bundleContext.installBundle(new URI(formattedPath).toString());

                if (autoStart && !TestFrameworkUtils.isFragment(bundle)) {
                    bundlesToStart.add(bundle);
                }
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

    private Collection<ServiceReference<BundleContext>> getUserRegionBundleContextServiceReferences(BundleContext bundleContext) {

        long startTime = System.currentTimeMillis();

        Collection<ServiceReference<BundleContext>> serviceReferences = doGetUserRegionBundleContextServiceReferences(bundleContext);

        while (serviceReferences.size() == 0) {
            if (System.currentTimeMillis() < (this.userRegionStartWaitTime + startTime)) {
                try {
                    Thread.sleep(500);
                    serviceReferences = doGetUserRegionBundleContextServiceReferences(bundleContext);
                } catch (InterruptedException ie) {
                    throw new RuntimeException(ie);
                }
            } else {
                break;
            }
        }
        return serviceReferences;
    }

    private Collection<ServiceReference<BundleContext>> doGetUserRegionBundleContextServiceReferences(BundleContext bundleContext) {
        try {
            return bundleContext.getServiceReferences(BundleContext.class, "(org.eclipse.virgo.kernel.regionContext=true)");
        } catch (InvalidSyntaxException e) {
            throw new RuntimeException("Unexpected InvalidSyntaxException when looking up the user region's BundleContext", e);
        }
    }
}
