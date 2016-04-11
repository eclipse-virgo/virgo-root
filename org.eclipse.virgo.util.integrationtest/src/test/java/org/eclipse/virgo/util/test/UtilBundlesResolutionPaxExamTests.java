/*
 * This file is part of the Eclipse Virgo project.
 *
 * Copyright (c) 2014 EclipseSource
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    EclipseSource - initial contribution
 */

package org.eclipse.virgo.util.test;

import static org.junit.Assert.assertEquals;
import static org.ops4j.pax.exam.CoreOptions.bundle;
import static org.ops4j.pax.exam.CoreOptions.junitBundles;
import static org.ops4j.pax.exam.CoreOptions.options;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Dictionary;
import java.util.Enumeration;

import javax.inject.Inject;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.options.CompositeOption;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

@RunWith(PaxExam.class)
public class UtilBundlesResolutionPaxExamTests {

    private static final String BASE_PACKAGE = "org.eclipse.virgo";

    @Inject
    private BundleContext bundleContext;

    private static String[] OSGI_BUNDLES = new String[] { //
        "org.eclipse.virgo.util.common", //
        "org.eclipse.virgo.util.env", //
        "org.eclipse.virgo.util.io", //
        "org.eclipse.virgo.util.jmx", //
        "org.eclipse.virgo.util.math", //
        "org.eclipse.virgo.util.osgi", //
        "org.eclipse.virgo.util.osgi.manifest", //
        "org.eclipse.virgo.util.parser.manifest", //
    };

    @Configuration
    public static Option[] configuration() throws Exception {
        final String absolutePath = new File(".").getAbsolutePath();
        CompositeOption utilBundles = new CompositeOption() {

            @Override
            public Option[] getOptions() {
                Option[] result = new Option[OSGI_BUNDLES.length];
                for (int i = 0; i < OSGI_BUNDLES.length; i++) {
                    String url = buildBundleUrl(absolutePath, i);
                    System.out.println("Adding bundle '" + url + "'.");
                    result[i] = bundle(url);
                }
                return result;
            }

            private String buildBundleUrl(final String absolutePath, int i) {
                String url = absolutePath + "/../" + OSGI_BUNDLES[i] + "/build/libs/";
                File libsDirectory = new File(url);
                FilenameFilter filter = new FilenameFilter() {

                    @Override
                    public boolean accept(File dir, String name) {
                        return name.endsWith(".jar") && !name.contains("-sources");
                    }
                };
                String[] list = libsDirectory.list(filter);
                if (list == null || list.length != 1) {
                    Assert.fail("Required build artefact in '" + libsDirectory + "'missing.");
                }
                String result = "file://" + libsDirectory + File.separator + list[0];
                return result;
            }
        };
        return options( //
            bundle("mvn:org.slf4j/slf4j-api/1.7.13"), // CQ 10520
            bundle("mvn:org.slf4j/slf4j-nop/1.7.13").noStart(), // CQ 11007
            bundle("wrap:mvn:org.aspectj/aspectjrt/1.7.2$Export-Package=org.aspectj.*;version=1.7.2"), //
            utilBundles, //
            junitBundles() //
        );
    }

    @Test
    public void shouldActivateAllOsgiBundles() throws Exception {
        int found = 0;
        Bundle[] bundles = this.bundleContext.getBundles();
        for (Bundle bundle : bundles) {
            String symbolicName = bundle.getSymbolicName();
            System.out.println("Testing bundle state for '" + symbolicName + "'..." + bundle.getState());
            if (symbolicName.contains(BASE_PACKAGE) && bundle.getState() != Bundle.ACTIVE) {
                System.out.println("Non *ACTIVE* bundle found: '" + bundle + "'");
                Dictionary<String, String> headers = bundle.getHeaders();
                Enumeration<String> elements = headers.keys();
                while (elements.hasMoreElements()) {
                    String key = elements.nextElement();
                    System.out.println(key + ": " + headers.get(key));
                }
                if (bundle.getHeaders().get("Fragment-Host") == null) {
                    System.out.println("Starting *inactive* bundle to make problem visible...");
                    // do not try to start fragment bundles
                    bundle.start();
                }
            }
            if (symbolicName.contains(BASE_PACKAGE)) {
                found++;
                assertEquals(symbolicName + " is not ACTIVE", Bundle.ACTIVE, bundle.getState());
            }
        }
        assertEquals("Unexpected number of ACTIVE bundles found", OSGI_BUNDLES.length, found);
    }
}
