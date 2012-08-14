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

package org.eclipse.virgo.test.launcher;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.eclipse.virgo.util.parser.launcher.ArgumentParser;
import org.eclipse.virgo.util.parser.launcher.BundleEntry;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.launch.Framework;
import org.osgi.framework.launch.FrameworkFactory;


/**
 * Simple builder API for creating instances of {@link Framework}.
 * <p/>
 * Created <code>Frameworks</code> are running when returned from {@link #start()}.
 * <p/>
 * Bundles can be installed automatically when starting using {@link #addBundle} and framework properties can be added
 * using {@link #addFrameworkProperty(String, String)}.
 * <p/>
 * Bundles and framework properties <strong>must</strong> must be added before calling <code>start</code>. Any changes
 * to the configuration of the builder is <strong>not</strong> applied to already running <code>Frameworks</code> .
 * <p/>
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Not threadsafe. Intended for single-threaded usage.
 */
public class FrameworkBuilder {

    private static final String PROP_LAUNCHER_BUNDLES = "launcher.bundles";

    private final ArgumentParser parser = new ArgumentParser();

    private final List<BundleEntry> bundleEntries = new ArrayList<BundleEntry>();

    private final FrameworkCustomizer customizer;

    private final Properties configuration;

    /**
     * Creates a new <code>FrameworkBuilder</code>.
     * 
     * @param frameworkProperties the initial set of framework properties.
     */
    public FrameworkBuilder(Properties frameworkProperties) {
        this(frameworkProperties, new NoOpFrameworkCustomizer());
    }
    
    /**
     * Creates a new <code>FrameworkBuilder</code>.
     * 
     * @param frameworkProperties the initial set of framework properties.
     * @param customizer the {@link FrameworkCustomizer} to use for customization
     */
    public FrameworkBuilder(Properties frameworkProperties, FrameworkCustomizer customizer) {
        this.customizer = customizer;
        this.configuration = (frameworkProperties == null ? new Properties() : frameworkProperties);
        parseLauncherBundlesProperty(this.configuration);
    }

    /**
     * Adds a bundle to the launch configuration. The bundle is not automatically started.
     * 
     * @param uri the <code>URI</code> of the bundle.
     * @return the <code>FrameworkBuilder</code>.
     */
    public final FrameworkBuilder addBundle(URI uri) {
        return addBundle(uri, false);
    }

    /**
     * Adds a bundle to the launch configuration. The bundle is not automatically started.
     * 
     * @param file a <code>File</code> pointing to the bundle.
     * @return the <code>FrameworkBuilder</code>
     */
    public final FrameworkBuilder addBundle(File file) {
        return addBundle(file.toURI(), false);
    }

    /**
     * Adds a bundle to the launch configuration.
     * 
     * @param uri the <code>URI</code> of the bundle.
     * @param autoStart flag signifying whether or not the bundle should be started automatically
     * @return the <code>FrameworkBuilder</code>.
     */
    public final FrameworkBuilder addBundle(URI uri, boolean autoStart) {
        BundleEntry entry = new BundleEntry(uri, autoStart);
        this.bundleEntries.add(entry);
        return this;
    }

    /**
     * Adds a bundle to the launch configuration.
     * 
     * @param file a <code>File</code> pointing to the bundle.
     * @param autoStart flag signifying whether or not the bundle should be started automatically
     * @return the <code>FrameworkBuilder</code>
     */
    public final FrameworkBuilder addBundle(File file, boolean autoStart) {
        return addBundle(file.toURI(), autoStart);
    }

    /**
     * Adds a bundle to the launch configuration. The declaration string is of the form:
     * <code>&lt;path&gt;[@start]</code> where path is either a URI or a file path.
     * 
     * @param declaration the bundle declaration string
     * @return the <code>FrameworkBuilder</code>
     */
    public final FrameworkBuilder addBundle(String declaration) {
        BundleEntry entry = this.parser.parseBundleEntry(declaration);
        return addBundle(entry.getURI(), entry.isAutoStart());
    }

    public final FrameworkBuilder addFrameworkProperty(String key, String value) {
        this.configuration.put(key, value);
        return this;
    }

    /**
     * Starts the {@link Framework}. Installs the configured set of <code>Bundles</code>. Starts <code>Bundles</code>
     * that have the <code>start</code> flag set.
     * 
     * @return the running <code>Framework</code>
     * @throws BundleException if any bundles fail to install or start.
     */
    public final Framework start() throws BundleException {
        FrameworkFactory frameworkFactory = FrameworkFactoryLocator.createFrameworkFactory();

        Properties resolvedConfiguration = new PropertyPlaceholderResolver().resolve(this.configuration);
        Map<String, String> resolvedConfigurationMap = convertPropertiesToMap(resolvedConfiguration);
        Framework fwk = frameworkFactory.newFramework(resolvedConfigurationMap);

        fwk.start();
        this.customizer.beforeInstallBundles(fwk);
        installAndStartBundles(fwk.getBundleContext());
        this.customizer.afterInstallBundles(fwk);

        return fwk;
    }

	private Map<String, String> convertPropertiesToMap(
			Properties props) {
		Map<String, String> map = new HashMap<String,String>();
        Set<String> stringPropertyNames = props.stringPropertyNames();
        for (String propName : stringPropertyNames) {
			map.put(propName, props.getProperty(propName));
		}
		return map;
	}

    private void installAndStartBundles(BundleContext bundleContext) throws BundleException {
        List<Bundle> bundlesToStart = new ArrayList<Bundle>();
        for (BundleEntry entry : this.bundleEntries) {
            Bundle bundle = bundleContext.installBundle(entry.getURI().toString());

            if (entry.isAutoStart()) {
                bundlesToStart.add(bundle);
            }
        }

        for (Bundle bundle : bundlesToStart) {
            try {
                bundle.start();
            } catch (BundleException be) {
                throw new BundleException("Bundle " + bundle.getSymbolicName() + " " + bundle.getVersion() + " failed to start.", be);
            }
        }
    }

    private void parseLauncherBundlesProperty(Properties configuration) {
        String launcherBundlesProp = configuration.getProperty(PROP_LAUNCHER_BUNDLES);
        if (launcherBundlesProp != null) {
            BundleEntry[] entries = this.parser.parseBundleEntries(launcherBundlesProp);
            Collections.addAll(this.bundleEntries, entries);

        }
    }

    /**
     * Simple callback interface that allows calling code to customize the created {@link Framework} instance.
     * 
     * @see FrameworkBuilder
     */
    public static interface FrameworkCustomizer {

        /**
         * Called before the {@link FrameworkBuilder} installs any bundles into the {@link Framework}.
         * 
         * @param framework the newly created framework.
         */
        void beforeInstallBundles(Framework framework);

        /**
         * Called after the {@link FrameworkBuilder} has installed the bundle into the {@link Framework}.
         * 
         * @param framework the created framework.
         */
        void afterInstallBundles(Framework framework);
    }

    private static class NoOpFrameworkCustomizer implements FrameworkCustomizer {

        public void afterInstallBundles(Framework framework) {
        }

        public void beforeInstallBundles(Framework framework) {
        }

    }
}
