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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.virgo.test.launcher.FrameworkBuilder;
import org.junit.Test;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.launch.Framework;

/**
 */
public class FrameworkBuilderTests {

    @Test
    public void testSimpleStart() throws BundleException {
        Properties frameworkProperties = basicFrameworkProperties();
        FrameworkBuilder builder = new FrameworkBuilder(frameworkProperties);
        Framework framework = builder.start();

        try {
            assertNotNull(framework);
            assertEquals(0, framework.getBundleId());
        } finally {
            framework.stop();
        }
    }

    @Test
    public void testStartWithProperties() throws BundleException {
        String name = "Rob Harrop";
        String age = "26";
        String ageOverride = "27";

        Properties frameworkProperties = basicFrameworkProperties();

        frameworkProperties.setProperty("name", name);
        frameworkProperties.setProperty("age", age);

        FrameworkBuilder builder = new FrameworkBuilder(frameworkProperties);
        builder.addFrameworkProperty("age", ageOverride);

        Framework framework = builder.start();

        try {
            assertNotNull(framework);

            BundleContext bundleContext = framework.getBundleContext();

            assertNotNull(bundleContext);

            assertEquals(name, bundleContext.getProperty("name"));
            assertEquals(ageOverride, bundleContext.getProperty("age"));
        } finally {
            framework.stop();
        }
    }

    @Test
    public void testStartWithBundle() throws BundleException {
        Properties frameworkProperties = basicFrameworkProperties();

        FrameworkBuilder builder = new FrameworkBuilder(frameworkProperties);
        builder.addFrameworkProperty("osgi.clean", "true");
        builder.addBundle(new File("src/test/resources/test-bundle"));

        Framework framework = builder.start();

        try {
            assertNotNull(framework);

            BundleContext bundleContext = framework.getBundleContext();

            assertNotNull(bundleContext);

            Bundle[] bundles = bundleContext.getBundles();

            assertEquals(2, bundles.length);

            Bundle testBundle = bundles[1];
            assertEquals("test.bundle", testBundle.getSymbolicName());
            assertEquals(Bundle.INSTALLED, testBundle.getState());

        } finally {
            framework.stop();
        }
    }

    @Test
    public void testStartWithBundleAutostart() throws BundleException {
        Properties frameworkProperties = basicFrameworkProperties();

        FrameworkBuilder builder = new FrameworkBuilder(frameworkProperties);
        builder.addFrameworkProperty("osgi.clean", "true");
        builder.addBundle(new File("src/test/resources/test-bundle"), true);

        Framework framework = builder.start();

        try {
            assertNotNull(framework);

            BundleContext bundleContext = framework.getBundleContext();

            assertNotNull(bundleContext);

            Bundle[] bundles = bundleContext.getBundles();

            assertEquals(2, bundles.length);

            Bundle testBundle = bundles[1];
            assertEquals("test.bundle", testBundle.getSymbolicName());
            assertEquals(Bundle.ACTIVE, testBundle.getState());

        } finally {
            framework.stop();
        }
    }

    @Test
    public void testStartWithLauncherBundles() throws BundleException {
        Properties frameworkProperties = basicFrameworkProperties();

        frameworkProperties.put("launcher.bundles", "src/test/resources/test-bundle@start");
        FrameworkBuilder builder = new FrameworkBuilder(frameworkProperties);
        builder.addFrameworkProperty("osgi.clean", "true");

        Framework framework = builder.start();

        try {
            assertNotNull(framework);

            BundleContext bundleContext = framework.getBundleContext();

            assertNotNull(bundleContext);

            Bundle[] bundles = bundleContext.getBundles();

            assertEquals(2, bundles.length);

            Bundle testBundle = bundles[1];
            assertEquals("test.bundle", testBundle.getSymbolicName());
            assertEquals(Bundle.ACTIVE, testBundle.getState());

        } finally {
            framework.stop();
        }
    }

    @Test
    public void testStartWithBundleDeclaration() throws BundleException {
        Properties frameworkProperties = basicFrameworkProperties();

        FrameworkBuilder builder = new FrameworkBuilder(frameworkProperties);
        builder.addBundle("src/test/resources/test-bundle@start");
        builder.addFrameworkProperty("osgi.clean", "true");

        Framework framework = builder.start();

        try {
            assertNotNull(framework);

            BundleContext bundleContext = framework.getBundleContext();

            assertNotNull(bundleContext);

            Bundle[] bundles = bundleContext.getBundles();

            assertEquals(2, bundles.length);

            Bundle testBundle = bundles[1];
            assertEquals("test.bundle", testBundle.getSymbolicName());
            assertEquals(Bundle.ACTIVE, testBundle.getState());

        } finally {
            framework.stop();
        }
    }

    @Test
    public void testCustomizer() throws BundleException {
        Properties frameworkProperties = basicFrameworkProperties();

        final AtomicInteger calls = new AtomicInteger();

        FrameworkBuilder builder = new FrameworkBuilder(frameworkProperties, new FrameworkBuilder.FrameworkCustomizer() {

            public void beforeInstallBundles(Framework framework) {
                assertEquals(1, framework.getBundleContext().getBundles().length);
                calls.incrementAndGet();
            }

            public void afterInstallBundles(Framework framework) {
                assertEquals(2, framework.getBundleContext().getBundles().length);
                calls.incrementAndGet();
            }
        });
        builder.addBundle("src/test/resources/test-bundle@start");
        builder.addFrameworkProperty("osgi.clean", "true");

        Framework framework = builder.start();
        assertEquals(2, calls.get());

        framework.stop();

    }
    
    private static Properties basicFrameworkProperties() {
        Properties fp = new Properties();
        fp.put("osgi.configuration.area", "build");
        return fp;
    }
}
