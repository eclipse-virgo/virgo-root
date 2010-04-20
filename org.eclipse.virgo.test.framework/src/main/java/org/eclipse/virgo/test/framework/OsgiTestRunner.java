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

package org.eclipse.virgo.test.framework;

import java.lang.reflect.Field;
import java.net.URI;
import java.util.Properties;

import org.eclipse.osgi.framework.internal.core.FrameworkProperties;
import org.eclipse.virgo.test.framework.plugin.PluginManager;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.InitializationError;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.launch.Framework;

import org.eclipse.virgo.osgi.launcher.FrameworkBuilder;
import org.eclipse.virgo.osgi.launcher.FrameworkBuilder.FrameworkCustomizer;
import org.eclipse.virgo.util.common.PropertyPlaceholderResolver;

public class OsgiTestRunner extends BlockJUnit4ClassRunner {

    private final ConfigurationPropertiesLoader loader = new ConfigurationPropertiesLoader();

    private final PluginManager pluginManager;

    public OsgiTestRunner(Class<?> klass) throws InitializationError {
        super(klass);
        this.pluginManager = new PluginManager(klass);
    }

    private void stupidEquinoxHack() {
        try {
            Field field = FrameworkProperties.class.getDeclaredField("properties");
            synchronized (FrameworkProperties.class) {
                field.setAccessible(true);
                field.set(null, null);
            }
        } catch (Exception e) {
            throw new RuntimeException("Unable to hack Equinox", e);
        }
    }

    @Override
    public final void run(RunNotifier notifier) {

        Framework framework = null;

        try {
            stupidEquinoxHack();

            // Preserve and re-instate the context classloader since tests can sometimes leave it in a strange state.
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            try {
                framework = launchOsgi();
                BundleContext targetBundleContext = getTargetBundleContext(framework.getBundleContext());
                Bundle testBundle = installAndStartTestBundle(targetBundleContext);
                Class<?> osgiTestClass = createOsgiTestClass(testBundle);
                // create the real runner, dispatch it against the class loaded from OSGi
                BlockJUnit4ClassRunner realRunner = new BlockJUnit4ClassRunner(osgiTestClass);
                realRunner.run(notifier);
            } finally {
                Thread.currentThread().setContextClassLoader(classLoader);
            }
        } catch (Throwable e) {
            notifier.fireTestFailure(new Failure(getDescription(), e));
        } finally {
            if (framework != null) {
                try {
                    framework.stop();
                    framework.waitForStop(30000);
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }            
        }
    }

    private Bundle installAndStartTestBundle(BundleContext targetBundleContext) throws BundleException {
        Bundle testBundle = targetBundleContext.installBundle(getTestBundleLocation());
        testBundle.start();
        return testBundle;
    }

    /**
     * Returns the {@link BundleContext} that should be used to install the test bundle
     * 
     * @return the target <code>BundleContext</code>.
     */
    protected BundleContext getTargetBundleContext(BundleContext bundleContext) {
        return bundleContext;
    }

    // load the test class from within OSGi
    private Class<?> createOsgiTestClass(Bundle testBundle) throws ClassNotFoundException {
        Class<?> osgiJavaTestClass = testBundle.loadClass(getTestClass().getName());
        Class<?> osgiTestClass = osgiJavaTestClass;
        return osgiTestClass;
    }

    // launch the OSGi framework. will also install the test bundle
    private Framework launchOsgi() throws Exception {
        final Properties configurationProperties = createConfigurationProperties();
        FrameworkBuilder builder = new FrameworkBuilder(configurationProperties, new FrameworkCustomizer() {

            public void beforeInstallBundles(Framework framework) {
                OsgiTestRunner.this.pluginManager.getPluginDelegate().beforeInstallBundles(framework, configurationProperties);
            }

            public void afterInstallBundles(Framework framework) {

            }
        });
        addUserConfiguredBundles(builder, configurationProperties);
        return builder.start();
    }

    private void addUserConfiguredBundles(FrameworkBuilder builder, Properties configurationProperties) throws Exception {
        BundleDependencies bundleDependencies = getTestClass().getJavaClass().getAnnotation(BundleDependencies.class);

        if (bundleDependencies != null) {
            String[] paths = bundleDependencies.value();

            for (String path : paths) {
                String formattedPath = new PropertyPlaceholderResolver().resolve(path, configurationProperties);
                builder.addBundle(new URI(formattedPath));
            }
        }
    }

    private String getTestBundleLocation() {
        return BundleLocationLocator.determineBundleLocation(getTestClass().getJavaClass());
    }

    private Properties createConfigurationProperties() throws Exception {
        return this.loader.loadConfigurationProperties(getTestClass().getJavaClass());
    }
}
